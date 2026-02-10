package com.easylogin.auth;

import com.easylogin.EasyLoginConstants;
import com.easylogin.config.EasyLoginConfig;
import com.easylogin.data.PlayerAccount;
import com.easylogin.data.PlayerDataStore;
import com.easylogin.platform.PlatformHelper;
import com.easylogin.util.MessageFormatter;
import com.easylogin.util.PlayerUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central authentication manager. Tracks login state, handles timeouts,
 * sends reminders, and orchestrates the full login/register flow.
 */
public class AuthManager {

    public static AuthManager INSTANCE;

    private final EasyLoginConfig config;
    private final PlayerDataStore dataStore;
    private final RateLimiter rateLimiter;
    private final MessageFormatter formatter;

    // Players that have authenticated this session
    private final Set<UUID> authenticated = ConcurrentHashMap.newKeySet();

    // Timestamp when each unauthenticated player joined (for timeout)
    private final Map<UUID, Long> joinTimestamps = new ConcurrentHashMap<>();

    // Last reminder timestamp per player (to avoid spam)
    private final Map<UUID, Long> lastReminder = new ConcurrentHashMap<>();

    // Saved positions before limbo teleport (UUID → {x, y, z, yaw, pitch,
    // dimension})
    private final Map<UUID, double[]> savedPositions = new ConcurrentHashMap<>();
    private final Map<UUID, String> savedDimensions = new ConcurrentHashMap<>();

    // Logout cooldown tracking
    private final Map<UUID, Long> logoutCooldowns = new ConcurrentHashMap<>();

    // Invincibility after login tracking (UUID → Expiry Timestamp)
    private final Map<UUID, Long> invincibilityExpiry = new ConcurrentHashMap<>();

    public AuthManager(EasyLoginConfig config, PlayerDataStore dataStore) {
        this.config = config;
        this.dataStore = dataStore;
        this.rateLimiter = new RateLimiter(config.maxAttempts, config.attemptWindow, config.lockoutDuration);
        this.formatter = new MessageFormatter(config);
        INSTANCE = this;
    }

    // ─── State queries ──────────────────────────────────────

    public boolean isAuthenticated(ServerPlayer player) {
        return authenticated.contains(player.getUUID());
    }

    public boolean isInvincible(ServerPlayer player) {
        Long expiry = invincibilityExpiry.get(player.getUUID());
        if (expiry == null)
            return false;
        if (System.currentTimeMillis() >= expiry) {
            invincibilityExpiry.remove(player.getUUID());
            return false;
        }
        return true;
    }

    public boolean shouldBlock(ServerPlayer player) {
        if (PlatformHelper.INSTANCE.isFakePlayer(player))
            return false;
        return !authenticated.contains(player.getUUID());
    }

    public boolean isRegistered(UUID uuid) {
        return dataStore.isRegistered(uuid);
    }

    // ─── Player join/leave ──────────────────────────────────

    public void onPlayerJoin(ServerPlayer player) {
        if (PlatformHelper.INSTANCE.isFakePlayer(player))
            return;

        UUID uuid = player.getUUID();
        joinTimestamps.put(uuid, System.currentTimeMillis());
        lastReminder.put(uuid, 0L);

        // Save position before limbo
        savePlayerPosition(player);

        // Teleport to limbo
        if (config.limboEnabled) {
            PlayerUtil.teleportToLimbo(player, config);
        }

        // Send welcome message
        boolean registered = dataStore.isRegistered(uuid);
        String msg = registered ? config.msgWelcome : config.msgWelcomeFirstTime;
        player.sendSystemMessage(formatter.format(msg, player));

        // Send title
        String titleText = registered ? "&eUse /login" : "&eUse /register";
        String subtitleText = registered ? "&7Enter your password" : "&7Create your password";
        PlayerUtil.sendTitle(player, formatter.colorize(titleText), formatter.colorize(subtitleText));

        EasyLoginConstants.LOGGER.info("Player {} joined, awaiting authentication (registered: {})",
                player.getName().getString(), registered);
    }

    public void onPlayerLeave(ServerPlayer player) {
        if (PlatformHelper.INSTANCE.isFakePlayer(player))
            return;

        UUID uuid = player.getUUID();
        authenticated.remove(uuid);
        joinTimestamps.remove(uuid);
        lastReminder.remove(uuid);
        savedPositions.remove(uuid);
        savedDimensions.remove(uuid);
        logoutCooldowns.remove(uuid);
        invincibilityExpiry.remove(uuid);
    }

    // ─── Login ──────────────────────────────────────────────

    public enum LoginResult {
        SUCCESS, WRONG_PASSWORD, NOT_REGISTERED, ALREADY_LOGGED_IN, RATE_LIMITED
    }

    public LoginResult attemptLogin(ServerPlayer player, String password) {
        UUID uuid = player.getUUID();

        if (authenticated.contains(uuid)) {
            player.sendSystemMessage(formatter.format(config.msgAlreadyLoggedIn, player));
            return LoginResult.ALREADY_LOGGED_IN;
        }

        if (!dataStore.isRegistered(uuid)) {
            player.sendSystemMessage(formatter.format(config.msgNotRegistered, player));
            return LoginResult.NOT_REGISTERED;
        }

        String ip = getPlayerIp(player);
        String rateKey = "ip:" + ip;
        String uuidKey = "uuid:" + uuid;

        // Check rate limit
        long lockedIp = rateLimiter.isLocked(rateKey);
        long lockedUuid = rateLimiter.isLocked(uuidKey);
        long locked = Math.max(lockedIp, lockedUuid);
        if (locked > 0) {
            long minutes = (locked / 60000) + 1;
            player.sendSystemMessage(formatter.format(config.msgRateLimited, player,
                    Map.of("minutes", String.valueOf(minutes))));
            return LoginResult.RATE_LIMITED;
        }

        PlayerAccount account = dataStore.getAccount(uuid).orElse(null);
        if (account == null) {
            return LoginResult.NOT_REGISTERED;
        }

        if (PasswordService.verify(password, account.getPasswordHash())) {
            // Success
            authenticatePlayer(player);
            account.recordLogin(ip);
            dataStore.updateAccount(account);
            rateLimiter.clear(rateKey);
            rateLimiter.clear(uuidKey);

            player.sendSystemMessage(formatter.format(config.msgLoginSuccess, player));
            PlayerUtil.playSound(player, config.soundLoginSuccess);

            EasyLoginConstants.LOGGER.info("Player {} logged in successfully from {}",
                    player.getName().getString(), ip);
            return LoginResult.SUCCESS;
        } else {
            // Failed
            int remainingIp = rateLimiter.recordFailure(rateKey);
            int remainingUuid = rateLimiter.recordFailure(uuidKey);
            int remaining = Math.min(
                    remainingIp == -1 ? 0 : remainingIp,
                    remainingUuid == -1 ? 0 : remainingUuid);

            if (remaining <= 0) {
                long lockoutMinutes = config.lockoutDuration;
                player.sendSystemMessage(formatter.format(config.msgRateLimited, player,
                        Map.of("minutes", String.valueOf(lockoutMinutes))));
                EasyLoginConstants.LOGGER.warn("Player {} locked out (too many attempts) from {}",
                        player.getName().getString(), ip);
            } else {
                player.sendSystemMessage(formatter.format(config.msgWrongPassword, player,
                        Map.of("attempts", String.valueOf(remaining))));
            }

            PlayerUtil.playSound(player, config.soundLoginFail);
            return LoginResult.WRONG_PASSWORD;
        }
    }

    // ─── Register ───────────────────────────────────────────

    public enum RegisterResult {
        SUCCESS, ALREADY_REGISTERED, PASSWORD_MISMATCH, PASSWORD_TOO_SHORT, ALREADY_LOGGED_IN
    }

    public RegisterResult attemptRegister(ServerPlayer player, String password, String confirmPassword) {
        UUID uuid = player.getUUID();

        if (authenticated.contains(uuid)) {
            player.sendSystemMessage(formatter.format(config.msgAlreadyLoggedIn, player));
            return RegisterResult.ALREADY_LOGGED_IN;
        }

        if (dataStore.isRegistered(uuid)) {
            player.sendSystemMessage(formatter.format(config.msgAlreadyRegistered, player));
            return RegisterResult.ALREADY_REGISTERED;
        }

        if (!password.equals(confirmPassword)) {
            player.sendSystemMessage(formatter.format(config.msgPasswordMismatch, player));
            return RegisterResult.PASSWORD_MISMATCH;
        }

        if (password.length() < config.minPasswordLength) {
            player.sendSystemMessage(formatter.format(config.msgMinPasswordLength, player,
                    Map.of("min", String.valueOf(config.minPasswordLength))));
            return RegisterResult.PASSWORD_TOO_SHORT;
        }

        String hash = PasswordService.hash(password);
        String ip = getPlayerIp(player);
        PlayerAccount account = new PlayerAccount(uuid, player.getName().getString(), hash, ip);

        if (dataStore.register(account)) {
            authenticatePlayer(player);
            player.sendSystemMessage(formatter.format(config.msgRegisterSuccess, player));
            PlayerUtil.playSound(player, config.soundLoginSuccess);

            EasyLoginConstants.LOGGER.info("Player {} registered and logged in from {}",
                    player.getName().getString(), ip);
            return RegisterResult.SUCCESS;
        }

        return RegisterResult.ALREADY_REGISTERED;
    }

    // ─── Change Password ────────────────────────────────────

    public enum ChangePasswordResult {
        SUCCESS, WRONG_OLD_PASSWORD, PASSWORD_MISMATCH, PASSWORD_TOO_SHORT, NOT_LOGGED_IN, NOT_REGISTERED
    }

    public ChangePasswordResult changePassword(ServerPlayer player, String oldPassword, String newPassword,
            String confirmNew) {
        UUID uuid = player.getUUID();

        if (!authenticated.contains(uuid)) {
            player.sendSystemMessage(formatter.format(config.msgBlocked, player));
            return ChangePasswordResult.NOT_LOGGED_IN;
        }

        PlayerAccount account = dataStore.getAccount(uuid).orElse(null);
        if (account == null) {
            return ChangePasswordResult.NOT_REGISTERED;
        }

        if (!PasswordService.verify(oldPassword, account.getPasswordHash())) {
            player.sendSystemMessage(formatter.format(config.msgWrongOldPassword, player));
            return ChangePasswordResult.WRONG_OLD_PASSWORD;
        }

        if (!newPassword.equals(confirmNew)) {
            player.sendSystemMessage(formatter.format(config.msgPasswordMismatch, player));
            return ChangePasswordResult.PASSWORD_MISMATCH;
        }

        if (newPassword.length() < config.minPasswordLength) {
            player.sendSystemMessage(formatter.format(config.msgMinPasswordLength, player,
                    Map.of("min", String.valueOf(config.minPasswordLength))));
            return ChangePasswordResult.PASSWORD_TOO_SHORT;
        }

        account.setPasswordHash(PasswordService.hash(newPassword));
        dataStore.updateAccount(account);
        player.sendSystemMessage(formatter.format(config.msgPasswordChanged, player));

        EasyLoginConstants.LOGGER.info("Player {} changed their password", player.getName().getString());
        return ChangePasswordResult.SUCCESS;
    }

    // ─── Logout ─────────────────────────────────────────────

    public boolean logout(ServerPlayer player) {
        UUID uuid = player.getUUID();
        if (!authenticated.contains(uuid))
            return false;
        if (!config.enableLogout)
            return false;

        // Check cooldown
        Long lastLogout = logoutCooldowns.get(uuid);
        if (lastLogout != null) {
            long elapsed = (System.currentTimeMillis() - lastLogout) / 1000;
            if (elapsed < config.logoutCooldown) {
                long remaining = config.logoutCooldown - elapsed;
                player.sendSystemMessage(formatter.format(config.msgLogoutCooldown, player,
                        Map.of("seconds", String.valueOf(remaining))));
                return false;
            }
        }

        deauthenticatePlayer(player);
        logoutCooldowns.put(uuid, System.currentTimeMillis());
        player.sendSystemMessage(formatter.format(config.msgLogoutSuccess, player));
        return true;
    }

    // ─── Admin commands ─────────────────────────────────────

    public void forceLogin(ServerPlayer target) {
        authenticatePlayer(target);
    }

    public void forceRegister(ServerPlayer target, String password) {
        UUID uuid = target.getUUID();
        String hash = PasswordService.hash(password);
        String ip = getPlayerIp(target);
        PlayerAccount account = new PlayerAccount(uuid, target.getName().getString(), hash, ip);

        if (dataStore.isRegistered(uuid)) {
            account = dataStore.getAccount(uuid).orElse(account);
            account.setPasswordHash(hash);
            dataStore.updateAccount(account);
        } else {
            dataStore.register(account);
        }

        authenticatePlayer(target);
    }

    public boolean resetPassword(UUID uuid) {
        return dataStore.removeAccount(uuid);
    }

    public boolean purgeAccount(UUID uuid) {
        authenticated.remove(uuid);
        return dataStore.removeAccount(uuid);
    }

    // ─── Tick (called every server tick) ────────────────────

    public void onServerTick(MinecraftServer server) {
        long now = System.currentTimeMillis();

        // Create a copy to avoid ConcurrentModificationException during player kicks
        List<ServerPlayer> players = new ArrayList<>(server.getPlayerList().getPlayers());

        for (ServerPlayer player : players) {
            if (PlatformHelper.INSTANCE.isFakePlayer(player))
                continue;

            UUID uuid = player.getUUID();
            if (authenticated.contains(uuid))
                continue;

            // Check timeout
            Long joinTime = joinTimestamps.get(uuid);
            if (joinTime != null) {
                long elapsed = (now - joinTime) / 1000;
                if (elapsed >= config.loginTimeout) {
                    // Kick for timeout
                    player.connection.disconnect(Component.literal(formatter.colorize(config.msgKicked)));
                    EasyLoginConstants.LOGGER.info("Player {} kicked for login timeout",
                            player.getName().getString());
                    continue;
                }
            }

            // Send reminder
            Long lastRem = lastReminder.get(uuid);
            if (lastRem != null) {
                long elapsed = (now - lastRem) / 1000;
                if (elapsed >= config.reminderInterval) {
                    boolean registered = dataStore.isRegistered(uuid);
                    String msg = registered ? config.msgActionBarReminder : config.msgActionBarReminderNew;
                    PlayerUtil.sendActionBar(player, formatter.format(msg, player));
                    lastReminder.put(uuid, now);
                }
            }

            // Freeze or Limbo correction
            if (config.blockMovement) {
                if (config.limboEnabled) {
                    double dx = Math.abs(player.getX() - config.limboX);
                    double dz = Math.abs(player.getZ() - config.limboZ);
                    if (dx > 1.5 || dz > 1.5) {
                        PlayerUtil.teleportToLimbo(player, config);
                    }
                } else {
                    // Limbo disabled but freeze enabled: keep at saved position
                    double[] pos = savedPositions.get(uuid);
                    if (pos != null) {
                        double dx = Math.abs(player.getX() - pos[0]);
                        double dy = Math.abs(player.getY() - pos[1]);
                        double dz = Math.abs(player.getZ() - pos[2]);

                        // Check if player moved significantly (including Y axis)
                        if (dx > 0.1 || dy > 0.1 || dz > 0.1) {
                            // Teleport back to exact spawn spot including rotation
                            player.teleportTo(pos[0], pos[1], pos[2]);

                            // Reset velocity to prevent upward momentum or falling loops
                            player.setDeltaMovement(0, 0, 0);
                            player.hurtMarked = true; // Sync velocity to client
                        }
                    }
                }
            }
        }

        // Periodic cleanup of rate limiter (every ~30 seconds = 600 ticks)
        if (server.getTickCount() % 600 == 0) {
            rateLimiter.cleanup();
        }
    }

    // ─── Private helpers ────────────────────────────────────

    private void authenticatePlayer(ServerPlayer player) {
        UUID uuid = player.getUUID();
        authenticated.add(uuid);
        joinTimestamps.remove(uuid);
        lastReminder.remove(uuid);

        // Teleport back
        if (config.limboEnabled) {
            restorePlayerPosition(player);
        }

        // Set invincibility
        if (config.invincibilityDuration > 0) {
            invincibilityExpiry.put(uuid, System.currentTimeMillis() + (config.invincibilityDuration * 1000L));
        }

        // Clear title
        PlayerUtil.clearTitle(player);
    }

    private void deauthenticatePlayer(ServerPlayer player) {
        UUID uuid = player.getUUID();
        authenticated.remove(uuid);
        joinTimestamps.put(uuid, System.currentTimeMillis());
        lastReminder.put(uuid, 0L);

        savePlayerPosition(player);

        if (config.limboEnabled) {
            PlayerUtil.teleportToLimbo(player, config);
        }
    }

    private void savePlayerPosition(ServerPlayer player) {
        savedPositions.put(player.getUUID(), new double[] {
                player.getX(), player.getY(), player.getZ(),
                player.getYRot(), player.getXRot()
        });
        savedDimensions.put(player.getUUID(),
                player.level().dimension().location().toString());
    }

    private void restorePlayerPosition(ServerPlayer player) {
        double[] pos = savedPositions.remove(player.getUUID());
        String dim = savedDimensions.remove(player.getUUID());

        if (pos != null && dim != null) {
            MinecraftServer server = player.getServer();
            if (server != null) {
                ResourceLocation dimRL = ResourceLocation.parse(dim);
                for (ServerLevel level : server.getAllLevels()) {
                    if (level.dimension().location().equals(dimRL)) {
                        player.teleportTo(level, pos[0], pos[1], pos[2], (float) pos[3], (float) pos[4]);
                        return;
                    }
                }
            }
            // Fallback: just teleport in current dimension
            player.teleportTo(pos[0], pos[1], pos[2]);
        }
    }

    private String getPlayerIp(ServerPlayer player) {
        try {
            return player.getIpAddress();
        } catch (Exception e) {
            return "unknown";
        }
    }

    public EasyLoginConfig getConfig() {
        return config;
    }

    public PlayerDataStore getDataStore() {
        return dataStore;
    }

    public MessageFormatter getFormatter() {
        return formatter;
    }
}
