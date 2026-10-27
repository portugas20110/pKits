package pkits.utils;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class ItemFactory {

    private ItemFactory() {
    }

    public static Material material(String name, Material fallback) {
        Material material = name == null ? null : Material.matchMaterial(name.trim());
        if (material == null || material.isAir() || !material.isItem()) {
            return fallback;
        }
        return material;
    }

    public static ItemStack fromSection(ConfigurationSection section, Map<String, String> placeholders) {
        return fromSection(section, placeholders, Map.of());
    }

    public static ItemStack fromSection(ConfigurationSection section, Map<String, String> placeholders, Map<String, List<String>> multiline) {
        if (section == null) {
            return new ItemStack(Material.STONE);
        }
        Material material = material(section.getString("material"), Material.STONE);
        String name = section.contains("name") ? section.getString("name") : null;
        List<String> lore = section.contains("lore") ? section.getStringList("lore") : null;
        return build(material, Math.max(1, section.getInt("amount", 1)), name, lore, section.getBoolean("glow"), placeholders, multiline);
    }

    public static ItemStack simple(Material material, String name) {
        return build(material, 1, name, null, false, Map.of(), Map.of());
    }

    public static ItemStack build(Material material, int amount, String name, List<String> lore, boolean glow,
                                  Map<String, String> placeholders, Map<String, List<String>> multiline) {
        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }
        if (name != null) {
            meta.displayName(Text.parse(name, placeholders));
        }
        if (lore != null) {
            List<Component> lines = new ArrayList<>();
            for (String line : lore) {
                List<String> expanded = multiline.get(line.trim());
                if (expanded == null) {
                    lines.add(Text.parse(line, placeholders));
                    continue;
                }
                for (String extra : expanded) {
                    lines.add(Text.parse(extra, placeholders));
                }
            }
            meta.lore(lines);
        }
        if (glow) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.setEnchantmentGlintOverride(true);
        }
        meta.addItemFlags(ItemFlag.values());
        item.setItemMeta(meta);
        return item;
    }
}
