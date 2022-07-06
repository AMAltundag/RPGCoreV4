package me.blutkrone.rpgcore.command.impl;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.command.AbstractCommand;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class EquipCommand extends AbstractCommand {

    @Override
    public boolean canUseCommand(CommandSender sender) {
        return sender instanceof Player;
    }

    @Override
    public BaseComponent[] getHelpText() {
        return TextComponent.fromLegacyText("§fConfigure your skills");
    }

    @Override
    public void invoke(CommandSender sender, String... args) {
        RPGCore.inst().getHUDManager().getEquipMenu().open(((Player) sender));
    }

    @Override
    public List<String> suggest(CommandSender sender, String... args) {
        return null;
    }
}
