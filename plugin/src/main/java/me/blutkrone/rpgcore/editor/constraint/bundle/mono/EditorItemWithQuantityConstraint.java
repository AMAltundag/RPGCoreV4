package me.blutkrone.rpgcore.editor.constraint.bundle.mono;

import me.blutkrone.rpgcore.editor.bundle.item.EditorItemWithQuantity;
import me.blutkrone.rpgcore.editor.constraint.bundle.AbstractMonoConstraint;

import java.util.ArrayList;
import java.util.List;

public class EditorItemWithQuantityConstraint extends AbstractMonoConstraint {
    @Override
    public void setElementAt(List container, int index, String value) {
        container.set(index, new EditorItemWithQuantity());
    }

    @Override
    public void addElement(List container, String value) {
        container.add(new EditorItemWithQuantity());
    }

    @Override
    public List<String> getPreview(List<Object> list) {
        List<String> output = new ArrayList<>();
        for (Object o : list) {
            EditorItemWithQuantity in = ((EditorItemWithQuantity) o);
            output.add("Item " + in.item + " Quantity " + in.quantity);
        }
        return output;
    }

    @Override
    public String getPreview(Object object) {
        return "Crafting";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Item With Quantity");
        instruction.add("Check if at-least the given quantity of the item");
        instruction.add("Is available to use.");
        return instruction;
    }
}
