package me.blutkrone.rpgcore.skill.selector;

import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.skill.SkillContext;

import java.util.List;

public abstract class CoreSelector {

    /**
     * Select a new set of targets based off the preceding subset, do
     * not modify the preceding collection.
     *
     * @param context  the context to evaluate within
     * @param previous the preceding targets
     * @return updated targets that were selected
     */
    public abstract List<IOrigin> doSelect(SkillContext context, List<IOrigin> previous);
}
