package me.blutkrone.rpgcore.skill.behaviour;

import me.blutkrone.rpgcore.api.entity.IEntityEffect;
import me.blutkrone.rpgcore.skill.SkillContext;
import me.blutkrone.rpgcore.skill.trigger.AbstractCoreTrigger;

/**
 * An effect generated while an entity holds a behaviour instance.
 */
public class BehaviourEffect implements IEntityEffect {

    // context which created the behaviour
    private SkillContext context;
    // the behaviour that created the effect
    private CoreBehaviour behaviour;
    // remaining duration
    private int duration;
    // whether to hide the effect from the status
    private boolean hide_icon;
    // snapshot when the behaviour was acquired
    private long timestamp;
    // extra info for the trigger
    private AbstractCoreTrigger.TriggerInfo info;
    // used by main passives when the skill is lost
    private boolean abandoned;

    public BehaviourEffect(SkillContext context, CoreBehaviour behaviour) {
        this.context = context;
        this.behaviour = behaviour;
        this.duration = Integer.MAX_VALUE;
        this.timestamp = System.currentTimeMillis();
    }

    public BehaviourEffect(SkillContext context, CoreBehaviour behaviour, int duration) {
        this.context = context;
        this.behaviour = behaviour;
        this.duration = duration;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Which behaviour created the effect.
     *
     * @return backing behaviour.
     */
    public CoreBehaviour getBehaviour() {
        return behaviour;
    }

    /**
     * The context which generated an effect.
     *
     * @return context creating the effect.
     */
    public SkillContext getContext() {
        return context;
    }

    public AbstractCoreTrigger.TriggerInfo getInfo() {
        return info;
    }

    public void setInfo(AbstractCoreTrigger.TriggerInfo info) {
        this.info = info;
    }

    public void setAbandoned() {
        this.abandoned = true;
    }

    @Override
    public boolean tickEffect(int delta) {
        // check whether to hide the icon
        String icon = behaviour.getIcon(context);
        if (icon != null && behaviour.isHideWhenCooldown(context)) {
            String cdId = behaviour.getTrigger().cooldown_id.evaluate(context);
            hide_icon = context.getCoreEntity().getCooldown(cdId) > 0;
        }
        // unlimited duration
        if (duration == Integer.MAX_VALUE) {
            return false;
        }
        // consume duration and possibly expire
        duration -= delta;
        return duration <= 0;
    }

    @Override
    public int getStacks() {
        return 1;
    }

    @Override
    public int getDuration() {
        return this.duration;
    }

    @Override
    public String getIcon() {
        if (hide_icon) {
            return null;
        }

        return behaviour.getIcon(context);
    }

    @Override
    public long getLastUpdated() {
        return timestamp;
    }

    @Override
    public boolean isDebuff() {
        return behaviour.isDebuff(context);
    }

    @Override
    public boolean isValid() {
        return !this.context.getCoreEntity().isInvalid() && !this.abandoned;
    }

    @Override
    public void manipulate(int stack, int duration, boolean override) {
        if (override) {
            this.duration = duration;
        } else {
            this.duration += duration;
        }

        this.timestamp = System.currentTimeMillis();
    }
}
