package me.blutkrone.rpgcore.skill.mechanic;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.hud.editor.bundle.mechanic.EditorTriggerMechanic;
import me.blutkrone.rpgcore.skill.CoreSkill;
import me.blutkrone.rpgcore.skill.SkillContext;
import me.blutkrone.rpgcore.skill.behaviour.CoreAction;
import me.blutkrone.rpgcore.skill.modifier.CoreModifierNumber;
import me.blutkrone.rpgcore.skill.modifier.CoreModifierString;
import me.blutkrone.rpgcore.skill.skillbar.ISkillBind;
import me.blutkrone.rpgcore.skill.skillbar.bound.SkillBindCast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class TriggerMechanic extends AbstractCoreMechanic {

    // extra skills to trigger
    private List<String> manual_options;
    // filter of skill tags
    private Set<String> skill_filter;
    // % chance to actually run
    private CoreModifierNumber chance;
    // maximum concurrent targets
    private CoreModifierNumber multi;
    // global cooldown for trigger
    private CoreModifierNumber cooldown_reduction;
    private CoreModifierNumber cooldown_time;
    private CoreModifierNumber cooldown_recovery;
    private CoreModifierString cooldown_id;

    public TriggerMechanic(EditorTriggerMechanic editor) {
        this.manual_options = new ArrayList<>(editor.manual_options);
        this.skill_filter = editor.skill_filter.stream().map(String::toLowerCase).collect(Collectors.toSet());
        this.chance = editor.chance.build();
        this.multi = editor.multi.build();
        this.cooldown_reduction = editor.cooldown_reduction.build();
        this.cooldown_time = editor.cooldown_time.build();
        this.cooldown_recovery = editor.cooldown_recovery.build();
        this.cooldown_id = editor.cooldown_id.build();
    }

    @Override
    public void doMechanic(IContext context, List<IOrigin> targets) {
        double chance = this.chance.evalAsDouble(context);
        int multi = this.multi.evalAsInt(context);
        double cooldown_reduction = this.cooldown_reduction.evalAsDouble(context);
        int cooldown_time = this.cooldown_time.evalAsInt(context);
        double cooldown_recovery = this.cooldown_recovery.evalAsDouble(context);
        String cooldown_id = this.cooldown_id.evaluate(context);

        // skip if on cooldown
        if (context.getCoreEntity().getCooldown(cooldown_id) > 0) {
            return;
        }

        // options which we can cast
        List<CoreSkill> options = new ArrayList<>();
        for (String id : this.manual_options) {
            options.add(RPGCore.inst().getSkillManager().getIndex().get(id));
        }
        if (context instanceof SkillContext) {
            for (String id : ((SkillContext) context).getLinkedSkills()) {
                options.add(RPGCore.inst().getSkillManager().getIndex().get(id));
            }
        }
        if (!skill_filter.isEmpty()) {
            options.removeIf(skill -> {
                // check if we match any tag listed here
                for (String tag : skill.getTags()) {
                    if (skill_filter.contains(tag)) {
                        return false;
                    }
                }
                // not a choice if we do not have a tag
                return true;
            });
        }
        options.removeIf(skill -> {
            ISkillBind binding = skill.getBinding();
            return !(binding instanceof SkillBindCast) || !((SkillBindCast) binding).triggerable.evaluate(context);
        });

        // do not cast spell if no tag matches
        if (options.isEmpty()) {
            return;
        }

        // limit to the total trigger subset
        if (targets.size() > multi) {
            targets = new ArrayList<>(targets);
            Collections.shuffle(targets);
            targets.subList(multi, targets.size()).clear();
        }

        // strip away with chance rolls
        if (chance < 1d) {
            targets = new ArrayList<>(targets);
            targets.removeIf(target -> Math.random() > chance);
        }

        // try triggering the relevant skills
        for (IOrigin target : targets) {
            CoreSkill casting = options.get(ThreadLocalRandom.current().nextInt(options.size()));
            if (casting.getBinding() instanceof SkillBindCast) {
                for (CoreAction action : ((SkillBindCast) casting.getBinding()).actions) {
                    CoreAction.ActionPipeline pipeline = action.pipeline(context, Collections.singletonList(target));
                    context.getCoreEntity().getActions().add(pipeline);
                }
            }
        }

        // apply a cooldown if necessary
        int cooldown = (int) (cooldown_time * Math.max(0d, 1d - cooldown_reduction) / Math.max(0.01d, 1d + cooldown_recovery));
        if (cooldown > 0) {
            context.getCoreEntity().setCooldown(cooldown_id, cooldown);
        }
    }
}
