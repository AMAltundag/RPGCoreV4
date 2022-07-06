package me.blutkrone.rpgcore.skill.mechanic;

import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.skill.SkillContext;

import java.util.List;

public abstract class CoreMechanic {
    /**
     * Apply the mechanic to all targets.
     *
     * @param context the context to evaluate within.
     * @param targets targets which are affected by the mechanic.
     */
    public abstract void doMechanic(SkillContext context, List<IOrigin> targets);
}
