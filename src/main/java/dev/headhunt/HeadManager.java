package dev.headhunt;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/** Creates and recognises the heads this plugin drops. Only heads carrying our tag can be sold. */
final class HeadManager {

    private final HeadHunt plugin;
    private final NamespacedKey typeKey;
    private final NamespacedKey noHeadKey;
    private final Set<CreatureSpawnEvent.SpawnReason> blockedReasons =
            EnumSet.noneOf(CreatureSpawnEvent.SpawnReason.class);

    HeadManager(HeadHunt plugin) {
        this.plugin = plugin;
        this.typeKey = new NamespacedKey(plugin, "head_type");
        this.noHeadKey = new NamespacedKey(plugin, "no_head");
        reload();
    }

    void reload() {
        blockedReasons.clear();
        for (String name : plugin.getConfig().getStringList("blocked-spawn-reasons")) {
            try {
                blockedReasons.add(CreatureSpawnEvent.SpawnReason.valueOf(name.toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException ex) {
                plugin.getLogger().warning("Unknown spawn reason in config.yml: " + name);
            }
        }
    }

    boolean isBlockedReason(CreatureSpawnEvent.SpawnReason reason) {
        return blockedReasons.contains(reason);
    }

    NamespacedKey noHeadKey() {
        return noHeadKey;
    }

    /** Participating mob ids, in config order. */
    List<String> participatingIds() {
        ConfigurationSection mobs = plugin.getConfig().getConfigurationSection("mobs");
        if (mobs == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(mobs.getKeys(false));
    }

    /** "cow", "wither_skeleton"... Empty string if the type has no key. */
    String idOf(Entity entity) {
        try {
            return entity.getType().getKey().getKey();
        } catch (IllegalArgumentException ex) {
            return "";
        }
    }

    static String displayName(String id) {
        StringBuilder sb = new StringBuilder();
        for (String part : id.split("_")) {
            if (part.isEmpty()) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return sb.toString();
    }

    /** A real, sellable head. */
    ItemStack create(String id, int amount) {
        return build(id, amount, true);
    }

    /** Looks identical but carries no sell tag. Used for menu icons. */
    ItemStack createDisplay(String id) {
        return build(id, 1, false);
    }

    private ItemStack build(String id, int amount, boolean sellable) {
        Material material = switch (id) {
            case "zombie" -> Material.ZOMBIE_HEAD;
            case "skeleton" -> Material.SKELETON_SKULL;
            case "wither_skeleton" -> Material.WITHER_SKELETON_SKULL;
            case "creeper" -> Material.CREEPER_HEAD;
            case "piglin" -> Material.PIGLIN_HEAD;
            case "ender_dragon" -> Material.DRAGON_HEAD;
            default -> Material.PLAYER_HEAD;
        };

        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text(displayName(id) + " Head", NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(Component.text("Trophy. Sell with /sellheads", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false)));

        if (sellable) {
            meta.getPersistentDataContainer().set(typeKey, PersistentDataType.STRING, id);
        }

        if (material == Material.PLAYER_HEAD && meta instanceof SkullMeta skull) {
            applySkin(id, skull);
        }

        item.setItemMeta(meta);
        return item;
    }

    private void applySkin(String id, SkullMeta skull) {
        String texture = plugin.getConfig().getString("mobs." + id + ".texture", "");
        String owner = plugin.getConfig().getString("mobs." + id + ".owner", "");

        if (!texture.isBlank()) {
            // Fixed UUID per type so identical heads stack.
            UUID uuid = UUID.nameUUIDFromBytes(("headhunt:" + id).getBytes(StandardCharsets.UTF_8));
            PlayerProfile profile = Bukkit.createProfile(uuid, null);
            profile.setProperty(new ProfileProperty("textures", texture.trim()));
            skull.setPlayerProfile(profile);
        } else if (!owner.isBlank()) {
            skull.setPlayerProfile(Bukkit.createProfile(owner.trim()));
        }
    }

    /** The head type id stored on the item, or null if it isn't one of our sellable heads. */
    String typeOf(ItemStack item) {
        if (item == null || item.getType().isAir() || !item.hasItemMeta()) {
            return null;
        }
        return item.getItemMeta().getPersistentDataContainer().get(typeKey, PersistentDataType.STRING);
    }

    /** Current configured price (read at sale time, so config changes apply to heads already dropped). */
    double priceOf(String id) {
        return plugin.getConfig().getDouble("mobs." + id + ".price", 0.0);
    }
}
