package me.blutkrone.rpgcore.skill.selector;

import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.editor.bundle.selector.EditorAttributeSelector;
import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import me.blutkrone.rpgcore.skill.modifier.CoreModifierNumber;

import java.util.ArrayList;
import java.util.List;

public class AttributeSelector extends AbstractCoreSelector {

    private String attribute;
    private CoreModifierNumber minimum;
    private CoreModifierNumber maximum;

    public AttributeSelector(EditorAttributeSelector editor) {
        this.attribute = editor.attribute;
        this.minimum = editor.minimum.build();
        this.maximum = editor.maximum.build();
    }

    @Override
    public List<IOrigin> doSelect(IContext context, List<IOrigin> previous) {
        double minimum = this.minimum.evalAsDouble(context);
        double maximum = this.maximum.evalAsDouble(context);

        List<IOrigin> output = new ArrayList<>();
        for (IOrigin origin : previous) {
            if (origin instanceof CoreEntity) {
                double result = ((CoreEntity) origin).evaluateAttribute(this.attribute);
                if (result >= minimum && result <= maximum) {
                    output.add(origin);
                }
            }
        }
        return output;
    }
}
