package pkits.guis;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import pkits.utils.ItemFactory;
import pkits.utils.Placeholders;
import pkits.utils.Slots;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class KitLayout {

    private final Map<Integer, Integer> guiToKit = new HashMap<>();
    private final Map<Integer, Integer> kitToGui = new HashMap<>();
    private final Map<Integer, String> labels = new HashMap<>();
    private final List<Integer> armorCells = new ArrayList<>();
    private final int page;
    private final int previousSlot;
    private final int nextSlot;

    public KitLayout(ConfigurationSection layout, int page) {
        this.page = page;
        this.previousSlot = layout.getInt("page-previous.slot", 0);
        this.nextSlot = layout.getInt("page-next.slot", 5);
        for (int offset = 0; offset < Slots.STORAGE_SIZE; offset++) {
            bind(18 + offset, Slots.storageKey(page, offset));
        }
        bindArmor(layout, "helmet", 1, Slots.HELMET);
        bindArmor(layout, "chestplate", 2, Slots.CHESTPLATE);
        bindArmor(layout, "leggings", 3, Slots.LEGGINGS);
        bindArmor(layout, "boots", 4, Slots.BOOTS);
    }

    public Integer getKitSlot(int guiSlot) {
        return guiToKit.get(guiSlot);
    }

    public Integer getGuiSlot(int kitSlot) {
        return kitToGui.get(kitSlot);
    }

    public boolean isEditable(int guiSlot) {
        return guiToKit.containsKey(guiSlot);
    }

    public boolean isArmorCell(int guiSlot) {
        return armorCells.contains(guiSlot);
    }

    public Set<Integer> getEditableSlots() {
        return guiToKit.keySet();
    }

    public List<Integer> getArmorCells() {
        return armorCells;
    }

    public List<Integer> getStorageSlots() {
        List<Integer> slots = new ArrayList<>();
        for (int slot = 18; slot < 54; slot++) {
            slots.add(slot);
        }
        return slots;
    }

    public int getPreviousSlot() {
        return previousSlot;
    }

    public int getNextSlot() {
        return nextSlot;
    }

    public void decorate(Inventory inventory, FileConfiguration config) {
        ItemStack filler = ItemFactory.fromSection(config.getConfigurationSection("layout.filler"), Map.of());
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            inventory.setItem(slot, filler);
        }
        for (int slot : guiToKit.keySet()) {
            if (slot >= 0 && slot < inventory.getSize()) {
                inventory.setItem(slot, null);
            }
        }
        Material material = ItemFactory.material(config.getString("layout.label.material"), Material.GRAY_STAINED_GLASS_PANE);
        for (Map.Entry<Integer, String> entry : labels.entrySet()) {
            int slot = entry.getKey();
            if (slot >= 0 && slot < inventory.getSize()) {
                String name = config.getString("layout.label.names." + entry.getValue(), entry.getValue());
                inventory.setItem(slot, ItemFactory.simple(material, name));
            }
        }
    }

    public void navigation(Inventory inventory, FileConfiguration config, int pages) {
        if (page > 0) {
            inventory.setItem(previousSlot, ItemFactory.fromSection(
                    config.getConfigurationSection("layout.page-previous"),
                    Placeholders.of("%target%", String.valueOf(page), "%page%", String.valueOf(page + 1), "%pages%", String.valueOf(pages))));
        }
        if (page < pages - 1) {
            inventory.setItem(nextSlot, ItemFactory.fromSection(
                    config.getConfigurationSection("layout.page-next"),
                    Placeholders.of("%target%", String.valueOf(page + 2), "%page%", String.valueOf(page + 1), "%pages%", String.valueOf(pages))));
        }
    }

    private void bindArmor(ConfigurationSection layout, String key, int fallback, int kitSlot) {
        int guiSlot = layout.getInt("armor-slots." + key, fallback);
        bind(guiSlot, kitSlot);
        armorCells.add(guiSlot);
        labels.put(guiSlot + 9, key);
    }

    private void bind(int guiSlot, int kitSlot) {
        guiToKit.put(guiSlot, kitSlot);
        kitToGui.put(kitSlot, guiSlot);
    }
}
