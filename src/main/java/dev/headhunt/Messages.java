package dev.headhunt;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;

final class Messages {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    private final HeadHunt plugin;

    Messages(HeadHunt plugin) {
        this.plugin = plugin;
    }

    Component get(String key, TagResolver... resolvers) {
        String prefix = plugin.getConfig().getString("messages.prefix", "");
        String raw = plugin.getConfig().getString("messages." + key, "<red>Missing message: " + key);
        return MM.deserialize(prefix + raw, resolvers);
    }

    void send(CommandSender to, String key, TagResolver... resolvers) {
        to.sendMessage(get(key, resolvers));
    }
}
