package me.blutkrone.rpgcore.command.impl;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.command.AbstractCommand;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class HologramDeleteCommand extends AbstractCommand {
    @Override
    public boolean canUseCommand(CommandSender sender) {
        return sender.hasPermission("rpg.admin") && sender instanceof Player;
    }

    @Override
    public BaseComponent[] getHelpText() {
        return TextComponent.fromLegacyText("§fDestroys the closest hologram to you.");
    }

    @Override
    public void invoke(CommandSender sender, String... args) {
        if (RPGCore.inst().getHologramManager().deleteHologram(((Player) sender))) {
            String msg = RPGCore.inst().getLanguageManager().getTranslation("command_success");
            sender.sendMessage(msg);
        } else {
            String msg = RPGCore.inst().getLanguageManager().getTranslation("command_failed");
            sender.sendMessage(msg);
        }
    }

    @Override
    public List<String> suggest(CommandSender sender, String... args) {
        return null;
    }
}
