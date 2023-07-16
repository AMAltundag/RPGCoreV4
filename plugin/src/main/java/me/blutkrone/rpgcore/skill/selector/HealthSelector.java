package me.blutkrone.rpgcore.skill.selector;

import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.editor.bundle.selector.EditorHealthSelector;
import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import me.blutkrone.rpgcore.entity.resource.ResourceSnapshot;
import me.blutkrone.rpgcore.skill.modifier.CoreModifierBoolean;
import me.blutkrone.rpgcore.skill.modifier.CoreModifierNumber;

import java.util.ArrayList;
import java.util.List;

public class HealthSelector extends AbstractCoreSelector {

    private final CoreModifierNumber minimum;
    private final CoreModifierNumber maximum;
    private final CoreModifierBoolean percentage;

    public HealthSelector(EditorHealthSelector editor) {
        this.minimum = editor.minimum.build();
        this.maximum = editor.maximum.build();
        this.percentage = editor.percentage.build();
    }

    @Override
    public List<IOrigin> doSelect(IContext context, List<IOrigin> previous) {
        double minimum = this.minimum.evalAsDouble(context);
        double maximum = this.maximum.evalAsDouble(context);
        boolean percentage = this.percentage.evaluate(context);

        List<IOrigin> output = new ArrayList<>();
        for (IOrigin origin : previous) {
            if (origin instanceof CoreEntity) {
                ResourceSnapshot snapshot = ((CoreEntity) origin).getHealth().snapshot();
                if (percentage) {
                    if (snapshot.fraction >= minimum && snapshot.fraction <= maximum) {
                        output.add(origin);
                    }
                } else {
                    if (snapshot.current >= minimum && snapshot.current <= maximum) {
                        output.add(origin);
                    }
                }
            }
        }
        return output;
    }
}
