package com.easylogin.util;

import com.easylogin.config.EasyLoginConfig;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Formats messages with color codes and placeholders.
 */
public class MessageFormatter {

    private final EasyLoginConfig config;

    public MessageFormatter(EasyLoginConfig config) {
        this.config = config;
    }

    /**
     * Format a message with player context placeholders.
     */
    public Component format(String message, ServerPlayer player) {
        return format(message, player, Map.of());
    }

    /**
     * Format a message with player context and extra placeholders.
     */
    public Component format(String message, ServerPlayer player, Map<String, String> extra) {
        String result = message;

        // Player placeholders
        result = result.replace("{player}", player.getName().getString());
        result = result.replace("{server}", config.serverName);
        result = result.replace("{discord}", config.discordLink);

        // Server stats
        if (player.getServer() != null) {
            result = result.replace("{online}",
                    String.valueOf(player.getServer().getPlayerCount()));
        }

        // Extra placeholders
        for (Map.Entry<String, String> entry : extra.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue());
        }

        // Colorize
        result = colorize(result);

        return Component.literal(result);
    }

    /**
     * Convert {@code &}-color codes to § formatting codes.
     * Supports {@code &}0-9, {@code &}a-f, {@code &}k-o, {@code &}r (standard
     * Minecraft codes).
     */
    public String colorize(String text) {
        if (text == null)
            return "";

        char[] chars = text.toCharArray();
        StringBuilder sb = new StringBuilder(chars.length);

        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '&' && i + 1 < chars.length) {
                char code = chars[i + 1];
                if ("0123456789abcdefklmnorABCDEFKLMNOR".indexOf(code) != -1) {
                    sb.append('§');
                    sb.append(Character.toLowerCase(code));
                    i++; // skip next char
                    continue;
                }
            }
            sb.append(chars[i]);
        }

        return sb.toString();
    }
}
