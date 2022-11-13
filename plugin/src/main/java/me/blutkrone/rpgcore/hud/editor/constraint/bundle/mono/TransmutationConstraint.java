package me.blutkrone.rpgcore.hud.editor.constraint.bundle.mono;

import me.blutkrone.rpgcore.hud.editor.bundle.other.EditorTransmutation;
import me.blutkrone.rpgcore.hud.editor.constraint.bundle.AbstractMonoConstraint;

import java.util.ArrayList;
import java.util.List;

public class TransmutationConstraint extends AbstractMonoConstraint {

    @Override
    public void setElementAt(List container, int index, String value) {
        container.set(index, new EditorTransmutation());
    }

    @Override
    public void addElement(List container, String value) {
        container.add(new EditorTransmutation());
    }

    @Override
    public List<String> getPreview(List<Object> list) {
        return new ArrayList<>();
    }

    @Override
    public String getPreview(Object object) {
        return "Transmutation";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Transmutation");
        instruction.add("Disguises a block as another one.");
        instruction.add("");
        instruction.add("Do NOT make any collider shape alteration!");
        return instruction;
    }
}
