package com.il0veminecraft.untitled;

import Commands.AuthCommands;
import Commands.Commands;
import Commands.nh_command;
import auth.AuthManager;
import auth.AuthListener;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

public class NingHelper extends JavaPlugin {
    private AuthManager authManager;
    private AuthListener authListener;
    private AuthCommands authCommands;

    // ANSI коды для цветов в консоли
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_WHITE = "\u001B[37m";
    private static final String ANSI_BOLD = "\u001B[1m";

    @Override
    public void onEnable() {
        // Зеленое сообщение о начале запуска с ANSI кодами
        logColor(ANSI_GREEN, "╔══════════════════════════════════╗");
        logColor(ANSI_GREEN, "║     ЗАПУСК NINGHELPER v" + getDescription().getVersion() + "     ║");
        logColor(ANSI_GREEN, "╚══════════════════════════════════╝");

        saveDefaultConfig();
        setupDefaultConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();

        // Зеленое сообщение о загрузке конфигурации
        logSuccess("Конфигурация загружена и сохранена");

        // Инициализируем систему авторизации
        try {
            logInfo("Инициализация системы авторизации...");
            authManager = new AuthManager(this);
            authListener = new AuthListener(this, authManager);

            // Создаем обработчик команд авторизации
            logInfo("Создание обработчика команд авторизации...");
            authCommands = new AuthCommands(this, authManager, authListener);

            getServer().getPluginManager().registerEvents(authListener, this);
            logSuccess("Слушатель событий AuthListener зарегистрирован");

            // Регистрируем команды авторизации
            logInfo("Регистрация команд авторизации...");

            PluginCommand regCommand = getCommand("reg");
            if (regCommand != null) {
                regCommand.setExecutor(authCommands);
                regCommand.setTabCompleter(authCommands);
                logSuccess("  Команда 'reg' зарегистрирована (алиасы: /r, /register)");
            } else {
                logError("  Команда 'reg' не найдена в plugin.yml!");
            }

            PluginCommand logCommand = getCommand("log");
            if (logCommand != null) {
                logCommand.setExecutor(authCommands);
                logCommand.setTabCompleter(authCommands);
                logSuccess("  Команда 'log' зарегистрирована (алиасы: /l, /login)");
            } else {
                logError("  Команда 'log' не найдена в plugin.yml!");
            }

            // Зеленое сообщение об успешной загрузке системы авторизации
            logSuccess("Система авторизации загружена успешно!");

        } catch (Exception e) {
            logError("Не удалось загрузить систему авторизации: " + e.getMessage());
            logWarning("Плагин будет работать без системы авторизации");
        }

        // Регистрируем слушатель событий (старый Events.java)
        logInfo("Регистрация слушателя событий Events...");
        try {
            // Если у вас нет класса Events или он называется иначе, удалите или закомментируйте эту строку:
            getServer().getPluginManager().registerEvents(new Events(this), this);
            logSuccess("Слушатель событий Events зарегистрирован");
        } catch (Exception e) {
            logError("Ошибка при регистрации Events: " + e.getMessage());
            logWarning("Events не зарегистрирован, продолжаем запуск...");
        }

        // Регистрируем команды через общий класс команд
        logInfo("Регистрация обычных команд...");
        Commands commandManager = new Commands(this);

        // Создаем nh_command с authManager если он есть
        nh_command nhCommand;
        if (authManager != null) {
            nhCommand = new nh_command(this, authManager);
            logSuccess("  nh_command создан с поддержкой авторизации");
        } else {
            nhCommand = new nh_command(this);
            logWarning("  nh_command создан без поддержки авторизации");
        }

        // Регистрируем команды
        try {
            getCommand("NingHelperReload").setExecutor(commandManager);
            getCommand("гдея").setExecutor(commandManager);
            getCommand("nh").setExecutor(nhCommand);
            getCommand("nh").setTabCompleter(nhCommand);

            logSuccess("Все команды зарегистрированы:");
            logSuccess("  - /NingHelperReload");
            logSuccess("  - /гдея");
            logSuccess("  - /nh (алиасы: /ninghelper)");
            if (authManager != null) {
                logSuccess("  - /reg (регистрация)");
                logSuccess("  - /log (вход)");
            }
        } catch (Exception e) {
            logError("Ошибка при регистрации команд: " + e.getMessage());
        }

        // ЗЕЛЕНОЕ сообщение об успешном запуске
        logColor(ANSI_GREEN, "╔══════════════════════════════════════════════╗");
        logColor(ANSI_GREEN, "║          " + ANSI_YELLOW + "NINGHELPER ЗАПУЩЕН УСПЕШНО!" + ANSI_GREEN + "         ║");
        logColor(ANSI_GREEN, "╠══════════════════════════════════════════════╣");
        logColor(ANSI_GREEN, "║ " + ANSI_CYAN + "Версия: " + ANSI_WHITE + this.getDescription().getVersion() +
                ANSI_GREEN + "                              ║");
        logColor(ANSI_GREEN, "║ " + ANSI_CYAN + "Авторизация: " +
                (authManager != null ? ANSI_GREEN + "ВКЛЮЧЕНА" : ANSI_RED + "ОТКЛЮЧЕНА") +
                ANSI_GREEN + "                      ║");
        logColor(ANSI_GREEN, "╚══════════════════════════════════════════════╝");



        logSuccess("Плагин для помощи с сервером NingMine запущен!");
        logColor(ANSI_GREEN + ANSI_BOLD, "✅ Готов к работе!");
    }

    @Override
    public void onDisable() {
        // Цветное сообщение о выключении
        logColor(ANSI_RED, "╔══════════════════════════════════╗");
        logColor(ANSI_RED, "║     NINGHELPER ВЫКЛЮЧЕН         ║");
        logColor(ANSI_RED, "╚══════════════════════════════════╝");
    }

    // Методы для цветного вывода с ANSI кодами
    private void logColor(String color, String message) {
        getLogger().info(color + message + ANSI_RESET);
    }

    private void logSuccess(String message) {
        getLogger().info(ANSI_GREEN + "✓ " + message + ANSI_RESET);
    }

    private void logError(String message) {
        getLogger().info(ANSI_RED + "✗ " + message + ANSI_RESET);
    }

    private void logWarning(String message) {
        getLogger().info(ANSI_YELLOW + "⚠ " + message + ANSI_RESET);
    }

    private void logInfo(String message) {
        getLogger().info(ANSI_CYAN + "→ " + message + ANSI_RESET);
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