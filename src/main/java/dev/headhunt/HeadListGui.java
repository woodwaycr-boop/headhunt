package dev.headhunt;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Locale;

/**
 * The /headlist menu. Heads sit in a 7-wide area inside a glass border, up to 4 rows (28 mobs).
 * The holder type lets GuiListener recognise this inventory and block every click in it.
 */
final class HeadListGui implements InventoryHolder {

    private static final int COLUMNS = 7;
    private static final int MAX_INNER_ROWS = 4;
    private static final int MAX_HEADS = COLUMNS * MAX_INNER_ROWS;

    private final Inventory inventory;

    HeadListGui(HeadHunt plugin) {
        List<String> ids = plugin.heads().participatingIds();
        if (ids.size() > MAX_HEADS) {
            plugin.getLogger().warning("/headlist can show " + MAX_HEADS + " mobs, but " + ids.size()
                    + " are configured. Extra mobs are hidden from the menu (they still drop and sell).");
            ids = ids.subList(0, MAX_HEADS);
        }

        int innerRows = Math.max(1, (ids.size() + COLUMNS - 1) / COLUMNS);
        int rows = innerRows + 2;
        int size = rows * 9;

        Component title = MiniMessage.miniMessage()
                .deserialize(plugin.getConfig().getString("gui.title", "<gold>HeadHunt"));
        this.inventory = Bukkit.createInventory(this, size, title);

        ItemStack filler = filler();
        for (int slot = 0; slot < size; slot++) {
            inventory.setItem(slot, filler);
        }

        for (int i = 0; i < ids.size(); i++) {
            int row = i / COLUMNS + 1;
            int col = i % COLUMNS + 1;
            inventory.setItem(row * 9 + col, headIcon(plugin, ids.get(i)));
        }

        inventory.setItem(size - 5, infoItem());
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    private static ItemStack headIcon(HeadHunt plugin, String id) {
        ItemStack item = plugin.heads().createDisplay(id);
        ItemMeta meta = item.getItemMeta();

        double price = plugin.heads().priceOf(id);
        double chancePercent = plugin.getConfig().getDouble("mobs." + id + ".chance", 0.0) * 100.0;

        Economy economy = plugin.economy();
        String worth = economy != null ? economy.format(price) : String.format(Locale.US, "%.2f", price);

        meta.lore(List.of(
                line("Worth: ", NamedTextColor.GRAY, worth, NamedTextColor.GREEN),
                line("Drop chance: ", NamedTextColor.GRAY,
                        String.format(Locale.US, "%.1f%%", chancePercent), NamedTextColor.YELLOW)));
        item.setItemMeta(meta);
        return item;
    }

    private static Component line(String label, NamedTextColor labelColor, String value, NamedTextColor valueColor) {
        return Component.text(label, labelColor)
                .append(Component.text(value, valueColor))
                .decoration(TextDecoration.ITALIC, false);
    }

    private static ItemStack filler() {
        ItemStack item = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(" "));
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack infoItem() {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("How it works", NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.text("Kill these mobs to get their heads.", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.text("Sell them with /sellheads.", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false)));
        item.setItemMeta(meta);
        return item;
    }
}
