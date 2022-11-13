package me.blutkrone.rpgcore.skill.behaviour;

import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.hud.editor.bundle.cost.AbstractEditorCost;
import me.blutkrone.rpgcore.hud.editor.bundle.other.EditorAction;
import me.blutkrone.rpgcore.hud.editor.bundle.other.EditorBehaviour;
import me.blutkrone.rpgcore.hud.editor.bundle.trigger.AbstractEditorTrigger;
import me.blutkrone.rpgcore.skill.CoreSkill;
import me.blutkrone.rpgcore.skill.SkillContext;
import me.blutkrone.rpgcore.skill.cost.AbstractCoreCost;
import me.blutkrone.rpgcore.skill.modifier.CoreModifierBoolean;
import me.blutkrone.rpgcore.skill.modifier.CoreModifierString;
import me.blutkrone.rpgcore.skill.trigger.AbstractCoreTrigger;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;

/**
 * A behaviour refers to a "passive" behaviour assigned to
 * the relevant entity, which will invoke a set of actions
 * once the trigger condition has been met.
 */
public class CoreBehaviour {
    private CoreSkill skill;
    // optional status effect symbol
    private CoreModifierString icon;
    // whether this a debuff
    private CoreModifierBoolean debuff;
    // whether to hide symbol while on cooldown
    private CoreModifierBoolean hide_when_cooldown;
    // what behaviour to be triggered
    private AbstractCoreTrigger trigger;
    // costs consumed each time triggered
    private List<AbstractCoreCost> costs;
    // actions invoked each time triggered
    private List<CoreAction> actions;

    public CoreBehaviour(CoreSkill skill, EditorBehaviour editor) {
        this.skill = skill;
        this.icon = editor.icon.build();
        this.debuff = editor.debuff.build();
        this.hide_when_cooldown = editor.hidden.build();
        for (IEditorBundle bundle : editor.trigger) {
            this.trigger = ((AbstractEditorTrigger) bundle).build();
        }
        this.costs = new ArrayList<>();
        for (IEditorBundle bundle : editor.costs) {
            this.costs.add(((AbstractEditorCost) bundle).build());
        }
        this.actions = new ArrayList<>();
        for (IEditorBundle bundle : editor.actions) {
            this.actions.add(((EditorAction) bundle).build());
        }

        Bukkit.getLogger().severe("Not implemented (usage of behaviours & triggers)");
    }

    /**
     * The skill that the behaviour is attached to.
     *
     * @return attached skill
     */
    public CoreSkill getSkill() {
        return skill;
    }

    /**
     * Create an effect serving as a proxy for the behaviour.
     *
     * @param context the context created within.
     */
    public BehaviourEffect createEffect(SkillContext context) {
        return new BehaviourEffect(context, this);
    }

    /**
     * Create an effect serving as a proxy for the behaviour.
     *
     * @param context the context created within.
     */
    public BehaviourEffect createEffect(SkillContext context, int duration) {
        return new BehaviourEffect(context, this, duration);
    }

    /**
     * The trigger to archive to run the attached logic.
     *
     * @return the trigger for the behaviour.
     */
    public AbstractCoreTrigger getTrigger() {
        return trigger;
    }

    /**
     * Icon to render the allocated effect with.
     *
     * @return effect that we've allocated.
     */
    public String getIcon(IContext context) {
        String icon = this.icon.evaluate(context);
        if ("null".equalsIgnoreCase(icon)) {
            return null;
        } else if ("none".equalsIgnoreCase(icon)) {
            return null;
        } else {
            return icon;
        }
    }

    /**
     * Whether to render icon as a debuff.
     *
     * @return true if we are a debuff icon.
     */
    public boolean isDebuff(IContext context) {
        return debuff.evaluate(context);
    }

    /**
     * Whether to hide the behaviour while on a cooldown.
     *
     * @return hides icon while on cooldown
     */
    public boolean isHideWhenCooldown(IContext context) {
        return hide_when_cooldown.evaluate(context);
    }

    /**
     * Invoke the behaviour for the given entity.
     *
     * @param context which context to invoke in.
     */
    public void doBehaviour(IContext context) {
        // ensure we can afford all costs
        for (AbstractCoreCost cost : this.costs) {
            if (!cost.canAfford(context)) {
                return;
            }
        }
        // consume the cost from the context
        for (AbstractCoreCost cost : this.costs) {
            cost.consumeCost(context);
        }
        // run the actions backing the behaviour
        CoreEntity entity = context.getCoreEntity();
        for (CoreAction action : this.actions) {
            entity.getActions().add(action.pipeline(context));
        }
    }
}