package me.blutkrone.rpgcore.hud.editor.constraint.bundle.mono;

import me.blutkrone.rpgcore.hud.editor.bundle.item.EditorAffixLimit;
import me.blutkrone.rpgcore.hud.editor.constraint.bundle.AbstractMonoConstraint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AffixLimitConstraint extends AbstractMonoConstraint {

    @Override
    public void setElementAt(List container, int index, String value) {
        container.set(index, new EditorAffixLimit());
    }

    @Override
    public void addElement(List container, String value) {
        container.add(new EditorAffixLimit());
    }

    @Override
    public List<String> getPreview(List<Object> list) {
        Map<String, Double> merged = new HashMap<>();
        for (Object o : list) {
            EditorAffixLimit eac = (EditorAffixLimit) o;
            merged.merge(eac.tag, eac.limit, (a, b) -> a + b);
        }
        List<String> output = new ArrayList<>();
        merged.forEach((k, v) -> {
            output.add("Tag " + k + " Limit " + v);
        });
        return output;
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Affix Limit");
        instruction.add("Caps affixes which share a given tag");
        instruction.add("Affix is capped based on lowest limit");
        return instruction;
    }

    @Override
    public String getPreview(Object object) {
        return "Affix Limit";
    }
}