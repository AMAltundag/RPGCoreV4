package me.blutkrone.rpgcore.hud.editor.constraint;

import me.blutkrone.rpgcore.hud.editor.bundle.EditorLoot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LootConstraint extends AbstractMonoConstraint {

    @Override
    public void setElementAt(List container, int index, String value) {
        container.set(index, new EditorLoot());
    }

    @Override
    public void addElement(List container, String value) {
        container.add(new EditorLoot());
    }

    @Override
    public List<String> getPreview(List<Object> list) {
        Map<String, Double> merged = new HashMap<>();
        for (Object o : list) {
            EditorLoot eac = (EditorLoot) o;
            merged.merge(eac.tag, eac.weight, (a,b) -> a+b);
        }
        List<String> output = new ArrayList<>();
        merged.forEach((k, v) -> {
            output.add("Tag " + k + " Weight " + v);
        });
        return output;
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("§bLoot");
        instruction.add("Roll a random item which matches with a tag, all weights");
        instruction.add("are summed and multiplied with the base weight of the item.");
        return instruction;
    }

    @Override
    public String getPreview(Object object) {
        return "Loot";
    }
}