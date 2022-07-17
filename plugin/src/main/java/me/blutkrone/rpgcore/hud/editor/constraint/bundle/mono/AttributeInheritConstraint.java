package me.blutkrone.rpgcore.hud.editor.constraint.bundle.mono;

import me.blutkrone.rpgcore.hud.editor.constraint.bundle.AbstractMonoConstraint;
import me.blutkrone.rpgcore.hud.editor.root.other.EditorAttribute;

import java.util.ArrayList;
import java.util.List;

public class AttributeInheritConstraint extends AbstractMonoConstraint {
    @Override
    public void setElementAt(List container, int index, String value) {
        container.set(index, new EditorAttribute.Inherited());
    }

    @Override
    public void addElement(List container, String value) {
        container.add(new EditorAttribute.Inherited());
    }

    @Override
    public List<String> getPreview(List<Object> list) {
        List<String> output = new ArrayList<>();
        for (Object o : list) {
            EditorAttribute.Inherited in = ((EditorAttribute.Inherited) o);
            output.add("Source " + in.source + " Multiplier " + in.multiplier);
        }
        return output;
    }

    @Override
    public String getPreview(Object object) {
        return "Inheritance";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("§fAttribute Inheritance");
        instruction.add("Multiply attribute A with B and add to this attribute.");
        return instruction;
    }
}
