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

public class ExpCommand extends AbstractCommand {

    @Override
    public boolean canUseCommand(CommandSender sender) {
        return sender.isOp();
    }

    @Override
    public BaseComponent[] getHelpText() {
        return TextComponent.fromLegacyText("<*player> <amount> §fGrants exp either to you, or a player");
    }

    @Override
    public void invoke(CommandSender sender, String... args) {
        if (args.length == 3) {
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
            core_player.setCurrentExp(core_player.getCurrentExp() + Double.parseDouble(args[2]));

            // inform about success
            String msg = RPGCore.inst().getLanguageManager().getTranslation("command_success");
            sender.sendMessage(msg);
        } else if (args.length == 2) {
            CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(((Player) sender));
            if (core_player == null) {
                String msg = RPGCore.inst().getLanguageManager().getTranslation("command_failed");
                sender.sendMessage(msg);
                return;
            }
            core_player.setCurrentExp(core_player.getCurrentExp() + Double.parseDouble(args[1]));

            // inform about success
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
