package com.il0veminecraft.untitled;

import Commands.Commands;
import Commands.nh_command;
import auth.AuthManager;
import auth.AuthListener;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

public class NingHelper extends JavaPlugin {
    private AuthManager authManager;
    private AuthListener authListener;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        setupDefaultConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();

        // Инициализируем систему авторизации
        try {
            authManager = new AuthManager(this);
            authListener = new AuthListener(this, authManager);

            getServer().getPluginManager().registerEvents(authListener, this);
            getLogger().info("Система авторизации загружена");

            // НЕТ AuthCommands - значит команды авторизации должны быть в другом классе
            // или их нужно создать отдельно

        } catch (Exception e) {
            getLogger().warning("Не удалось загрузить систему авторизации: " + e.getMessage());
            getLogger().warning("Плагин будет работать без системы авторизации");
        }

        // Регистрируем слушатель событий (старый Events.java)
        getServer().getPluginManager().registerEvents(new Events(this), this);

        // Регистрируем команды через общий класс команд
        Commands commandManager = new Commands(this);

        // Создаем nh_command с authManager если он есть
        nh_command nhCommand;
        if (authManager != null) {
            nhCommand = new nh_command(this, authManager);  // с authManager
        } else {
            nhCommand = new nh_command(this);  // без authManager
        }

        // Регистрируем команды
        getCommand("NingHelperReload").setExecutor(commandManager);
        getCommand("гдея").setExecutor(commandManager);
        getCommand("nh").setExecutor(nhCommand);
        getCommand("nh").setTabCompleter(nhCommand);

        // КОМАНДЫ АВТОРИЗАЦИИ НУЖНО СОЗДАТЬ ОТДЕЛЬНО!
        // Создайте класс для команд авторизации или добавьте их в существующий

        getLogger().info(ChatColor.GREEN + "|" + ChatColor.BLUE + "  |\\   |   |     |  " + ChatColor.GREEN + "|");
        getLogger().info(ChatColor.GREEN + "|" + ChatColor.BLUE + "  | \\  |   |_____|  " + ChatColor.GREEN + "|");
        getLogger().info(ChatColor.GREEN + "|" + ChatColor.BLUE + "  |  \\ |   |-----|  " + ChatColor.GREEN + "|");
        getLogger().info(ChatColor.GREEN + "|" + ChatColor.BLUE + "  |   \\|   |     |  " + ChatColor.GREEN + "|");
        getLogger().info("NingHelper -- плагин для помощи с сервером NingMine! Плагин запущен. v " + this.getDescription().getVersion());
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