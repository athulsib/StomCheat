package me.athulsib.stomcheat.commands;

import me.athulsib.stomcheat.StomCheat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;

public class ACExtensionInfoCommand extends Command {
    public ACExtensionInfoCommand() {
        super("acextensions", "ace");
        setDefaultExecutor((sender, context) -> sendInfo(sender));
    }

    private void sendInfo(CommandSender sender) {
        StomCheat stomCheat = StomCheat.getInstance();

        if (stomCheat == null) {
            sender.sendMessage(Component.text("[!] StomCheat is not initialized.").color(NamedTextColor.RED));
            return;
        }

        sender.sendMessage(Component.text("══════ StomCheat - Extensions ══════").color(NamedTextColor.AQUA));
        stomCheat.getExtensionManager().getLoadedExtensions().forEach(extensionInfo -> {
            sender.sendMessage(Component.text("- " + extensionInfo.name() + " by " + extensionInfo.author() + " | " + extensionInfo.version()).color(NamedTextColor.GRAY));
        });
    }
}
