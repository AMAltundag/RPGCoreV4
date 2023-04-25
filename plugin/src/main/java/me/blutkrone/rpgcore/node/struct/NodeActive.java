package me.blutkrone.rpgcore.node.struct;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.nms.api.packet.handle.IHighlight;
import me.blutkrone.rpgcore.node.NodeManager;

import java.util.UUID;

public class NodeActive {
    // identifier of the node
    private final UUID uuid;
    // exact location of the node
    private final int x, y, z;
    // an identifier of the backing node
    private final String node;

    private transient NodeData data;
    private transient IHighlight highlight;

    /**
     * A wrapper that associates a node with a location, the node
     * has a lazy addressing to respect editor mutability.
     *
     * @param uuid unique identifier of the node
     * @param x    position
     * @param y    position
     * @param z    position
     * @param node type:id
     */
    public NodeActive(UUID uuid, int x, int y, int z, String node) {
        this.uuid = uuid;
        this.x = x;
        this.y = y;
        this.z = z;
        this.node = node;
    }

    /**
     * Internal identifier of the node.
     *
     * @return Internal node identifier.
     */
    public String getRawNode() {
        return this.node;
    }

    /**
     * Retrieve the highlighter for this node.
     *
     * @return Highlighter
     */
    public IHighlight getHighlight() {
        if (this.highlight == null) {
            this.highlight = RPGCore.inst().getVolatileManager().getPackets().highlight(x, y, z);
        }

        return this.highlight;
    }

    /**
     * Data used for the current server session.
     *
     * @return the data, null if not setup.
     */
    public NodeData getData() {
        return data;
    }

    /**
     * Data used for the current server session.
     *
     * @param data new data to use.
     */
    public void setData(NodeData data) {
        this.data = data;
    }

    /**
     * Fetch the node instance which contains actual logic.
     *
     * @return the node which we are backed up by.
     */
    public AbstractNode getNode() {
        NodeManager node_manager = RPGCore.inst().getNodeManager();

        String[] id = this.node.split("\\:");
        if (id[0].equalsIgnoreCase("box")) {
            return node_manager.getIndexBox().get(id[1]);
        } else if (id[0].equalsIgnoreCase("collectible")) {
            return node_manager.getIndexCollectible().get(id[1]);
        } else if (id[0].equalsIgnoreCase("spawner")) {
            return node_manager.getIndexSpawner().get(id[1]);
        } else if (id[0].equalsIgnoreCase("hotspot")) {
            return node_manager.getIndexHotspot().get(id[1]);
        } else if (id[0].equalsIgnoreCase("npc")) {
            return RPGCore.inst().getNPCManager().getIndex().get(id[1]);
        } else if (id[0].equalsIgnoreCase("gate")) {
            return node_manager.getIndexGate().get(id[1]);
        }

        throw new IllegalArgumentException("Bad node: '" + this.node + "'!");
    }

    /**
     * Location of the node.
     *
     * @return position
     */
    public int getX() {
        return x;
    }

    /**
     * Location of the node.
     *
     * @return position
     */
    public int getY() {
        return y;
    }

    /**
     * Location of the node.
     *
     * @return position
     */
    public int getZ() {
        return z;
    }

    /**
     * Retrieve the identifier associated with this node instance.
     *
     * @return unique identifier.
     */
    public UUID getID() {
        return uuid;
    }
}
