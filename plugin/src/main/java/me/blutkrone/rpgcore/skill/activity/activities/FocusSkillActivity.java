package me.blutkrone.rpgcore.skill.activity.activities;

import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import me.blutkrone.rpgcore.skill.CoreSkill;
import me.blutkrone.rpgcore.skill.SkillContext;
import me.blutkrone.rpgcore.skill.activity.ISkillActivity;

/**
 * An activity that once enough targets have been selected will
 * invoke the skill. Pressing F again will force early usage of
 * the skill.
 */
public class FocusSkillActivity implements ISkillActivity {

    // trigger instantly when updating again
    private boolean force_usage;

    public FocusSkillActivity() {
        throw new UnsupportedOperationException("not implemented (focused activity)");
    }

    public void forceUsage() {
        this.force_usage = true;
    }

    @Override
    public boolean update() {
        return false;
    }

    @Override
    public double getProgress() {
        return 0;
    }

    @Override
    public String getInfoText() {
        return null;
    }

    @Override
    public void interrupt(CoreEntity entity) {

    }

    @Override
    public CoreSkill getSkill() {
        throw new UnsupportedOperationException("not implemented (focused skill)");
    }

    @Override
    public SkillContext getContext() {
        throw new UnsupportedOperationException("not implemented (focused context)");
    }
}
