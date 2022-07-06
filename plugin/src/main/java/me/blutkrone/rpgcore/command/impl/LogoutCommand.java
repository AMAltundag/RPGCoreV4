package me.blutkrone.rpgcore.command.impl;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.command.AbstractCommand;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class LogoutCommand extends AbstractCommand {

    @Override
    public boolean canUseCommand(CommandSender sender) {
        return sender instanceof Player;
    }

    @Override
    public BaseComponent[] getHelpText() {
        return TextComponent.fromLegacyText("§fLogout out of your current character.");
    }

    @Override
    public void invoke(CommandSender sender, String... args) {
        RPGCore.inst().getEntityManager().unregister(((Player) sender).getUniqueId());
        sender.sendMessage("§aCore received a request to sign off.");
    }

    @Override
    public List<String> suggest(CommandSender sender, String... args) {
        return null;
    }
}
