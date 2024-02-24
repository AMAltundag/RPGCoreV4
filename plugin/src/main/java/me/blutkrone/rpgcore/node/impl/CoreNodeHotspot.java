package me.blutkrone.rpgcore.node.impl;

import me.blutkrone.rpgcore.editor.root.node.EditorNodeHotspot;
import me.blutkrone.rpgcore.node.struct.AbstractNode;
import me.blutkrone.rpgcore.node.struct.NodeActive;
import me.blutkrone.rpgcore.node.struct.NodeDataWithModel;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

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

    public class HotspotNodeData extends NodeDataWithModel {

        // basic information from the node
        private String node_identifier;
        private Location where;

        HotspotNodeData(World world, NodeActive node) {
            this.node_identifier = node.getID().toString();
            this.where = new Location(world, node.getX(), node.getY(), node.getZ());
        }

        @Override
        protected Location getWhere() {
            return this.where;
        }

        @Override
        protected double getColliderSize() {
            return 1d;
        }

        @Override
        protected String getNodeID() {
            return this.node_identifier;
        }
    }
}
