package me.blutkrone.rpgcore.hud.editor.constraint.reference.index;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.hud.editor.index.EditorIndex;

import java.util.ArrayList;
import java.util.List;

public class TreeConstraint extends AbstractIndexConstraint {

    @Override
    public EditorIndex<?, ?> getIndex() {
        return RPGCore.inst().getPassiveManager().getTreeIndex();
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Passive Tree");
        instruction.add("Access to a passive tree, please note that a tree needs");
        instruction.add("A third-party to actually apply it.");
        return instruction;
    }
}
