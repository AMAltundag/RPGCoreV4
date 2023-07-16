package me.blutkrone.rpgcore.editor.constraint.bundle.mono;

import me.blutkrone.rpgcore.editor.bundle.other.EditorTalk;
import me.blutkrone.rpgcore.editor.constraint.bundle.AbstractMonoConstraint;

import java.util.ArrayList;
import java.util.List;

public class TalkConstraint extends AbstractMonoConstraint {

    @Override
    public void setElementAt(List container, int index, String value) {
        container.set(index, new EditorTalk());
    }

    @Override
    public void addElement(List container, String value) {
        container.add(new EditorTalk());
    }

    @Override
    public List<String> getPreview(List<Object> list) {
        List<String> output = new ArrayList<>();
        for (Object o : list) {
            EditorTalk talk = (EditorTalk) o;
            output.add(talk.dialogue + " with " + talk.npc);
        }
        return output;
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Talk");
        instruction.add("Used by quests to show dialogue for a certain NPC.");
        return instruction;
    }

    @Override
    public String getPreview(Object object) {
        return "Talk";
    }
}
