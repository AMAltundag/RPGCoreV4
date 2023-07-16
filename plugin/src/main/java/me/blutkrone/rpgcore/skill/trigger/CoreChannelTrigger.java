package me.blutkrone.rpgcore.skill.trigger;

import me.blutkrone.rpgcore.editor.bundle.trigger.EditorChannelTrigger;
import me.blutkrone.rpgcore.skill.SkillContext;
import me.blutkrone.rpgcore.skill.activity.activities.ChannelSkillActivity;

import java.util.HashSet;
import java.util.Set;

/**
 * Triggered whenever a channeling skill is ticked.
 */
public class CoreChannelTrigger extends AbstractCoreTrigger {

    private Set<String> tags;

    public CoreChannelTrigger(EditorChannelTrigger editor) {
        super(editor);

        this.tags = new HashSet<>(editor.tags);

        this.cooldown_recovery = editor.cooldown_recovery.build();
        this.cooldown_reduction = editor.cooldown_reduction.build();
        this.cooldown_time = editor.cooldown_time.build();
        this.cooldown_id = editor.cooldown_id.build();
    }

    @Override
    public boolean update(SkillContext context, Object event, TriggerInfo info) {
        if (!(event instanceof ChannelSkillActivity)) {
            return false;
        }

        boolean matched = false;
        for (String tag : ((ChannelSkillActivity) event).getSkill().getTags()) {
            if (this.tags.contains(tag)) {
                matched = true;
            }
        }

        return matched;
    }
}
