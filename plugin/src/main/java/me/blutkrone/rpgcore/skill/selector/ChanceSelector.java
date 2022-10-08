package me.blutkrone.rpgcore.skill.selector;

import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.hud.editor.bundle.selector.EditorChanceSelector;
import me.blutkrone.rpgcore.skill.modifier.CoreModifierNumber;

import java.util.ArrayList;
import java.util.List;

public class ChanceSelector extends AbstractCoreSelector {

    private final CoreModifierNumber chance;

    public ChanceSelector(EditorChanceSelector editor) {
        this.chance = editor.chance.build();
    }

    @Override
    public List<IOrigin> doSelect(IContext context, List<IOrigin> previous) {
        double chance = this.chance.evalAsDouble(context);
        // retain targets with % chance
        List<IOrigin> retained = new ArrayList<>();
        for (IOrigin target : previous) {
            if (Math.random() < chance) {
                retained.add(target);
            }
        }
        return retained;
    }
}
