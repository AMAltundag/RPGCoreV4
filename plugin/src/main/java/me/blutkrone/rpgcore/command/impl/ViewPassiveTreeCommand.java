package me.blutkrone.rpgcore.command.impl;

import me.blutkrone.rpgcore.command.AbstractCommand;
import me.blutkrone.rpgcore.menu.PassiveMenu;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class ViewPassiveTreeCommand extends AbstractCommand {
    @Override
    public boolean canUseCommand(CommandSender sender) {
        return sender.hasPermission("rpg.admin");
    }

    @Override
    public BaseComponent[] getHelpText() {
        return TextComponent.fromLegacyText("<tree> §fShows any passive tree.");
    }

    @Override
    public void invoke(CommandSender sender, String... args) {
        new PassiveMenu(args[1]).finish(((Player) sender));
    }

    @Override
    public List<String> suggest(CommandSender sender, String... args) {
        return null;
    }
}
