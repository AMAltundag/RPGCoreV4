package me.blutkrone.rpgcore.hud.editor.constraint.bundle.mono;

import me.blutkrone.rpgcore.hud.editor.bundle.item.EditorItemPrice;
import me.blutkrone.rpgcore.hud.editor.constraint.bundle.AbstractMonoConstraint;

import java.util.ArrayList;
import java.util.List;

public class ItemPriceConstraint extends AbstractMonoConstraint {
    @Override
    public void setElementAt(List container, int index, String value) {
        container.set(index, new EditorItemPrice());
    }

    @Override
    public void addElement(List container, String value) {
        container.add(new EditorItemPrice());
    }

    @Override
    public List<String> getPreview(List<Object> list) {
        List<String> output = new ArrayList<>();
        for (Object o : list) {
            EditorItemPrice in = ((EditorItemPrice) o);
            output.add("Item " + in.item + " Cost " + in.price + " Currency " + in.currency);
        }

        return output;
    }

    @Override
    public String getPreview(Object object) {
        return "Purchase";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Item With Price");
        instruction.add("Meant for a vendor NPC, allows you to assign");
        instruction.add("A price for purchase to an item.");
        instruction.add("");
        instruction.add("The currency is a bank group, but it has to be");
        instruction.add("Itemized in the player inventory.");
        return instruction;
    }
}
