package pkits.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pkits.PKitsPlugin;
import pkits.guis.MainMenu;

public class KitsCommand implements CommandExecutor {

    private final PKitsPlugin plugin;

    public KitsCommand(PKitsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.messages().send(sender, "player-only");
            return true;
        }
        new MainMenu(plugin, player, 0).open(player);
        return true;
    }
}
