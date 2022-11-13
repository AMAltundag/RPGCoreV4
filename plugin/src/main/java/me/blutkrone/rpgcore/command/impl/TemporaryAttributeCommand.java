package me.blutkrone.rpgcore.command.impl;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.command.AbstractCommand;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class TemporaryAttributeCommand extends AbstractCommand {

    @Override
    public boolean canUseCommand(CommandSender sender) {
        return sender.isOp() && sender instanceof Player;
    }

    @Override
    public BaseComponent[] getHelpText() {
        return new BaseComponent[0];
    }

    @Override
    public void invoke(CommandSender sender, String... args) {
        CorePlayer player = RPGCore.inst().getEntityManager().getPlayer(((Player) sender));
        if (player == null) {
            sender.sendMessage("§cNot initialized within RPGCore!");
            return;
        }
        player.getAttribute(args[1].toLowerCase()).create(Double.parseDouble(args[2]));
        sender.sendMessage("§fAdded " + args[2] + " of " + args[1].toUpperCase() + " attribute!");
    }

    @Override
    public List<String> suggest(CommandSender sender, String... args) {
        return null;
    }
}
