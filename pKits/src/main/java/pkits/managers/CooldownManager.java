package pkits.managers;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import pkits.PKitsPlugin;
import pkits.models.Kit;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.UUID;
import java.util.logging.Level;

public class CooldownManager {

    private static final Object LOCK = new Object();

    private final PKitsPlugin plugin;
    private final File file;
    private final YamlConfiguration data;

    public CooldownManager(PKitsPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "data.yml");
        this.data = YamlConfiguration.loadConfiguration(file);
    }

    public long getLastClaim(UUID playerId, String kitId) {
        return data.getLong("players." + playerId + "." + kitId, 0L);
    }

    public long remainingSeconds(UUID playerId, Kit kit) {
        long last = getLastClaim(playerId, kit.getId());
        if (last <= 0L) {
            return 0L;
        }
        if (kit.getCooldown() < 0L) {
            return -1L;
        }
        long remaining = last + kit.getCooldown() * 1000L - System.currentTimeMillis();
        return remaining <= 0L ? 0L : (remaining + 999L) / 1000L;
    }

    public void markClaimed(UUID playerId, String kitId) {
        data.set("players." + playerId + "." + kitId, System.currentTimeMillis());
        saveAsync();
    }

    public void clearKit(String kitId) {
        ConfigurationSection players = data.getConfigurationSection("players");
        if (players == null) {
            return;
        }
        for (String playerId : players.getKeys(false)) {
            data.set("players." + playerId + "." + kitId, null);
        }
        saveAsync();
    }

    public void saveAsync() {
        String snapshot = data.saveToString();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> write(snapshot));
    }

    public void saveNow() {
        write(data.saveToString());
    }

    private void write(String content) {
        synchronized (LOCK) {
            try {
                Files.createDirectories(file.getParentFile().toPath());
                Files.writeString(file.toPath(), content, StandardCharsets.UTF_8);
            } catch (IOException exception) {
                plugin.getLogger().log(Level.SEVERE, "Falha ao salvar data.yml", exception);
            }
        }
    }
}
