package dev.headhunt;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

final class HeadListCommand implements CommandExecutor {

    private final HeadHunt plugin;

    HeadListCommand(HeadHunt plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Messages messages = plugin.messages();

        if (!(sender instanceof Player player)) {
            messages.send(sender, "players-only");
            return true;
        }
        if (!player.hasPermission("headhunt.list")) {
            messages.send(player, "no-permission");
            return true;
        }
        if (plugin.heads().participatingIds().isEmpty()) {
            messages.send(player, "no-heads-configured");
            return true;
        }

        player.openInventory(new HeadListGui(plugin).getInventory());
        return true;
    }
}
