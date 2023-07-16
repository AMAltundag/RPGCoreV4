package me.blutkrone.rpgcore.skill.selector;

import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.editor.bundle.selector.EditorLevelSelector;
import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import me.blutkrone.rpgcore.skill.modifier.CoreModifierNumber;

import java.util.ArrayList;
import java.util.List;

public class LevelSelector extends AbstractCoreSelector {

    private CoreModifierNumber minimum;
    private CoreModifierNumber maximum;

    public LevelSelector(EditorLevelSelector editor) {
        this.minimum = editor.minimum.build();
        this.maximum = editor.maximum.build();
    }

    @Override
    public List<IOrigin> doSelect(IContext context, List<IOrigin> previous) {
        int minimum = this.minimum.evalAsInt(context);
        int maximum = this.maximum.evalAsInt(context);

        List<IOrigin> output = new ArrayList<>();
        for (IOrigin origin : previous) {
            if (origin instanceof CoreEntity) {
                int level = ((CoreEntity) origin).getCurrentLevel();
                if (level >= minimum && level <= maximum) {
                    output.add(origin);
                }
            }
        }

        return output;
    }
}
