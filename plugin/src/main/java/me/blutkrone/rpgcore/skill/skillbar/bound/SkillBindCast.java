package me.blutkrone.rpgcore.skill.skillbar.bound;

import me.blutkrone.rpgcore.api.activity.IActivity;
import me.blutkrone.rpgcore.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.editor.bundle.binding.EditorCastBind;
import me.blutkrone.rpgcore.editor.bundle.cost.AbstractEditorCost;
import me.blutkrone.rpgcore.editor.bundle.other.EditorAction;
import me.blutkrone.rpgcore.item.modifier.ModifierStyle;
import me.blutkrone.rpgcore.skill.CoreSkill;
import me.blutkrone.rpgcore.skill.SkillContext;
import me.blutkrone.rpgcore.skill.activity.activities.CastSkillActivity;
import me.blutkrone.rpgcore.skill.behaviour.CoreAction;
import me.blutkrone.rpgcore.skill.cost.AbstractCoreCost;
import me.blutkrone.rpgcore.skill.info.CoreSkillInfo;
import me.blutkrone.rpgcore.skill.modifier.CoreModifierBoolean;
import me.blutkrone.rpgcore.skill.modifier.CoreModifierNumber;
import me.blutkrone.rpgcore.skill.modifier.CoreModifierString;
import me.blutkrone.rpgcore.skill.skillbar.ISkillBind;

import java.util.ArrayList;
import java.util.List;

public class SkillBindCast implements ISkillBind {

    // skill which we are bound to
    public CoreSkill skill;
    // icon of this binding
    public CoreModifierString icon;
    // cooldown to apply after usage
    public CoreModifierNumber cooldown_time;
    public CoreModifierNumber cooldown_recovery;
    public CoreModifierString cooldown_id;
    // reduces negative effect of movement
    public CoreModifierNumber stability;
    // time before triggering the pattern
    public CoreModifierNumber cast_time;
    // accelerates casting time
    public CoreModifierNumber cast_faster;
    // costs consumed each time triggered
    public List<AbstractCoreCost> costs;
    // patterns invoked each time triggered
    public List<CoreAction> actions;
    // whether we can be externally triggered
    public CoreModifierBoolean triggerable;
    // interruption flag
    public CoreModifierBoolean interruptable;

    public SkillBindCast(CoreSkill skill, EditorCastBind editor) {
        this.skill = skill;
        this.icon = editor.icon.build();
        this.cooldown_time = editor.cooldown_time.build();
        this.cooldown_recovery = editor.cooldown_recovery.build();
        this.cooldown_id = editor.cooldown_id.build();
        this.stability = editor.stability.build();
        this.costs = new ArrayList<>();
        for (IEditorBundle bundle : editor.costs) {
            this.costs.add(((AbstractEditorCost) bundle).build());
        }
        this.actions = new ArrayList<>();
        for (IEditorBundle bundle : editor.actions) {
            this.actions.add(((EditorAction) bundle).build());
        }
        this.cast_time = editor.cast_time.build();
        this.cast_faster = editor.cast_faster.build();
        this.triggerable = editor.triggerable.build();
        this.interruptable = editor.interruptable.build();
    }

    @Override
    public List<CoreSkillInfo> getInfo() {
        List<CoreSkillInfo> infos = new ArrayList<>();
        infos.add(new CoreSkillInfo("category_skill_binding", ModifierStyle.HEADER, "lc_skill_cooldown_time", cooldown_time));
        infos.add(new CoreSkillInfo("category_skill_binding", ModifierStyle.HEADER, "lc_skill_cooldown_recovery", cooldown_recovery));
        infos.add(new CoreSkillInfo("category_skill_binding", ModifierStyle.HEADER, "lc_skill_stability", stability));
        infos.add(new CoreSkillInfo("category_skill_binding", ModifierStyle.HEADER, "lc_cast_time", cast_time));
        infos.add(new CoreSkillInfo("category_skill_binding", ModifierStyle.HEADER, "lc_cast_faster", cast_faster));
        return infos;
    }

    @Override
    public String getName() {
        return this.skill.getName();
    }

    @Override
    public String getIcon(SkillContext context) {
        return this.icon.evaluate(context);
    }

    @Override
    public int getCooldown(SkillContext context) {
        return context.getCoreEntity().getCooldown(this.cooldown_id.evaluate(context));
    }

    @Override
    public boolean isAffordable(SkillContext context) {
        for (AbstractCoreCost cost : costs) {
            if (!cost.canAfford(context)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean invoke(SkillContext context) {
        // consume all relevant costs
        for (AbstractCoreCost cost : costs)
            cost.consumeCost(context);
        // query an activity for casting the skill
        context.getCoreEntity().setActivity(new CastSkillActivity(this, context));

        return false;
    }

    @Override
    public boolean isCreatorOf(IActivity activity) {
        return activity instanceof CastSkillActivity && ((CastSkillActivity) activity).binding == this;
    }
}