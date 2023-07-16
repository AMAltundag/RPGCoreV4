package me.blutkrone.rpgcore.skill.mechanic;

import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.api.entity.IEntityEffect;
import me.blutkrone.rpgcore.editor.bundle.mechanic.EditorStackMechanic;
import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import me.blutkrone.rpgcore.skill.modifier.CoreModifierBoolean;
import me.blutkrone.rpgcore.skill.modifier.CoreModifierNumber;
import me.blutkrone.rpgcore.skill.modifier.CoreModifierString;

import java.util.List;

public class StackMechanic extends AbstractCoreMechanic {

    private final CoreModifierNumber duration;
    private final CoreModifierNumber stack;
    private final CoreModifierString effect;
    private final CoreModifierBoolean override;

    public StackMechanic(EditorStackMechanic editor) {
        this.duration = editor.duration.build();
        this.stack = editor.stack.build();
        this.effect = editor.effect.build();
        this.override = editor.override.build();
    }

    @Override
    public void doMechanic(IContext context, List<IOrigin> targets) {
        String effect = this.effect.evaluate(context);
        int duration = this.duration.evalAsInt(context);
        int stack = this.stack.evalAsInt(context);
        boolean override = this.override.evaluate(context);

        for (IOrigin target : targets) {
            if (target instanceof CoreEntity) {
                IEntityEffect active = ((CoreEntity) target).getEffect(effect);
                if (active != null) {
                    active.manipulate(stack, duration, override);
                }
            }
        }
    }
}
