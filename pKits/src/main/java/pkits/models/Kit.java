package pkits.models;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Kit {

    private final String id;
    private final List<String> lore = new ArrayList<>();
    private final Map<Integer, ItemStack> items = new TreeMap<>();
    private final Map<Integer, ItemStack> previewItems = new TreeMap<>();
    private String displayName;
    private String permission;
    private String shulkerName = "";
    private long cooldown;
    private boolean autoEquip;
    private boolean shulker;
    private boolean glow;
    private Material icon = Material.CHEST;
    private int slot;

    public Kit(String id) {
        this.id = id;
        this.displayName = "<white>" + id;
        this.permission = "pkits.kit." + id;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public List<String> getLore() {
        return lore;
    }

    public void setLore(List<String> lines) {
        lore.clear();
        lore.addAll(lines);
    }

    public String getShulkerName() {
        return shulkerName;
    }

    public void setShulkerName(String shulkerName) {
        this.shulkerName = shulkerName;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public long getCooldown() {
        return cooldown;
    }

    public void setCooldown(long cooldown) {
        this.cooldown = cooldown;
    }

    public boolean isAutoEquip() {
        return autoEquip;
    }

    public void setAutoEquip(boolean autoEquip) {
        this.autoEquip = autoEquip;
    }

    public boolean isShulker() {
        return shulker;
    }

    public void setShulker(boolean shulker) {
        this.shulker = shulker;
    }

    public boolean isGlow() {
        return glow;
    }

    public void setGlow(boolean glow) {
        this.glow = glow;
    }

    public Material getIcon() {
        return icon;
    }

    public void setIcon(Material icon) {
        this.icon = icon;
    }

    public int getSlot() {
        return slot;
    }

    public void setSlot(int slot) {
        this.slot = slot;
    }

    public Map<Integer, ItemStack> getItems() {
        return items;
    }

    public void setItems(Map<Integer, ItemStack> source) {
        copy(source, items);
    }

    public Map<Integer, ItemStack> getPreviewItems() {
        return previewItems;
    }

    public void setPreviewItems(Map<Integer, ItemStack> source) {
        copy(source, previewItems);
    }

    public boolean isCustomPreview() {
        return !previewItems.isEmpty();
    }

    public Map<Integer, ItemStack> getResolvedPreview() {
        return previewItems.isEmpty() ? items : previewItems;
    }

    private static void copy(Map<Integer, ItemStack> source, Map<Integer, ItemStack> target) {
        Map<Integer, ItemStack> snapshot = new TreeMap<>();
        source.forEach((slot, item) -> snapshot.put(slot, item.clone()));
        target.clear();
        target.putAll(snapshot);
    }
}
