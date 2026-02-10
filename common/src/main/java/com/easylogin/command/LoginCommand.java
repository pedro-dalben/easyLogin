package com.easylogin.command;

import com.easylogin.auth.AuthManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

/**
 * {@code /login <password>} — authenticate an existing account.
 * Alias: /l
 */
public class LoginCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, AuthManager authManager) {
        var command = Commands.literal("login")
                .then(Commands.argument("password", StringArgumentType.word())
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            String password = StringArgumentType.getString(ctx, "password");
                            authManager.attemptLogin(player, password);
                            return 1;
                        }))
                .executes(ctx -> {
                    // No args: show usage
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    player.sendSystemMessage(authManager.getFormatter().format(
                            authManager.getConfig().msgLoginUsage, player));
                    return 1;
                });

        dispatcher.register(command);
        dispatcher.register(Commands.literal("l").redirect(dispatcher.getRoot().getChild("login")));
    }
}
