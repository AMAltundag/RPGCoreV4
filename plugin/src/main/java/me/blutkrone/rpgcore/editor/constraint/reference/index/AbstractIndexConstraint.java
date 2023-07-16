package me.blutkrone.rpgcore.editor.constraint.reference.index;

import me.blutkrone.rpgcore.editor.IEditorConstraint;
import me.blutkrone.rpgcore.editor.index.EditorIndex;
import me.blutkrone.rpgcore.editor.root.IEditorRoot;
import me.blutkrone.rpgcore.menu.EditorMenu;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractIndexConstraint implements IEditorConstraint {

    @Override
    public ItemStack getIconForList(String id) {
        if (getIndex().has(id)) {
            return getIndex().edit(id).getPreview();
        } else {
            return IEditorConstraint.super.getIconForList(id);
        }
    }

    /**
     * Which index we are linked to.
     *
     * @return index we are linked to
     */
    public abstract EditorIndex<?, ?> getIndex();

    @Override
    public List<String> getHint(String value) {
        value = value.toLowerCase();
        List<String> matched = new ArrayList<>();
        for (String translated : getIndex().getKeys()) {
            if (translated.startsWith(value)) {
                matched.add(translated);
            }
        }
        return matched;
    }

    @Override
    public boolean isDefined(String value) {
        return getIndex().has(value);
    }

    @Override
    public void extend(String value) {
        getIndex().get(value);
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

            preview.add("... And " + (list.size() - 16) + " More!");
        }

        return preview;
    }

    @Override
    public String getPreview(Object object) {
        return String.valueOf(object);
    }

    @Override
    public boolean doListFocus(EditorMenu editor, Object element) {
        // grab the ID we are focusing
        String id = element.toString();
        // create an editor element to use
        IEditorRoot<?> root = getIndex().edit(id);
        // put focus on the selected element
        editor.getFocus().setFocusToRoot(id, root, getIndex());
        // flush the editor
        editor.getMenu().queryRebuild();
        // we can always focus from here
        return true;
    }
}