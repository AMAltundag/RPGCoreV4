package me.blutkrone.rpgcore.editor.constraint.reference.index;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.editor.index.EditorIndex;

import java.util.ArrayList;
import java.util.List;

public class ImplicitModifierConstraint extends AbstractIndexConstraint {

    @Override
    public EditorIndex<?, ?> getIndex() {
        return RPGCore.inst().getItemManager().getModifierIndex();
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Modifier");
        instruction.add("Affects the parameters of the entity which");
        instruction.add("holds the modifier.");
        return instruction;
    }
}
