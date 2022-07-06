package me.blutkrone.rpgcore.command.impl;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.command.AbstractCommand;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.item.CoreItem;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class AdminBoxCommand extends AbstractCommand {

    @Override
    public boolean canUseCommand(CommandSender sender) {
        return sender.hasPermission("rpg.admin") && sender instanceof Player;
    }

    @Override
    public BaseComponent[] getHelpText() {
        return TextComponent.fromLegacyText("§fContains a box of debug items.");
    }

    @Override
    public void invoke(CommandSender sender, String... args) {
        Inventory inv = Bukkit.createInventory(null, 6*9, "Admin Box");
        // inv.addItem(ItemBuilder.of(Material.NETHER_STAR)
        //         .name("§fEvolution Key 1")
        //         .lore("§fUnlocks up to 1 evolution key")
        //         .persist("skill-evolution-key", 1)
        //         .build());
        // inv.addItem(ItemBuilder.of(Material.NETHER_STAR)
        //         .name("§fEvolution Key 2")
        //         .lore("§fUnlocks up to 2 evolution key")
        //         .persist("skill-evolution-key", 2)
        //         .build());
        // inv.addItem(ItemBuilder.of(Material.NETHER_STAR)
        //         .name("§fEvolution Key 3")
        //         .lore("§fUnlocks up to 3 evolution key")
        //         .persist("skill-evolution-key", 3)
        //         .build());
        // inv.addItem(ItemBuilder.of(Material.DIAMOND)
        //         .name("§fEvolution:Alpha")
        //         .persist("unique", UUID.randomUUID().toString())
        //         .persist("skill-evolution-stone", "Alpha")
        //         .build());
        // inv.addItem(ItemBuilder.of(Material.DIAMOND)
        //         .name("§fEvolution:Beta")
        //         .persist("unique", UUID.randomUUID().toString())
        //         .persist("skill-evolution-stone", "Beta")
        //         .build());
        // inv.addItem(ItemBuilder.of(Material.DIAMOND)
        //         .name("§fEvolution:Gamma")
        //         .persist("unique", UUID.randomUUID().toString())
        //         .persist("skill-evolution-stone", "Gamma")
        //         .build());
        // inv.addItem(ItemBuilder.of(Material.GOLDEN_AXE)
        //         .name("§fMain Hand Item")
        //         .lore("§fthis is a debugging item.")
        //         .persist("equip-slot", "WEAPON")
        //         .build());
        // inv.addItem(ItemBuilder.of(Material.GOLDEN_HELMET)
        //         .name("§fHelmet Item")
        //         .lore("§fthis is a debugging item.")
        //         .persist("equip-slot", "HELMET")
        //         .build());
        // inv.addItem(ItemBuilder.of(Material.GOLDEN_LEGGINGS)
        //         .name("§fHelmet Item")
        //         .lore("§fthis is a debugging item.")
        //         .persist("equip-slot", "PANTS")
        //         .build());
        // inv.addItem(ItemBuilder.of(Material.PRISMARINE)
        //         .name("§fAmulet Item")
        //         .lore("§fthis is a debugging item.")
        //         .persist("unique", UUID.randomUUID().toString())
        //         .persist("equip-slot", "AMULET")
        //         .build());

        CorePlayer player = RPGCore.inst().getEntityManager().getPlayer(((Player) sender));
        if (player == null) {
            sender.sendMessage("§cNot initialized within RPGCore!");
            return;
        }

        List<CoreItem> items = new ArrayList<>(RPGCore.inst().getItemManager().getItemIndex().getAll());
        for (int i = 0; i < 6*9; i++) {
            inv.addItem(items.get(ThreadLocalRandom.current().nextInt(items.size())).acquire(player, 0d));
        }
        ((Player) sender).openInventory(inv);

        // evolution slot unlocking 1-5
        // evolution stone example

    }

    @Override
    public List<String> suggest(CommandSender sender, String... args) {
        return null;
    }
}
