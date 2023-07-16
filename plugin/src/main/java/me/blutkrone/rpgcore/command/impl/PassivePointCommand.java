package me.blutkrone.rpgcore.command.impl;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.command.AbstractCommand;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class PassivePointCommand extends AbstractCommand {

    @Override
    public boolean canUseCommand(CommandSender sender) {
        return sender.hasPermission("rpg.admin");
    }

    @Override
    public BaseComponent[] getHelpText() {
        return TextComponent.fromLegacyText("<player> <type> <amount> §fGrants number of passive points");
    }

    @Override
    public void invoke(CommandSender sender, String... args) {
        if (args.length == 4) {
            Player player = Bukkit.getPlayer(args[1]);
            if (player == null) {
                String msg = RPGCore.inst().getLanguageManager().getTranslation("command_failed");
                sender.sendMessage(msg);
                return;
            }
            CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(player);
            if (core_player == null) {
                String msg = RPGCore.inst().getLanguageManager().getTranslation("command_failed");
                sender.sendMessage(msg);
                return;
            }

            core_player.getPassivePoints().merge(args[2], Integer.parseInt(args[3]), (a, b) -> Math.max(0, a + b));

            // inform about success
            String msg = RPGCore.inst().getLanguageManager().getTranslation("command_success");
            sender.sendMessage(msg);
        }
    }

    @Override
    public List<String> suggest(CommandSender sender, String... args) {
        return null;
    }
}
