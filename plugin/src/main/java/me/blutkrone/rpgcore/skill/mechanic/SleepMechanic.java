package me.blutkrone.rpgcore.skill.mechanic;

import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.hud.editor.bundle.mechanic.EditorMobSleepMechanic;
import me.blutkrone.rpgcore.skill.modifier.CoreModifierNumber;

import java.util.List;

public class SleepMechanic extends AbstractCoreMechanic {

    private CoreModifierNumber duration;

    public SleepMechanic(EditorMobSleepMechanic editor) {
        this.duration = editor.duration.build();
    }

    @Override
    public void doMechanic(IContext context, List<IOrigin> targets) {
        throw new UnsupportedOperationException("Cannot be invoked directly!");
    }

    /**
     * The mob which calls this method is expected to sleep on the
     * given AI routine for the offered number of ticks.
     *
     * @param mob who is asking
     * @return the duration to sleep
     */
    public int timeToSleep(IContext mob) {
        return Math.max(0, this.duration.evalAsInt(mob));
    }
}
