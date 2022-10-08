package me.blutkrone.rpgcore.skill.activity;

import me.blutkrone.rpgcore.api.activity.IActivity;
import me.blutkrone.rpgcore.skill.CoreSkill;
import me.blutkrone.rpgcore.skill.SkillContext;

public interface ISkillActivity extends IActivity {
    /**
     * Which skill is associated with this activity.
     *
     * @return the skill which ties to the activity.
     */
    CoreSkill getSkill();

    /**
     * The context the skill had been invoked with.
     *
     * @return the context for this activity
     */
    SkillContext getContext();

    /**
     * Attempt to make a barrier soak the damage, a barrier always
     * soaks any damage. The return value implies whether there is
     * a barrier available.
     *
     * @param damage the damage we want to soak.
     * @return whether the creature has any barrier.
     */
    boolean doBarrierDamageSoak(int damage);
}
