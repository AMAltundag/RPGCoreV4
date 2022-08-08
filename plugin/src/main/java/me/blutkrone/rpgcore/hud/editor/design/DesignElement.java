package me.blutkrone.rpgcore.hud.editor.design;

import me.blutkrone.rpgcore.hud.editor.FocusQueue;
import me.blutkrone.rpgcore.hud.editor.annotation.EditorHideWhen;
import me.blutkrone.rpgcore.hud.editor.annotation.value.*;
import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.hud.editor.design.designs.*;
import me.blutkrone.rpgcore.nms.api.menu.IChestMenu;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * A singular element
 */
public class DesignElement {
    // which field is bound to this element
    private final Field field;
    // which design is containing this element
    private Design container;
    // rules on when to hide this element
    private boolean hide_when_invert = false;
    private String hide_when_field = "";
    private Set<String> hide_when_value = new HashSet<>();
    // a tool dedicated to manipulating an instance
    private IDesignFieldEditor editor;
    // a unique identifier for this element
    private UUID uuid = UUID.randomUUID();

    /**
     * An element of the container which can be edited.
     *
     * @param container which design contains this element.
     * @param field     the field to manipulate
     * @throws IllegalArgumentException should the field not be editable.
     */
    public DesignElement(Design container, Field field) throws IllegalArgumentException {
        // what exactly are we editing
        this.container = container;
        this.field = field;
        // retrieve condition to hide field
        if (field.isAnnotationPresent(EditorHideWhen.class)) {
            EditorHideWhen annotation = field.getAnnotation(EditorHideWhen.class);
            this.hide_when_invert = annotation.invert();
            this.hide_when_field = annotation.field();
            this.hide_when_value = new HashSet<>(Arrays.asList(annotation.value()));
        }
        // link a design editor that can manipulate a field
        if (field.isAnnotationPresent(EditorList.class)) {
            editor = new DesignList(this, field);
        } else if (field.isAnnotationPresent(EditorBundle.class)) {
            editor = new DesignBundle(this, field);
        } else if (field.isAnnotationPresent(EditorBoolean.class)) {
            editor = new DesignBoolean(this, field);
        } else if (field.isAnnotationPresent(EditorColor.class)) {
            editor = new DesignColor(this, field);
        } else if (field.isAnnotationPresent(EditorNumber.class)) {
            editor = new DesignNumber(this, field);
        } else if (field.isAnnotationPresent(EditorWrite.class)) {
            editor = new DesignWrite(this, field);
        } else {
            throw new UnsupportedOperationException("Field " + field.getName() + " in " + container.getClazz().getName() + " has no editor annotation!");
        }
    }

    @Override
    public String toString() {
        return String.format("DesignElement{field=%s;category=%s}", this.field.getName(), this.container);
    }

    /**
     * Check if this field meets the condition to be visible.
     *
     * @param object the object to base the filter on
     * @return true if the element is to be hidden away
     */
    public boolean isHidden(IEditorBundle object) {
        // skip if the field is undefined
        if (this.hide_when_field.isBlank())
            return false;

        // check if we are to hide the field
        Class where = this.container.getClazz();
        while (where != Object.class) {
            try {
                Field f = where.getDeclaredField(this.hide_when_field); // throws an exception, which bumps us up a class
                f.setAccessible(true);
                String str = String.valueOf(f.get(object));
                return this.hide_when_value.contains(str) ^ this.hide_when_invert;
            } catch (NoSuchFieldException | IllegalAccessException ignored) {
                // ignored
            }

            where = where.getSuperclass();
        }

        return false;
    }

    /**
     * Open an editor bound to the given bundle.
     *
     * @param bundle   the object we are editing.
     * @param viewer   who will receive the editor.
     * @param previous the preceding editor we came from.
     * @param focus    a queue to track our scope
     */
    public void edit(IEditorBundle bundle, Player viewer, IChestMenu previous, FocusQueue focus) {
        this.editor.edit(bundle, viewer, previous, focus);
    }

    /**
     * An icon to summarize the information of the field on the bundle.
     *
     * @return the icon of this design element.
     */
    public ItemStack getDesignIcon(IEditorBundle bundle) {
        try {
            return this.editor.getIcon(bundle);
        } catch (Exception ex) {
            return ItemBuilder.of(Material.BARRIER)
                    .name("Unexpected Error")
                    .appendLore("Field: " + field.getName())
                    .appendLore("Class: " + container.getClazz().getSimpleName())
                    .appendLore("Error: " + ex.getClass())
                    .build();
        }
    }

    /**
     * A user-readable name of this design element.
     *
     * @return the name of the design element.
     */
    public String getDesignName() {
        return this.editor.getName();
    }

    /**
     * A user-readable value of the given bundle.
     *
     * @param bundle the element we read from
     * @return string-ified
     */
    public String getDesignInfo(IEditorBundle bundle) {
        try {
            return this.editor.getInfo(bundle);
        } catch (Exception ex) {
            ex.printStackTrace();
            return "???";
        }
    }

    /**
     * Retrieve the unique identifier.
     *
     * @return the unique identifier for this element.
     */
    public UUID getUUID() {
        return uuid;
    }
}
