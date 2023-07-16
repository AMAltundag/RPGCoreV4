package me.blutkrone.rpgcore.skill.mechanic;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.editor.bundle.mechanic.EditorEffectMechanic;
import me.blutkrone.rpgcore.effect.CoreEffect;
import me.blutkrone.rpgcore.skill.modifier.CoreModifierNumber;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class EffectMechanic extends AbstractCoreMechanic {

    private List<String> effects;
    private CoreModifierNumber scale;

    public EffectMechanic(EditorEffectMechanic editor) {
        this.effects = new ArrayList<>(editor.effects);
        this.scale = editor.scale.build();
    }

    @Override
    public void doMechanic(IContext context, List<IOrigin> targets) {
        // ensure we have effects to pick
        if (this.effects.isEmpty()) {
            return;
        }
        // cannot show effect at sub-zero scale
        double scale = this.scale.evalAsDouble(context);
        if (scale <= 0d) {
            return;
        }
        // show effect at all targets
        for (IOrigin target : targets) {
            // extract the effect
            String effect = this.effects.get(ThreadLocalRandom.current().nextInt(this.effects.size()));
            CoreEffect core_effect = RPGCore.inst().getEffectManager().getIndex().get(effect);
            // present the effect
            core_effect.show(target.getLocation(), scale);
        }
    }
}
