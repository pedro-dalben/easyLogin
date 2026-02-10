package com.easylogin.command;

import com.easylogin.auth.AuthManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

/**
 * {@code /register <password> <confirm_password>} — create a new account.
 * Alias: /reg
 */
public class RegisterCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, AuthManager authManager) {
        var command = Commands.literal("register")
                .then(Commands.argument("password", StringArgumentType.word())
                        .then(Commands.argument("confirmPassword", StringArgumentType.word())
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    String password = StringArgumentType.getString(ctx, "password");
                                    String confirm = StringArgumentType.getString(ctx, "confirmPassword");
                                    authManager.attemptRegister(player, password, confirm);
                                    return 1;
                                }))
                        .executes(ctx -> {
                            // Only one arg: show usage
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            player.sendSystemMessage(authManager.getFormatter().format(
                                    authManager.getConfig().msgRegisterUsage, player));
                            return 1;
                        }))
                .executes(ctx -> {
                    // No args: show usage
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    player.sendSystemMessage(authManager.getFormatter().format(
                            authManager.getConfig().msgRegisterUsage, player));
                    return 1;
                });

        dispatcher.register(command);
        dispatcher.register(Commands.literal("reg").redirect(dispatcher.getRoot().getChild("register")));
    }
}
