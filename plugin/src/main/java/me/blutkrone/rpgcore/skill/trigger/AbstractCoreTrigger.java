package me.blutkrone.rpgcore.skill.trigger;

import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.skill.SkillContext;
import me.blutkrone.rpgcore.skill.modifier.CoreModifierNumber;
import me.blutkrone.rpgcore.skill.modifier.CoreModifierString;

public abstract class AbstractCoreTrigger {

    // cooldown to apply after usage (no multi use with CD)
    public CoreModifierNumber cooldown_reduction;
    public CoreModifierNumber cooldown_time;
    public CoreModifierNumber cooldown_recovery;
    public CoreModifierString cooldown_id;

    /**
     * Whenever extending this, please ensure that you have
     * appropriately configured the cooldown parameters.
     *
     * @param editor which editor we are operating with
     */
    public AbstractCoreTrigger(IEditorBundle editor) {

    }

    /**
     * Update the trigger, and if the condition has been archived
     * invoke it.
     *
     * @param context context provided by the skill.
     * @param event event supplied by the trigger.
     * @param info data that persists until triggered.
     * @return true if we can trigger.
     */
    public abstract boolean update(SkillContext context, Object event, TriggerInfo info);

    /**
     * Create an info object that can track data, if necessary.
     *
     * @return the created info wrapper
     */
    public TriggerInfo createInfo() {
        return new TriggerInfo();
    }

    /**
     * Information about the trigger.
     */
    public static class TriggerInfo {
        // most triggers work fine with 1 number
        public double value;

        public TriggerInfo() {
        }
    }
}
