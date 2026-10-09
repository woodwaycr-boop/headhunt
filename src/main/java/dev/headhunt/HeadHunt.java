package dev.headhunt;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class HeadHunt extends JavaPlugin {

    private Messages messages;
    private HeadManager heads;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        messages = new Messages(this);
        heads = new HeadManager(this);

        getServer().getPluginManager().registerEvents(new HeadDropListener(this), this);
        getServer().getPluginManager().registerEvents(new GuiListener(), this);

        getCommand("sellheads").setExecutor(new SellHeadsCommand(this));
        getCommand("headlist").setExecutor(new HeadListCommand(this));
        getCommand("headhunt").setExecutor(this);

        if (economy() == null) {
            getLogger().warning("No Vault economy found yet. Head selling stays disabled until an economy plugin (e.g. EssentialsX) loads.");
        }
    }

    /** Looked up on every use so load order between plugins doesn't matter. Null if none is registered. */
    Economy economy() {
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        return rsp == null ? null : rsp.getProvider();
    }

    Messages messages() {
        return messages;
    }

    HeadManager heads() {
        return heads;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("headhunt.admin")) {
                messages.send(sender, "no-permission");
                return true;
            }
            reloadConfig();
            heads.reload();
            messages.send(sender, "reloaded");
            return true;
        }
        messages.send(sender, "usage-headhunt");
        return true;
    }
}
