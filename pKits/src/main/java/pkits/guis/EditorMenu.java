package pkits.guis;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import pkits.PKitsPlugin;
import pkits.models.InputType;
import pkits.models.Kit;
import pkits.utils.ItemFactory;
import pkits.utils.Placeholders;
import pkits.utils.Text;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditorMenu extends MenuHolder {

    private final PKitsPlugin plugin;
    private final Kit kit;
    private final Map<Integer, String> buttons = new HashMap<>();

    public EditorMenu(PKitsPlugin plugin, Kit kit) {
        this.plugin = plugin;
        this.kit = kit;
        ConfigurationSection editor = plugin.getConfig().getConfigurationSection("editor");
        int rows = Math.max(1, Math.min(6, editor.getInt("rows", 4)));
        Map<String, String> placeholders = Placeholders.of("%kit-name%", kit.getDisplayName(), "%kit-id%", kit.getId());
        this.inventory = Bukkit.createInventory(this, rows * 9, Text.parse(editor.getString("title", ""), placeholders));
        render();
    }

    private void render() {
        inventory.clear();
        buttons.clear();
        ConfigurationSection editor = plugin.getConfig().getConfigurationSection("editor");
        ItemStack filler = ItemFactory.fromSection(editor.getConfigurationSection("filler"), Map.of());
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            inventory.setItem(slot, filler);
        }
        ConfigurationSection section = editor.getConfigurationSection("buttons");
        Map<String, String> placeholders = placeholders(editor);
        List<String> currentLore = kit.getLore().isEmpty() ? List.of(editor.getString("no-lore", "")) : kit.getLore();
        for (String id : section.getKeys(false)) {
            ConfigurationSection button = section.getConfigurationSection(id);
            int slot = button.getInt("slot");
            if (slot < 0 || slot >= inventory.getSize()) {
                continue;
            }
            Material material = id.equals("icon") ? kit.getIcon() : ItemFactory.material(button.getString("material"), Material.STONE);
            boolean active = switch (id) {
                case "auto-equip" -> kit.isAutoEquip();
                case "shulker" -> kit.isShulker();
                case "glow" -> kit.isGlow();
                default -> false;
            };
            inventory.setItem(slot, ItemFactory.build(
                    material,
                    1,
                    button.getString("name", id),
                    button.getStringList("lore"),
                    active || button.getBoolean("glow"),
                    placeholders,
                    Map.<String, List<String>>of("%current-lore%", currentLore)));
            buttons.put(slot, id);
        }
    }

    private Map<String, String> placeholders(ConfigurationSection editor) {
        String cooldown;
        if (kit.getCooldown() < 0L) {
            cooldown = editor.getString("cooldown-once", "");
        } else if (kit.getCooldown() == 0L) {
            cooldown = editor.getString("cooldown-none", "");
        } else {
            cooldown = plugin.formatTime(kit.getCooldown());
        }
        String permission = kit.getPermission().isEmpty() ? editor.getString("permission-none", "") : kit.getPermission();
        return Placeholders.of(
                "%kit-name%", kit.getDisplayName(),
                "%kit-id%", kit.getId(),
                "%cooldown%", cooldown,
                "%permission%", permission,
                "%auto-equip%", state(editor, kit.isAutoEquip()),
                "%shulker%", state(editor, kit.isShulker()),
                "%shulker-name%", kit.getShulkerName().isEmpty() ? editor.getString("shulker-name-default", "") : kit.getShulkerName(),
                "%glow%", state(editor, kit.isGlow()),
                "%slot%", String.valueOf(kit.getSlot()),
                "%icon%", kit.getIcon().name(),
                "%lore-lines%", String.valueOf(kit.getLore().size()),
                "%items%", String.valueOf(kit.getItems().size()),
                "%preview-items%", String.valueOf(kit.getResolvedPreview().size()),
                "%preview-mode%", editor.getString(kit.isCustomPreview() ? "states.custom-preview" : "states.default-preview", ""));
    }

    private String state(ConfigurationSection editor, boolean value) {
        return editor.getString(value ? "states.on" : "states.off", "");
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player) || event.getClickedInventory() != inventory) {
            return;
        }
        String id = buttons.get(event.getRawSlot());
        if (id == null) {
            return;
        }
        switch (id) {
            case "name" -> prompt(player, InputType.NAME);
            case "lore" -> handleLore(player, event);
            case "cooldown" -> prompt(player, InputType.COOLDOWN);
            case "permission" -> prompt(player, InputType.PERMISSION);
            case "shulker-name" -> prompt(player, InputType.SHULKER_NAME);
            case "auto-equip" -> {
                kit.setAutoEquip(!kit.isAutoEquip());
                changed();
            }
            case "shulker" -> {
                kit.setShulker(!kit.isShulker());
                changed();
            }
            case "glow" -> {
                kit.setGlow(!kit.isGlow());
                changed();
            }
            case "items" -> new ItemsMenu(plugin, kit, ItemsMenu.Mode.RECEIVED).openLater(plugin, player);
            case "preview-items" -> new ItemsMenu(plugin, kit, ItemsMenu.Mode.PREVIEW).openLater(plugin, player);
            case "icon" -> handleIcon(player, event);
            case "slot" -> {
                int step = event.isShiftClick() ? 5 : 1;
                int delta = event.isRightClick() ? -step : step;
                kit.setSlot(Math.max(0, kit.getSlot() + delta));
                changed();
            }
            case "close" -> Bukkit.getScheduler().runTask(plugin, () -> player.closeInventory());
            default -> {
            }
        }
    }

    private void handleLore(Player player, InventoryClickEvent event) {
        if (event.isShiftClick()) {
            kit.getLore().clear();
            changed();
            return;
        }
        if (event.isRightClick()) {
            if (!kit.getLore().isEmpty()) {
                kit.getLore().remove(kit.getLore().size() - 1);
            }
            changed();
            return;
        }
        prompt(player, InputType.LORE_ADD);
    }

    private void handleIcon(Player player, InventoryClickEvent event) {
        ItemStack cursor = event.getCursor();
        if (cursor != null && !cursor.getType().isAir()) {
            kit.setIcon(cursor.getType());
            changed();
            return;
        }
        prompt(player, InputType.ICON);
    }

    private void prompt(Player player, InputType type) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            player.closeInventory();
            plugin.inputManager().request(player.getUniqueId(), kit.getId(), type);
            plugin.messages().send(player, "prompt." + type.getKey(), Placeholders.of(
                    "%kit-name%", kit.getDisplayName(),
                    "%kit-id%", kit.getId()));
        });
    }

    private void changed() {
        plugin.kitManager().save();
        render();
    }
}
