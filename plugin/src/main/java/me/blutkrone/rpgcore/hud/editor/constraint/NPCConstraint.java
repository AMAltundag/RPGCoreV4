package me.blutkrone.rpgcore.hud.editor.constraint;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.hud.editor.IEditorConstraint;

import java.util.ArrayList;
import java.util.List;

public class NPCConstraint implements IEditorConstraint {

    @Override
    public List<String> getHint(String value) {
        value = value.toLowerCase();
        List<String> matched = new ArrayList<>();
        for (String id : RPGCore.inst().getNPCManager().getIndex().getKeys()) {
            if (id.startsWith(value)) {
                matched.add(id);
            }
        }
        return matched;
    }

    @Override
    public boolean isDefined(String value) {
        return RPGCore.inst().getNPCManager().getIndex().has(value);
    }

    @Override
    public void extend(String value) {
        RPGCore.inst().getNPCManager().getIndex().get(value);
    }

    @Override
    public boolean canExtend() {
        return true;
    }

    @Override
    public String getConstraintAt(List container, int index) {
        return (String) container.get(index);
    }

    @Override
    public void setElementAt(List container, int index, String value) {
        container.set(index, value);
    }

    @Override
    public void addElement(List container, String value) {
        container.add(value);
    }

    @Override
    public Object asTypeOf(String value) {
        return value;
    }

    @Override
    public String toTypeOf(Object value) {
        return ((String) value);
    }

    @Override
    public List<String> getPreview(List<Object> list) {
        List<String> preview = new ArrayList<>();

        if (list.size() <= 16) {
            for (int i = 0; i < list.size(); i++) {
                preview.add(i + ": " + list.get(i));
            }
        } else {
            for (int i = 0; i < 16; i++) {
                preview.add(i + ": " + list.get(i));
            }

            preview.add("... And " + (list.size()-16) + " More!");
        }

        return preview;
    }

    @Override
    public String getPreview(Object object) {
        return String.valueOf(object);
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("§fNPC");
        instruction.add("§fEntity meant for miscellaneous purposes. Use for Quests,");
        instruction.add("§fVendors, Quests, Background. Not meant for combat.");
        return instruction;
    }
}
