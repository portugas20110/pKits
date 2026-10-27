package pkits.utils;

import org.bukkit.inventory.ItemStack;

public final class ArmorUtil {

    private ArmorUtil() {
    }

    public static boolean fits(ItemStack item, int slot) {
        String name = item.getType().name();
        return switch (slot) {
            case Slots.HELMET -> name.endsWith("_HELMET") || name.endsWith("_HEAD") || name.endsWith("_SKULL") || name.equals("CARVED_PUMPKIN");
            case Slots.CHESTPLATE -> name.endsWith("_CHESTPLATE") || name.equals("ELYTRA");
            case Slots.LEGGINGS -> name.endsWith("_LEGGINGS");
            case Slots.BOOTS -> name.endsWith("_BOOTS");
            default -> false;
        };
    }
}
