package me.blutkrone.rpgcore.skill.mechanic;

import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.editor.bundle.mechanic.EditorLogicFlagMechanic;
import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import me.blutkrone.rpgcore.skill.modifier.CoreModifierNumber;
import me.blutkrone.rpgcore.skill.modifier.CoreModifierString;

import java.util.List;

/**
 * Applies a "flag" on a mob, which lasts for a certain
 * duration.
 */
public class LogicFlagMechanic extends AbstractCoreMechanic {

    public CoreModifierString flag;
    public CoreModifierNumber time;

    public LogicFlagMechanic(EditorLogicFlagMechanic editor) {
        this.flag = editor.flag.build();
        this.time = editor.time.build();
    }

    @Override
    public void doMechanic(IContext context, List<IOrigin> targets) {
        String flag = this.flag.evaluate(context);
        int time = this.time.evalAsInt(context);

        for (IOrigin target : targets) {
            if (target instanceof CoreEntity) {
                ((CoreEntity) target).grantTag(flag, time);
            }
        }
    }
}
