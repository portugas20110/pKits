package pkits.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import pkits.guis.MenuHolder;

public class MenuListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        MenuHolder holder = holderOf(event.getView().getTopInventory());
        if (holder != null) {
            holder.handleClick(event);
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        MenuHolder holder = holderOf(event.getView().getTopInventory());
        if (holder != null) {
            holder.handleDrag(event);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        MenuHolder holder = holderOf(event.getInventory());
        if (holder != null) {
            holder.handleClose(event);
        }
    }

    private MenuHolder holderOf(Inventory inventory) {
        return inventory.getHolder(false) instanceof MenuHolder holder ? holder : null;
    }
}
