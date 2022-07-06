package me.blutkrone.rpgcore.hud.editor.constraint;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.hud.editor.EditorIndex;
import me.blutkrone.rpgcore.hud.editor.FocusQueue;
import me.blutkrone.rpgcore.hud.editor.IEditorConstraint;
import me.blutkrone.rpgcore.hud.editor.root.EditorModifier;
import me.blutkrone.rpgcore.item.modifier.CoreModifier;
import me.blutkrone.rpgcore.nms.api.menu.IChestMenu;

import java.util.ArrayList;
import java.util.List;

public class ImplicitModifierConstraint implements IEditorConstraint {
    @Override
    public List<String> getHint(String value) {
        value = value.toLowerCase();
        List<String> matched = new ArrayList<>();
        for (CoreModifier modifier : RPGCore.inst().getItemManager().getModifierIndex().getAll()) {
            if (modifier.getId().startsWith(value) && modifier.isImplicit()) {
                matched.add(modifier.getId());
            }
        }

        return matched;
    }

    @Override
    public boolean isDefined(String value) {
        return RPGCore.inst().getItemManager().getModifierIndex().has(value);
    }

    @Override
    public void extend(String value) {
        EditorIndex<CoreModifier, EditorModifier> index = RPGCore.inst().getItemManager().getModifierIndex();
        index.create(value, EditorModifier::setImplicit);
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
        instruction.add("§fModifier");
        instruction.add("Affects the parameters of the entity which");
        instruction.add("holds the modifier.");
        return instruction;
    }

    @Override
    public void editDelegate(IChestMenu source, Object value) {
        // retrieve the instance we are editing
        EditorModifier edit = RPGCore.inst().getItemManager().getModifierIndex().edit((String) value);
        // put the focus on that instance, and rebuild.
        FocusQueue focus = source.getData("focus");
        focus.focus(edit);
        source.rebuild();
    }
}
