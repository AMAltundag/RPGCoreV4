package me.blutkrone.rpgcore.command.impl;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.command.AbstractCommand;
import me.blutkrone.rpgcore.editor.index.IndexAttachment;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.item.CoreItem;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class RandomItemBoxCommand extends AbstractCommand {

    private IndexAttachment<CoreItem, List<CoreItem>> attachment_any_item = RPGCore.inst().getItemManager().getItemIndex().createFiltered(item -> true);

    @Override
    public boolean canUseCommand(CommandSender sender) {
        return sender.hasPermission("rpg.admin") && sender instanceof Player;
    }

    @Override
    public BaseComponent[] getHelpText() {
        return buildHelpText("", "§cA double-chest full of random items [ADMIN]");
    }

    @Override
    public void invoke(CommandSender sender, String... args) {
        Inventory inv = Bukkit.createInventory(null, 6 * 9, "Admin Box");

        CorePlayer player = RPGCore.inst().getEntityManager().getPlayer(((Player) sender));
        if (player == null) {
            sender.sendMessage("§cNot initialized within RPGCore!");
            return;
        }

        List<CoreItem> items = attachment_any_item.get();
        for (int i = 0; i < 6 * 9; i++) {
            inv.addItem(items.get(ThreadLocalRandom.current().nextInt(items.size())).acquire(player, 0d));
        }
        ((Player) sender).openInventory(inv);
    }

    @Override
    public List<String> suggest(CommandSender sender, String... args) {
        return null;
    }
}
