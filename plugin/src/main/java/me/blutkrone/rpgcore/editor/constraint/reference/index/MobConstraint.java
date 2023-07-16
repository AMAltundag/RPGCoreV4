package me.blutkrone.rpgcore.editor.constraint.reference.index;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.editor.index.EditorIndex;

import java.util.ArrayList;
import java.util.List;

public class MobConstraint extends AbstractIndexConstraint {
    @Override
    public EditorIndex<?, ?> getIndex() {
        return RPGCore.inst().getMobManager().getIndex();
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Mob");
        instruction.add("Template for mobs spawned into the world.");
        return instruction;
    }
}
