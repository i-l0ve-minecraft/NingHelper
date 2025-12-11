package auth;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class AuthListener implements Listener {
    private final JavaPlugin plugin;
    private final AuthManager authManager;
    private final Set<UUID> loggedInPlayers = new HashSet<>();
    private final Set<UUID> loginTimeoutTasks = new HashSet<>();

    public AuthListener(JavaPlugin plugin, AuthManager authManager) {
        this.plugin = plugin;
        this.authManager = authManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Проверяем, нужно ли авторизовывать этого игрока
        if (!authManager.isAuthEnabled() || authManager.isExempt(player)) {
            plugin.getLogger().info(player.getName() + " освобожден от авторизации");
            loggedInPlayers.add(player.getUniqueId());
            return;
        }

        if (!authManager.isRegistered(player)) {
            // Новый игрок
            String welcomeMessage = authManager.getMessage("messages.general.welcome-unregistered",
                    "&6══════════════════════════════════\n&aДобро пожаловать!\n&eВы не зарегистрированы.\n&fДля регистрации: &6/r <пароль> <пароль>\n&6══════════════════════════════════");
            sendMultiLineMessage(player, welcomeMessage);

            // Блокируем движения
            if (authManager.getBoolean("block-actions.move", true)) {
                player.setWalkSpeed(0);
                player.setFlySpeed(0);
            }

        } else {
            // Зарегистрированный игрок
            String welcomeMessage = authManager.getMessage("messages.general.welcome-registered",
                    "&6══════════════════════════════════\n&aДобро пожаловать обратно!\n&eДля входа: &6/l <пароль>\n&6══════════════════════════════════");
            sendMultiLineMessage(player, welcomeMessage);

            if (authManager.getBoolean("block-actions.move", true)) {
                player.setWalkSpeed(0);
                player.setFlySpeed(0);
            }
        }

        // Запускаем таймер на авторизацию
        startLoginTimeout(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        loggedInPlayers.remove(uuid);
        loginTimeoutTasks.remove(uuid);
    }

    /**
     * Запускает таймер на авторизацию
     */
    private void startLoginTimeout(Player player) {
        if (authManager.isExempt(player)) {
            return;
        }

        int timeoutSeconds = authManager.getInt("timeouts.login-timeout", 60);

        if (timeoutSeconds > 0) {
            BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (!loggedInPlayers.contains(player.getUniqueId()) && player.isOnline()) {
                    String kickMessage = authManager.getMessage("timeouts.kick-message",
                                    "&cВы не успели авторизоваться за {time} секунд!")
                            .replace("{time}", String.valueOf(timeoutSeconds));

                    player.kickPlayer(kickMessage);
                    plugin.getLogger().info(player.getName() + " кикнут за превышение времени авторизации");
                }
                loginTimeoutTasks.remove(player.getUniqueId());
            }, timeoutSeconds * 20L); // Конвертируем секунды в тики

            loginTimeoutTasks.add(player.getUniqueId());
        }
    }

    /**
     * Отправляет многострочное сообщение
     */
    private void sendMultiLineMessage(Player player, String multiLineMessage) {
        for (String line : multiLineMessage.split("\n")) {
            player.sendMessage(line);
        }
    }

    // Блокировка действий согласно конфигу

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!isLoggedIn(player) && authManager.getBoolean("block-actions.move", true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (!isLoggedIn(player) && authManager.getBoolean("block-actions.chat", true)) {
            event.setCancelled(true);
            player.sendMessage(authManager.getMessage("messages.general.login-required", "&cСначала авторизуйтесь!"));
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!isLoggedIn(event.getPlayer()) && authManager.getBoolean("block-actions.build", true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!isLoggedIn(event.getPlayer()) && authManager.getBoolean("block-actions.build", true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!isLoggedIn(event.getPlayer()) && authManager.getBoolean("block-actions.interact", true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (event.getPlayer() instanceof Player) {
            Player player = (Player) event.getPlayer();
            if (!isLoggedIn(player) && authManager.getBoolean("block-actions.inventory", true)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            if (!isLoggedIn(player) && authManager.getBoolean("block-actions.damage", true)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (!isLoggedIn(event.getPlayer()) && authManager.getBoolean("block-actions.drop-items", true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        if (!isLoggedIn(event.getPlayer()) && authManager.getBoolean("block-actions.pickup-items", true)) {
            event.setCancelled(true);
        }
    }

    /**
     * Проверяет, авторизован ли игрок
     */
    public boolean isLoggedIn(Player player) {
        if (!authManager.isAuthEnabled() || authManager.isExempt(player)) {
            return true;
        }
        return loggedInPlayers.contains(player.getUniqueId());
    }

    /**
     * Авторизует игрока
     */
    public void loginPlayer(Player player) {
        loggedInPlayers.add(player.getUniqueId());
        loginTimeoutTasks.remove(player.getUniqueId());

        // Восстанавливаем нормальную скорость
        if (authManager.getBoolean("block-actions.move", true)) {
            player.setWalkSpeed(0.2f);
            player.setFlySpeed(0.1f);
        }

        player.sendMessage(authManager.getMessage("messages.login.success", "&aВход выполнен успешно!"));
    }

    /**
     * Разлогинивает игрока
     */
    public void logoutPlayer(Player player) {
        loggedInPlayers.remove(player.getUniqueId());
        player.kickPlayer(ChatColor.YELLOW + "Вы вышли из аккаунта.");
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();

        // Если игрок не авторизован
        if (!isLoggedIn(player)) {
            String command = event.getMessage().toLowerCase();

            // Разрешаем только команды авторизации
            boolean allowedCommand =
                    command.startsWith("/l ") ||
                            command.startsWith("/login ") ||
                            command.startsWith("/r ") ||
                            command.startsWith("/register ") ||
                            command.equals("/l") ||
                            command.equals("/login") ||
                            command.equals("/r") ||
                            command.equals("/register");

            if (!allowedCommand) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "══════════════════════════════════");
                player.sendMessage(ChatColor.RED + "Сначала авторизуйтесь!");
                player.sendMessage(ChatColor.YELLOW + "Для регистрации: /r <пароль> <пароль>");
                player.sendMessage(ChatColor.YELLOW + "Для входа: /l <пароль>");
                player.sendMessage(ChatColor.RED + "══════════════════════════════════");
            }
        }
    }
}