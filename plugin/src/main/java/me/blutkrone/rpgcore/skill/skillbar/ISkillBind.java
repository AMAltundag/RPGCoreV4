package me.blutkrone.rpgcore.skill.skillbar;

import me.blutkrone.rpgcore.api.activity.IActivity;
import me.blutkrone.rpgcore.skill.SkillContext;

public interface ISkillBind {

    /**
     * Name of the skill that backs up the skill.
     *
     * @return name of the skill
     */
    String getName();

    /**
     * The icon to use for the skill-bind.
     *
     * @param context the context to invoke the skill in.
     * @return the icon used for the skill binding.
     */
    String getIcon(SkillContext context);

    /**
     * Get how long the entity backing the context is on a
     * cooldown for this binding.
     *
     * @param context the context to invoke the skill in.
     * @return cooldown in ticks.
     */
    int getCooldown(SkillContext context);

    /**
     * Check if the entity backing the given context is able
     * to afford casting this binding.
     *
     * @param context the context to invoke the skill in.
     * @return true if costs are affordable
     */
    boolean isAffordable(SkillContext context);

    /**
     * Invoke the binding, for the given context.
     *
     * @param context the context to invoke the skill in.
     * @return true if cast successfully.
     */
    boolean invoke(SkillContext context);

    /**
     * Check if the given activity was created by this
     * binding.
     *
     * @param activity the activity to check
     * @return true this binding created the activity
     */
    boolean isCreatorOf(IActivity activity);
}
