package me.blutkrone.rpgcore.editor.constraint.bundle.mono;

import me.blutkrone.rpgcore.editor.bundle.other.EditorBehaviour;
import me.blutkrone.rpgcore.editor.constraint.bundle.AbstractMonoConstraint;

import java.util.ArrayList;
import java.util.List;

public class BehaviourConstraint extends AbstractMonoConstraint {

    @Override
    public void setElementAt(List container, int index, String value) {
        container.set(index, new EditorBehaviour());
    }

    @Override
    public void addElement(List container, String value) {
        container.add(new EditorBehaviour());
    }

    @Override
    public List<String> getPreview(List<Object> list) {
        return new ArrayList<>();
    }

    @Override
    public String getPreview(Object object) {
        return "Behaviour";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Behaviour");
        instruction.add("A passive behaviour attached to an entity, which can run");
        instruction.add("Run actions once a trigger condition was archived.");
        return instruction;
    }
}
