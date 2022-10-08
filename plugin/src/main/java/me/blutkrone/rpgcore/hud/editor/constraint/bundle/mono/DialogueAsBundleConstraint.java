package me.blutkrone.rpgcore.hud.editor.constraint.bundle.mono;

import me.blutkrone.rpgcore.hud.editor.bundle.other.EditorDialogue;
import me.blutkrone.rpgcore.hud.editor.constraint.bundle.AbstractMonoConstraint;

import java.util.ArrayList;
import java.util.List;

public class DialogueAsBundleConstraint extends AbstractMonoConstraint {
    @Override
    public void setElementAt(List container, int index, String value) {
        container.set(index, new EditorDialogue());
    }

    @Override
    public void addElement(List container, String value) {
        container.add(new EditorDialogue());
    }

    @Override
    public List<String> getPreview(List<Object> list) {
        List<String> output = new ArrayList<>();
        for (Object o : list) {
            EditorDialogue in = ((EditorDialogue) o);
            output.add(in.lc_text);
        }
        return output;
    }

    @Override
    public String getPreview(Object object) {
        return "Dialogue";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Dialogue");
        instruction.add("A dialogue meant for NPC/Quest, do note that while");
        instruction.add("Your dialogue can branch, the result is the same.");
        return instruction;
    }
}
