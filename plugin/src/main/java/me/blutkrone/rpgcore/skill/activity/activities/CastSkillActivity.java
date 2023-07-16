package me.blutkrone.rpgcore.skill.activity.activities;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.skill.CoreSkill;
import me.blutkrone.rpgcore.skill.SkillContext;
import me.blutkrone.rpgcore.skill.activity.ISkillActivity;
import me.blutkrone.rpgcore.skill.behaviour.CoreAction;
import me.blutkrone.rpgcore.skill.mechanic.BarrierMechanic;
import me.blutkrone.rpgcore.skill.skillbar.bound.SkillBindCast;
import me.blutkrone.rpgcore.skill.trigger.CoreCastTrigger;
import me.blutkrone.rpgcore.util.Utility;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * An activity that casts a skill at the end of the process, the
 * cost is expected to already be paid.
 */
public class CastSkillActivity implements ISkillActivity {

    // generic constants for a skill activity
    public final SkillBindCast binding;
    private final SkillContext context;
    // cast specific constant modifiers
    private final double stability;
    private final double time_want;
    // cast specific variable modifiers
    private double time_have;
    private double instability;
    private Location snapshot;
    // pipelines need to finish already
    private List<CoreAction.ActionPipeline> working = new ArrayList<>();
    // allow binding to be interrupted
    private boolean interruptable;

    public CastSkillActivity(SkillBindCast binding, SkillContext context) {
        this.context = context;
        this.binding = binding;

        // compute actual casting time of ability
        double cast_time = binding.cast_time.evalAsInt(context);
        double cast_faster = binding.cast_faster.evalAsDouble(context);
        cast_time = Math.max(1d, cast_time / Math.max(0.1d, (1d + cast_faster)));
        double action_speed = context.evaluateAttribute("ACTION_SPEED");
        cast_time = Math.max(1d, cast_time / Math.max(0.1d, (1d + action_speed)));

        // update the binding
        this.time_want = cast_time;
        this.stability = binding.stability.evalAsDouble(context);
        this.time_have = 0;
        this.instability = 0d;
        this.snapshot = context.getLocation();
        this.interruptable = binding.interruptable.evaluate(context);

        // instant cast should max out cast time
        if (context.getCoreEntity().hasInstantCast(getSkill(), true)) {
            this.time_have = this.time_want;
        }
    }

    @Override
    public boolean update() {
        // work off the channeling ability first
        if (!this.working.isEmpty()) {
            this.working.removeIf(CoreAction.ActionPipeline::update);

            if (this.working.isEmpty()) {
                // calculate how long a cooldown we receive
                double cooldown_recovery = 1d + this.binding.cooldown_recovery.evalAsDouble(this.context);
                int cooldown_time = this.binding.cooldown_time.evalAsInt(context);
                int cooldown = (int) Math.max(0, cooldown_time / Math.max(0.1d, cooldown_recovery));
                // have the context owner put on a cooldown
                this.context.getCoreEntity().setCooldown(this.binding.cooldown_id.evaluate(this.context), cooldown);
                // we have finished
                return true;
            } else {
                // some logic is still running
                return false;
            }
        }

        // gain instability relative to how much we've moved
        Location snapshot = this.context.getLocation();
        this.instability += Math.sqrt(Utility.distanceSqOrWorld(snapshot, this.snapshot)) / (1d + this.stability);
        this.snapshot = snapshot;
        // lose 5% of total instability per tick
        this.instability *= 0.95d;
        // gain an appropriate amount of cast progress
        this.time_have += 1d / Math.sqrt(1d + this.instability);

        // if we didn't cap, we are done
        if (this.time_have < this.time_want)
            return false;

        // invoke the skill trigger
        this.context.getCoreEntity().proliferateTrigger(CoreCastTrigger.class, this);

        // track the working pipelines
        for (CoreAction action : this.binding.actions) {
            this.working.add(action.pipeline(this.context, Arrays.asList(this.context)));
        }

        // drop the activity
        return false;
    }

    @Override
    public double getProgress() {
        return this.time_have / this.time_want;
    }

    @Override
    public String getInfoText() {
        double time_approximate = (this.time_want - this.time_have) * Math.sqrt(1d + this.instability);
        return RPGCore.inst().getLanguageManager().getTranslation("cast_info_text")
                .replace("{TIME}", String.valueOf((int) Math.max(1, time_approximate / 20d)))
                .replace("{NAME}", this.binding.skill.getName());
    }

    @Override
    public void interrupt() {
        // calculate how long a cooldown we receive
        double cooldown_recovery = 1d + this.binding.cooldown_recovery.evalAsDouble(this.context);
        int cooldown_time = this.binding.cooldown_time.evalAsInt(context);
        int cooldown = (int) Math.max(0, cooldown_time / Math.max(0.1d, cooldown_recovery));
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
