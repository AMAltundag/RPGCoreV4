package me.blutkrone.rpgcore.node.struct;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.util.EntityReference;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

public abstract class NodeDataWithModel extends NodeData {

    private static ItemStack NOTHING = ItemBuilder.of(Material.STONE_PICKAXE).model(0).build();

    // visual entity holds a model
    private EntityReference<ItemDisplay> visual_entity;
    // collider entity provides hitbox
    private EntityReference<Interaction> collider_entity;

    public NodeDataWithModel() {
        this.visual_entity = new EntityReference<>();
        this.collider_entity = new EntityReference<>();
    }

    /**
     * Location of this node.
     *
     * @return Where is the node located.
     */
    protected abstract Location getWhere();

    /**
     * Size of the collider we want.
     *
     * @return Collider size.
     */
    protected abstract double getColliderSize();

    /**
     * Distinctive ID of the node.
     *
     * @return Node ID
     */
    protected abstract String getNodeID();

    /**
     * Update the model of our collectible.
     *
     * @param item the collectible we got
     */
    public void update(ItemStack item) {
        // wrapper against undefined items
        if (NodeDataWithModel.NOTHING.isSimilar(item)) {
            item = new ItemStack(Material.AIR);
        }
        // update the item to be shown
        ItemStack last_item = getVisual().getItemStack();
        if (last_item == null || !last_item.isSimilar(item)) {
            getVisual().setItemStack(item);
        }
    }

    /**
     * Retrieve the entity serving as a visual indicator.
     *
     * @return Visual indicator.
     */
    protected ItemDisplay getVisual() {
        Location where = getWhere();
        World world = where.getWorld();
        if (world == null) {
            throw new NullPointerException("World cannot be null!");
        }

        // initialize absent visual entity
        ItemDisplay visual = this.visual_entity.get();
        if (visual == null) {
            visual = world.spawn(where.clone().add(0d, 0.5d, 0d), ItemDisplay.class);
            visual.setPersistent(false);
            visual.setMetadata("rpgcore-node", new FixedMetadataValue(RPGCore.inst(), this.getNodeID()));
            visual.setBillboard(Display.Billboard.FIXED);
            visual.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.FIXED);
            visual.setInvulnerable(true);
            this.visual_entity = new EntityReference<>(visual);
        }

        // initialize absent collider entity
        Interaction collider = this.collider_entity.get();
        if (collider == null) {
            collider = world.spawn(where.clone().add(0d, 0.5d, 0d), Interaction.class);
            collider.setPersistent(false);
            collider.setInteractionHeight((float) getColliderSize());
            collider.setInteractionWidth((float) getColliderSize());
            Bukkit.getLogger().severe("COLLIDER SIZE: " + getColliderSize() + " " + this.getNodeID());
            collider.setInvulnerable(true);
            collider.setMetadata("rpgcore-node", new FixedMetadataValue(RPGCore.inst(), this.getNodeID()));
            this.collider_entity = new EntityReference<>(collider);
        }

        // offer up our model entity
        return visual;
    }

    @Override
    public void abandon() {
        ItemDisplay entity = this.visual_entity.get();
        if (entity != null) {
            entity.remove();
        }

        Interaction collide = this.collider_entity.get();
        if (collide != null) {
            collide.remove();
        }
    }
}
