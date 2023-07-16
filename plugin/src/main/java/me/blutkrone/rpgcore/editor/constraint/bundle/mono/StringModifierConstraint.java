package me.blutkrone.rpgcore.editor.constraint.bundle.mono;

import me.blutkrone.rpgcore.editor.bundle.modifier.EditorModifierString;
import me.blutkrone.rpgcore.editor.constraint.bundle.AbstractMonoConstraint;

import java.util.ArrayList;
import java.util.List;

public class StringModifierConstraint extends AbstractMonoConstraint {

    @Override
    public void setElementAt(List container, int index, String value) {
        container.set(index, new EditorModifierString());
    }

    @Override
    public void addElement(List container, String value) {
        container.add(new EditorModifierString());
    }

    @Override
    public List<String> getPreview(List<Object> list) {
        return new ArrayList<>();
    }

    @Override
    public String getPreview(Object object) {
        return "String Modifier";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("String Modifier");
        instruction.add("Evaluates into a string.");
        return instruction;
    }
}
