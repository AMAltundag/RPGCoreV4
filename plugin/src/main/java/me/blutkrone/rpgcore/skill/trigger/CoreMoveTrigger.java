package me.blutkrone.rpgcore.skill.trigger;

import me.blutkrone.rpgcore.editor.bundle.trigger.EditorMoveTrigger;
import me.blutkrone.rpgcore.skill.SkillContext;
import me.blutkrone.rpgcore.skill.modifier.CoreModifierNumber;

/**
 * Triggers after moving a certain distance
 */
public class CoreMoveTrigger extends AbstractCoreTrigger {

    // total damage to be accumulated
    private CoreModifierNumber distance;

    public CoreMoveTrigger(EditorMoveTrigger editor) {
        super(editor);

        this.distance = editor.distance.build();

        this.cooldown_recovery = editor.cooldown_recovery.build();
        this.cooldown_reduction = editor.cooldown_reduction.build();
        this.cooldown_time = editor.cooldown_time.build();
        this.cooldown_id = editor.cooldown_id.build();
    }

    @Override
    public boolean update(SkillContext context, Object event, TriggerInfo info) {
        // movement was computed and provided to us
        info.value += (Double) event;
        // check if enough motion has happened
        return info.value >= this.distance.evalAsDouble(context);
    }
}

