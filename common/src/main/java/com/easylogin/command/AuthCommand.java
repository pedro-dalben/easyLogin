package com.easylogin.command;

import com.easylogin.EasyLoginConstants;
import com.easylogin.auth.AuthManager;
import com.easylogin.config.EasyLoginConfig;
import com.easylogin.data.PlayerAccount;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;

/**
 * /auth — admin commands for EasyLogin.
 * Subcommands: status, reload, force-login, force-register, reset-password,
 * purge
 */
public class AuthCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher,
            AuthManager authManager,
            EasyLoginConfig config,
            Runnable reloadAction) {

        dispatcher.register(Commands.literal("auth")
                // /auth status
                .then(Commands.literal("status")
                        .requires(src -> src.hasPermission(2))
                        .executes(ctx -> {
                            CommandSourceStack source = ctx.getSource();
                            int total = authManager.getDataStore().getAccountCount();
                            int online = source.getServer().getPlayerCount();
                            source.sendSuccess(() -> Component.literal(
                                    "§6[EasyLogin] §7Registered accounts: §e" + total +
                                            " §7| Online: §e" + online),
                                    false);
                            return 1;
                        }))

                // /auth reload
                .then(Commands.literal("reload")
                        .requires(src -> src.hasPermission(3))
                        .executes(ctx -> {
                            reloadAction.run();
                            ctx.getSource().sendSuccess(() -> Component.literal(
                                    authManager.getFormatter().colorize(config.msgAdminReload)), true);
                            EasyLoginConstants.LOGGER.info("Configuration reloaded by {}",
                                    ctx.getSource().getTextName());
                            return 1;
                        }))

                // /auth force-login <player>
                .then(Commands.literal("force-login")
                        .requires(src -> src.hasPermission(3))
                        .then(Commands.argument("target", EntityArgument.player())
                                .executes(ctx -> {
                                    ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
                                    authManager.forceLogin(target);
                                    ctx.getSource().sendSuccess(() -> authManager.getFormatter().format(
                                            config.msgAdminForceLogin, target), true);
                                    EasyLoginConstants.LOGGER.info("Admin {} force-logged in {}",
                                            ctx.getSource().getTextName(),
                                            target.getName().getString());
                                    return 1;
                                })))

                // /auth force-register <player> <password>
                .then(Commands.literal("force-register")
                        .requires(src -> src.hasPermission(3))
                        .then(Commands.argument("target", EntityArgument.player())
                                .then(Commands.argument("password", StringArgumentType.word())
                                        .executes(ctx -> {
                                            ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
                                            String password = StringArgumentType.getString(ctx, "password");
                                            authManager.forceRegister(target, password);
                                            ctx.getSource().sendSuccess(() -> authManager.getFormatter().format(
                                                    config.msgAdminForceRegister, target), true);
                                            EasyLoginConstants.LOGGER.info("Admin {} force-registered {}",
                                                    ctx.getSource().getTextName(),
                                                    target.getName().getString());
                                            return 1;
                                        }))))

                // /auth reset-password <player>
                .then(Commands.literal("reset-password")
                        .requires(src -> src.hasPermission(3))
                        .then(Commands.argument("target", EntityArgument.player())
                                .executes(ctx -> {
                                    ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
                                    boolean success = authManager.resetPassword(target.getUUID());
                                    if (success) {
                                        ctx.getSource().sendSuccess(() -> authManager.getFormatter().format(
                                                config.msgAdminResetPassword, target), true);
                                        EasyLoginConstants.LOGGER.info("Admin {} reset password for {}",
                                                ctx.getSource().getTextName(),
                                                target.getName().getString());
                                    } else {
                                        ctx.getSource().sendFailure(
                                                authManager.getFormatter().format(
                                                        config.msgAdminNotRegistered, target));
                                    }
                                    return 1;
                                })))

                // /auth purge <player>
                .then(Commands.literal("purge")
                        .requires(src -> src.hasPermission(3))
                        .then(Commands.argument("target", EntityArgument.player())
                                .executes(ctx -> {
                                    ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
                                    boolean success = authManager.purgeAccount(target.getUUID());
                                    if (success) {
                                        ctx.getSource().sendSuccess(() -> authManager.getFormatter().format(
                                                config.msgAdminPurge, target), true);
                                        EasyLoginConstants.LOGGER.info("Admin {} purged account for {}",
                                                ctx.getSource().getTextName(),
                                                target.getName().getString());
                                    } else {
                                        ctx.getSource().sendFailure(
                                                authManager.getFormatter().format(
                                                        config.msgAdminNotRegistered, target));
                                    }
                                    return 1;
                                }))));
    }
}
