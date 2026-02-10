package com.easylogin.platform;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;

import java.nio.file.Path;
import java.util.ServiceLoader;

/**
 * Platform abstraction interface. Each loader provides its own implementation
 * via Java ServiceLoader.
 */
public interface PlatformHelper {

    PlatformHelper INSTANCE = ServiceLoader.load(PlatformHelper.class)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No PlatformHelper implementation found!"));

    /**
     * Returns true if the given player is a FakePlayer (automation, carpet, etc.)
     */
    boolean isFakePlayer(ServerPlayer player);

    /**
     * Returns the platform's config directory path (e.g., config/).
     */
    Path getConfigDir();
}
