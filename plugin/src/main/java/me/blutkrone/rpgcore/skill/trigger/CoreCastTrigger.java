package me.blutkrone.rpgcore.skill.trigger;

import me.blutkrone.rpgcore.hud.editor.bundle.trigger.EditorCastTrigger;
import me.blutkrone.rpgcore.skill.SkillContext;
import me.blutkrone.rpgcore.skill.activity.activities.CastSkillActivity;

import java.util.HashSet;
import java.util.Set;

/**
 * Triggered when the casting process of a skill is complete.
 */
public class CoreCastTrigger extends AbstractCoreTrigger {

    private Set<String> tags;

    public CoreCastTrigger(EditorCastTrigger editor) {
        super(editor);

        this.tags = new HashSet<>(editor.tags);

        this.cooldown_recovery = editor.cooldown_recovery.build();
        this.cooldown_reduction = editor.cooldown_reduction.build();
        this.cooldown_time = editor.cooldown_time.build();
        this.cooldown_id = editor.cooldown_id.build();
    }

    @Override
    public boolean update(SkillContext context, Object event, TriggerInfo info) {
        if (!(event instanceof CastSkillActivity)) {
            return false;
        }

        boolean matched = false;
        for (String tag : ((CastSkillActivity) event).getSkill().getTags()) {
            if (this.tags.contains(tag)) {
                matched = true;
            }
        }

        return matched;
    }
}
