package me.blutkrone.rpgcore.skill.mechanic;

import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.editor.bundle.mechanic.EditorMobBarrierMechanic;
import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import me.blutkrone.rpgcore.skill.modifier.CoreModifierNumber;

import java.util.Collections;
import java.util.List;

/**
 * Stalls execution of a logic thread, until a certain amount
 * of damage is dealt. The damage will not be passed on to the
 * health of the mob.
 * <br>
 * You cannot have multiple barriers at once.
 */
public class BarrierMechanic extends AbstractCoreMechanic {

    // how much damage to "break" barrier
    private CoreModifierNumber damage;
    // maximum time to break the barrier
    private CoreModifierNumber countdown;
    // mechanics invoked if we fail countdown
    private MultiMechanic failure;
    // whether to continue logic thread on fail
    private boolean terminate_when_failed;

    public BarrierMechanic(EditorMobBarrierMechanic editor) {
        this.damage = editor.damage.build();
        this.countdown = editor.countdown.build();
        this.failure = editor.failure.build();
        this.terminate_when_failed = editor.terminate_when_failed;
    }

    @Override
    public void doMechanic(IContext context, List<IOrigin> targets) {
        throw new UnsupportedOperationException("Cannot be invoked directly!");
    }

    /**
     * Create a barrier that stalls execution until a certain
     * amount of damage is taken.
     *
     * @param context Context to build barrier of
     * @return the barrier instance created.
     */
    public ActiveBarrier activate(IContext context) {
        CoreEntity mob = context.getCoreEntity();
        if (mob == null) {
            return null;
        }
        return new ActiveBarrier(mob);
    }

    /**
     * A wrapper to assist with handling barrier related logic.
     */
    public class ActiveBarrier {

        public int damage;
        public int duration;

        ActiveBarrier(CoreEntity mob) {
            this.damage = BarrierMechanic.this.damage.evalAsInt(mob);
            this.duration = BarrierMechanic.this.countdown.evalAsInt(mob);
        }

        public void applyFailure(CoreEntity mob) {
            failure.doMechanic(mob, Collections.singletonList(mob));
        }

        public boolean doTerminateWhenFailed() {
            return terminate_when_failed;
        }
    }
}
