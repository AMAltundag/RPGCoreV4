package me.blutkrone.rpgcore.editor.constraint.reference.index;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.editor.index.EditorIndex;

import java.util.ArrayList;
import java.util.List;

public class JobConstraint extends AbstractIndexConstraint {

    @Override
    public EditorIndex<?, ?> getIndex() {
        return RPGCore.inst().getJobManager().getIndexJob();
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("§bJob");
        instruction.add("A job grants additional power scaling to a player,");
        instruction.add("thorough a passive tree and dedicated abilities.");
        instruction.add("");
        instruction.add("§cNever grant access to the same tree from two sources!");
        return instruction;
    }
}
