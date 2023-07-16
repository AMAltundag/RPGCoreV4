package me.blutkrone.rpgcore.editor.constraint.bundle.mono;

import me.blutkrone.rpgcore.editor.bundle.other.EditorAttributeAndFactor;
import me.blutkrone.rpgcore.editor.constraint.bundle.AbstractMonoConstraint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AttributeAndFactorConstraint extends AbstractMonoConstraint {

    @Override
    public void setElementAt(List container, int index, String value) {
        container.set(index, new EditorAttributeAndFactor());
    }

    @Override
    public void addElement(List container, String value) {
        container.add(new EditorAttributeAndFactor());
    }

    @Override
    public List<String> getPreview(List<Object> list) {
        Map<String, Double> multipliers = new HashMap<>();
        for (Object o : list) {
            EditorAttributeAndFactor eac = (EditorAttributeAndFactor) o;
            multipliers.merge(eac.attribute, eac.factor, (a, b) -> a + b);
        }
        List<String> output = new ArrayList<>();
        multipliers.forEach((k, v) -> {
            output.add("Tag " + k + " Weight " + v);
        });
        return output;
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Attribute And Factor");
        instruction.add("Grant a certain amount of a given attribute");
        instruction.add("Modifiers to the same attribute always sum up");
        return instruction;
    }

    @Override
    public String getPreview(Object object) {
        return ((EditorAttributeAndFactor) object).attribute;
    }
}
