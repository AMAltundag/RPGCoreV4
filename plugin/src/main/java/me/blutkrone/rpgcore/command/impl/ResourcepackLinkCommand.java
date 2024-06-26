package me.blutkrone.rpgcore.command.impl;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.command.AbstractCommand;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import java.util.List;

public class ResourcepackLinkCommand extends AbstractCommand {

    @Override
    public boolean canUseCommand(CommandSender sender) {
        return sender instanceof ConsoleCommandSender;
    }

    @Override
    public BaseComponent[] getHelpText() {
        return buildHelpText("§furl", "§cUpdate URL of resourcepack [ADMIN]",
        "§7URL: Link to updated resourcepack");
    }

    @Override
    public void invoke(CommandSender sender, String... args) {
        RPGCore.inst().getResourcepackManager().setUrl(args[1]);
    }

    @Override
    public List<String> suggest(CommandSender sender, String... args) {
        return null;
    }
}
