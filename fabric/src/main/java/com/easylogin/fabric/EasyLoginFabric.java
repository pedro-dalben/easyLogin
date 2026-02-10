package com.easylogin.fabric;

import com.easylogin.EasyLoginConstants;
import com.easylogin.auth.AuthManager;
import com.easylogin.command.*;
import com.easylogin.config.EasyLoginConfig;
import com.easylogin.data.PlayerDataStore;
import com.easylogin.handler.ProtectionHandler;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;

public class EasyLoginFabric implements DedicatedServerModInitializer {

    private EasyLoginConfig config;
    private PlayerDataStore dataStore;
    private AuthManager authManager;
    private ProtectionHandler protectionHandler;

    @Override
    public void onInitializeServer() {
        EasyLoginConstants.LOGGER.info("EasyLogin (Fabric) initializing...");

        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            config = new EasyLoginConfig(FabricLoader.getInstance().getConfigDir());
            config.load();

            dataStore = new PlayerDataStore(FabricLoader.getInstance().getConfigDir());
            dataStore.load();

            authManager = new AuthManager(config, dataStore);
            protectionHandler = new ProtectionHandler(authManager);

            // Register event handlers
            FabricEventHandler.register(authManager, protectionHandler, config);

            EasyLoginConstants.LOGGER.info("EasyLogin (Fabric) started successfully!");
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            if (dataStore != null) {
                dataStore.save();
            }
            EasyLoginConstants.LOGGER.info("EasyLogin (Fabric) shutting down.");
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            if (authManager == null)
                return;
            LoginCommand.register(dispatcher, authManager);
            RegisterCommand.register(dispatcher, authManager);
            ChangePasswordCommand.register(dispatcher, authManager);
            LogoutCommand.register(dispatcher, authManager);
            AuthCommand.register(dispatcher, authManager, config, () -> config.load());
            EasyLoginConstants.LOGGER.info("EasyLogin commands registered.");
        });
    }
}
