package me.blutkrone.rpgcore.node.impl;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.api.activity.IActivity;
import me.blutkrone.rpgcore.effect.CoreEffect;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.hud.editor.bundle.item.EditorLoot;
import me.blutkrone.rpgcore.hud.editor.index.IndexAttachment;
import me.blutkrone.rpgcore.hud.editor.root.node.EditorNodeCollectible;
import me.blutkrone.rpgcore.item.CoreItem;
import me.blutkrone.rpgcore.nms.api.entity.IEntityCollider;
import me.blutkrone.rpgcore.nms.api.entity.IEntityVisual;
import me.blutkrone.rpgcore.node.activity.ObserveCollectionActivity;
import me.blutkrone.rpgcore.node.struct.AbstractNode;
import me.blutkrone.rpgcore.node.struct.NodeActive;
import me.blutkrone.rpgcore.node.struct.NodeData;
import me.blutkrone.rpgcore.util.ItemBuilder;
import me.blutkrone.rpgcore.util.collection.WeightedRandomMap;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.*;

public class CoreNodeCollectible extends AbstractNode {

    private static ItemStack NOTHING = ItemBuilder.of(Material.STONE_PICKAXE).model(0).build();

    // which items can spawn in the box
    private IndexAttachment<CoreItem, WeightedRandomMap<CoreItem>> item_choices;
    // how many items can spawn in the box
    private int total;
    // restocking duration for the chest
    private int cooldown;
    // itemization at relevant stage
    private ItemStack available;
    private ItemStack unavailable;
    // collider size
    private int collider_size;
    // information about collection rate
    private int collect_volume;
    private String collect_attribute;
    private String lc_message;
    // effects played while collecting/breaking
    private String effect_collecting;
    private String effect_collected;

    public CoreNodeCollectible(String id, EditorNodeCollectible editor) {
        super(id, (int) editor.radius);

        this.item_choices = EditorLoot.build(new ArrayList<>(editor.item_weight));
        this.total = (int) editor.item_total;
        this.cooldown = (int) editor.cooldown;
        this.available = ItemBuilder.of(editor.available_icon)
                .model((int) editor.available_model)
                .build();
        this.unavailable = ItemBuilder.of(editor.unavailable_icon)
                .model((int) editor.unavailable_model)
                .build();
        this.collider_size = (int) editor.collide_size;
        this.collect_volume = (int) editor.collect_volume;
        this.collect_attribute = editor.collect_attribute;
        this.lc_message = editor.lc_message;
        this.effect_collected = editor.effect_collected;
        this.effect_collecting = editor.effect_collecting;
    }

    @Override
    public void tick(World world, NodeActive active, List<Player> players) {
        // retrieve or create the node data
        CollectibleNodeData data = (CollectibleNodeData) active.getData();
        if (data == null) {
            active.setData(data = new CollectibleNodeData(world, active));
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
    public void left(World world, NodeActive active, Player player) {
        this.collect(world, active, player);
    }

    @Override
    public void right(World world, NodeActive active, Player player) {
        this.collect(world, active, player);
    }

    /*
     * Utility method which handles the attempt of collecting from
     * a node.
     *
     * @param world which world is the node in
     * @param active which node are we trying to collect
     * @param player who tries to collect the node
     */
    private void collect(World world, NodeActive active, Player player) {
        // only core players can harvest a node
        CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(player);
        if (core_player == null) {
            return;
        }

        // make sure no other activity is being run
        IActivity activity = core_player.getActivity();
        if (activity != null) {
            if (!(activity instanceof ObserveCollectionActivity)) {
                return;
            }
        }

        // retrieve or create the node data
        CollectibleNodeData data = (CollectibleNodeData) active.getData();
        if (data == null) {
            active.setData(data = new CollectibleNodeData(world, active));
        }

        // do not do anything while we are on cooldown
        if (!data.isAvailable()) {
            return;
        }

        // observe the harvesting of this node
        if (activity == null) {
            core_player.setActivity(activity = new ObserveCollectionActivity(data));
        }
        ((ObserveCollectionActivity) activity).focus(data);

        // update collection rating of the node
        if (RPGCore.inst().getTimestamp() >= data.collecting_expire) {
            data.have_collected = 0d;
        }
        data.collecting_expire = RPGCore.inst().getTimestamp() + 40;
        data.collecting.add(player.getUniqueId());
        double collection = 1d;
        if (!this.collect_attribute.equalsIgnoreCase("nothingness")) {
            collection += core_player.evaluateAttribute(this.collect_attribute);
        }
        data.have_collected += collection;

        // scatter particles from collection
        if (!this.effect_collecting.equalsIgnoreCase("NOTHINGNESS")) {
            // retrieve the effect we want to operate
            CoreEffect effect = RPGCore.inst().getEffectManager().getIndex().get(this.effect_collecting);
            // show the effect for collecting the node
            for (int i = 0; i < 5; i++) {
                // sample randomly around collider level
                Location where = new Location(player.getWorld(), active.getX(), active.getY(), active.getZ());
                where.add(new Vector(Math.random() * 2 - 1, Math.random() * 2 - 1, Math.random() * 2 - 1).multiply((0d + this.collider_size) / 2));
                // invoke the effect that we retrieved
                effect.show(where, player);
            }
        }

        // if we are not collection capped, we are done
        if (data.have_collected < this.collect_volume) {
            return;
        }

        // particle explosion since we collected the node
        if (!this.effect_collected.equalsIgnoreCase("NOTHINGNESS")) {
            // retrieve the effect we want to operate
            CoreEffect effect = RPGCore.inst().getEffectManager().getIndex().get(this.effect_collected);
            // show the effect for collecting the node
            for (int i = 0; i < 5; i++) {
                // sample randomly around collider level
                Location where = new Location(player.getWorld(), active.getX(), active.getY(), active.getZ());
                where.add(new Vector(Math.random() * 2 - 1, Math.random() * 2 - 1, Math.random() * 2 - 1).multiply((0d + this.collider_size) / 2));
                // invoke the effect that we retrieved
                effect.show(where, player);
            }
        }

        for (int i = 0; i < 20; i++) {
            Location where = new Location(player.getWorld(), active.getX(), active.getY(), active.getZ());
            where.add(new Vector(Math.random() * 2 - 1, Math.random() * 2 - 1, Math.random() * 2 - 1).multiply(((0d + this.collider_size) / 2) + 2));
            player.spawnParticle(Particle.BLOCK_CRACK, where, 7, Material.DIAMOND_ORE.createBlockData());
        }

        // mark the node as being exploited
        data.cooldown_until = RPGCore.inst().getTimestamp() + this.cooldown;
        data.update(unavailable);

        // scatter items from the node
        if (!this.item_choices.get().isEmpty()) {
            Location scatter_point = new Location(world, active.getX(), active.getY(), active.getZ());
            for (int i = 0; i < this.total * Math.sqrt(data.collecting.size()); i++) {
                ItemStack stack = this.item_choices.get().next().acquire(core_player, 0d);
                world.dropItem(scatter_point, stack, (item -> {
                    item.setVelocity(new Vector(Math.random() * 2 - 1, 0.5d, Math.random() * 2 - 1).multiply(0.25d));
                }));
            }
        }

        // reset the collection data
        data.have_collected = 0;
        data.collecting.clear();
        data.collecting_expire = 0;
    }

    public class CollectibleNodeData extends NodeData {
        // identifier based on the location
        private String node_identifier;
        // when can the node be restocked
        private int cooldown_until;
        // physical entity representing us
        private Reference<IEntityVisual> visual;
        private Reference<IEntityCollider> collide;
        // location of the node
        private Location where;
        // crack-age of the item
        private double have_collected = 0;
        private Set<UUID> collecting = new HashSet<>();
        private int collecting_expire = 0;

        CollectibleNodeData(World world, NodeActive node) {
            this.node_identifier = node.getID().toString();
            this.cooldown_until = 0;
            this.where = new Location(world, node.getX(), node.getY(), node.getZ());
            this.visual = new WeakReference<>(null);
            this.collide = new WeakReference<>(null);
        }

        /**
         * Progress of collecting the current node instance.
         *
         * @return the collection progress.
         */
        public double getProgress() {
            return Math.max(0d, Math.min(1d, (0d + this.have_collected) / CoreNodeCollectible.this.collect_volume));
        }

        /**
         * The message used for the activity while collecting.
         *
         * @return the collection message.
         */
        public String getMessage() {
            return CoreNodeCollectible.this.lc_message;
        }

        /**
         * Check if the collectible is available to be harvested.
         *
         * @return true if we are not on a cooldown
         */
        public boolean isAvailable() {
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

        @Override
        public void highlight(int time) {
            IEntityCollider collider_entity = this.collide.get();
            if (collider_entity == null) {
                return;
            }
            collider_entity.highlight(30);
        }

        @Override
        public void abandon() {
            // clean up the model of the collectible object
            IEntityVisual entity = this.visual.get();
            if (entity != null) {
                entity.remove();
            }
            IEntityCollider collide = this.collide.get();
            if (collide != null) {
                collide.destroy();
            }
        }
    }
}