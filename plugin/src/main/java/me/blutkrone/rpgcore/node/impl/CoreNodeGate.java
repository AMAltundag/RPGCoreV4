package me.blutkrone.rpgcore.node.impl;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.api.social.IPartySnapshot;
import me.blutkrone.rpgcore.dungeon.CoreDungeon;
import me.blutkrone.rpgcore.editor.instruction.InstructionBuilder;
import me.blutkrone.rpgcore.editor.root.node.EditorNodeGate;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.menu.AbstractCoreMenu;
import me.blutkrone.rpgcore.nms.api.menu.IChestMenu;
import me.blutkrone.rpgcore.node.struct.AbstractNode;
import me.blutkrone.rpgcore.node.struct.NodeActive;
import me.blutkrone.rpgcore.util.fontmagic.MagicStringBuilder;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CoreNodeGate extends AbstractNode {

    private final List<String> content;

    public CoreNodeGate(String id, EditorNodeGate editor) {
        super(id, (int) editor.radius, editor.getPreview());
        this.content = new ArrayList<>(editor.content);
    }

    @Override
    public void tick(World world, NodeActive active, List<Player> players) {
        for (Player player : players) {
            // ignore while already in queue
            if (RPGCore.inst().getSocialManager().getGroupHandler().isQueued(player)) {
                continue;
            }
            // ensure registered within core
            CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(player);
            if (core_player == null) {
                continue;
            }
            // ensure no menu is open
            if (player.getOpenInventory().getType() != InventoryType.CRAFTING) {
                continue;
            }
            // leader or solo player can queue
            IPartySnapshot snapshot = RPGCore.inst().getSocialManager().getGroupHandler().getPartySnapshot(player);
            if (snapshot == null || snapshot.isLeader(player)) {
                // verify which content types are fine
                List<String> content = new ArrayList<>();
                for (String id : this.content) {
                    CoreDungeon template = RPGCore.inst().getDungeonManager().getDungeonIndex().get(id);
                    if (template.canAccess(core_player)) {
                        content.add(id);
                    }
                }
                // present content menu to player
                if (!content.isEmpty()) {
                    new MatchQueueMenu(content).finish(player);
                }
            } else {
                RPGCore.inst().getLanguageManager().sendMessage(player, "not_leader_matchmaker");
            }
        }
    }

    @Override
    public void right(World world, NodeActive active, Player player) {

    }

    @Override
    public void left(World world, NodeActive active, Player player) {

    }

    private class MatchQueueMenu extends AbstractCoreMenu {

        private List<String> content;
        private int offset = 0;

        private Set<String> interested = new HashSet<>();
        private int dirty_time = 0;

        public MatchQueueMenu(List<String> content) {
            super(6);
            this.content = content;
        }

        @Override
        public void rebuild() {
            // base texture
            MagicStringBuilder msb = new MagicStringBuilder();
            msb.shiftToExact(-208);
            msb.append(resourcepack().texture("menu_scroller_grid"), ChatColor.WHITE);

            // render viewport and place items
            for (int i = 0; i < 6; i++) {
                for (int j = 0; j < 8; j++) {
                    int k = (i + offset) * 8 + j;
                    if (k < content.size()) {
                        String content = this.content.get(i);
                        CoreDungeon template = RPGCore.inst().getDungeonManager().getDungeonIndex().get(content);
                        ItemStack icon = RPGCore.inst().getLanguageManager().getAsItem(template.getLcIcon()).build();
                        IChestMenu.setBrand(icon, RPGCore.inst(), "content-id", content);
                        // place clickable item we are using
                        this.getMenu().setItemAt(i * 9 + j, icon);
                    }
                }
            }

            // render scroll-bar for the viewport
            msb.shiftToExact(150);
            if (content.size() <= 48) {
                msb.append(resourcepack().texture("pointer_huge_0"), ChatColor.WHITE);
            } else if (content.size() <= 96) {
                double length = Math.ceil(content.size() / 8d) - 6d;
                double ratio = offset / length;
                msb.append(resourcepack().texture("pointer_medium_" + (int) (100 * ratio)), ChatColor.WHITE);
            } else if (content.size() <= 192) {
                double length = Math.ceil(content.size() / 8d) - 6d;
                double ratio = offset / length;
                msb.append(resourcepack().texture("pointer_small_" + (int) (100 * ratio)), ChatColor.WHITE);
            } else {
                double length = Math.ceil(content.size() / 8d) - 6d;
                double ratio = offset / length;
                msb.append(resourcepack().texture("pointer_tiny_" + (int) (100 * ratio)), ChatColor.WHITE);
            }

            InstructionBuilder instructions = new InstructionBuilder();
            instructions.add(RPGCore.inst().getLanguageManager().getTranslationList("instruction_player_matchmaker"));
            instructions.apply(msb);

            this.getMenu().setTitle(msb.compile());
        }

        @Override
        public void click(InventoryClickEvent event) {
            event.setCancelled(true);

            if (event.getView().getTopInventory() == event.getClickedInventory()) {
                if (event.getSlot() == 8) {
                    // scroll up by one
                    this.offset = Math.max(0, this.offset - 1);
                    this.getMenu().queryRebuild();
                } else if (event.getSlot() == 17) {
                    // scroll to top
                    this.offset = 0;
                    this.getMenu().queryRebuild();
                } else if (event.getSlot() == 26) {
                    // ignore other clicks
                } else if (event.getSlot() == 35) {
                    // ignore other clicks
                } else if (event.getSlot() == 44) {
                    // scroll to bottom
                    this.offset = (content.size() / 8) - 6;
                    this.getMenu().queryRebuild();
                } else if (event.getSlot() == 53) {
                    // scroll down by one
                    int floor = Math.max(0, (content.size() / 8) - 6);
                    this.offset = Math.min(floor, this.offset + 1);
                    this.getMenu().queryRebuild();
                } else if (event.getCurrentItem() != null) {
                    String content = IChestMenu.getBrand(event.getCurrentItem(), RPGCore.inst(), "content-id", null);
                    if (content != null) {
                        if (this.interested.add(content)) {
                            // we are now queue-ing up
                            event.getCurrentItem().addUnsafeEnchantment(Enchantment.DURABILITY, 1);
                            this.dirty_time = 50;
                        } else if (this.interested.remove(content)) {
                            // we are now queue-ing off
                            event.getCurrentItem().removeEnchantment(Enchantment.DURABILITY);
                            this.dirty_time = 20;
                        }
                    }
                }
            }
        }

        @Override
        public boolean isTrivial() {
            return true;
        }

        @Override
        public void tick() {
            // update queue if marked as dirty
            if (this.dirty_time > 0 && --this.dirty_time == 0) {
                RPGCore.inst().getSocialManager().getGroupHandler()
                        .queueForContent(this.getMenu().getViewer(), this.interested.toArray(new String[0]));
            }
        }

        @Override
        public void close(InventoryCloseEvent event) {
            // update queue if marked as dirty
            if (this.dirty_time != 0) {
                Bukkit.getScheduler().runTask(RPGCore.inst(), () -> {
                    RPGCore.inst().getSocialManager().getGroupHandler()
                            .queueForContent(this.getMenu().getViewer(), this.interested.toArray(new String[0]));
                });
                this.dirty_time = 0;
            }
        }
    }
}