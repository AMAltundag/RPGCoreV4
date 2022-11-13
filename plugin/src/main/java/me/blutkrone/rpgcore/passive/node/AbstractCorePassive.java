package me.blutkrone.rpgcore.passive.node;

import me.blutkrone.rpgcore.item.CoreItem;

public abstract class AbstractCorePassive {

    /**
     * Check if this passive is considered a socket passive.
     *
     * @return whether if we are a socket passive.
     */
    public abstract boolean isSocket();

    /**
     * Check if the given stack can be socketed into the tree.
     *
     * @param stack the stack we're socketing.
     * @return true if stack is compatible with passive.
     */
    public abstract boolean canSocket(CoreItem stack);
}
