package me.blutkrone.rpgcore.node.struct;

/**
 * Data used by a specific node, the exact implementation
 * depends on the node backing it up.
 */
public abstract class NodeData {

    /**
     * Highlight the node for a given duration.
     *
     * @param time how many ticks to highlight.
     */
    public abstract void highlight(int time);

    /**
     * Called to abandon the node data being updated.
     */
    public abstract void abandon();
}
