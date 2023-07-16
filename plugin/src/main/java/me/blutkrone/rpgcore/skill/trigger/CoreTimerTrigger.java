package me.blutkrone.rpgcore.skill.trigger;

import me.blutkrone.rpgcore.editor.bundle.trigger.EditorTimerTrigger;
import me.blutkrone.rpgcore.skill.SkillContext;
import me.blutkrone.rpgcore.skill.modifier.CoreModifierNumber;

/**
 * Triggers at an interval of X ticks
 */
public class CoreTimerTrigger extends AbstractCoreTrigger {

    // the interval to invoke the timer at
    private CoreModifierNumber interval;

    public CoreTimerTrigger(EditorTimerTrigger editor) {
        super(editor);

        this.interval = editor.interval.build();

        this.cooldown_recovery = editor.cooldown_recovery.build();
        this.cooldown_reduction = editor.cooldown_reduction.build();
        this.cooldown_time = editor.cooldown_time.build();
        this.cooldown_id = editor.cooldown_id.build();
    }

    @Override
    public boolean update(SkillContext context, Object event, TriggerInfo info) {
        return ++info.value >= this.interval.evalAsDouble(context);
    }
}
