package Commands;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Commands implements CommandExecutor {
    private final JavaPlugin plugin;

    // УДАЛИТЕ лишний конструктор, оставьте только этот:
    public Commands(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String cmdName = command.getName().toLowerCase();

        switch (cmdName) {
            case "гдея":
                return handleWhereCommand(sender);
            case "ninghelperreload":
                return handleReloadCommand(sender, args);
            default:
                return false;
        }
    }

    private boolean handleWhereCommand(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Эту команду может использовать только игрок!");
            return true;
        }

        Player p = (Player) sender;
        Location loc = p.getLocation();

        p.sendMessage("Вы находитесь в мире " +
                ChatColor.GREEN + p.getWorld().getName() +
                ChatColor.WHITE + " на координатах " +
                ChatColor.GREEN + "X:" + loc.getBlockX() +
                " Y:" + loc.getBlockY() +
                " Z:" + loc.getBlockZ());

        return true;
    }

    private boolean handleReloadCommand(CommandSender sender, String[] args) {
        // Проверка прав
        if (sender instanceof Player && !sender.hasPermission("whitelist.reload")) {
            sender.sendMessage(ChatColor.RED + "У вас нет прав на использование этой команды!");
            return true;
        }

        // Перезагрузка конфига
        plugin.reloadConfig();

        // Отправка сообщения
        sender.sendMessage(ChatColor.GREEN + "Конфигурация вайтлиста перезагружена!");
        plugin.getLogger().info("Конфигурация перезагружена по команде от " + sender.getName());

        return true;
    }
}