package pkits;

import org.bukkit.plugin.java.JavaPlugin;
import pkits.commands.KitCommand;
import pkits.commands.KitsCommand;
import pkits.listeners.ChatInputListener;
import pkits.listeners.MenuListener;
import pkits.managers.CooldownManager;
import pkits.managers.InputManager;
import pkits.managers.KitManager;
import pkits.managers.MessageManager;
import pkits.utils.TimeFormat;

public final class PKitsPlugin extends JavaPlugin {

    private MessageManager messageManager;
    private CooldownManager cooldownManager;
    private KitManager kitManager;
    private InputManager inputManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        messageManager = new MessageManager(this);
        cooldownManager = new CooldownManager(this);
        kitManager = new KitManager(this);
        inputManager = new InputManager(this);

        KitCommand kitCommand = new KitCommand(this);
        getCommand("kits").setExecutor(new KitsCommand(this));
        getCommand("kit").setExecutor(kitCommand);
        getCommand("kit").setTabCompleter(kitCommand);

        getServer().getPluginManager().registerEvents(new MenuListener(), this);
        getServer().getPluginManager().registerEvents(new ChatInputListener(this), this);
    }

    @Override
    public void onDisable() {
        if (kitManager != null) {
            kitManager.save();
        }
        if (cooldownManager != null) {
            cooldownManager.saveNow();
        }
    }

    public void reloadAll() {
        reloadConfig();
        messageManager.reload();
        kitManager.load();
    }

    public String formatTime(long seconds) {
        return TimeFormat.format(
                seconds,
                getConfig().getString("time-format.days", "d"),
                getConfig().getString("time-format.hours", "h"),
                getConfig().getString("time-format.minutes", "m"),
                getConfig().getString("time-format.seconds", "s"));
    }

    public MessageManager messages() {
        return messageManager;
    }

    public CooldownManager cooldowns() {
        return cooldownManager;
    }

    public KitManager kitManager() {
        return kitManager;
    }

    public InputManager inputManager() {
        return inputManager;
    }
}
