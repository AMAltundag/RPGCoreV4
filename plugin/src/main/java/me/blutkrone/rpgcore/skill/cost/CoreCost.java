package me.blutkrone.rpgcore.skill.cost;

import me.blutkrone.rpgcore.skill.SkillContext;

/**
 * A cost which is consumed to invoke a trigger.
 */
public abstract class CoreCost {

    /**
     * Verify if the given skill context can afford this cost.
     *
     * @param context the context we work within.
     * @return true if affordable
     */
    public abstract boolean canAfford(SkillContext context);

    /**
     * Consume the cost, no issue should arise if we cannot
     * afford this cost.
     *
     * @param context the context we work within.
     */
    public abstract void consumeCost(SkillContext context);
}
