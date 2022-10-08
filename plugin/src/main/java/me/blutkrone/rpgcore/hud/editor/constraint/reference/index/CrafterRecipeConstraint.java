package me.blutkrone.rpgcore.hud.editor.constraint.reference.index;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.hud.editor.index.EditorIndex;

import java.util.ArrayList;
import java.util.List;

public class CrafterRecipeConstraint extends AbstractIndexConstraint {

    @Override
    public EditorIndex<?, ?> getIndex() {
        return RPGCore.inst().getItemManager().getCraftIndex();
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Crafting Recipe");
        instruction.add("Combine multiple items into another item.");
        return instruction;
    }
}
