package me.blutkrone.rpgcore.skill.trigger;

import me.blutkrone.rpgcore.editor.bundle.trigger.EditorWardBreakTrigger;
import me.blutkrone.rpgcore.skill.SkillContext;

public class CoreWardBreakTrigger extends AbstractCoreTrigger {

    public CoreWardBreakTrigger(EditorWardBreakTrigger editor) {
        super(editor);

        this.cooldown_recovery = editor.cooldown_recovery.build();
        this.cooldown_reduction = editor.cooldown_reduction.build();
        this.cooldown_time = editor.cooldown_time.build();
        this.cooldown_id = editor.cooldown_id.build();
    }

    @Override
    public boolean update(SkillContext context, Object event, TriggerInfo info) {
        return true;
    }
}
