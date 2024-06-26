package me.blutkrone.rpgcore.editor.design.designs;

import me.blutkrone.rpgcore.editor.annotation.value.EditorBoolean;
import me.blutkrone.rpgcore.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.editor.design.DesignElement;
import me.blutkrone.rpgcore.menu.EditorMenu;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;

public class DesignBoolean implements IDesignFieldEditor {

    private final DesignElement element;
    private final Field field;
    private final String name;

    public DesignBoolean(DesignElement element, Field field) {
        this.element = element;
        this.field = field;
        this.name = field.getAnnotation(EditorBoolean.class).name();
    }

    @Override
    public void edit(IEditorBundle bundle, Player viewer, EditorMenu editor, boolean wasShiftFocus) {
        try {
            boolean value = Boolean.valueOf(field.get(bundle).toString());
            this.field.set(bundle, !value);
            editor.getMenu().queryRebuild();
        } catch (IllegalAccessException e) {
            viewer.sendMessage("§cAn unexpected error occurred");
            e.printStackTrace();
        }
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getInfo(IEditorBundle bundle) throws Exception {
        boolean value = Boolean.valueOf(field.get(bundle).toString());
        return value ? "yes" : "no";
    }

    @Override
    public ItemStack getIcon(IEditorBundle bundle) throws Exception {
        boolean value = Boolean.valueOf(field.get(bundle).toString());
        return ItemBuilder.of(value ? Material.GLOWSTONE : Material.SOUL_SAND)
                .name("§f" + this.name)
                .lore("§fActive: " + value)
                .build();
    }
}