package pkits.guis;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import pkits.PKitsPlugin;
import pkits.models.Kit;
import pkits.utils.ItemFactory;
import pkits.utils.Placeholders;
import pkits.utils.SoundUtil;
import pkits.utils.Text;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;

public class MainMenu extends MenuHolder {

    private static final List<Integer> DEFAULT_CELLS = List.of(10, 11, 12, 13, 14, 15, 16, 28, 29, 30, 31, 32, 33, 34);

    private final PKitsPlugin plugin;
    private final Player player;
    private final List<Integer> cells;
    private final NavigableMap<Integer, Kit> positions;
    private final Map<Integer, Kit> kitCells = new HashMap<>();
    private final Map<Integer, Kit> previewCells = new HashMap<>();
    private final int page;
    private final int pages;

    public MainMenu(PKitsPlugin plugin, Player player, int requestedPage) {
        this.plugin = plugin;
        this.player = player;
        ConfigurationSection menu = plugin.getConfig().getConfigurationSection("menu");
        List<Integer> configured = menu.getIntegerList("kit-slots");
        this.cells = configured.isEmpty() ? DEFAULT_CELLS : configured;
        this.positions = plugin.kitManager().positions();
        int highest = positions.isEmpty() ? 0 : positions.lastKey();
        this.pages = highest / cells.size() + 1;
        this.page = Math.max(0, Math.min(requestedPage, pages - 1));
        int rows = Math.max(1, Math.min(6, menu.getInt("rows", 6)));
        Map<String, String> placeholders = Placeholders.of(
                "%page%", String.valueOf(page + 1),
                "%pages%", String.valueOf(pages));
        this.inventory = Bukkit.createInventory(this, rows * 9, Text.parse(menu.getString("title", ""), placeholders));
        render();
    }

    private void render() {
        kitCells.clear();
        previewCells.clear();
        inventory.clear();
        ConfigurationSection menu = plugin.getConfig().getConfigurationSection("menu");
        int size = inventory.getSize();
        ItemStack filler = ItemFactory.fromSection(menu.getConfigurationSection("filler"), Map.of());
        for (int slot = 0; slot < size; slot++) {
            inventory.setItem(slot, filler);
        }
        int offset = menu.getInt("preview-offset", 9);
        int perPage = cells.size();
        for (Map.Entry<Integer, Kit> entry : positions.entrySet()) {
            if (entry.getKey() / perPage != page) {
                continue;
            }
            int cell = cells.get(entry.getKey() % perPage);
            if (cell < 0 || cell >= size) {
                continue;
            }
            Kit kit = entry.getValue();
            inventory.setItem(cell, KitItems.icon(plugin, player, kit));
            kitCells.put(cell, kit);
            int below = cell + offset;
            if (below >= 0 && below < size) {
                inventory.setItem(below, KitItems.previewButton(plugin, player, kit));
                previewCells.put(below, kit);
            }
        }
        if (page > 0) {
            setNavigation(menu, "previous", page);
        }
        if (page < pages - 1) {
            setNavigation(menu, "next", page + 2);
        }
        setNavigation(menu, "close", page + 1);
    }

    private void setNavigation(ConfigurationSection menu, String key, int target) {
        ConfigurationSection section = menu.getConfigurationSection(key);
        int slot = section.getInt("slot");
        if (slot < 0 || slot >= inventory.getSize()) {
            return;
        }
        Map<String, String> placeholders = Placeholders.of("%target%", String.valueOf(target));
        inventory.setItem(slot, ItemFactory.fromSection(section, placeholders));
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player clicker) || event.getClickedInventory() != inventory) {
            return;
        }
        ConfigurationSection menu = plugin.getConfig().getConfigurationSection("menu");
        int slot = event.getRawSlot();
        Kit kit = kitCells.get(slot);
        if (kit != null) {
            if (event.isRightClick()) {
                new PreviewMenu(plugin, kit, page).openLater(plugin, clicker);
                return;
            }
            plugin.kitManager().claim(clicker, kit);
            render();
            return;
        }
        Kit previewKit = previewCells.get(slot);
        if (previewKit != null) {
            SoundUtil.play(clicker, plugin.getConfig().getString("sounds.click"));
            new PreviewMenu(plugin, previewKit, page).openLater(plugin, clicker);
            return;
        }
        if (slot == menu.getInt("previous.slot") && page > 0) {
            SoundUtil.play(clicker, plugin.getConfig().getString("sounds.click"));
            new MainMenu(plugin, clicker, page - 1).openLater(plugin, clicker);
            return;
        }
        if (slot == menu.getInt("next.slot") && page < pages - 1) {
            SoundUtil.play(clicker, plugin.getConfig().getString("sounds.click"));
            new MainMenu(plugin, clicker, page + 1).openLater(plugin, clicker);
            return;
        }
        if (slot == menu.getInt("close.slot")) {
            Bukkit.getScheduler().runTask(plugin, () -> clicker.closeInventory());
        }
    }
}
