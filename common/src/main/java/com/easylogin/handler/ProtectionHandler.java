package com.easylogin.handler;

import com.easylogin.auth.AuthManager;
import com.easylogin.platform.PlatformHelper;
import net.minecraft.server.level.ServerPlayer;

import java.util.Set;

/**
 * Central protection handler. Determines whether actions from a player
 * should be blocked (i.e., the player is not yet authenticated).
 */
public class ProtectionHandler {

    private final AuthManager authManager;

    /** Commands that are allowed even when unauthenticated. */
    private static final Set<String> ALLOWED_COMMANDS = Set.of(
            "login", "l",
            "register", "reg",
            "easylogin:login", "easylogin:l",
            "easylogin:register", "easylogin:reg");

    public ProtectionHandler(AuthManager authManager) {
        this.authManager = authManager;
    }

    /**
     * Returns true if this player's action should be blocked.
     */
    public boolean shouldBlock(ServerPlayer player) {
        return authManager.shouldBlock(player);
    }

    /**
     * Check if a command is allowed for unauthenticated players.
     */
    public boolean isCommandAllowed(String commandName) {
        // Strip leading /
        if (commandName.startsWith("/")) {
            commandName = commandName.substring(1);
        }
        // Get the root command (first word)
        int space = commandName.indexOf(' ');
        if (space > 0) {
            commandName = commandName.substring(0, space);
        }
        return ALLOWED_COMMANDS.contains(commandName.toLowerCase());
    }
}
