package me.blutkrone.rpgcore.node.impl;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.hud.editor.root.node.EditorNodeHotspot;
import me.blutkrone.rpgcore.nms.api.entity.IEntityCollider;
import me.blutkrone.rpgcore.nms.api.entity.IEntityVisual;
import me.blutkrone.rpgcore.node.struct.AbstractNode;
import me.blutkrone.rpgcore.node.struct.NodeActive;
import me.blutkrone.rpgcore.node.struct.NodeData;
import me.blutkrone.rpgcore.quest.CoreQuest;
import me.blutkrone.rpgcore.quest.task.AbstractQuestTask;
import me.blutkrone.rpgcore.quest.task.impl.CoreQuestTaskVisit;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.List;

public class CoreNodeHotspot extends AbstractNode {
    private static ItemStack NOTHING = ItemBuilder.of(Material.STONE_PICKAXE).model(0).build();

    public CoreNodeHotspot(String id, EditorNodeHotspot editor) {
        super(id, (int) editor.radius);
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
        } else {
            // update the quests of the player
            for (Player player : players) {
                CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(player);
                for (String id : core_player.getActiveQuestIds()) {
                    CoreQuest quest = RPGCore.inst().getQuestManager().getIndexQuest().get(id);
                    AbstractQuestTask task = quest.getCurrentTask(core_player);
                    if (task instanceof CoreQuestTaskVisit) {
                        ((CoreQuestTaskVisit) task).updateQuest(core_player, super.getId());
                    }
                }
            }
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
        private Reference<IEntityVisual> visual;
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
                collider.resize(1);
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
