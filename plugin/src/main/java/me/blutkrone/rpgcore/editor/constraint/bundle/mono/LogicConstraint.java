package me.blutkrone.rpgcore.editor.constraint.bundle.mono;

import me.blutkrone.rpgcore.editor.bundle.other.EditorMobLogic;
import me.blutkrone.rpgcore.editor.constraint.bundle.AbstractMonoConstraint;

import java.util.ArrayList;
import java.util.List;

public class LogicConstraint extends AbstractMonoConstraint {

    @Override
    public void setElementAt(List container, int index, String value) {
        container.set(index, new EditorMobLogic());
    }

    @Override
    public void addElement(List container, String value) {
        container.add(new EditorMobLogic());
    }

    @Override
    public List<String> getPreview(List<Object> list) {
        return new ArrayList<>();
    }

    @Override
    public String getPreview(Object object) {
        return "Logic";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Creature Logic");
        instruction.add("The first logic we can find is invoked, priority allows to");
        instruction.add("Check some logic before another. Logic that started will be");
        instruction.add("Expected to finish first.");
        instruction.add("");
        instruction.add("Groups allow you to prevent running conflicting logic at once.");
        return instruction;
    }
}
