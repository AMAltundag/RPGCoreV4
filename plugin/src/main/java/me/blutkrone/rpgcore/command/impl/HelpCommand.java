package me.blutkrone.rpgcore.command.impl;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.command.AbstractCommand;
import me.blutkrone.rpgcore.command.CommandArgumentException;
import me.blutkrone.rpgcore.util.fontmagic.FontMagicConstant;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import java.util.*;

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
        Map<String, BaseComponent[]> mapping = new HashMap<>();
        RPGCore.inst().getCommands().forEach((id, command) -> {
            // ensure command is available
            if (!command.canUseCommand(sender))
                return;
            // ensure there is a description to use
            BaseComponent[] description = command.getHelpText();
            if (description.length == 0)
                return;
            // register command
            mapping.put("§f/rpg " + id + " ", description);
        });

        Map<String, BaseComponent[]> sorted = new TreeMap<>(mapping);
        if (sender instanceof ConsoleCommandSender) {
            int width = sorted.keySet().stream().map(String::length).max(Integer::compareTo).orElse(0);
            sorted.forEach((command, description) -> {
                String padding = " ".repeat((width-command.length())) + " : ";

                // lead with the command identity
                List<BaseComponent> joined = new ArrayList<>();
                joined.addAll(Arrays.asList(TextComponent.fromLegacyText(command + padding)));
                joined.addAll(Arrays.asList(description));

                // offer up the description
                sender.spigot().sendMessage(joined.toArray(new BaseComponent[0]));
            });
        } else {
            int width_command = sorted.keySet().stream()
                    .map(RPGCore.inst().getResourcepackManager()::measure)
                    .max(Integer::compareTo).orElse(0);
            int width_description = sorted.values().stream()
                    .map(TextComponent::toLegacyText)
                    .map(RPGCore.inst().getResourcepackManager()::measure)
                    .max(Integer::compareTo).orElse(0);

            sorted.forEach((command, description) -> {
                String padding = FontMagicConstant.advance((width_command-RPGCore.inst().getResourcepackManager().measure(command))) + " : ";

                // lead with the command identity
                List<BaseComponent> joined = new ArrayList<>();
                joined.addAll(Arrays.asList(TextComponent.fromLegacyText(command + padding)));
                joined.addAll(Arrays.asList(description));
                int width = RPGCore.inst().getResourcepackManager().measure(TextComponent.toLegacyText(description));
                if (width < width_description) {
                    joined.addAll(Arrays.asList(TextComponent.fromLegacyText(FontMagicConstant.advance(width_description - width))));
                }

                for (BaseComponent component : joined) {
                    component.setHoverEvent(description[0].getHoverEvent());
                }

                for (BaseComponent message : joined) {
                    message.setFont(RPGCore.inst().getResourcepackManager().aliasToReal("default_fixed"));
                }

                // offer up the description
                sender.spigot().sendMessage(joined.toArray(new BaseComponent[0]));
            });
        }
    }

    @Override
    public List<String> suggest(CommandSender sender, String... args) {
        return null;
    }
}
