package pkits.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import pkits.PKitsPlugin;

public class ChatInputListener implements Listener {

    private final PKitsPlugin plugin;

    public ChatInputListener(PKitsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncChatEvent event) {
        capture(event.getPlayer(), PlainTextComponentSerializer.plainText().serialize(event.message()), event);
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.LOWEST)
    public void onLegacyChat(AsyncPlayerChatEvent event) {
        capture(event.getPlayer(), event.getMessage(), event);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.inputManager().clear(event.getPlayer().getUniqueId());
    }

    private void capture(Player player, String text, Cancellable event) {
        if (!plugin.inputManager().has(player.getUniqueId())) {
            return;
        }
        event.setCancelled(true);
        String message = text.trim();
        Bukkit.getScheduler().runTask(plugin, () -> plugin.inputManager().process(player, message));
    }
}
