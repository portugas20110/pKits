package pkits.managers;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import pkits.PKitsPlugin;
import pkits.guis.EditorMenu;
import pkits.models.InputType;
import pkits.models.Kit;
import pkits.utils.ItemFactory;
import pkits.utils.Placeholders;
import pkits.utils.TimeFormat;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InputManager {

    public record PendingInput(String kitId, InputType type) {
    }

    private final PKitsPlugin plugin;
    private final Map<UUID, PendingInput> pending = new ConcurrentHashMap<>();

    public InputManager(PKitsPlugin plugin) {
        this.plugin = plugin;
    }

    public void request(UUID playerId, String kitId, InputType type) {
        pending.put(playerId, new PendingInput(kitId, type));
    }

    public boolean has(UUID playerId) {
        return pending.containsKey(playerId);
    }

    public void clear(UUID playerId) {
        pending.remove(playerId);
    }

    public void process(Player player, String text) {
        PendingInput input = pending.remove(player.getUniqueId());
        if (input == null) {
            return;
        }
        MessageManager messages = plugin.messages();
        Kit kit = plugin.kitManager().getKit(input.kitId());
        if (kit == null) {
            messages.send(player, "kit-not-found", Placeholders.of("%kit-id%", input.kitId()));
            return;
        }
        if (text.equalsIgnoreCase(messages.raw("input.cancel-word"))) {
            messages.send(player, "input.cancelled", placeholders(kit));
            new EditorMenu(plugin, kit).open(player);
            return;
        }
        if (!apply(kit, input, text)) {
            messages.send(player, "input.invalid." + input.type().getKey(), placeholders(kit));
            new EditorMenu(plugin, kit).open(player);
            return;
        }
        plugin.kitManager().save();
        messages.send(player, "input.updated", placeholders(kit));
        new EditorMenu(plugin, kit).open(player);
    }

    private Map<String, String> placeholders(Kit kit) {
        return Placeholders.of("%kit-name%", kit.getDisplayName(), "%kit-id%", kit.getId());
    }

    private boolean apply(Kit kit, PendingInput input, String text) {
        String none = plugin.messages().raw("input.none-word");
        switch (input.type()) {
            case NAME -> kit.setDisplayName(text);
            case LORE_ADD -> kit.getLore().add(text);
            case COOLDOWN -> {
                Long value = TimeFormat.parse(text);
                if (value == null) {
                    return false;
                }
                kit.setCooldown(value < 0L ? -1L : value);
            }
            case PERMISSION -> kit.setPermission(text.equalsIgnoreCase(none) ? "" : text);
            case SHULKER_NAME -> kit.setShulkerName(text.equalsIgnoreCase(none) ? "" : text);
            case ICON -> {
                Material material = ItemFactory.material(text, null);
                if (material == null) {
                    return false;
                }
                kit.setIcon(material);
            }
        }
        return true;
    }
}
