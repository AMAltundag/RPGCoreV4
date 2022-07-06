package me.blutkrone.rpgcore.entity.focus;

import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;

/**
 * A tracker for a player focus, please note that this is
 * a best-effort tracking for UX purposes.
 */
public class FocusTracker {

    // respective focuses which we apply
    private Map<FocusType, FocusTracked> tracker = new HashMap<>();

    public FocusTracker() {
        this.tracker.put(FocusType.LOOKING, new FocusTracked(0, 16d, 20));
        this.tracker.put(FocusType.COMBAT_ATTACK, new FocusTracked(1, 32d, 120));
        this.tracker.put(FocusType.COMBAT_DEFEND, new FocusTracked(2, 32d, 120));
        this.tracker.put(FocusType.PARTY, new FocusTracked(3, 48d, 200));
        this.tracker.put(FocusType.LOCKED, new FocusTracked(4, 48d, 200));
    }

    /**
     * Validate the targets we've tracked.
     *
     * @param where where to flush thorough.
     */
    public void validate(Location where) {
        this.tracker.forEach((type, tracked) -> tracked.validate(where));
    }

    /**
     * Update the entity which is tracked for a given purpose.
     *
     * @param type    the purpose we tracked thorough
     * @param tracked the entity which we tracked thorough
     */
    public void update(FocusType type, CoreEntity tracked) {
        this.tracker.get(type).setTargetEntity(tracked);
    }

    /**
     * Fetch the highest priority focused entity.
     *
     * @return which entity is focused, may be null.
     */
    public CoreEntity getFocus() {
        int priority = Integer.MIN_VALUE;
        CoreEntity target = null;
        for (FocusTracked wrapper : tracker.values()) {
            CoreEntity current = wrapper.getTargetEntity();
            // ensure that there is a target provided
            if (current == null || !current.isAllowTarget())
                continue;
            // ensure that we match priority wise
            if (target == null || wrapper.priority > priority)
                target = current;
        }
        // offer up the entity with the highest priority
        return target;
    }
}

