package me.blutkrone.rpgcore.editor.design.designs;

import me.blutkrone.rpgcore.editor.IEditorConstraint;
import me.blutkrone.rpgcore.editor.annotation.value.EditorList;
import me.blutkrone.rpgcore.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.editor.design.DesignElement;
import me.blutkrone.rpgcore.menu.EditorMenu;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.util.List;

/**
 * A shared base class which allows the usage of any design-able
 * class as a list.
 */
public class DesignList implements IDesignFieldEditor {

    // generic header information
    private DesignElement element;
    private Field field;
    // name of the field
    private String name;
    // cap at a maximum of 1 element
    private boolean singleton;
    // constraint of the value
    private IEditorConstraint constraint;

    public DesignList(DesignElement element, Field field) {
        this.element = element;
        this.field = field;
        this.name = field.getAnnotation(EditorList.class).name();
        this.singleton = field.getAnnotation(EditorList.class).singleton();
        try {
            Class<? extends IEditorConstraint> clazz = field.getAnnotation(EditorList.class).constraint();
            this.constraint = clazz.getConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void edit(IEditorBundle bundle, Player viewer, EditorMenu editor, boolean wasShiftFocus) {
        editor.getFocus().setFocusToList(bundle, this);
        editor.getMenu().queryRebuild();
    }

    @Override
    public String getInfo(IEditorBundle bundle) throws Exception {
        return "x" + getList(bundle).size();
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public ItemStack getIcon(IEditorBundle bundle) throws Exception {
        List<Object> list = getList(bundle);
        return ItemBuilder.of(Material.BOOKSHELF)
                .name("§f" + this.name)
                .appendLore("§fList With " + list.size() + " Elements")
                .appendLore(this.constraint.getPreview(list))
                .build();
    }

    /**
     * Caps the list to a single element at most.
     *
     * @return true if we are capped to a single value.
     */
    public boolean isSingleton() {
        return singleton;
    }

    /**
     * Retrieve the constraint for the values of the list.
     *
     * @return how to constrain list values
     */
    public IEditorConstraint getConstraint() {
        return constraint;
    }

    /**
     * Retrieve the value list we are operating on.
     *
     * @param bundle which bundle to extract from
     * @return list of values that were extracted
     */
    public List<Object> getList(IEditorBundle bundle) {
        try {
            return (List<Object>) field.get(bundle);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("bundle " + bundle.getClass() + " has no/bad field " + field.getName());
        }
    }
}