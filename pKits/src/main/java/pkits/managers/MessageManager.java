package pkits.managers;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import pkits.PKitsPlugin;
import pkits.utils.Text;

import java.io.File;
import java.util.Map;

public class MessageManager {

    private final PKitsPlugin plugin;
    private YamlConfiguration messages;

    public MessageManager(PKitsPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        File file = new File(plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messages = YamlConfiguration.loadConfiguration(file);
    }

    public String raw(String path) {
        return messages.getString(path, "");
    }

    public void send(CommandSender target, String path) {
        send(target, path, Map.of());
    }

    public void send(CommandSender target, String path, Map<String, String> placeholders) {
        String value = raw(path);
        if (value.isEmpty()) {
            return;
        }
        target.sendMessage(Text.parse(raw("prefix") + value, placeholders));
    }

    public void sendLines(CommandSender target, String path, Map<String, String> placeholders) {
        for (String line : messages.getStringList(path)) {
            target.sendMessage(Text.parse(line, placeholders));
        }
    }
}
