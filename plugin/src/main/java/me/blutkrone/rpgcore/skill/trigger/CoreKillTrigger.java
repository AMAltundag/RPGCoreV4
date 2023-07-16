package me.blutkrone.rpgcore.skill.trigger;

import me.blutkrone.rpgcore.damage.interaction.DamageInteraction;
import me.blutkrone.rpgcore.editor.bundle.trigger.EditorKillTrigger;
import me.blutkrone.rpgcore.skill.SkillContext;
import me.blutkrone.rpgcore.skill.modifier.CoreModifierNumber;

import java.util.HashSet;
import java.util.Set;

/**
 * Triggers when enough entities of a certain type are killed.
 */
public class CoreKillTrigger extends AbstractCoreTrigger {

    // tags on the entity we require
    private Set<String> tags;
    // how many kills needed to trigger
    private CoreModifierNumber kills;

    /**
     * Whenever extending this, please ensure that you have
     * appropriately configured the cooldown parameters.
     *
     * @param editor which editor we are operating with
     */
    public CoreKillTrigger(EditorKillTrigger editor) {
        super(editor);

        this.tags = new HashSet<>(editor.tags);
        this.kills = editor.kills.build();

        this.cooldown_recovery = editor.cooldown_recovery.build();
        this.cooldown_reduction = editor.cooldown_reduction.build();
        this.cooldown_time = editor.cooldown_time.build();
        this.cooldown_id = editor.cooldown_id.build();
    }

    @Override
    public boolean update(SkillContext context, Object event, TriggerInfo info) {
        // check if the kill is of the wanted type
        if (!(event instanceof DamageInteraction)) {
            return false;
        }
        // check if we are of the wanted type
        if (!this.tags.isEmpty()) {
            boolean any_tag = false;
            for (String tag : ((DamageInteraction) event).getDefender().getMyTags()) {
                any_tag |= this.tags.contains(tag);
            }
            if (!any_tag) {
                return false;
            }
        }
        // update kill-count and check for enough kills
        return ++info.value >= this.kills.evalAsDouble(context);
    }
}
