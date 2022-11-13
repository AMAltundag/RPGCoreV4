package me.blutkrone.rpgcore.hud.editor.constraint.other;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.api.damage.IDamageManager;
import me.blutkrone.rpgcore.hud.editor.IEditorConstraint;

import java.util.ArrayList;
import java.util.List;

public class DamageElementConstraint implements IEditorConstraint {

    @Override
    public List<String> getHint(String value) {
        IDamageManager manager = RPGCore.inst().getDamageManager();

        value = value.toUpperCase();
        List<String> matched = new ArrayList<>();
        for (String type : manager.getElementIds()) {
            if (type.startsWith(value)) {
                matched.add(type);
            }
        }
        return matched;
    }

    @Override
    public boolean isDefined(String value) {
        IDamageManager manager = RPGCore.inst().getDamageManager();
        return manager.getElementIds().contains(value);
    }

    @Override
    public void extend(String value) {
        // unsupported
    }

    @Override
    public boolean canExtend() {
        return false; // unsupported
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
        return (String) value;
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

            preview.add("... And " + (list.size() - 16) + " More!");
        }

        return preview;
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Damage Element");
        instruction.add("The element of the damage inflicted.");
        instruction.add("");
        instruction.add("§cAdditional information on the wiki.");
        return instruction;
    }

    @Override
    public String getPreview(Object object) {
        return String.valueOf(object);
    }

}
