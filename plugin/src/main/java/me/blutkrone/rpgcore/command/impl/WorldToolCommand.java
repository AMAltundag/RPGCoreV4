package me.blutkrone.rpgcore.command.impl;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.command.AbstractCommand;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class WorldToolCommand extends AbstractCommand {

    @Override
    public boolean canUseCommand(CommandSender sender) {
        return sender.hasPermission("rpg.admin");
    }

    @Override
    public BaseComponent[] getHelpText() {
        return buildHelpText("", "§cWorld integration tool [ADMIN]");
    }

    @Override
    public void invoke(CommandSender sender, String... args) {
        ((Player) sender).getInventory().addItem(RPGCore.inst().getWorldIntegrationManager().getTool());
    }

    @Override
    public List<String> suggest(CommandSender sender, String... args) {
        return null;
    }
}
