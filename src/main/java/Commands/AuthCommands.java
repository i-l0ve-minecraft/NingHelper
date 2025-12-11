package Commands;

import auth.AuthManager;
import auth.AuthListener;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class AuthCommands implements CommandExecutor, TabCompleter {
    private final JavaPlugin plugin;
    private final AuthManager authManager;
    private final AuthListener authListener;

    public AuthCommands(JavaPlugin plugin, AuthManager authManager, AuthListener authListener) {
        this.plugin = plugin;
        this.authManager = authManager;
        this.authListener = authListener;
        plugin.getLogger().info("AuthCommands создан!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        plugin.getLogger().info("Вызвана команда: " + command.getName() + " с аргументами: " + String.join(", ", args));

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Только для игроков!");
            return true;
        }

        Player player = (Player) sender;
        String cmd = command.getName().toLowerCase();

        if (cmd.equals("r") || cmd.equals("register") || cmd.equals("reg")) {
            plugin.getLogger().info("Обработка регистрации для " + player.getName());
            return handleRegister(player, args);
        } else if (cmd.equals("l") || cmd.equals("login") || cmd.equals("log")) {
            plugin.getLogger().info("Обработка входа для " + player.getName());
            return handleLogin(player, args);
        } else if (cmd.equals("logout")) {
            return handleLogout(player);
        } else if (cmd.equals("authinfo")) {
            return handleAuthInfo(player);
        }

        return false;
    }

    private boolean handleRegister(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Использование: /r <пароль> <повторите пароль>");
            return true;
        }

        String password = args[0];
        String confirm = args[1];

        if (!password.equals(confirm)) {
            player.sendMessage(ChatColor.RED + "Пароли не совпадают!");
            return true;
        }

        if (authManager.registerPlayer(player, password)) {
            player.sendMessage(ChatColor.GREEN + "Регистрация успешна!");
            authListener.loginPlayer(player);
            return true;
        } else {
            player.sendMessage(ChatColor.RED + "Ошибка регистрации!");
            return true;
        }
    }

    private boolean handleLogin(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Использование: /l <пароль>");
            return true;
        }

        String password = args[0];

        if (authManager.loginPlayer(player, password)) {
            authListener.loginPlayer(player);
            return true;
        } else {
            player.sendMessage(ChatColor.RED + "Неверный пароль!");
            return true;
        }
    }

    private boolean handleLogout(Player player) {
        authListener.logoutPlayer(player);
        return true;
    }

    private boolean handleAuthInfo(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== Информация об аккаунте ===");
        player.sendMessage(ChatColor.YELLOW + "Игрок: " + ChatColor.WHITE + player.getName());
        player.sendMessage(ChatColor.YELLOW + "Статус: " +
                (authListener.isLoggedIn(player) ? ChatColor.GREEN + "Авторизован" : ChatColor.RED + "Не авторизован"));
        player.sendMessage(ChatColor.GOLD + "===========================");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();
        String cmd = command.getName().toLowerCase();

        if (cmd.equals("r") || cmd.equals("register") || cmd.equals("reg")) {
            if (args.length == 1) {
                suggestions.add("<пароль>");
            } else if (args.length == 2) {
                suggestions.add("<повторите пароль>");
            }
        } else if (cmd.equals("l") || cmd.equals("login") || cmd.equals("log")) {
            if (args.length == 1) {
                suggestions.add("<пароль>");
            }
        }

        return suggestions;
    }
}