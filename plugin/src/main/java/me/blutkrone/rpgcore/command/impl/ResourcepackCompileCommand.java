package me.blutkrone.rpgcore.command.impl;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.command.AbstractCommand;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class ResourcepackCompileCommand extends AbstractCommand {

    @Override
    public boolean canUseCommand(CommandSender sender) {
        return sender instanceof ConsoleCommandSender;
    }

    @Override
    public BaseComponent[] getHelpText() {
        return TextComponent.fromLegacyText("§fCompile core files into a resourcepack");
    }

    @Override
    public void invoke(CommandSender sender, String... args) {
        RPGCore.inst().getResourcePackManager().compile((result -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.setResourcePack(RPGCore.inst().getResourcePackManager().getDownloadLink());
                player.sendMessage(ChatColor.DARK_GRAY + "Reloading resourcepack ...");
            }
        }));
    }

    @Override
    public List<String> suggest(CommandSender sender, String... args) {
        return null;
    }
}
