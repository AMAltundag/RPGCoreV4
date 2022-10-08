package me.blutkrone.rpgcore.skill.cost;

import me.blutkrone.rpgcore.api.IContext;

/**
 * A cost which is consumed to invoke a trigger.
 */
public abstract class AbstractCoreCost {

    /**
     * Verify if the given skill context can afford this cost.
     *
     * @param context the context we work within.
     * @return true if affordable
     */
    public abstract boolean canAfford(IContext context);

    /**
     * Consume the cost, no issue should arise if we cannot
     * afford this cost.
     *
     * @param context the context we work within.
     */
    public abstract void consumeCost(IContext context);
}
