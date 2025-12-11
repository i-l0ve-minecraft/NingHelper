package auth;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.UUID;

public class AuthManager {
    private final JavaPlugin plugin;
    private final File authDataFile;
    private final File authConfigFile;
    private YamlConfiguration config;

    public AuthManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.authDataFile = new File(plugin.getDataFolder(), "auth-data.yml");
        this.authConfigFile = new File(plugin.getDataFolder(), "auth.yml");

        loadConfig();
        setupDataFile();
    }

    /**
     * Загружает или создает конфигурацию
     */
    private void loadConfig() {
        if (!authConfigFile.exists()) {
            saveDefaultConfig();
        }

        config = YamlConfiguration.loadConfiguration(authConfigFile);
        plugin.getLogger().info("Конфиг авторизации загружен");
    }

    /**
     * Сохраняет конфиг по умолчанию
     */
    private void saveDefaultConfig() {
        try {
            plugin.getDataFolder().mkdirs();
            try (InputStream in = plugin.getResource("auth.yml")) {
                if (in != null) {
                    Files.copy(in, authConfigFile.toPath());
                } else {
                    // Создаем базовый конфиг если нет в ресурсах
                    YamlConfiguration defaultConfig = new YamlConfiguration();
                    defaultConfig.set("auth-enabled", true);
                    defaultConfig.set("password.min-length", 4);
                    defaultConfig.set("password.max-length", 20);
                    defaultConfig.save(authConfigFile);
                }
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Не удалось создать auth.yml: " + e.getMessage());
        }
    }

    /**
     * Настраивает файл данных
     */
    private void setupDataFile() {
        try {
            if (!authDataFile.exists()) {
                authDataFile.getParentFile().mkdirs();
                authDataFile.createNewFile();

                YamlConfiguration dataConfig = new YamlConfiguration();
                dataConfig.save(authDataFile);
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Не удалось создать auth-data.yml: " + e.getMessage());
        }
    }

    /**
     * Перезагружает конфигурацию
     */
    public void reloadConfig() {
        config = YamlConfiguration.loadConfiguration(authConfigFile);
        plugin.getLogger().info("Конфиг авторизации перезагружен");
    }

    /**
     * Получает значение из конфига
     */
    public String getString(String path, String def) {
        return config.getString(path, def);
    }

    public int getInt(String path, int def) {
        return config.getInt(path, def);
    }

    public boolean getBoolean(String path, boolean def) {
        return config.getBoolean(path, def);
    }

    public List<String> getStringList(String path) {
        return config.getStringList(path);
    }

    /**
     * Проверяет, включена ли система авторизации
     */
    public boolean isAuthEnabled() {
        return getBoolean("auth-enabled", true);
    }

    /**
     * Проверяет, освобожден ли игрок от авторизации
     */
    public boolean isExempt(Player player) {
        // Проверка по нику
        List<String> exemptPlayers = getStringList("exemptions.players");
        if (exemptPlayers.contains(player.getName().toLowerCase())) {
            return true;
        }

        // Проверка по правам
        List<String> exemptPermissions = getStringList("exemptions.permissions");
        for (String perm : exemptPermissions) {
            if (player.hasPermission(perm)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Проверяет валидность пароля по правилам конфига
     */
    public String validatePassword(String password) {
        int minLength = getInt("password.min-length", 4);
        int maxLength = getInt("password.max-length", 20);

        if (password.length() < minLength) {
            return getString("messages.register.password-too-short", "Пароль слишком короткий!")
                    .replace("{min}", String.valueOf(minLength));
        }

        if (password.length() > maxLength) {
            return getString("messages.register.password-too-long", "Пароль слишком длинный!")
                    .replace("{max}", String.valueOf(maxLength));
        }

        if (getBoolean("password.require-letters", true)) {
            if (!password.matches(".*[a-zA-Z].*")) {
                return getString("messages.register.password-no-letters", "Пароль должен содержать буквы!");
            }
        }

        if (getBoolean("password.require-numbers", false)) {
            if (!password.matches(".*\\d.*")) {
                return getString("messages.register.password-no-numbers", "Пароль должен содержать цифры!");
            }
        }

        if (getBoolean("password.require-special-chars", false)) {
            if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
                return getString("messages.register.password-no-special", "Пароль должен содержать специальные символы!");
            }
        }

        return null; // Пароль валиден
    }

    /**
     * Получает сообщение из конфига с заменой цветов
     */
    public String getMessage(String path, String defaultValue) {
        String message = config.getString(path, defaultValue);
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    /**
     * Регистрирует игрока
     */
    public boolean registerPlayer(Player player, String password) {
        try {
            YamlConfiguration data = YamlConfiguration.loadConfiguration(authDataFile);
            String uuid = player.getUniqueId().toString();

            if (data.contains("players." + uuid)) {
                return false;
            }

            String hashedPassword = hashPassword(password);

            data.set("players." + uuid + ".username", player.getName());
            data.set("players." + uuid + ".password", hashedPassword);
            data.set("players." + uuid + ".registered", System.currentTimeMillis());
            data.set("players." + uuid + ".lastLogin", System.currentTimeMillis());
            data.set("players." + uuid + ".lastIp", player.getAddress().getAddress().getHostAddress());

            data.save(authDataFile);
            return true;

        } catch (IOException e) {
            plugin.getLogger().severe("Ошибка регистрации: " + e.getMessage());
            return false;
        }
    }

    /**
     * Авторизует игрока
     */
    public boolean loginPlayer(Player player, String password) {
        YamlConfiguration data = YamlConfiguration.loadConfiguration(authDataFile);
        String uuid = player.getUniqueId().toString();

        if (!data.contains("players." + uuid)) {
            return false;
        }

        String storedHash = data.getString("players." + uuid + ".password");
        String inputHash = hashPassword(password);

        if (storedHash.equals(inputHash)) {
            data.set("players." + uuid + ".lastLogin", System.currentTimeMillis());
            data.set("players." + uuid + ".lastIp", player.getAddress().getAddress().getHostAddress());
            try {
                data.save(authDataFile);
            } catch (IOException e) {
                plugin.getLogger().warning("Не удалось обновить данные: " + e.getMessage());
            }
            return true;
        }

        return false;
    }

    /**
     * Проверяет, зарегистрирован ли игрок
     */
    public boolean isRegistered(Player player) {
        YamlConfiguration data = YamlConfiguration.loadConfiguration(authDataFile);
        String uuid = player.getUniqueId().toString();
        return data.contains("players." + uuid);
    }

    /**
     * Хэширование пароля
     */
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(password.getBytes());
            byte[] bytes = md.digest();

            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();

        } catch (NoSuchAlgorithmException e) {
            plugin.getLogger().severe("Ошибка хэширования: " + e.getMessage());
            return password;
        }
    }
}