package pkits.guis;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import pkits.PKitsPlugin;
import pkits.models.Kit;
import pkits.utils.ItemFactory;
import pkits.utils.Placeholders;
import pkits.utils.Slots;
import pkits.utils.SoundUtil;
import pkits.utils.Text;

import java.util.Map;

public class PreviewMenu extends MenuHolder {

    private final PKitsPlugin plugin;
    private final Kit kit;
    private final KitLayout layout;
    private final int returnPage;
    private final int itemPage;
    private final int pages;
    private final int backSlot;

    public PreviewMenu(PKitsPlugin plugin, Kit kit, int returnPage) {
        this(plugin, kit, returnPage, 0);
    }

    private PreviewMenu(PKitsPlugin plugin, Kit kit, int returnPage, int requestedPage) {
        this.plugin = plugin;
        this.kit = kit;
        this.returnPage = returnPage;
        FileConfiguration config = plugin.getConfig();
        ConfigurationSection preview = config.getConfigurationSection("preview");
        Map<Integer, ItemStack> items = kit.getResolvedPreview();
        this.pages = Slots.usedPages(items.keySet());
        this.itemPage = Math.max(0, Math.min(requestedPage, pages - 1));
        this.layout = new KitLayout(config.getConfigurationSection("layout"), itemPage);
        Map<String, String> placeholders = Placeholders.of(
                "%kit-name%", kit.getDisplayName(),
                "%kit-id%", kit.getId(),
                "%page%", String.valueOf(itemPage + 1),
                "%pages%", String.valueOf(pages));
        this.inventory = Bukkit.createInventory(this, 54, Text.parse(preview.getString("title", ""), placeholders));
        layout.decorate(inventory, config);
        for (Map.Entry<Integer, ItemStack> entry : items.entrySet()) {
            Integer guiSlot = layout.getGuiSlot(entry.getKey());
            if (guiSlot != null) {
                inventory.setItem(guiSlot, entry.getValue().clone());
            }
        }
        layout.navigation(inventory, config, pages);
        this.backSlot = preview.getInt("back.slot", 8);
        inventory.setItem(backSlot, ItemFactory.fromSection(preview.getConfigurationSection("back"), placeholders));
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player) || event.getClickedInventory() != inventory) {
            return;
        }
        int slot = event.getRawSlot();
        if (slot == backSlot) {
            SoundUtil.play(player, plugin.getConfig().getString("sounds.click"));
            new MainMenu(plugin, player, returnPage).openLater(plugin, player);
            return;
        }
        if (slot == layout.getPreviousSlot() && itemPage > 0) {
            SoundUtil.play(player, plugin.getConfig().getString("sounds.click"));
            new PreviewMenu(plugin, kit, returnPage, itemPage - 1).openLater(plugin, player);
            return;
        }
        if (slot == layout.getNextSlot() && itemPage < pages - 1) {
            SoundUtil.play(player, plugin.getConfig().getString("sounds.click"));
            new PreviewMenu(plugin, kit, returnPage, itemPage + 1).openLater(plugin, player);
        }
    }
}
