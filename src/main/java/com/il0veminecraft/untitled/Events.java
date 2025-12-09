package com.il0veminecraft.untitled;

import jdk.vm.ci.meta.Local;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

public class Events implements Listener {
    private final JavaPlugin plugin;

    public Events(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    YamlConfiguration pldata = new YamlConfiguration();
    @EventHandler // ВАЙТЛИСТ ПО ИВЕНТУ НА ВХОД НА СЕРВЕР
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();
        handleNewPlayer(player);

        // Получаем настройки из конфига
        boolean whitelistEnabled = plugin.getConfig().getBoolean("whitelist-enabled", true);
        List<String> allowedPlayers = plugin.getConfig().getStringList("allowed-players");

        // Если вайтлист включен И игрока нет в списке - кикаем
        if (whitelistEnabled && !allowedPlayers.contains(playerName.toLowerCase())) {
            String kickMessage = plugin.getConfig().getString("kick-message",
                    "&cВы не в списке разрешенных игроков!");

            player.kickPlayer(ChatColor.translateAlternateColorCodes('&', kickMessage));

            // Логируем в консоль
            plugin.getLogger().info("Игрок " + playerName + " был кикнут (нет в вайтлисте)");
            return; // Прерываем выполнение метода
        }

        // Если игрок прошел проверку вайтлиста - приветствуем

        // Общее сообщение для всех игроков
        String joinMessage = plugin.getConfig().getString("join-message",
                "&e{player} &aзалетел к нам!");
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&',
                joinMessage.replace("{player}", playerName)));

        // СООБЩЕНИЕ ДЛЯ АДМИНОВ
        List<String> admins = plugin.getConfig().getStringList("admins");
        if (admins.contains(playerName.toLowerCase())) {
            // Отправляем специальное сообщение для администратора
            String adminMessage = plugin.getConfig().getString("admin-join-message",
                    "&cАдминистратор сервера &6{player} &cзашел на сервер!");

            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&',
                    adminMessage.replace("{player}", playerName)));

            // Меняем стандартное сообщение о входе
            event.setJoinMessage(
                    ChatColor.YELLOW + "Администратор присоединился к игре"
            );
        }
    }

    private void handleNewPlayer(Player player) {
        try {
            File file = new File("plugins/NingHelper/pldata.yml");
            file.getParentFile().mkdirs();

            YamlConfiguration config = file.exists() ?
                    YamlConfiguration.loadConfiguration(file) :
                    new YamlConfiguration();

            String playerKey = player.getName().toLowerCase();

            // ПРОВЕРКА: новый ли игрок?
            boolean isNewPlayer = !config.contains(playerKey);

            if (isNewPlayer) {
                // ===== СОЗДАЕМ ДАННЫЕ ДЛЯ НОВОГО ИГРОКА =====

                // 1. Основная информация
                config.set(playerKey + ".ник", player.getName());
                config.set(playerKey + ".uuid", player.getUniqueId().toString());

                // 2. Имя и фамилия (можно потом изменить)
                // По умолчанию используем ник как имя
//                config.set(playerKey + ".личные.данные.имя", player.getName());
//                config.set(playerKey + ".личные.данные.фамилия", "Игрок");

                // 3. Даты
                String now = java.time.LocalDateTime.now().toString();
                config.set(playerKey + ".даты.регистрация", now);
                config.set(playerKey + ".даты.первый-вход", now);
                config.set(playerKey + ".даты.последний-вход", now);

                // 4. Статистика
                config.set(playerKey + ".статистика.входов", 1);
                config.set(playerKey + ".статистика.статус", "новичок");



                // 5. Дополнительно
                // Получаем IP напрямую
                if (player.getAddress() != null) {
                    config.set(playerKey + ".ip", player.getAddress().getAddress().getHostAddress());
                } else {
                    config.set(playerKey + ".ip", "неизвестно");
                }

                // Сообщаем игроку
                player.sendMessage("§6══════════════════════════════════");
                player.sendMessage("§eДобро пожаловать на сервер!");
                player.sendMessage("§e           NingMine");
                player.sendMessage("§eСтройте, выживайте, воюйте!");
                player.sendMessage("§eПолетический сервер NingMine");
                player.sendMessage("§6══════════════════════════════════");

                System.out.println("НОВЫЙ ИГРОК: " + player.getName());

            } else {
                // ===== ОБНОВЛЯЕМ СУЩЕСТВУЮЩЕГО ИГРОКА =====

                // 1. Обновляем дату последнего входа
                config.set(playerKey + ".даты.последний.вход",
                        java.time.LocalDateTime.now().toString());

                // 2. Увеличиваем счетчик входов
                int logins = config.getInt(playerKey + ".статистика.входов", 0);
                config.set(playerKey + ".статистика.входов", logins + 1);

                System.out.println("СТАРЫЙ ИГРОК: " + player.getName() +
                        " (входов: " + (logins + 1) + ")");
            }

            // Сохраняем файл
            config.save(file);

        } catch (IOException e) {
            player.sendMessage("§cОшибка сохранения данных!");
            e.printStackTrace();
        }
    }

//    public void lastJoin(PlayerJoinEvent e) {
//    Player player = e.getPlayer();
//    String uuid = player.getUniqueId().toString();
//    String path = String.join(".", "users", uuid, "last-join");
//    LocalDateTime nowtime = LocalDateTime.now();
//    plugin.getConfig().set(path, nowtime.toString());
//    plugin.saveConfig();
//    }


}
