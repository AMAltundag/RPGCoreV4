package me.blutkrone.rpgcore.hud.editor.constraint.bundle.mono;

import me.blutkrone.rpgcore.hud.editor.bundle.other.EditorAction;
import me.blutkrone.rpgcore.hud.editor.constraint.bundle.AbstractMonoConstraint;

import java.util.ArrayList;
import java.util.List;

public class ActionConstraint extends AbstractMonoConstraint {
    @Override
    public void setElementAt(List container, int index, String value) {
        container.set(index, new EditorAction());
    }

    @Override
    public void addElement(List container, String value) {
        container.add(new EditorAction());
    }

    @Override
    public List<String> getPreview(List<Object> list) {
        List<String> output = new ArrayList<>();
        for (Object o : list) {
            EditorAction action = (EditorAction) o;
            output.add(String.format("Action (Mechanics: %s, Selectors: %s)", action.mechanics.size(), action.selectors.size()));
        }
        return output;
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Action");
        instruction.add("Mechanics are invoked to the final subset picked by");
        instruction.add("the selectors");
        return instruction;
    }

    @Override
    public String getPreview(Object object) {
        return "Action";
    }
}
