package me.blutkrone.rpgcore.skill.mechanic;

import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.api.IOrigin;

import java.util.List;

public abstract class AbstractCoreMechanic {

    /**
     * Apply the mechanic to all targets.
     *
     * @param context the context to evaluate within.
     * @param targets targets which are affected by the mechanic.
     */
    public abstract void doMechanic(IContext context, List<IOrigin> targets);
}
