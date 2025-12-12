package me.athulsib.server.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentEnum;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.entity.EntityFinder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class GamemodeCommand extends Command {

    public GamemodeCommand() {
        super("gamemode", "gm");

        setDefaultExecutor((sender, context) -> {
            sender.sendMessage(Component.text("Usage: /gamemode <mode> [player]", NamedTextColor.RED));
        });

        ArgumentEnum<GameMode> gamemodeArg = ArgumentType.Enum("mode", GameMode.class);
        ArgumentEntity targetArg = ArgumentType.Entity("target").onlyPlayers(true).singleEntity(true);

        // /gamemode <mode>
        addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(Component.text("Only players can use this command without specifying a target.", NamedTextColor.RED));
                return;
            }

            GameMode mode = context.get(gamemodeArg);
            player.setGameMode(mode);
            player.sendMessage(Component.text("Your game mode has been set to " + mode.name().toLowerCase(), NamedTextColor.GREEN));
        }, gamemodeArg);

        // /gamemode <mode> <player>
        addSyntax((sender, context) -> {
            GameMode mode = context.get(gamemodeArg);
            EntityFinder finder = context.get(targetArg);
            Player target = finder.findFirstPlayer(sender);

            if (target == null) {
                sender.sendMessage(Component.text("Player not found.", NamedTextColor.RED));
                return;
            }

            target.setGameMode(mode);
            sender.sendMessage(Component.text("Set " + target.getUsername() + "'s game mode to " + mode.name().toLowerCase(), NamedTextColor.GREEN));
            target.sendMessage(Component.text("Your game mode has been set to " + mode.name().toLowerCase(), NamedTextColor.GREEN));
        }, gamemodeArg, targetArg);
    }
}