package me.blutkrone.rpgcore.node.impl;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.editor.root.node.EditorNodeHotspot;
import me.blutkrone.rpgcore.nms.api.entity.IEntityCollider;
import me.blutkrone.rpgcore.node.struct.AbstractNode;
import me.blutkrone.rpgcore.node.struct.NodeActive;
import me.blutkrone.rpgcore.node.struct.NodeData;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.List;

public class CoreNodeHotspot extends AbstractNode {
    private static ItemStack NOTHING = ItemBuilder.of(Material.STONE_PICKAXE).model(0).build();

    public CoreNodeHotspot(String id, EditorNodeHotspot editor) {
        super(id, (int) editor.radius, editor.getPreview());
    }

    @Override
    public void tick(World world, NodeActive active, List<Player> players) {
        // retrieve or create the node data
        HotspotNodeData data = (HotspotNodeData) active.getData();
        if (data == null) {
            active.setData(data = new HotspotNodeData(world, active));
        }

        if (players.isEmpty()) {
            // drop the node should no player be nearby
            data.abandon();
        }
    }

    @Override
    public void right(World world, NodeActive active, Player player) {

    }

    @Override
    public void left(World world, NodeActive active, Player player) {

    }

    public class HotspotNodeData extends NodeData {
        // identifier based on the location
        private String node_identifier;
        // physical entity representing us
        private Reference<ItemDisplay> visual;
        private Reference<IEntityCollider> collide;
        // location of the node
        private Location where;

        HotspotNodeData(World world, NodeActive node) {
            this.node_identifier = node.getID().toString();
            this.where = new Location(world, node.getX(), node.getY(), node.getZ());
            this.visual = new WeakReference<>(null);
            this.collide = new WeakReference<>(null);
        }

        /*
         * Update the model of our collectible.
         *
         * @param item the collectible we got
         */
        void update(ItemStack item) {
            if (NOTHING.isSimilar(item))
                item = new ItemStack(Material.AIR);
            ItemDisplay visual = getVisual();
            if (!item.isSimilar(visual.getItemStack())) {
                visual.setItemStack(item);
            }
        }

        /*
         * Retrieve the primary visual entity.
         *
         * @return what entity we are backing
         */
        ItemDisplay getVisual() {
            // fetch the existing model entity
            ItemDisplay visual = this.visual.get();
            // if the model entity broke, create a new one
            if (visual == null || !visual.isValid()) {
                visual = (ItemDisplay) this.where.getWorld().spawnEntity(where.clone().add(0d, 0.5d, 0d), EntityType.ITEM_DISPLAY);
                visual.setPersistent(false);
                visual.setMetadata("rpgcore-node", new FixedMetadataValue(RPGCore.inst(), this.node_identifier));
                visual.setBillboard(Display.Billboard.FIXED);
                visual.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.FIXED);
                this.visual = new WeakReference<>(visual);
            }
            // if the collider broke, create a new one
            IEntityCollider collider = this.collide.get();
            if (collider == null || !collider.isActive()) {
                collider = RPGCore.inst().getVolatileManager().createCollider(visual);
                collider.resize(1);
                this.collide = new WeakReference<>(collider);
            }
            // establish a link
            collider.link(visual);
            collider.move(visual.getLocation());
            // offer up our model entity
            return visual;
        }

        @Override
        public void abandon() {
            // clean up the model of the collectible object
            ItemDisplay entity = this.visual.get();
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
