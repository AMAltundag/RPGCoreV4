package me.blutkrone.rpgcore.skill.selector;

import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.api.IOrigin;

import java.util.List;

public abstract class AbstractCoreSelector {

    /**
     * Run all selectors over the original input, and output the final result
     * of the given selector.
     *
     * @param selectors the selectors we have
     * @param context   the context we operate with
     * @param filtered  the original set of targets
     * @return the filtered set of targets
     */
    public static List<IOrigin> doSelect(List<AbstractCoreSelector> selectors, IContext context, List<IOrigin> filtered) {
        // work off all selectors
        for (AbstractCoreSelector selector : selectors) {
            filtered = selector.doSelect(context, filtered);
        }
        // offer up the result
        return filtered;
    }

    /**
     * Select a new set of targets based off the preceding subset, do
     * not modify the preceding collection.
     *
     * @param context  the context to evaluate within
     * @param previous the preceding targets
     * @return updated targets that were selected
     */
    public abstract List<IOrigin> doSelect(IContext context, List<IOrigin> previous);
}
