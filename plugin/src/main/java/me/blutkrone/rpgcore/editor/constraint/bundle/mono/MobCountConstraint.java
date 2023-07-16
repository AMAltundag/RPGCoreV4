package me.blutkrone.rpgcore.editor.constraint.bundle.mono;

import me.blutkrone.rpgcore.editor.bundle.other.EditorMobCount;
import me.blutkrone.rpgcore.editor.constraint.bundle.AbstractMonoConstraint;

import java.util.ArrayList;
import java.util.List;

public class MobCountConstraint extends AbstractMonoConstraint {

    @Override
    public void setElementAt(List container, int index, String value) {
        container.set(index, new EditorMobCount());
    }

    @Override
    public void addElement(List container, String value) {
        container.add(new EditorMobCount());
    }

    @Override
    public List<String> getPreview(List<Object> list) {
        return new ArrayList<>();
    }

    @Override
    public String getPreview(Object object) {
        return "Mob Count";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Mob Count");
        instruction.add("Count based on mobs.");
        return instruction;
    }
}
