package me.blutkrone.rpgcore.hud.editor.constraint.bundle.mono;

import me.blutkrone.rpgcore.hud.editor.bundle.other.EditorSkillInfo;
import me.blutkrone.rpgcore.hud.editor.constraint.bundle.AbstractMonoConstraint;

import java.util.ArrayList;
import java.util.List;

public class SkillInfoConstraint extends AbstractMonoConstraint {

    @Override
    public void setElementAt(List container, int index, String value) {
        container.set(index, new EditorSkillInfo());
    }

    @Override
    public void addElement(List container, String value) {
        container.add(new EditorSkillInfo());
    }

    @Override
    public List<String> getPreview(List<Object> list) {
        return new ArrayList<>();
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Skill Info");
        instruction.add("This is meant to quantify information about the skill, the");
        instruction.add("Information about the skill binding is automated.");
        return instruction;
    }

    @Override
    public String getPreview(Object object) {
        return "Info";
    }
}
