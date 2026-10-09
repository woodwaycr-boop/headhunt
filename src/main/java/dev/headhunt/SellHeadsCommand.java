package dev.headhunt;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;
import java.util.Map;

final class SellHeadsCommand implements CommandExecutor {

    private final HeadHunt plugin;

    SellHeadsCommand(HeadHunt plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Messages messages = plugin.messages();

        if (!(sender instanceof Player player)) {
            messages.send(sender, "players-only");
            return true;
        }
        if (!player.hasPermission("headhunt.sell")) {
            messages.send(player, "no-permission");
            return true;
        }

        Economy economy = plugin.economy();
        if (economy == null) {
            messages.send(player, "no-economy");
            return true;
        }

        HeadManager heads = plugin.heads();
        PlayerInventory inventory = player.getInventory();
        boolean handOnly = args.length > 0 && args[0].equalsIgnoreCase("hand");

        int firstSlot = handOnly ? inventory.getHeldItemSlot() : 0;
        int lastSlot = handOnly ? inventory.getHeldItemSlot() : 35;

        // Take the heads out first, remember them, and only then pay. If payment fails they go back.
        Map<Integer, ItemStack> removed = new HashMap<>();
        double total = 0.0;
        int count = 0;

        for (int slot = firstSlot; slot <= lastSlot; slot++) {
            ItemStack item = inventory.getItem(slot);
            String type = heads.typeOf(item);
            if (type == null) {
                continue;
            }
            double price = heads.priceOf(type);
            if (price <= 0.0) {
                continue;
            }
            total += price * item.getAmount();
            count += item.getAmount();
            removed.put(slot, item.clone());
            inventory.setItem(slot, null);
        }

        if (count == 0) {
            messages.send(player, "no-heads");
            return true;
        }

        double tax = plugin.getConfig().getDouble("sale-tax-percent", 0.0);
        double payout = total * (1.0 - Math.max(0.0, Math.min(tax, 100.0)) / 100.0);

        EconomyResponse response = economy.depositPlayer(player, payout);
        if (!response.transactionSuccess()) {
            for (Map.Entry<Integer, ItemStack> entry : removed.entrySet()) {
                inventory.setItem(entry.getKey(), entry.getValue());
            }
            messages.send(player, "sale-failed");
            return true;
        }

        messages.send(player, "sold",
                Placeholder.unparsed("count", String.valueOf(count)),
                Placeholder.unparsed("amount", economy.format(payout)));
        return true;
    }
}
