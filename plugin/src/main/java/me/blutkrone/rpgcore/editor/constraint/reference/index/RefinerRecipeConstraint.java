package me.blutkrone.rpgcore.editor.constraint.reference.index;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.editor.index.EditorIndex;

import java.util.ArrayList;
import java.util.List;

public class RefinerRecipeConstraint extends AbstractIndexConstraint {

    @Override
    public EditorIndex<?, ?> getIndex() {
        return RPGCore.inst().getItemManager().getRefineIndex();
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Refinement Recipe");
        instruction.add("Establishes a processing path for collected resources.");
        return instruction;
    }
}
