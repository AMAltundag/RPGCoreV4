package me.blutkrone.rpgcore.hud.editor.constraint.bundle.mono;

import me.blutkrone.rpgcore.hud.editor.bundle.other.EditorDialogueChoice;
import me.blutkrone.rpgcore.hud.editor.constraint.bundle.AbstractMonoConstraint;

import java.util.ArrayList;
import java.util.List;

public class DialogueChoiceConstraint extends AbstractMonoConstraint {
    @Override
    public void setElementAt(List container, int index, String value) {
        container.set(index, new EditorDialogueChoice());
    }

    @Override
    public void addElement(List container, String value) {
        container.add(new EditorDialogueChoice());
    }

    @Override
    public List<String> getPreview(List<Object> list) {
        List<String> output = new ArrayList<>();
        for (Object o : list) {
            EditorDialogueChoice in = ((EditorDialogueChoice) o);
            output.add(in.lc_text);
        }
        return output;
    }

    @Override
    public String getPreview(Object object) {
        return "Choice";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("§fDialogue Choice");
        instruction.add("A choice for dialogue, do note that this is only meant");
        instruction.add("for narrative steering - choices in dialogue will not be");
        instruction.add("able to branch quest progress.");
        return instruction;
    }
}