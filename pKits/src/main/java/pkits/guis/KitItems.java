package pkits.guis;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import pkits.PKitsPlugin;
import pkits.models.ClaimStatus;
import pkits.models.Kit;
import pkits.utils.ItemFactory;
import pkits.utils.Placeholders;

import java.util.List;
import java.util.Map;

public final class KitItems {

    private KitItems() {
    }

    public static Map<String, String> placeholders(PKitsPlugin plugin, Player player, Kit kit) {
        ConfigurationSection menu = plugin.getConfig().getConfigurationSection("menu");
        ClaimStatus status = plugin.kitManager().status(player, kit);
        long remaining = plugin.cooldowns().remainingSeconds(player.getUniqueId(), kit);
        String cooldown;
        if (kit.getCooldown() < 0L) {
            cooldown = menu.getString("cooldown-once", "");
        } else if (kit.getCooldown() == 0L) {
            cooldown = menu.getString("cooldown-none", "");
        } else {
            cooldown = plugin.formatTime(kit.getCooldown());
        }
        String permission = kit.getPermission().isEmpty() ? menu.getString("permission-none", "") : kit.getPermission();
        return Placeholders.of(
                "%status%", menu.getString("status." + status.getKey(), status.getKey()),
                "%remaining%", remaining > 0L ? plugin.formatTime(remaining) : "",
                "%kit-name%", kit.getDisplayName(),
                "%kit-id%", kit.getId(),
                "%cooldown%", cooldown,
                "%permission%", permission,
                "%items%", String.valueOf(kit.getItems().size()));
    }

    public static ItemStack icon(PKitsPlugin plugin, Player player, Kit kit) {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("menu.kit-item");
        return ItemFactory.build(
                kit.getIcon(),
                1,
                section.getString("name", "%kit-name%"),
                section.getStringList("lore"),
                kit.isGlow(),
                placeholders(plugin, player, kit),
                Map.<String, List<String>>of("%kit-lore%", kit.getLore()));
    }

    public static ItemStack previewButton(PKitsPlugin plugin, Player player, Kit kit) {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("menu.preview-button");
        Material material = ItemFactory.material(section.getString("material"), Material.PAPER);
        return ItemFactory.build(
                material,
                1,
                section.getString("name", ""),
                section.getStringList("lore"),
                section.getBoolean("glow"),
                placeholders(plugin, player, kit),
                Map.<String, List<String>>of("%kit-lore%", kit.getLore()));
    }
}
