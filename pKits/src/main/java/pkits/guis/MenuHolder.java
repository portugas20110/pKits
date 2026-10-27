package pkits.guis;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import pkits.PKitsPlugin;

public abstract class MenuHolder implements InventoryHolder {

    protected Inventory inventory;

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public abstract void handleClick(InventoryClickEvent event);

    public void handleDrag(InventoryDragEvent event) {
        event.setCancelled(true);
    }

    public void handleClose(InventoryCloseEvent event) {
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    public void openLater(PKitsPlugin plugin, Player player) {
        Bukkit.getScheduler().runTask(plugin, () -> open(player));
    }
}
