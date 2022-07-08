package me.blutkrone.rpgcore.command.impl;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.command.AbstractCommand;
import me.blutkrone.rpgcore.util.ItemBuilder;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class ToolCommand extends AbstractCommand {

    private static ItemStack TOOL_COLLECTIBLE = ItemBuilder
            .of(Material.BLAZE_ROD)
            .enchant(Enchantment.DURABILITY, 1)
            .flag(ItemFlag.values())
            .name("§cTool: Node (Collectible)")
            .appendLore("§fCreate a new node by clicking atop a block.")
            .appendLore("§fDestroy an existing node by clicking it.")
            .appendLore("§bA RPGCore tool to create a collectible node.")
            .build();

    private static ItemStack TOOL_BOX = ItemBuilder
            .of(Material.BLAZE_ROD)
            .enchant(Enchantment.DURABILITY, 1)
            .flag(ItemFlag.values())
            .name("§cTool: Node (Box)")
            .appendLore("§fCreate a new node by clicking atop a block.")
            .appendLore("§fDestroy an existing node by clicking it.")
            .appendLore("§bA RPGCore tool to create a box node.")
            .build();

    private static ItemStack TOOL_SPAWNER = ItemBuilder
            .of(Material.BLAZE_ROD)
            .enchant(Enchantment.DURABILITY, 1)
            .flag(ItemFlag.values())
            .name("§cTool: Node (Spawner)")
            .appendLore("§fCreate a new node by clicking atop a block.")
            .appendLore("§fDestroy an existing node by clicking it.")
            .appendLore("§bA RPGCore tool to create a spawner node.")
            .build();

    private static ItemStack TOOL_NPC = ItemBuilder
            .of(Material.BLAZE_ROD)
            .enchant(Enchantment.DURABILITY, 1)
            .flag(ItemFlag.values())
            .name("§cTool: Node (NPC)")
            .appendLore("§fCreate a new node by clicking atop a block.")
            .appendLore("§fDestroy an existing node by clicking it.")
            .appendLore("§bA RPGCore tool to create a spawner node.")
            .build();

    /**
     * Look up if the given item has a tool associated with them.
     *
     * @param tool the tool to check for
     * @return the tool associated with it
     */
    public static String getTool(ItemStack tool) {
        if (tool == null || tool.getType().isAir()) {
            return null;
        }
        ItemMeta meta = tool.getItemMeta();
        if (meta == null) {
            return null;
        }
        PersistentDataContainer data = meta.getPersistentDataContainer();
        return data.get(new NamespacedKey(RPGCore.inst(), "tool-value"), PersistentDataType.STRING);
    }

    @Override
    public boolean canUseCommand(CommandSender sender) {
        return sender instanceof Player && sender.hasPermission("rpg.admin");
    }

    @Override
    public BaseComponent[] getHelpText() {
        return TextComponent.fromLegacyText("<type> <value> §fGive an itemized administration tool.");
    }

    @Override
    public void invoke(CommandSender sender, String... args) {
        if (args.length == 3) {
            ItemStack item = null;
            if (args[1].equalsIgnoreCase("collectible")) {
                item = ItemBuilder.of(TOOL_COLLECTIBLE.clone())
                        .name("§cTool: Node (Collectible, " + args[2] + ")")
                        .persist("tool-value", args[1] + " " + args[2])
                        .build();
            } else if (args[1].equalsIgnoreCase("box")) {
                item = ItemBuilder.of(TOOL_BOX.clone())
                        .name("§cTool: Node (Box, " + args[2] + ")")
                        .persist("tool-value", args[1] + " " + args[2])
                        .build();
            } else if (args[1].equalsIgnoreCase("spawner")) {
                item = ItemBuilder.of(TOOL_SPAWNER.clone())
                        .name("§cTool: Node (Spawner, " + args[2] + ")")
                        .persist("tool-value", args[1] + " " + args[2])
                        .build();
            } else if (args[1].equalsIgnoreCase("npc")) {
                item = ItemBuilder.of(TOOL_SPAWNER.clone())
                        .name("§cTool: Node (NPC, " + args[2] + ")")
                        .persist("tool-value", args[1] + " " + args[2])
                        .build();
            }

            if (item != null) {
                ((Player) sender).getInventory().addItem(item);
            }
        } else {
            sender.sendMessage("§cA tool needs exactly two arguments!");
        }
    }

    @Override
    public List<String> suggest(CommandSender sender, String... args) {
        if (args.length == 2) {
            List<String> suggests = new ArrayList<>();
            suggests.add("collectible");
            suggests.add("box");
            suggests.add("spawner");
            suggests.add("npc");
            suggests.removeIf(t -> !t.startsWith(args[1]));
            return suggests;
        }

        return null;
    }
}
