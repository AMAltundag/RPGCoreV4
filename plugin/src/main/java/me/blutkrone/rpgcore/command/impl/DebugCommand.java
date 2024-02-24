package me.blutkrone.rpgcore.command.impl;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.command.AbstractCommand;
import me.blutkrone.rpgcore.nms.api.packet.handle.IItemDisplay;
import me.blutkrone.rpgcore.nms.api.packet.wrapper.VolatileBillboard;
import me.blutkrone.rpgcore.nms.api.packet.wrapper.VolatileDisplay;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class DebugCommand extends AbstractCommand {

    public DebugCommand() {

    }

    @Override
    public boolean canUseCommand(CommandSender sender) {
        return sender.hasPermission("rpg.admin");
    }

    @Override
    public BaseComponent[] getHelpText() {
        return new BaseComponent[0];
    }

    @Override
    public void invoke(CommandSender sender, String... args) {
    }

    @Override
    public List<String> suggest(CommandSender sender, String... args) {
        return null;
    }
}
