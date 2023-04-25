package me.blutkrone.rpgcore.command.impl;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.command.AbstractCommand;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class DungeonExitCommand extends AbstractCommand {

    @Override
    public boolean canUseCommand(CommandSender sender) {
        return sender instanceof Player;
    }

    @Override
    public BaseComponent[] getHelpText() {
        return TextComponent.fromLegacyText("§fExit the current dungeon");
    }

    @Override
    public void invoke(CommandSender sender, String... args) {
        if (RPGCore.inst().getDungeonManager().getInstance(((Player) sender).getWorld()) != null) {
            CorePlayer player = RPGCore.inst().getEntityManager().getPlayer(((Player) sender));
            player.getEntity().teleport(player.getRespawnPosition());
        }
    }

    @Override
    public List<String> suggest(CommandSender sender, String... args) {
        return null;
    }
}
