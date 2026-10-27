package pkits.managers;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import pkits.PKitsPlugin;
import pkits.models.ClaimStatus;
import pkits.models.Kit;
import pkits.utils.ItemFactory;
import pkits.utils.ItemSerializer;
import pkits.utils.Placeholders;
import pkits.utils.SoundUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;

public class KitManager {

    private final PKitsPlugin plugin;
    private final File file;
    private final Map<String, Kit> kits = new LinkedHashMap<>();
    private final DeliveryManager delivery;

    public KitManager(PKitsPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "kits.yml");
        this.delivery = new DeliveryManager(plugin);
        load();
    }

    public static String sanitize(String raw) {
        return raw == null ? "" : raw.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_-]", "");
    }

    public void load() {
        kits.clear();
        if (!file.exists()) {
            return;
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection root = yaml.getConfigurationSection("kits");
        if (root == null) {
            return;
        }
        for (String id : root.getKeys(false)) {
            ConfigurationSection section = root.getConfigurationSection(id);
            if (section == null) {
                continue;
            }
            Kit kit = new Kit(id);
            kit.setDisplayName(section.getString("display-name", kit.getDisplayName()));
            kit.setLore(section.getStringList("lore"));
            kit.setCooldown(section.getLong("cooldown", 0L));
            kit.setPermission(section.getString("permission", kit.getPermission()));
            kit.setAutoEquip(section.getBoolean("auto-equip"));
            kit.setShulker(section.getBoolean("shulker"));
            kit.setGlow(section.getBoolean("glow"));
            kit.setShulkerName(section.getString("shulker-name", ""));
            kit.setIcon(ItemFactory.material(section.getString("icon"), Material.CHEST));
            kit.setSlot(section.getInt("slot"));
            kit.setItems(readItems(section.getConfigurationSection("items")));
            kit.setPreviewItems(readItems(section.getConfigurationSection("preview-items")));
            kits.put(id, kit);
        }
    }

    public void save() {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.createSection("kits");
        for (Kit kit : kits.values()) {
            String base = "kits." + kit.getId();
            yaml.set(base + ".display-name", kit.getDisplayName());
            yaml.set(base + ".lore", kit.getLore());
            yaml.set(base + ".cooldown", kit.getCooldown());
            yaml.set(base + ".permission", kit.getPermission());
            yaml.set(base + ".auto-equip", kit.isAutoEquip());
            yaml.set(base + ".shulker", kit.isShulker());
            yaml.set(base + ".glow", kit.isGlow());
            yaml.set(base + ".shulker-name", kit.getShulkerName());
            yaml.set(base + ".icon", kit.getIcon().name());
            yaml.set(base + ".slot", kit.getSlot());
            writeItems(yaml, base + ".items", kit.getItems());
            writeItems(yaml, base + ".preview-items", kit.getPreviewItems());
        }
        try {
            yaml.save(file);
        } catch (IOException exception) {
            plugin.getLogger().log(Level.SEVERE, "Falha ao salvar kits.yml", exception);
        }
    }

    public Kit getKit(String id) {
        return kits.get(sanitize(id));
    }

    public boolean exists(String id) {
        return kits.containsKey(sanitize(id));
    }

    public Collection<Kit> getKits() {
        return kits.values();
    }

    public Set<String> getKitIds() {
        return kits.keySet();
    }

    public boolean register(Kit kit) {
        if (kits.containsKey(kit.getId())) {
            return false;
        }
        kits.put(kit.getId(), kit);
        save();
        return true;
    }

    public boolean delete(String id) {
        Kit removed = kits.remove(sanitize(id));
        if (removed == null) {
            return false;
        }
        plugin.cooldowns().clearKit(removed.getId());
        save();
        return true;
    }

    public NavigableMap<Integer, Kit> positions() {
        List<Kit> sorted = new ArrayList<>(kits.values());
        sorted.sort(Comparator.comparingInt(Kit::getSlot).thenComparing(Kit::getId));
        NavigableMap<Integer, Kit> positions = new TreeMap<>();
        for (Kit kit : sorted) {
            int index = Math.max(0, kit.getSlot());
            while (positions.containsKey(index)) {
                index++;
            }
            positions.put(index, kit);
        }
        return positions;
    }

    public ClaimStatus status(Player player, Kit kit) {
        boolean allowed = kit.getPermission().isEmpty()
                || player.hasPermission(kit.getPermission())
                || player.hasPermission("pkits.bypass.permission");
        if (!allowed) {
            return ClaimStatus.NO_PERMISSION;
        }
        if (player.hasPermission("pkits.bypass.cooldown")) {
            return ClaimStatus.AVAILABLE;
        }
        long remaining = plugin.cooldowns().remainingSeconds(player.getUniqueId(), kit);
        if (remaining < 0L) {
            return ClaimStatus.ALREADY_CLAIMED;
        }
        if (remaining > 0L) {
            return ClaimStatus.ON_COOLDOWN;
        }
        return ClaimStatus.AVAILABLE;
    }

    public void claim(Player player, Kit kit) {
        ClaimStatus status = status(player, kit);
        long remaining = plugin.cooldowns().remainingSeconds(player.getUniqueId(), kit);
        Map<String, String> placeholders = Placeholders.of(
                "%kit-name%", kit.getDisplayName(),
                "%kit-id%", kit.getId(),
                "%remaining%", remaining > 0L ? plugin.formatTime(remaining) : "");
        if (status != ClaimStatus.AVAILABLE) {
            plugin.messages().send(player, "claim." + status.getKey(), placeholders);
            SoundUtil.play(player, plugin.getConfig().getString("sounds.error"));
            return;
        }
        delivery.deliver(player, kit);
        if (kit.getCooldown() != 0L && !player.hasPermission("pkits.bypass.cooldown")) {
            plugin.cooldowns().markClaimed(player.getUniqueId(), kit.getId());
        }
        plugin.messages().send(player, "claim.success", placeholders);
        SoundUtil.play(player, plugin.getConfig().getString("sounds.success"));
    }

    private Map<Integer, ItemStack> readItems(ConfigurationSection section) {
        Map<Integer, ItemStack> items = new TreeMap<>();
        if (section == null) {
            return items;
        }
        for (String key : section.getKeys(false)) {
            ItemStack item = ItemSerializer.decode(section.getString(key, ""));
            if (item == null) {
                continue;
            }
            try {
                items.put(Integer.parseInt(key), item);
            } catch (NumberFormatException ignored) {
                continue;
            }
        }
        return items;
    }

    private void writeItems(YamlConfiguration yaml, String path, Map<Integer, ItemStack> items) {
        for (Map.Entry<Integer, ItemStack> entry : items.entrySet()) {
            yaml.set(path + "." + entry.getKey(), ItemSerializer.encode(entry.getValue()));
        }
    }
}
