package me.blutkrone.rpgcore.command.impl;

import me.blutkrone.rpgcore.command.AbstractCommand;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import java.util.List;

public class ToolPlayerMigrateCommand extends AbstractCommand {

    @Override
    public boolean canUseCommand(CommandSender sender) {
        return sender instanceof ConsoleCommandSender;
    }

    @Override
    public BaseComponent[] getHelpText() {
        return new BaseComponent[0];
    }

    @Override
    public void invoke(CommandSender sender, String... args) {
        sender.sendMessage("§cNot implemented (translate filename to objectID)");
    }

    @Override
    public List<String> suggest(CommandSender sender, String... args) {
        return null;
    }
}
