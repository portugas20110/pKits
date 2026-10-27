package pkits.managers;

import org.bukkit.Material;
import org.bukkit.block.ShulkerBox;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BlockStateMeta;
import pkits.PKitsPlugin;
import pkits.models.Kit;
import pkits.utils.ArmorUtil;
import pkits.utils.ItemFactory;
import pkits.utils.Placeholders;
import pkits.utils.Slots;
import pkits.utils.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DeliveryManager {

    private static final int SHULKER_SIZE = 27;

    private final PKitsPlugin plugin;

    public DeliveryManager(PKitsPlugin plugin) {
        this.plugin = plugin;
    }

    public void deliver(Player player, Kit kit) {
        if (kit.isShulker()) {
            deliverShulker(player, kit);
            return;
        }
        deliverLoose(player, kit);
    }

    private void deliverLoose(Player player, Kit kit) {
        PlayerInventory inventory = player.getInventory();
        for (Map.Entry<Integer, ItemStack> entry : kit.getItems().entrySet()) {
            int slot = entry.getKey();
            ItemStack item = entry.getValue().clone();
            if (kit.isAutoEquip() && Slots.isArmor(slot) && isFree(inventory.getItem(slot)) && ArmorUtil.fits(item, slot)) {
                inventory.setItem(slot, item);
                continue;
            }
            give(player, item);
        }
    }

    private void deliverShulker(Player player, Kit kit) {
        FileConfiguration config = plugin.getConfig();
        Material material = ItemFactory.material(config.getString("shulker.material"), Material.SHULKER_BOX);
        if (!material.name().endsWith("SHULKER_BOX")) {
            material = Material.SHULKER_BOX;
        }
        String nameTemplate = kit.getShulkerName().isEmpty() ? config.getString("shulker.name", "") : kit.getShulkerName();
        List<ItemStack> items = new ArrayList<>(kit.getItems().values());
        int boxes = (items.size() + SHULKER_SIZE - 1) / SHULKER_SIZE;
        for (int index = 0; index < boxes; index++) {
            List<ItemStack> chunk = items.subList(index * SHULKER_SIZE, Math.min(items.size(), (index + 1) * SHULKER_SIZE));
            ItemStack box = new ItemStack(material);
            BlockStateMeta meta = (BlockStateMeta) box.getItemMeta();
            ShulkerBox state = (ShulkerBox) meta.getBlockState();
            for (int position = 0; position < chunk.size(); position++) {
                state.getInventory().setItem(position, chunk.get(position).clone());
            }
            meta.setBlockState(state);
            Map<String, String> placeholders = Placeholders.of(
                    "%kit-name%", kit.getDisplayName(),
                    "%kit-id%", kit.getId(),
                    "%index%", String.valueOf(index + 1),
                    "%boxes%", String.valueOf(boxes));
            meta.displayName(Text.parse(nameTemplate, placeholders));
            meta.lore(Text.parse(config.getStringList("shulker.lore"), placeholders));
            box.setItemMeta(meta);
            give(player, box);
        }
    }

    private boolean isFree(ItemStack item) {
        return item == null || item.getType().isAir();
    }

    private void give(Player player, ItemStack item) {
        Map<Integer, ItemStack> leftovers = player.getInventory().addItem(item);
        for (ItemStack leftover : leftovers.values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), leftover);
        }
    }
}
