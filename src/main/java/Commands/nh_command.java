package Commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class nh_command implements CommandExecutor {
    private final JavaPlugin plugin;

    public nh_command(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            return showHelp(sender);
        }

        if (args[0].equalsIgnoreCase("helplugin")) {
            return showPluginHelp(sender);
        }

        sender.sendMessage(ChatColor.RED + "Используйте: /nh help или /nh helplugin");
        return true;
    }

    private boolean showHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== Помощь по серверу NingMine ===");
        sender.sendMessage(ChatColor.GREEN + "NingMine - сервер, на котором все возможно. Стройте, выживайте, воюйте. Подробности вы можете узнать в нашей группе в телеграмм! Данный плагин создан админестрацией помощи по серверу.");
        sender.sendMessage(ChatColor.YELLOW + "/nh help" + ChatColor.WHITE + " - эта справка");
        sender.sendMessage(ChatColor.YELLOW + "/nh helplugin" + ChatColor.WHITE + " - справка по плагину");
        sender.sendMessage(ChatColor.YELLOW + "/NingHelperReload" + ChatColor.WHITE + " - перезагрузить конфиг (OP)");
        return true;
    }

    private boolean showPluginHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== Помощь по плагину NingHelper ===");
        sender.sendMessage(ChatColor.YELLOW + "Версия: " + ChatColor.GREEN + plugin.getDescription().getVersion());
        sender.sendMessage(ChatColor.YELLOW + "Автор: " + ChatColor.AQUA + "il0veminecraft14");
        sender.sendMessage(ChatColor.YELLOW + "Функции:");
        sender.sendMessage(ChatColor.WHITE + "  • Вайтлист (белый список)");
        sender.sendMessage(ChatColor.WHITE + "  • Приветствие игроков");
        sender.sendMessage(ChatColor.WHITE + "  • Команда для координат");
        sender.sendMessage(ChatColor.WHITE + "  • Особые сообщения для админов");
        return true;
    }
}