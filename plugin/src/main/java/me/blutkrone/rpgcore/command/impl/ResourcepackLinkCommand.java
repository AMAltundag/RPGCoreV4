package me.blutkrone.rpgcore.command.impl;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.command.AbstractCommand;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
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
        return TextComponent.fromLegacyText("url §fDefine a download link for the resourcepack");
    }

    @Override
    public void invoke(CommandSender sender, String... args) {
        RPGCore.inst().getResourcePackManager().setUrl(args[1]);
    }

    @Override
    public List<String> suggest(CommandSender sender, String... args) {
        return null;
    }
}
