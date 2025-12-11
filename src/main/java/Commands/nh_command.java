package Commands;

import auth.AuthManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class nh_command implements CommandExecutor, TabCompleter {
    private final JavaPlugin plugin;
    private AuthManager authManager;  // Может быть null если система авторизации не инициализирована

    // Конструктор без AuthManager (для совместимости)
    public nh_command(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    // Конструктор с AuthManager
    public nh_command(JavaPlugin plugin, AuthManager authManager) {
        this.plugin = plugin;
        this.authManager = authManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            return showHelp(sender);
        }

        if (args[0].equalsIgnoreCase("helplugin")) {
            return showPluginHelp(sender);
        }

        if (args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("перезагрузка")) {
            return handleReloadAllCommand(sender);
        }

        if (args[0].equalsIgnoreCase("version") || args[0].equalsIgnoreCase("версия")) {
            return handleVersionCommand(sender);
        }

        sender.sendMessage(ChatColor.RED + "Неизвестная команда! Используйте /nh help");
        return true;
    }

    /**
     * Перезагружает все конфигурации с детальным отчетом
     */
    private boolean handleReloadAllCommand(CommandSender sender) {
        // Проверка прав
        if (sender instanceof Player && !sender.hasPermission("ninghelper.reload")) {
            sender.sendMessage(ChatColor.RED + "У вас нет прав на использование этой команды!");
            return true;
        }

        sender.sendMessage(ChatColor.GOLD + "══════════════════════════════════");
        sender.sendMessage(ChatColor.YELLOW + "Начинаю перезагрузку конфигураций...");

        int successful = 0;
        int failed = 0;

        try {
            // 1. Основной конфиг
            try {
                long startTime = System.currentTimeMillis();
                plugin.reloadConfig();
                long endTime = System.currentTimeMillis();

                sender.sendMessage(ChatColor.GREEN + "✓ config.yml - перезагружен (" + (endTime - startTime) + "мс)");
                successful++;

            } catch (Exception e) {
                sender.sendMessage(ChatColor.RED + "✗ config.yml - ошибка: " + e.getMessage());
                failed++;
            }

            // 2. Конфиг авторизации
            try {
                if (authManager != null) {
                    long startTime = System.currentTimeMillis();
                    authManager.reloadConfig();
                    long endTime = System.currentTimeMillis();

                    sender.sendMessage(ChatColor.GREEN + "✓ auth.yml - перезагружен (" + (endTime - startTime) + "мс)");
                    successful++;
                } else {
                    sender.sendMessage(ChatColor.YELLOW + "⚠ auth.yml - система авторизации не активна");
                }

            } catch (Exception e) {
                sender.sendMessage(ChatColor.RED + "✗ auth.yml - ошибка: " + e.getMessage());
                failed++;
            }

            // 3. Можно добавить другие конфиги здесь

            // Итог
            sender.sendMessage(ChatColor.GOLD + "══════════════════════════════════");

            if (failed == 0) {
                sender.sendMessage(ChatColor.GREEN + "✓ Успешно перезагружено: " + successful + " конфигураций");
            } else {
                sender.sendMessage(ChatColor.YELLOW + "⚠ Перезагружено: " + successful + " из " + (successful + failed));
                sender.sendMessage(ChatColor.RED + "✗ Ошибок: " + failed);
            }

            sender.sendMessage(ChatColor.GOLD + "══════════════════════════════════");

            // Логируем в консоль
            plugin.getLogger().info("Перезагрузка конфигов завершена. Успешно: " + successful + ", Ошибок: " + failed);

            return true;

        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "✗ Критическая ошибка при перезагрузке!");
            sender.sendMessage(ChatColor.RED + "Причина: " + e.getMessage());

            plugin.getLogger().severe("Критическая ошибка перезагрузки: " + e.getMessage());
            return true;
        }
    }

    /**
     * Показывает версию плагина
     */
    private boolean handleVersionCommand(CommandSender sender) {
        String version = plugin.getDescription().getVersion();
        String name = plugin.getDescription().getName();
        String author = plugin.getDescription().getAuthors().toString();

        sender.sendMessage(ChatColor.GOLD + "=== Информация о плагине ===");
        sender.sendMessage(ChatColor.YELLOW + "Плагин: " + ChatColor.GREEN + name);
        sender.sendMessage(ChatColor.YELLOW + "Версия: " + ChatColor.AQUA + version);
        sender.sendMessage(ChatColor.YELLOW + "Автор: " + ChatColor.WHITE + author);
        sender.sendMessage(ChatColor.GOLD + "===========================");
        return true;
    }

    private boolean showHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== Помощь по NingHelper ===");
        sender.sendMessage(ChatColor.YELLOW + "/гдея" + ChatColor.WHITE + " - показать координаты");
        sender.sendMessage(ChatColor.YELLOW + "/nh help" + ChatColor.WHITE + " - эта справка");
        sender.sendMessage(ChatColor.YELLOW + "/nh helplugin" + ChatColor.WHITE + " - справка по плагину");
        sender.sendMessage(ChatColor.YELLOW + "/nh reload" + ChatColor.WHITE + " - перезагрузить ВСЕ конфиги");
        sender.sendMessage(ChatColor.YELLOW + "/nh version" + ChatColor.WHITE + " - версия плагина");

        // Команды авторизации
        sender.sendMessage(ChatColor.YELLOW + "/r <пароль> <пароль>" + ChatColor.WHITE + " - регистрация");
        sender.sendMessage(ChatColor.YELLOW + "/l <пароль>" + ChatColor.WHITE + " - вход");
        sender.sendMessage(ChatColor.YELLOW + "/register, /login" + ChatColor.WHITE + " - полные команды");

        sender.sendMessage(ChatColor.YELLOW + "/NingHelperReload" + ChatColor.WHITE + " - перезагрузить конфиг");
        sender.sendMessage(ChatColor.GOLD + "===========================");
        return true;
    }

    private boolean showPluginHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== Помощь по плагину NingHelper ===");
        sender.sendMessage(ChatColor.YELLOW + "Версия: " + ChatColor.GREEN + plugin.getDescription().getVersion());
        sender.sendMessage(ChatColor.YELLOW + "Автор: " + ChatColor.AQUA + "il0veminecraft14");
        sender.sendMessage(ChatColor.YELLOW + "Функции:");
        sender.sendMessage(ChatColor.WHITE + "  • Вайтлист (белый список)");
        sender.sendMessage(ChatColor.WHITE + "  • Приветствие игроков");
        sender.sendMessage(ChatColor.WHITE + "  • Система регистрации/авторизации");
        sender.sendMessage(ChatColor.WHITE + "  • Команда для координат");
        sender.sendMessage(ChatColor.WHITE + "  • Особые сообщения для админов");
        sender.sendMessage(ChatColor.WHITE + "  • Управление через /nh команды");
        sender.sendMessage(ChatColor.GOLD + "===========================");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();

        if (args.length == 1) {
            String input = args[0].toLowerCase();
            String[] commands = {"help", "helplugin", "reload", "version", "перезагрузка", "версия"};

            for (String cmd : commands) {
                if (cmd.startsWith(input)) {
                    suggestions.add(cmd);
                }
            }
        }

        return suggestions;
    }
}