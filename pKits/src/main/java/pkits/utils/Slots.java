package pkits.utils;

import java.util.Collection;

public final class Slots {

    public static final int BOOTS = 36;
    public static final int LEGGINGS = 37;
    public static final int CHESTPLATE = 38;
    public static final int HELMET = 39;
    public static final int STORAGE_SIZE = 36;
    public static final int PAGE_STRIDE = 100;

    private Slots() {
    }

    public static boolean isArmor(int slot) {
        return slot >= BOOTS && slot <= HELMET;
    }

    public static int pageOf(int slot) {
        return slot / PAGE_STRIDE;
    }

    public static int storageKey(int page, int offset) {
        return page * PAGE_STRIDE + offset;
    }

    public static int usedPages(Collection<Integer> keys) {
        int pages = 1;
        for (int key : keys) {
            if (!isArmor(key) && key >= 0) {
                pages = Math.max(pages, pageOf(key) + 1);
            }
        }
        return pages;
    }
}
