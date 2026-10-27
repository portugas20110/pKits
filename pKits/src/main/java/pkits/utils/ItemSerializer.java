package pkits.utils;

import org.bukkit.inventory.ItemStack;

import java.util.Base64;

public final class ItemSerializer {

    private ItemSerializer() {
    }

    public static String encode(ItemStack item) {
        return Base64.getEncoder().encodeToString(item.serializeAsBytes());
    }

    public static ItemStack decode(String data) {
        try {
            return ItemStack.deserializeBytes(Base64.getDecoder().decode(data));
        } catch (RuntimeException exception) {
            return null;
        }
    }
}
