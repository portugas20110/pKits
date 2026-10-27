package pkits.guis;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import pkits.PKitsPlugin;
import pkits.models.Kit;
import pkits.utils.ArmorUtil;
import pkits.utils.ItemFactory;
import pkits.utils.Placeholders;
import pkits.utils.Slots;
import pkits.utils.Text;

import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class ItemsMenu extends MenuHolder {

    public enum Mode { CREATE, RECEIVED, PREVIEW }

    private final PKitsPlugin plugin;
    private final Kit kit;
    private final Mode mode;
    private final KitLayout layout;
    private final Map<Integer, ItemStack> working;
    private final int page;
    private final int pages;
    private final int actionSlot;
    private final int resetSlot;
    private final Map<String, String> placeholders;
    private boolean returning;
    private boolean discard;
    private boolean switching;

    public ItemsMenu(PKitsPlugin plugin, Kit kit, Mode mode) {
        this(plugin, kit, mode, initial(kit, mode), 0);
    }

    private ItemsMenu(PKitsPlugin plugin, Kit kit, Mode mode, Map<Integer, ItemStack> working, int requestedPage) {
        this.plugin = plugin;
        this.kit = kit;
        this.mode = mode;
        this.working = working;
        FileConfiguration config = plugin.getConfig();
        ConfigurationSection editor = config.getConfigurationSection("item-editor");
        int maxPages = Math.max(1, editor.getInt("max-pages", 10));
        int used = Slots.usedPages(working.keySet());
        this.page = Math.max(0, Math.min(requestedPage, maxPages - 1));
        this.pages = Math.min(maxPages, Math.max(used + 1, page + 1));
        this.layout = new KitLayout(config.getConfigurationSection("layout"), page);
        this.actionSlot = editor.getInt("action.slot", 8);
        this.resetSlot = editor.getInt("reset-preview.slot", 7);
        this.placeholders = Placeholders.of(
                "%kit-name%", kit.getDisplayName(),
                "%kit-id%", kit.getId(),
                "%page%", String.valueOf(page + 1),
                "%pages%", String.valueOf(pages));
        String title = editor.getString("titles." + mode.name().toLowerCase(Locale.ROOT), "");
        this.inventory = Bukkit.createInventory(this, 54, Text.parse(title, placeholders));
        layout.decorate(inventory, config);
        for (Map.Entry<Integer, ItemStack> entry : working.entrySet()) {
            Integer guiSlot = layout.getGuiSlot(entry.getKey());
            if (guiSlot != null) {
                inventory.setItem(guiSlot, entry.getValue().clone());
            }
        }
        layout.navigation(inventory, config, pages);
        inventory.setItem(actionSlot, ItemFactory.fromSection(editor.getConfigurationSection("action"), placeholders));
        if (mode == Mode.PREVIEW) {
            inventory.setItem(resetSlot, ItemFactory.fromSection(editor.getConfigurationSection("reset-preview"), placeholders));
        }
    }

    private static Map<Integer, ItemStack> initial(Kit kit, Mode mode) {
        Map<Integer, ItemStack> source = mode == Mode.PREVIEW ? kit.getResolvedPreview() : kit.getItems();
        Map<Integer, ItemStack> copy = new TreeMap<>();
        source.forEach((slot, item) -> copy.put(slot, item.clone()));
        return copy;
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (event.getAction() == InventoryAction.COLLECT_TO_CURSOR) {
            event.setCancelled(true);
            return;
        }
        Inventory clicked = event.getClickedInventory();
        if (clicked == null) {
            return;
        }
        if (clicked == inventory) {
            handleTopClick(event, player);
            return;
        }
        if (event.isShiftClick()) {
            event.setCancelled(true);
            moveFromPlayer(event);
        }
    }

    private void handleTopClick(InventoryClickEvent event, Player player) {
        int slot = event.getRawSlot();
        if (layout.isEditable(slot)) {
            if (layout.isArmorCell(slot) && !acceptsIncoming(event, player, slot)) {
                event.setCancelled(true);
            }
            return;
        }
        event.setCancelled(true);
        if (slot == actionSlot) {
            finish(player);
            return;
        }
        if (slot == layout.getPreviousSlot() && page > 0) {
            switchPage(player, page - 1);
            return;
        }
        if (slot == layout.getNextSlot() && page < pages - 1) {
            switchPage(player, page + 1);
            return;
        }
        if (mode == Mode.PREVIEW && slot == resetSlot) {
            kit.setPreviewItems(Map.of());
            plugin.kitManager().save();
            plugin.messages().send(player, "editor.preview-reset", placeholders);
            discard = true;
            finish(player);
        }
    }

    private boolean acceptsIncoming(InventoryClickEvent event, Player player, int slot) {
        ItemStack incoming = switch (event.getAction()) {
            case PLACE_ALL, PLACE_ONE, PLACE_SOME, SWAP_WITH_CURSOR -> event.getCursor();
            case HOTBAR_SWAP -> event.getHotbarButton() < 0
                    ? player.getInventory().getItemInOffHand()
                    : player.getInventory().getItem(event.getHotbarButton());
            default -> null;
        };
        if (incoming == null || incoming.getType().isAir()) {
            return true;
        }
        return ArmorUtil.fits(incoming, layout.getKitSlot(slot));
    }

    private void moveFromPlayer(InventoryClickEvent event) {
        ItemStack moved = event.getCurrentItem();
        if (moved == null || moved.getType().isAir()) {
            return;
        }
        for (int slot : layout.getArmorCells()) {
            if (inventory.getItem(slot) == null && ArmorUtil.fits(moved, layout.getKitSlot(slot))) {
                inventory.setItem(slot, moved.clone());
                event.setCurrentItem(null);
                return;
            }
        }
        for (int slot : layout.getStorageSlots()) {
            if (inventory.getItem(slot) == null) {
                inventory.setItem(slot, moved.clone());
                event.setCurrentItem(null);
                return;
            }
        }
    }

    @Override
    public void handleDrag(InventoryDragEvent event) {
        int size = inventory.getSize();
        for (int raw : event.getRawSlots()) {
            if (raw >= size) {
                continue;
            }
            if (!layout.isEditable(raw)) {
                event.setCancelled(true);
                return;
            }
            ItemStack dragged = event.getOldCursor();
            if (layout.isArmorCell(raw) && !dragged.getType().isAir() && !ArmorUtil.fits(dragged, layout.getKitSlot(raw))) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @Override
    public void handleClose(InventoryCloseEvent event) {
        if (switching || !(event.getPlayer() instanceof Player player)) {
            return;
        }
        if (discard) {
            reopenEditor(player);
            return;
        }
        capture();
        switch (mode) {
            case CREATE -> {
                if (working.isEmpty()) {
                    plugin.messages().send(player, "editor.create-empty", placeholders);
                    return;
                }
                kit.setItems(working);
                if (!plugin.kitManager().register(kit)) {
                    plugin.messages().send(player, "editor.exists", placeholders);
                    return;
                }
                plugin.messages().send(player, "editor.created", placeholders);
            }
            case RECEIVED -> {
                kit.setItems(working);
                plugin.kitManager().save();
                plugin.messages().send(player, "editor.items-saved", placeholders);
            }
            case PREVIEW -> {
                if (!working.equals(kit.getResolvedPreview())) {
                    kit.setPreviewItems(working);
                    plugin.kitManager().save();
                    plugin.messages().send(player, "editor.preview-saved", placeholders);
                }
            }
        }
        reopenEditor(player);
    }

    private void capture() {
        working.keySet().removeIf(key -> Slots.isArmor(key)
                || (Slots.pageOf(key) == page && key % Slots.PAGE_STRIDE < Slots.STORAGE_SIZE));
        for (int guiSlot : layout.getEditableSlots()) {
            ItemStack item = inventory.getItem(guiSlot);
            if (item == null || item.getType().isAir()) {
                continue;
            }
            working.put(layout.getKitSlot(guiSlot), item.clone());
        }
    }

    private void switchPage(Player player, int target) {
        capture();
        switching = true;
        new ItemsMenu(plugin, kit, mode, working, target).openLater(plugin, player);
    }

    private void finish(Player player) {
        returning = true;
        Bukkit.getScheduler().runTask(plugin, () -> player.closeInventory());
    }

    private void reopenEditor(Player player) {
        if (returning) {
            new EditorMenu(plugin, kit).openLater(plugin, player);
        }
    }
}
