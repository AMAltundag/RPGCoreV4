package me.blutkrone.rpgcore.command.impl;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.command.AbstractCommand;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class SocialCommand extends AbstractCommand {

    @Override
    public boolean canUseCommand(CommandSender sender) {
        return sender instanceof Player;
    }

    @Override
    public BaseComponent[] getHelpText() {
        return TextComponent.fromLegacyText("<player> §fSocial menu for target player");
    }

    @Override
    public void invoke(CommandSender sender, String... args) {
        if (args.length == 2) {
            OfflinePlayer offline = Bukkit.getOfflinePlayer(args[0]);
            RPGCore.inst().getHUDManager().getSocialMenu().present(((Player) sender), offline.getUniqueId());
        }
    }

    @Override
    public List<String> suggest(CommandSender sender, String... args) {
        return null;
    }
}
