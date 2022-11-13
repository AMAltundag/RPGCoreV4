package me.blutkrone.rpgcore.skill.activity.activities;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import me.blutkrone.rpgcore.skill.CoreSkill;
import me.blutkrone.rpgcore.skill.SkillContext;
import me.blutkrone.rpgcore.skill.activity.ISkillActivity;
import me.blutkrone.rpgcore.skill.behaviour.CoreAction;
import me.blutkrone.rpgcore.skill.mechanic.BarrierMechanic;
import me.blutkrone.rpgcore.skill.skillbar.bound.SkillBindChannel;
import me.blutkrone.rpgcore.skill.trigger.CoreChannelTrigger;
import me.blutkrone.rpgcore.util.Utility;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;


/**
 * An activity which repeatedly triggers a given skill, until running
 * out of time or being unable to pay the cost anymore.
 */
public class ChannelSkillActivity implements ISkillActivity {

    // generic constants for a skill activity
    public final SkillBindChannel binding;
    private final SkillContext context;
    // channel specific constant modifiers
    private final double stability;
    private final double maximum_time;
    private final double interval;
    // channel specific variable modifiers
    private int tick;
    private double instability;
    private double remaining_time;
    private Location snapshot;
    // pipelines need to finish already
    private List<CoreAction.ActionPipeline> working = new ArrayList<>();

    public ChannelSkillActivity(SkillBindChannel binding, SkillContext context) {
        this.context = context;
        this.binding = binding;

        this.stability = binding.stability.evalAsDouble(context);
        this.maximum_time = binding.channel_time.evalAsDouble(context);
        double interval = Math.max(1d, this.binding.channel_interval.evalAsDouble(this.context));
        double faster = Math.max(0.1d, 1d + this.binding.channel_faster.evalAsDouble(this.context));
        this.interval = (int) Math.max(1, interval / faster);

        this.tick = 0;
        this.instability = 0d;
        this.remaining_time = this.maximum_time;
        this.snapshot = context.getLocation();
    }

    @Override
    public boolean update() {
        // work off the channeling ability first
        if (!this.working.isEmpty()) {
            this.working.removeIf(CoreAction.ActionPipeline::update);
            return false;
        }

        // instability increases as player moves
        Location snapshot = this.context.getLocation();
        this.instability += Math.sqrt(Utility.distanceSqOrWorld(snapshot, this.snapshot)) / (1d + this.stability);
        this.snapshot = snapshot;
        // lose 5% of total instability per tick
        this.instability *= 0.95d;
        // gain an appropriate amount of cast progress
        this.remaining_time -= 1d * Math.sqrt(1d + this.instability);

        if (this.remaining_time <= 0d) {
            // set off the last trigger
            this.context.addTag("CHANNEL_LAST_USE");
            // invoke the skill trigger
            this.context.getCoreEntity().proliferateTrigger(CoreChannelTrigger.class, this);
            // invoke the logic
            for (CoreAction action : this.binding.actions) {
                this.working.add(action.pipeline(this.context));
            }
            // calculate how long a cooldown we receive
            double cooldown_recovery = 1d + this.binding.cooldown_recovery.evalAsDouble(this.context);
            int cooldown_time = this.binding.cooldown_time.evalAsInt(context);
            int cooldown = (int) Math.max(0, (cooldown_time) / Math.max(0.1d, cooldown_recovery));
            // have the context owner put on a cooldown
            this.context.getCoreEntity().setCooldown(this.binding.cooldown_id.evaluate(this.context), cooldown);
            // strip the channel activity away
            return true;
        } else if (this.tick++ % this.interval == 0) {
            // invoke the skill trigger
            this.context.getCoreEntity().proliferateTrigger(CoreChannelTrigger.class, this);
            // invoke the logic
            for (CoreAction action : this.binding.actions) {
                this.working.add(action.pipeline(this.context));
            }
            // ensure we can afford the cost, otherwise we are done
            if (!this.binding.isAffordable(this.context)) {
                this.remaining_time = 0d;
                return true;
            }
        }

        return false;
    }

    @Override
    public double getProgress() {
        return this.remaining_time / this.maximum_time;
    }

    @Override
    public String getInfoText() {
        double time_approximate = this.remaining_time / Math.sqrt(1d + this.instability);
        return RPGCore.inst().getLanguageManager().getTranslation("channel_info_text")
                .replace("{TIME}", String.valueOf((int) Math.max(1, time_approximate / 20d)))
                .replace("{NAME}", this.binding.skill.getName());
    }

    @Override
    public void interrupt(CoreEntity entity) {
        // calculate how long a cooldown we receive
        double cooldown_recovery = 1d + this.binding.cooldown_recovery.evalAsDouble(this.context);
        int cooldown_time = this.binding.cooldown_time.evalAsInt(context);
        int cooldown = (int) Math.max(0, (cooldown_time) / Math.max(0.1d, cooldown_recovery));
        // have the context owner put on a cooldown
        this.context.getCoreEntity().setCooldown(this.binding.cooldown_id.evaluate(this.context), cooldown);
    }

    @Override
    public CoreSkill getSkill() {
        return this.binding.skill;
    }

    @Override
    public SkillContext getContext() {
        return this.context;
    }

    @Override
    public boolean doBarrierDamageSoak(int damage) {
        boolean barrier = false;
        for (CoreAction.ActionPipeline pipeline : this.working) {
            BarrierMechanic.ActiveBarrier active = pipeline.getBarrier();
            if (active != null) {
                active.damage -= damage;
                barrier = true;
            }
        }
        return barrier;
    }
}
