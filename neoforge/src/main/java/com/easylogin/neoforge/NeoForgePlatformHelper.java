package com.easylogin.neoforge;

import com.easylogin.platform.PlatformHelper;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.util.FakePlayer;

import java.nio.file.Path;

/**
 * NeoForge implementation of PlatformHelper.
 */
public class NeoForgePlatformHelper implements PlatformHelper {

    @Override
    public boolean isFakePlayer(ServerPlayer player) {
        return player instanceof FakePlayer;
    }

    @Override
    public Path getConfigDir() {
        return FMLPaths.CONFIGDIR.get();
    }
}
