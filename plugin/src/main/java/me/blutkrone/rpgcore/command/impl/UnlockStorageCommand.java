package me.blutkrone.rpgcore.command.impl;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.command.AbstractCommand;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class UnlockStorageCommand extends AbstractCommand {
    @Override
    public boolean canUseCommand(CommandSender sender) {
        return sender.hasPermission("rpg.admin");
    }

    @Override
    public BaseComponent[] getHelpText() {
        return buildHelpText("§fplayer storage §8days", "§cUnlocks storage space for a player [ADMIN]",
                "§7Player: Whose storage do you want to unlock",
                "§7Storage: The storage that will be opened",
                "§8Days: Unlock for a limited duration instead");
    }

    @Override
    public void invoke(CommandSender sender, String... args) {
        if (args.length >= 3) {
            // grab the bukkit player
            Player player = Bukkit.getPlayer(args[1]);
            if (player == null) {
                sender.sendMessage("§cPlayer '" + args[1] + "' is not online!");
                return;
            }
            // grab the core player
            CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(player);
            if (core_player == null) {
                sender.sendMessage("§cPlayer '" + args[1] + "' is not registered!");
                return;
            }
            // when does the storage expire
            long timestamp;
            if (args.length >= 4) {
                timestamp = (long) (System.currentTimeMillis() + (1000L * 60L * 60L * 24L * Double.parseDouble(args[3])));
            } else {
                timestamp = -1;
            }
            // unlock the storage for the player
            core_player.getStorageUnlocked().put(args[2], timestamp);
            sender.sendMessage("§aPlayer storage was updated!");
        }
    }

    @Override
    public List<String> suggest(CommandSender sender, String... args) {
        return null;
    }
}
