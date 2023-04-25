package me.blutkrone.rpgcore.node.impl;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.api.activity.IActivity;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.hud.editor.bundle.item.EditorLoot;
import me.blutkrone.rpgcore.hud.editor.index.IndexAttachment;
import me.blutkrone.rpgcore.hud.editor.root.node.EditorNodeBox;
import me.blutkrone.rpgcore.item.CoreItem;
import me.blutkrone.rpgcore.nms.api.entity.IEntityCollider;
import me.blutkrone.rpgcore.nms.api.entity.IEntityVisual;
import me.blutkrone.rpgcore.node.struct.AbstractNode;
import me.blutkrone.rpgcore.node.struct.NodeActive;
import me.blutkrone.rpgcore.node.struct.NodeData;
import me.blutkrone.rpgcore.util.ItemBuilder;
import me.blutkrone.rpgcore.util.collection.WeightedRandomMap;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
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

    public class BoxNodeData extends NodeData {

        // identifier based on the location
        private String node_identifier;
        // when can the node be restocked
        private int cooldown_until;
        // physical entity representing us
        private Reference<IEntityVisual> visual;
        private Reference<IEntityCollider> collide;
        // location of the node
        private Location where;

        public BoxNodeData(World world, NodeActive node) {
            this.node_identifier = node.getID().toString();
            this.cooldown_until = 0;
            this.where = new Location(world, node.getX() + 0.5d, node.getY(), node.getZ() + 0.5d);
            this.visual = new WeakReference<>(null);
            this.collide = new WeakReference<>(null);
        }

        @Override
        public void abandon() {
            IEntityVisual entity = this.visual.get();
            if (entity != null) {
                entity.remove();
            }
            IEntityCollider collide = this.collide.get();
            if (collide != null) {
                collide.destroy();
            }
        }

        /*
         * Check if the collectible is available to be harvested.
         *
         * @return true if we are not on a cooldown
         */
        boolean isAvailable() {
            return RPGCore.inst().getTimestamp() >= this.cooldown_until;
        }

        /*
         * Update the model of our collectible.
         *
         * @param item the collectible we got
         */
        void update(ItemStack item) {
            if (NOTHING.isSimilar(item))
                item = new ItemStack(Material.AIR);
            IEntityVisual visual = getVisual();
            if (!item.isSimilar(visual.getItem(EquipmentSlot.HAND))) {
                visual.setItem(EquipmentSlot.HAND, item);
            }
        }

        /*
         * Retrieve the primary visual entity.
         *
         * @return what entity we are backing
         */
        IEntityVisual getVisual() {
            // fetch the existing model entity
            IEntityVisual visual = this.visual.get();
            // if the model entity broke, create a new one
            if (visual == null || !visual.isActive()) {
                visual = RPGCore.inst().getVolatileManager().createVisualEntity(this.where, false);
                visual.asBukkit().setMetadata("rpgcore-node", new FixedMetadataValue(RPGCore.inst(), this.node_identifier));
                this.visual = new WeakReference<>(visual);
            }
            // if the collider broke, create a new one
            IEntityCollider collider = this.collide.get();
            if (collider == null || !collider.isActive()) {
                collider = RPGCore.inst().getVolatileManager().createCollider(visual.asBukkit());
                collider.resize(collider_size);
                this.collide = new WeakReference<>(collider);
            }
            // establish a link
            collider.link(visual.asBukkit());
            collider.move(visual.asBukkit().getLocation());
            // offer up our model entity
            return visual;
        }
    }
}