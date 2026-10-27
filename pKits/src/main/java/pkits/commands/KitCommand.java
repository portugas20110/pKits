package pkits.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import pkits.PKitsPlugin;
import pkits.guis.EditorMenu;
import pkits.guis.ItemsMenu;
import pkits.managers.KitManager;
import pkits.models.Kit;
import pkits.utils.Placeholders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class KitCommand implements TabExecutor {

    private static final String ADMIN = "pkits.admin";

    private final PKitsPlugin plugin;

    public KitCommand(PKitsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            plugin.messages().sendLines(sender, "help", Map.of());
            return true;
        }
        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "criar" -> create(sender, args);
            case "editar" -> edit(sender, args);
            case "deletar", "remover" -> delete(sender, args);
            case "reload" -> reload(sender);
            case "valor" -> value(sender, args);
            default -> claim(sender, args[0]);
        }
        return true;
    }

    private void create(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.messages().send(sender, "player-only");
            return;
        }
        if (!player.hasPermission(ADMIN)) {
            plugin.messages().send(player, "no-permission");
            return;
        }
        String id = args.length > 1 ? KitManager.sanitize(args[1]) : "";
        if (id.isEmpty()) {
            plugin.messages().send(player, "usage.create");
            return;
        }
        Map<String, String> placeholders = Placeholders.of("%kit-id%", id, "%kit-name%", id);
        if (plugin.kitManager().exists(id)) {
            plugin.messages().send(player, "editor.exists", placeholders);
            return;
        }
        plugin.messages().send(player, "editor.create-hint", placeholders);
        new ItemsMenu(plugin, new Kit(id), ItemsMenu.Mode.CREATE).open(player);
    }

    private void edit(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.messages().send(sender, "player-only");
            return;
        }
        if (!player.hasPermission(ADMIN)) {
            plugin.messages().send(player, "no-permission");
            return;
        }
        if (args.length < 2) {
            plugin.messages().send(player, "usage.edit");
            return;
        }
        Kit kit = plugin.kitManager().getKit(args[1]);
        if (kit == null) {
            plugin.messages().send(player, "kit-not-found", Placeholders.of("%kit-id%", args[1]));
            return;
        }
        new EditorMenu(plugin, kit).open(player);
    }

    private void delete(CommandSender sender, String[] args) {
        if (!sender.hasPermission(ADMIN)) {
            plugin.messages().send(sender, "no-permission");
            return;
        }
        if (args.length < 2) {
            plugin.messages().send(sender, "usage.delete");
            return;
        }
        Map<String, String> placeholders = Placeholders.of("%kit-id%", args[1], "%kit-name%", args[1]);
        if (!plugin.kitManager().delete(args[1])) {
            plugin.messages().send(sender, "kit-not-found", placeholders);
            return;
        }
        plugin.messages().send(sender, "editor.deleted", placeholders);
    }

    private void value(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.messages().send(sender, "player-only");
            return;
        }
        if (!player.hasPermission(ADMIN)) {
            plugin.messages().send(player, "no-permission");
            return;
        }
        if (!plugin.inputManager().has(player.getUniqueId())) {
            plugin.messages().send(player, "input.no-pending");
            return;
        }
        String text = String.join(" ", Arrays.copyOfRange(args, 1, args.length)).trim();
        if (text.isEmpty()) {
            plugin.messages().send(player, "usage.value");
            return;
        }
        plugin.inputManager().process(player, text);
    }

    private void reload(CommandSender sender) {
        if (!sender.hasPermission(ADMIN)) {
            plugin.messages().send(sender, "no-permission");
            return;
        }
        plugin.reloadAll();
        plugin.messages().send(sender, "reloaded");
    }

    private void claim(CommandSender sender, String id) {
        if (!(sender instanceof Player player)) {
            plugin.messages().send(sender, "player-only");
            return;
        }
        Kit kit = plugin.kitManager().getKit(id);
        if (kit == null) {
            plugin.messages().send(player, "kit-not-found", Placeholders.of("%kit-id%", id));
            return;
        }
        plugin.kitManager().claim(player, kit);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> options = new ArrayList<>();
        boolean admin = sender.hasPermission(ADMIN);
        if (args.length == 1) {
            options.addAll(plugin.kitManager().getKitIds());
            if (admin) {
                options.addAll(List.of("criar", "editar", "deletar", "reload"));
            }
        } else if (args.length == 2 && admin) {
            String sub = args[0].toLowerCase(Locale.ROOT);
            if (sub.equals("editar") || sub.equals("deletar") || sub.equals("remover")) {
                options.addAll(plugin.kitManager().getKitIds());
            }
        }
        String typed = args[args.length - 1].toLowerCase(Locale.ROOT);
        options.removeIf(option -> !option.toLowerCase(Locale.ROOT).startsWith(typed));
        return options;
    }
}
