package me.blutkrone.rpgcore.command.impl;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.command.AbstractCommand;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import net.md_5.bungee.api.chat.BaseComponent;
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
        return buildHelpText("§fplayer type amount", "§cAdd passive points [ADMIN]",
                "§7Player: §fWho will receive the points",
                "§7Type: What type of points to add",
                "§7Amount: How many points to add");
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
