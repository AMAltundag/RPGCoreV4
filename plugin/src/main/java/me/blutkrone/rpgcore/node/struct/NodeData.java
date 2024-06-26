package me.blutkrone.rpgcore.node.struct;

/**
 * Data used by a specific node, the exact implementation
 * depends on the node backing it up.
 */
public abstract class NodeData {

    /**
     * Called to abandon the node data being updated.
     */
    public abstract void abandon();
}
