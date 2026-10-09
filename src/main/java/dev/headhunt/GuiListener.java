package dev.headhunt;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

/** The /headlist menu is read-only: every click or drag while it is open is cancelled. */
final class GuiListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof HeadListGui) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof HeadListGui) {
            event.setCancelled(true);
        }
    }
}
