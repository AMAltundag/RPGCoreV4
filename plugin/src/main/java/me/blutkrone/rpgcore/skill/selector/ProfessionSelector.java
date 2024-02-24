package me.blutkrone.rpgcore.skill.selector;

import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.editor.bundle.selector.EditorProfessionSelector;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.skill.modifier.CoreModifierNumber;
import me.blutkrone.rpgcore.skill.modifier.CoreModifierString;

import java.util.ArrayList;
import java.util.List;

public class ProfessionSelector extends AbstractCoreSelector {

    private final CoreModifierString profession;
    private CoreModifierNumber minimum;
    private CoreModifierNumber maximum;

    public ProfessionSelector(EditorProfessionSelector editor) {
        this.profession = editor.profession.build();
        this.minimum = editor.minimum.build();
        this.maximum = editor.maximum.build();
    }

    @Override
    public List<IOrigin> doSelect(IContext context, List<IOrigin> previous) {
        String profession = this.profession.evaluate(context);
        int minimum = this.minimum.evalAsInt(context);
        int maximum = this.maximum.evalAsInt(context);

        List<IOrigin> output = new ArrayList<>();
        for (IOrigin origin : previous) {
            if (origin instanceof CorePlayer player) {
                int level = player.getProfessionLevel().getOrDefault(profession, 0);
                if (level >= minimum && level <= maximum) {
                    output.add(origin);
                }
            }
        }

        return output;
    }
}
