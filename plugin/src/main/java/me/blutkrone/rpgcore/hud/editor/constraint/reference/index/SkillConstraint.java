package me.blutkrone.rpgcore.hud.editor.constraint.reference.index;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.hud.editor.index.EditorIndex;

import java.util.ArrayList;
import java.util.List;

public class SkillConstraint extends AbstractIndexConstraint {
    @Override
    public EditorIndex<?, ?> getIndex() {
        return RPGCore.inst().getSkillManager().getIndex();
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("§bSkill");
        instruction.add("An ability which can be invoked by player and monster");
        instruction.add("alike, the outcome depends on the configuration.");
        return instruction;
    }
}
