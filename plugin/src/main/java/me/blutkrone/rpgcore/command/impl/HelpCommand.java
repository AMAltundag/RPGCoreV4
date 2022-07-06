package me.blutkrone.rpgcore.command.impl;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.command.AbstractCommand;
import me.blutkrone.rpgcore.command.CommandArgumentException;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HelpCommand extends AbstractCommand {

    @Override
    public boolean canUseCommand(CommandSender sender) {
        return true;
    }

    @Override
    public BaseComponent[] getHelpText() {
        return new BaseComponent[0];
    }

    @Override
    public void invoke(CommandSender sender, String... args) throws CommandArgumentException {
        RPGCore.inst().getCommands().forEach((id, command) -> {
            // ensure command is available
            if (!command.canUseCommand(sender))
                return;
            // ensure there is a description to use
            BaseComponent[] description = command.getHelpText();
            if (description.length == 0)
                return;
            // lead with the command identity
            List<BaseComponent> joined = new ArrayList<>();
            joined.addAll(Arrays.asList(TextComponent.fromLegacyText("§f/rpg " + id + " §a")));
            joined.addAll(Arrays.asList(description));
            // offer up the description
            sender.spigot().sendMessage(joined.toArray(new BaseComponent[0]));
        });
    }

    @Override
    public List<String> suggest(CommandSender sender, String... args) {
        return null;
    }
}
