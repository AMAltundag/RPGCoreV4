package me.blutkrone.rpgcore.node.struct;

import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * A node refers to a specific point on the map which
 * serve as a point-of-interest to a player.
 */
public abstract class AbstractNode {

    // unique identifier to edit the node
    private String id;
    // permission necessary to engage node
    private String permission;
    // maximum distance to 'tick' the node
    private int engage_radius;

    public AbstractNode(String id, String permission, int radius) {
        this.id = id;
        this.permission = permission;
        this.engage_radius = radius;
    }

    /**
     * The unique identifier of the node implementation.
     *
     * @return the node identifier.
     */
    public String getId() {
        return id;
    }

    /**
     * What permission is necessary to engage with the
     * node.
     *
     * @return the permission needed to engage.
     */
    public String getPermission() {
        return permission;
    }

    /**
     * The radius within which we can engage the ticking
     * of the node.
     *
     * @return maximum distance to player to tick.
     */
    public int getEngageRadius() {
        return engage_radius;
    }

    /**
     * Triggers regularly, may also trigger without players.
     *
     * @param world the world the node is located in.
     * @param active which node instance triggered this.
     * @param players all players within radius.
     * */
    public abstract void tick(World world, NodeActive active, List<Player> players);

    /**
     * Triggers when a player explicitly engages with the node.
     *
     * @param world the world the node is located in.
     * @param active which node instance triggered this.
     * @param player who engaged with it
     */
    public abstract void right(World world, NodeActive active, Player player);

    /**
     * Triggers when a player explicitly engages with the node.
     *
     * @param world the world the node is located in.
     * @param active which node instance triggered this.
     * @param player who engaged with it
     */
    public abstract void left(World world, NodeActive active, Player player);
}