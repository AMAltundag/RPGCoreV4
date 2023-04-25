package me.blutkrone.rpgcore.hud.editor.constraint.bundle.mono;

import me.blutkrone.rpgcore.hud.editor.bundle.other.EditorStringAndNumber;
import me.blutkrone.rpgcore.hud.editor.constraint.bundle.AbstractMonoConstraint;

import java.util.ArrayList;
import java.util.List;

public class StringAndNumberConstraint extends AbstractMonoConstraint {

    @Override
    public void setElementAt(List container, int index, String value) {
        container.set(index, new EditorStringAndNumber());
    }

    @Override
    public void addElement(List container, String value) {
        container.add(new EditorStringAndNumber());
    }

    @Override
    public List<String> getPreview(List<Object> list) {
        List<String> output = new ArrayList<>();
        for (Object o : list) {
            EditorStringAndNumber in = ((EditorStringAndNumber) o);
            output.add(in.string + " = " + in.number);
        }
        return output;
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("String And Number");
        instruction.add("A string associated with a number.");
        return instruction;
    }

    @Override
    public String getPreview(Object object) {
        return "Pair";
    }

}
