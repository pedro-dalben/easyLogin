package com.easylogin.command;

import com.easylogin.auth.AuthManager;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

/**
 * /logout — deauthenticate (with cooldown).
 */
public class LogoutCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, AuthManager authManager) {
        dispatcher.register(Commands.literal("logout")
                .executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    authManager.logout(player);
                    return 1;
                }));
    }
}
