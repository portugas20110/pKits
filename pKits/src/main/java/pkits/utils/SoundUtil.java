package pkits.utils;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.entity.Player;

public final class SoundUtil {

    private SoundUtil() {
    }

    public static void play(Player player, String key) {
        if (key == null || key.isBlank()) {
            return;
        }
        try {
            player.playSound(Sound.sound(Key.key(key), Sound.Source.MASTER, 1.0f, 1.0f));
        } catch (RuntimeException ignored) {
            return;
        }
    }
}
