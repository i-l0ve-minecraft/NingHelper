package com.il0veminecraft.untitled;

import Commands.AuthCommands;
import Commands.Commands;
import Commands.nh_command;
import auth.AuthManager;
import auth.AuthListener;
import org.bukkit.ChatColor;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

public class NingHelper extends JavaPlugin {
    private AuthManager authManager;
    private AuthListener authListener;
    private AuthCommands authCommands;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        setupDefaultConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();

        getLogger().info("§6=== §aЗАПУСК NINGHELPER §6===");

        // Инициализируем систему авторизации
        try {
            getLogger().info("Инициализация системы авторизации...");
            authManager = new AuthManager(this);
            authListener = new AuthListener(this, authManager);

            // Создаем обработчик команд авторизации
            getLogger().info("Создание обработчика команд авторизации...");
            authCommands = new AuthCommands(this, authManager, authListener);

            getServer().getPluginManager().registerEvents(authListener, this);
            getLogger().info("§aСлушатель событий зарегистрирован");

            // Регистрируем команды авторизации - ДОБАВЬТЕ ЭТО!
            getLogger().info("Регистрация команд авторизации...");

            PluginCommand regCommand = getCommand("reg");
            if (regCommand != null) {
                regCommand.setExecutor(authCommands);
                regCommand.setTabCompleter(authCommands);
                getLogger().info("§aКоманда 'reg' зарегистрирована");
            } else {
                getLogger().warning("§4Команда 'reg' не найдена в plugin.yml!");
            }

            PluginCommand logCommand = getCommand("log");
            if (logCommand != null) {
                logCommand.setExecutor(authCommands);
                logCommand.setTabCompleter(authCommands);
                getLogger().info("§aКоманда 'log' зарегистрирована");
            } else {
                getLogger().warning("§4Команда 'log' не найдена в plugin.yml!");
            }

        } catch (Exception e) {
            getLogger().warning("§4Не удалось загрузить систему авторизации: " + e.getMessage());
            e.printStackTrace();
        }

        // Регистрируем слушатель событий (старый Events.java)
        getLogger().info("Регистрация слушателя событий Events...");
        getServer().getPluginManager().registerEvents(new Events(this), this);

        // Регистрируем команды через общий класс команд
        getLogger().info("Регистрация обычных команд...");
        Commands commandManager = new Commands(this);

        // Создаем nh_command с authManager если он есть
        nh_command nhCommand;
        if (authManager != null) {
            nhCommand = new nh_command(this, authManager);
        } else {
            nhCommand = new nh_command(this);
        }

        // Регистрируем команды
        getCommand("NingHelperReload").setExecutor(commandManager);
        getCommand("гдея").setExecutor(commandManager);
        getCommand("nh").setExecutor(nhCommand);
        getCommand("nh").setTabCompleter(nhCommand);

        getLogger().info("=== NINGHELPER ЗАПУЩЕН УСПЕШНО ===");
        getLogger().info("§a  |\\   |   |      |");
        getLogger().info("§a  | \\  |   |      |");
        getLogger().info("§a  |  \\ |   |______|");
        getLogger().info("§a  |   \\|   |      |");
        getLogger().info("§a  |    \\|   |      |");
        getLogger().info("§6NingHelper v" + this.getDescription().getVersion() + "§6 запущен!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Плагин NingHelper - выключен.");
    }

    private void setupDefaultConfig() {
        getConfig().addDefault("whitelist-enabled", true);
        getConfig().addDefault("kick-message", "&cВы не в белом списке, обратитесь к администраторам!");
        getConfig().addDefault("join-message", "&e{player} &aзалетел к нам!");
        getConfig().addDefault("admin-join-message", "&cАдминистратор сервера &6{player} &cзашел на сервер!");

        getConfig().addDefault("allowed-players", Arrays.asList(
                "il0veminecraft14",
                "xd_kar",
                "lagahka",
                "clayworld200",
                "frutikkek",
                "sulayd27",
                "unhappyparrot012",
                "anonsn",
                "marinaksolotl"
        ));

        getConfig().addDefault("admins", Arrays.asList("il0veminecraft14"));
    }

    public AuthManager getAuthManager() {
        return authManager;
    }
}