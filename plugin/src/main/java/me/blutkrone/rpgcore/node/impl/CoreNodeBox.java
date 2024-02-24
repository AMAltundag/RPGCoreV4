package me.blutkrone.rpgcore.node.impl;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.api.activity.IActivity;
import me.blutkrone.rpgcore.editor.bundle.item.EditorLoot;
import me.blutkrone.rpgcore.editor.index.IndexAttachment;
import me.blutkrone.rpgcore.editor.root.node.EditorNodeBox;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.item.CoreItem;
import me.blutkrone.rpgcore.node.struct.AbstractNode;
import me.blutkrone.rpgcore.node.struct.NodeActive;
import me.blutkrone.rpgcore.node.struct.NodeDataWithModel;
import me.blutkrone.rpgcore.util.ItemBuilder;
import me.blutkrone.rpgcore.util.collection.WeightedRandomMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class CoreNodeBox extends AbstractNode {

    private static ItemStack NOTHING = ItemBuilder.of(Material.STONE_PICKAXE).model(0).build();

    // which items can spawn in the box
    private IndexAttachment<CoreItem, WeightedRandomMap<CoreItem>> item_choices;
    // how many items can spawn in the box
    private int total;
    // cooldown before using box again
    private int cooldown;
    // collider size
    private int collider_size;
    // itemization at relevant stage
    private ItemStack available;
    private ItemStack unavailable;

    public CoreNodeBox(String id, EditorNodeBox editor) {
        super(id, (int) editor.radius, editor.getPreview());

        this.item_choices = EditorLoot.build(new ArrayList<>(editor.item_weight));
        this.total = (int) editor.item_total;
        this.cooldown = (int) editor.cooldown;
        this.collider_size = (int) editor.collide_size;
        this.available = ItemBuilder.of(editor.available_icon)
                .model((int) editor.available_model)
                .build();
        this.unavailable = ItemBuilder.of(editor.unavailable_icon)
                .model((int) editor.unavailable_model)
                .build();
    }

    @Override
    public void tick(World world, NodeActive active, List<Player> players) {
        // retrieve or create the node data
        BoxNodeData data = (BoxNodeData) active.getData();
        if (data == null) {
            active.setData(data = new BoxNodeData(world, active));
        }
        // drop the node should no player be nearby
        if (players.isEmpty()) {
            data.abandon();
            return;
        }
        // refresh appearance of the node
        data.update(data.isAvailable() ? available : unavailable);
    }

    @Override
    public void right(World world, NodeActive active, Player player) {
        // only core players can open a box
        CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(player);
        if (core_player == null) {
            return;
        }

        // make sure no other activity is being run
        IActivity activity = core_player.getActivity();
        if (activity != null) {
            return;
        }

        // retrieve or create the node data
        BoxNodeData data = (BoxNodeData) active.getData();
        if (data == null) {
            active.setData(data = new BoxNodeData(world, active));
        }

        // do not do anything while we are on cooldown
        if (!data.isAvailable()) {
            return;
        }

        // mark the box as having been consumed
        data.cooldown_until = RPGCore.inst().getTimestamp() + cooldown;
        data.update(unavailable);

        // give the player a box of random items
        showBox(player, core_player);
    }

    private void showBox(Player player, CorePlayer core_player) {
        // error in case no items could be discovered
        if (this.item_choices.get().isEmpty()) {
            player.sendMessage("§cUnexpected error (No items found!)");
            return;
        }

        // randomly shuffled list to populate the slots
        List<Integer> slots = new ArrayList<>();
        for (int i = 0; i < 6 * 9; i++) {
            slots.add(i);
        }

        // inventory to populate with items
        Inventory inventory = Bukkit.createInventory(null, 9 * 6, "");
        for (int i = 0; i < total && !slots.isEmpty(); i++) {
            int slot = slots.remove(ThreadLocalRandom.current().nextInt(slots.size()));
            CoreItem selected = this.item_choices.get().next();
            inventory.setItem(slot, selected.acquire(core_player, 0d));
        }

        // present the menu to the player
        player.openInventory(inventory);
    }

    @Override
    public void left(World world, NodeActive active, Player player) {
        // only a right click should open a box
    }

    public class BoxNodeData extends NodeDataWithModel {

        // basic information from the node
        private String node_identifier;
        private Location where;

        // when can the node be restocked
        private int cooldown_until;

        public BoxNodeData(World world, NodeActive node) {
            this.node_identifier = node.getID().toString();
            this.where = new Location(world, node.getX() + 0.5d, node.getY(), node.getZ() + 0.5d);
            this.cooldown_until = 0;
        }

        /**
         * Check if the collectible is available to be harvested.
         *
         * @return true if we are not on a cooldown
         */
        public boolean isAvailable() {
            return RPGCore.inst().getTimestamp() >= this.cooldown_until;
        }

        @Override
        protected Location getWhere() {
            return this.where;
        }

        @Override
        protected double getColliderSize() {
            return CoreNodeBox.this.collider_size;
        }

        @Override
        protected String getNodeID() {
            return this.node_identifier;
        }
    }
}