package me.blutkrone.rpgcore.skill.behaviour;

import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.skill.SkillContext;
import me.blutkrone.rpgcore.skill.mechanic.CoreMechanic;
import me.blutkrone.rpgcore.skill.selector.CoreSelector;

import java.util.Collections;
import java.util.List;

/**
 * A pattern refers to a set of mechanics that should
 * apply to selected targets, so long the condition to
 * do so was met.
 */
public class CorePattern {
    // picks entities which the mechanic should run on
    public CoreSelector[] selectors;
    // things which we want to happen
    public CoreMechanic[] mechanics;
    // if empty pattern will not run
    public CoreSelector[] conditions;

    /**
     * Invoke this pattern.
     *
     * @param context the context to proceed with.
     */
    public void invoke(SkillContext context) {
        // ensure we can be invoked
        List<IOrigin> targets = Collections.singletonList(context);
        for (CoreSelector condition : this.conditions)
            targets = condition.doSelect(context, targets);
        if (targets.isEmpty()) {
            return;
        }

        // select appropriate targets
        targets = Collections.singletonList(context);
        for (CoreSelector selector : this.selectors)
            targets = selector.doSelect(context, targets);
        if (targets.isEmpty()) {
            return;
        }

        // have all mechanics affect the selected targets
        for (CoreMechanic mechanic : this.mechanics) {
            mechanic.doMechanic(context, targets);
        }
    }
}
