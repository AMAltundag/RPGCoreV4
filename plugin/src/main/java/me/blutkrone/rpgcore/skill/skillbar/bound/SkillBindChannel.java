package me.blutkrone.rpgcore.skill.skillbar.bound;

import me.blutkrone.rpgcore.api.activity.IActivity;
import me.blutkrone.rpgcore.skill.CoreSkill;
import me.blutkrone.rpgcore.skill.SkillContext;
import me.blutkrone.rpgcore.skill.activity.activities.ChannelSkillActivity;
import me.blutkrone.rpgcore.skill.behaviour.CorePattern;
import me.blutkrone.rpgcore.skill.cost.CoreCost;
import me.blutkrone.rpgcore.skill.modifier.CoreModifierNumber;
import me.blutkrone.rpgcore.skill.modifier.CoreModifierString;
import me.blutkrone.rpgcore.skill.skillbar.ISkillBind;

public class SkillBindChannel implements ISkillBind {

    // skill which we are bound to
    public CoreSkill skill;
    // icon of this binding
    public CoreModifierString icon;
    // cooldown to apply after usage
    public CoreModifierNumber cooldown_reduction;
    public CoreModifierNumber cooldown_time;
    public CoreModifierNumber cooldown_recovery;
    public CoreModifierString cooldown_id;
    // reduces negative effect of movement
    public CoreModifierNumber stability;
    // costs consumed each time triggered
    public CoreCost[] costs;
    // patterns invoked each time triggered
    public CorePattern[] patterns;
    // maximum duration to be channeled
    public CoreModifierNumber channel_time;
    // interval to trigger patterns
    public CoreModifierNumber channel_interval;
    // multiplier to trigger rate
    public CoreModifierNumber channel_faster;

    @Override
    public String getIcon(SkillContext context) {
        return this.icon.evaluate(context);
    }

    @Override
    public int getCooldown(SkillContext context) {
        return context.getOwner().getCooldown(this.cooldown_id.evaluate(context));
    }

    @Override
    public boolean isAffordable(SkillContext context) {
        for (CoreCost cost : costs) {
            if (!cost.canAfford(context)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean invoke(SkillContext context) {
        // consume all relevant costs
        for (CoreCost cost : costs)
            cost.consumeCost(context);

        // query our activity
        context.getOwner().setActivity(new ChannelSkillActivity(this, context));

        return true;
    }

    @Override
    public boolean isCreatorOf(IActivity activity) {
        return activity instanceof ChannelSkillActivity && ((ChannelSkillActivity) activity).binding == this;
    }
}
