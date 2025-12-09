package com.il0veminecraft.untitled;

import Commands.Commands;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Arrays;
import Commands.nh_command;

public class NingHelper extends JavaPlugin {

    @Override
    public void onEnable() {
        // Сохраняем дефолтный конфиг
        saveDefaultConfig();


        // Устанавливаем значения по умолчанию
        setupDefaultConfig();

        getConfig().options().copyDefaults(true);
        saveConfig();

        // Регистрируем слушатель событий
        getServer().getPluginManager().registerEvents(new Events(this), this);

        // Регистрируем команды через общий класс команд
        Commands commandManager = new Commands(this);

        // Регистрируем команду перезагрузки
        getCommand("NingHelperReload").setExecutor(commandManager);
        getCommand("гдея").setExecutor(commandManager);
        getCommand("nh").setExecutor(new nh_command(this));

        // Здесь можно добавить другие команды




        getLogger().info(ChatColor.GREEN + "|" + ChatColor.BLUE + "  |\\   |   |     |  " + ChatColor.GREEN + "|");
        getLogger().info(ChatColor.GREEN + "|" + ChatColor.BLUE + "  | \\  |   |_____|  " + ChatColor.GREEN + "|");
        getLogger().info(ChatColor.GREEN + "|" + ChatColor.BLUE + "  |  \\ |   |-----|  " + ChatColor.GREEN + "|");
        getLogger().info(ChatColor.GREEN + "|" + ChatColor.BLUE + "  |   \\|   |     |  " + ChatColor.GREEN + "|");
        getLogger().info("NingHelper -- плагин для помощи с сервером NingMine! Плагин запущен. v а"+ this.getDescription().getVersion());

    }

    @Override
    public void onDisable() {
        getLogger().info("Плагин NingHelper - выключен.");
    }

    private void setupDefaultConfig() {
        getConfig().addDefault("whitelist-enabled", true);
        getConfig().addDefault("kick-message", "&cВы не в белом списке, обратитесь к аминестраторам!");
        getConfig().addDefault("join-message", "&e{player} &aзалетел к нам!");
        getConfig().addDefault("admin-join-message", "&cАдминистратор сервера &6{player} &cзашел на сервер!");

        // Список разрешенных игроков по умолчанию
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

        // Список администраторов по умолчанию
        getConfig().addDefault("admins", Arrays.asList(
                "il0veminecraft14"
        ));
    }
}