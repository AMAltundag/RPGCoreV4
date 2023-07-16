package me.blutkrone.rpgcore.editor.design;

import me.blutkrone.rpgcore.editor.annotation.EditorCategory;
import me.blutkrone.rpgcore.editor.annotation.EditorName;
import me.blutkrone.rpgcore.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.editor.root.IEditorRoot;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * The editor 'design' of a singular class, describing how
 * it should be presented for editing.
 *
 * @see IEditorBundle for a generic class in an editor
 * @see IEditorRoot for a container class linked to an index
 */
public class Design {

    // which class are we the design of
    private final Class clazz;
    // all categories belonging to this design
    private List<DesignCategory> categories = new ArrayList<>();
    private String name = "Unnamed Design";
    private String[] description = {};

    /**
     * This is a wrapper class which extracts the annotation structure from
     * a class, and translates it into a format which the editor can use to
     * build an editor for it.
     * <p>
     *
     * @param design the class whose design we want to extract.
     */
    public <K extends IEditorBundle> Design(Class<K> design) {
        // track which class is being designed
        this.clazz = design;

        // retrieve basic information on class to design
        EditorName name_annotation = design.getAnnotation(EditorName.class);
        if (name_annotation != null) {
            this.name = name_annotation.name();
        }
        EditorTooltip tooltip_annotation = design.getAnnotation(EditorTooltip.class);
        if (tooltip_annotation != null) {
            this.description = tooltip_annotation.tooltip();
        }

        // retrieve info/tooltip annotation to use
        Class current = design;
        while (current != Object.class) {
            // create a category by default
            DesignCategory category = new DesignCategory(this);
            this.categories.add(category);
            // work over all fields declared
            for (Field field : current.getDeclaredFields()) {
                // create a new category if necessary
                EditorCategory new_category = field.getAnnotation(EditorCategory.class);
                if (new_category != null) {
                    // swap with the new category
                    category = new DesignCategory(this, field);
                    this.categories.add(category);
                }
                // create design element if we got none
                try {
                    category.addElement(new DesignElement(this, field));
                } catch (Exception ignored) {

                }
            }

            current = current.getSuperclass();
        }

        // delete a category if it is empty
        this.categories.removeIf(c -> c.getElements().isEmpty());
    }

    @Override
    public String toString() {
        return String.format("Design{class=%s;categories=%s}", this.clazz.getSimpleName(), this.categories.size());
    }

    /**
     * Fetch all categories present under this design.
     *
     * @return categories under this design.
     */
    public List<DesignCategory> getCategories() {
        return categories;
    }

    /**
     * A description of this design element.
     *
     * @return tooltip info of the class we are designing.
     */
    public String[] getDescription() {
        return description;
    }

    /**
     * A user-readable name for this design base.
     *
     * @return name of the class we are designing.
     */
    public String getName() {
        return name;
    }

    /**
     * The class which this designer is intended for.
     *
     * @return class we are designing.
     */
    public Class getClazz() {
        return clazz;
    }
}
