package me.blutkrone.rpgcore.hud.editor.design;

import me.blutkrone.rpgcore.hud.editor.annotation.EditorCategory;
import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A category to group up multiple elements of one class.
 */
public class DesignCategory {
    // the designer which we are contained by
    private Design container;
    // the icon of this category
    private ItemStack icon;
    // the name of this category
    private String name;
    // the elements under this category
    private List<DesignElement> elements = new ArrayList<>();
    // a distinct unique ID of the category
    private UUID uuid = UUID.randomUUID();

    /**
     * A category to group up multiple elements of one class.
     */
    public DesignCategory(Design container) {
        this.container = container;

        this.icon = ItemBuilder.of(Material.BOOKSHELF)
                .name("§aUnnamed Category")
                .build();
        this.name = "§aUnnamed Category";
    }

    /**
     * A category to group up multiple elements of one class.
     *
     * @param field the field with the annotation.
     */
    public DesignCategory(Design container, Field field) {
        this.container = container;

        EditorCategory annotation = field.getAnnotation(EditorCategory.class);
        ItemBuilder builder = ItemBuilder.of(annotation.icon());
        builder.name(annotation.info()[0]);
        for (int i = 1; i < annotation.info().length; i++) {
            builder.name(annotation.info()[i]);
        }
        this.icon = builder.build();
        this.name = annotation.info()[0];
    }

    @Override
    public String toString() {
        return String.format("DesignCategory{design=%s;elements=%s;name=%s}", this.container, this.elements.size(), this.name);
    }

    /**
     * Allocate an element to be underneath this category.
     *
     * @param element which element to add to category.
     */
    public void addElement(DesignElement element) {
        this.elements.add(element);
    }

    /**
     * Fetch all elements under this category.
     *
     * @return all elements in category.
     */
    public List<DesignElement> getElements() {
        return elements;
    }

    /**
     * Check if no element under the category is visible.
     *
     * @param object an object described by this category.
     * @return true if this object hides the whole category.
     */
    public boolean isHidden(IEditorBundle object) {
        // check if any of our children are visible
        for (DesignElement element : this.elements) {
            if (!element.isHidden(object)) {
                return false;
            }
        }
        // otherwise we remain invisible here
        return true;
    }

    /**
     * The icon representing this category.
     *
     * @return the itemized description of the category
     */
    public ItemStack getIcon() {
        return icon;
    }

    /**
     * The name of this category.
     *
     * @return name of category.
     */
    public String getName() {
        return name;
    }

    /**
     * Retrieve the unique identifier of the category.
     *
     * @return unique category identifier.
     */
    public UUID getUUID() {
        return uuid;
    }
}
