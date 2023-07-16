package me.blutkrone.rpgcore.editor.constraint.bundle.mono;

import me.blutkrone.rpgcore.editor.bundle.other.EditorRequirement;
import me.blutkrone.rpgcore.editor.constraint.bundle.AbstractMonoConstraint;

import java.util.ArrayList;
import java.util.List;

public class RequirementConstraint extends AbstractMonoConstraint {

    @Override
    public void setElementAt(List container, int index, String value) {
        container.set(index, new EditorRequirement());
    }

    @Override
    public void addElement(List container, String value) {
        container.add(new EditorRequirement());
    }

    @Override
    public List<String> getPreview(List<Object> list) {
        return new ArrayList<>();
    }

    @Override
    public String getPreview(Object object) {
        return "Requirement";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Requirement");
        instruction.add("A requirement to have access to something.");
        return instruction;
    }
}
