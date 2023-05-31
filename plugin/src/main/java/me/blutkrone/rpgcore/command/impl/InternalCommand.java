package me.blutkrone.rpgcore.command.impl;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.command.AbstractCommand;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class InternalCommand extends AbstractCommand {
    @Override
    public boolean canUseCommand(CommandSender sender) {
        return sender instanceof Player;
    }

    @Override
    public BaseComponent[] getHelpText() {
        return new BaseComponent[0];
    }

    @Override
    public void invoke(CommandSender sender, String... args) {
        try {
            if (args[1].equals("revive")) {
                // chat prompt to revive was accepted
                CorePlayer player = RPGCore.inst().getEntityManager().getPlayer(((Player) sender));
                if (player != null) {
                    player.revive();
                }
            }
        } catch (Exception e) {

        }
    }

    @Override
    public List<String> suggest(CommandSender sender, String... args) {
        return null;
    }
}
