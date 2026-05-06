package com.easylogin.util;

import com.easylogin.config.EasyLoginConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.*;
//? if <=1.21.1 {
/*import net.minecraft.resources.ResourceLocation;
*///?} else {
import net.minecraft.resources.Identifier;
//?}
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

import java.util.Optional;

/**
 * Utility methods for player teleportation, titles, action bars, and sounds.
 * Uses vanilla packets — no client mod needed.
 */
public final class PlayerUtil {

    private PlayerUtil() {
    }

    /**
     * Teleport a player to the limbo coordinates defined in config.
     */
    public static void teleportToLimbo(ServerPlayer player, EasyLoginConfig config) {
        //? if <=1.21.1 {
        /*player.teleportTo(config.limboX, config.limboY, config.limboZ);
        *///?} else {
        player.teleportTo((ServerLevel) player.level(), config.limboX, config.limboY, config.limboZ, java.util.Set.of(), player.getYRot(), player.getXRot(), true);
        //?}
    }

    /**
     * Send a title and subtitle to a player (vanilla packet).
     */
    public static void sendTitle(ServerPlayer player, String title, String subtitle) {
        // Set times: fadeIn=10, stay=70, fadeOut=20 (ticks)
        player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 70, 20));

        if (title != null && !title.isEmpty()) {
            player.connection.send(new ClientboundSetTitleTextPacket(
                    Component.literal(title)));
        }

        if (subtitle != null && !subtitle.isEmpty()) {
            player.connection.send(new ClientboundSetSubtitleTextPacket(
                    Component.literal(subtitle)));
        }
    }

    /**
     * Send an action bar message to a player (vanilla packet).
     */
    public static void sendActionBar(ServerPlayer player, Component message) {
        player.connection.send(new ClientboundSetActionBarTextPacket(message));
    }

    /**
     * Clear title from player screen.
     */
    public static void clearTitle(ServerPlayer player) {
        player.connection.send(new ClientboundClearTitlesPacket(true));
    }

    /**
     * Play a vanilla sound to a player.
     *
     * @param player  the player
     * @param soundId the sound resource location (e.g.,
     *                "minecraft:entity.player.levelup")
     */
    public static void playSound(ServerPlayer player, String soundId) {
        try {
            //? if <=1.21.1 {
            /*ResourceLocation rl = ResourceLocation.parse(soundId);
            *///?} else {
            Identifier rl = Identifier.parse(soundId);
            //?}
            Optional<SoundEvent> soundEvent = BuiltInRegistries.SOUND_EVENT.getOptional(rl);
            //? if <=1.21.1 {
            /*soundEvent.ifPresent(event -> player.playNotifySound(event, SoundSource.MASTER, 1.0f, 1.0f));
            *///?} else {
            soundEvent.ifPresent(event -> player.level().playSound(null, player.getX(), player.getY(), player.getZ(), event, SoundSource.MASTER, 1.0f, 1.0f));
            //?}
        } catch (Exception e) {
            // Silently ignore invalid sound IDs
        }
    }
}
