package com.easylogin.command;

import com.easylogin.auth.AuthManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

/**
 * {@code /changepassword <old> <new> <confirm_new>}
 */
public class ChangePasswordCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, AuthManager authManager) {
        dispatcher.register(Commands.literal("changepassword")
                .then(Commands.argument("oldPassword", StringArgumentType.word())
                        .then(Commands.argument("newPassword", StringArgumentType.word())
                                .then(Commands.argument("confirmNew", StringArgumentType.word())
                                        .executes(ctx -> {
                                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                                            String oldPass = StringArgumentType.getString(ctx, "oldPassword");
                                            String newPass = StringArgumentType.getString(ctx, "newPassword");
                                            String confirm = StringArgumentType.getString(ctx, "confirmNew");
                                            authManager.changePassword(player, oldPass, newPass, confirm);
                                            return 1;
                                        }))))
                .executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    player.sendSystemMessage(authManager.getFormatter().format(
                            authManager.getConfig().msgChangePasswordUsage, player));
                    return 1;
                }));
    }
}
