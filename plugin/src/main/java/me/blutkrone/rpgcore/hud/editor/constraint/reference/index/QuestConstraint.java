package me.blutkrone.rpgcore.hud.editor.constraint.reference.index;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.hud.editor.index.EditorIndex;

import java.util.ArrayList;
import java.util.List;

public class QuestConstraint extends AbstractIndexConstraint {

    @Override
    public EditorIndex<?, ?> getIndex() {
        return RPGCore.inst().getQuestManager().getIndexQuest();
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Quest");
        instruction.add("A series of tasks for a player to complete. A quest is");
        instruction.add("Completed once the rewards have been claimed.");
        instruction.add("");
        instruction.add("Quests are designed to be linear, if you wish to branch");
        instruction.add("A quest create two follow ups and make their completion");
        instruction.add("Abandon each other.");
        return instruction;
    }
}
