package dev.headhunt;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.persistence.PersistentDataType;

import java.util.concurrent.ThreadLocalRandom;

final class HeadDropListener implements Listener {

    private final HeadHunt plugin;

    HeadDropListener(HeadHunt plugin) {
        this.plugin = plugin;
    }

    /** Optional anti-farm: tags mobs spawned for reasons listed in blocked-spawn-reasons (empty by default). */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSpawn(CreatureSpawnEvent event) {
        if (plugin.heads().isBlockedReason(event.getSpawnReason())) {
            event.getEntity().getPersistentDataContainer()
                    .set(plugin.heads().noHeadKey(), PersistentDataType.BYTE, (byte) 1);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDeath(EntityDeathEvent event) {
        LivingEntity victim = event.getEntity();
        if (!(victim instanceof Mob)) {
            return; // players, armor stands, etc.
        }

        Player killer = victim.getKiller();
        if (killer == null) {
            return;
        }

        if (plugin.getConfig().getStringList("disabled-worlds").contains(victim.getWorld().getName())) {
            return;
        }

        HeadManager heads = plugin.heads();
        if (victim.getPersistentDataContainer().has(heads.noHeadKey(), PersistentDataType.BYTE)) {
            return;
        }

        String id = heads.idOf(victim);
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("mobs." + id);
        if (id.isEmpty() || section == null) {
            return;
        }

        double chance = section.getDouble("chance", 0.0);
        int looting = killer.getInventory().getItemInMainHand().getEnchantmentLevel(Enchantment.LOOTING);
        chance += looting * plugin.getConfig().getDouble("looting-bonus-per-level", 0.0);
        chance = Math.min(chance, 1.0);

        if (ThreadLocalRandom.current().nextDouble() >= chance) {
            return;
        }

        event.getDrops().add(heads.create(id, 1));

        if (plugin.getConfig().getBoolean("notify-on-drop", true)) {
            plugin.messages().send(killer, "head-dropped",
                    Placeholder.unparsed("head", HeadManager.displayName(id) + " Head"));
        }
    }
}
