package me.blutkrone.rpgcore.hud.editor.design.designs;

import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorBundle;
import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.hud.editor.design.DesignElement;
import me.blutkrone.rpgcore.menu.EditorMenu;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;

public class DesignBundle implements IDesignFieldEditor {

    private final DesignElement element;
    private final Field field;
    private final String name;

    public DesignBundle(DesignElement element, Field field) {
        this.element = element;
        this.field = field;
        this.name = field.getAnnotation(EditorBundle.class).name();
    }

    @Override
    public void edit(IEditorBundle bundle, Player viewer, EditorMenu editor) {
        try {
            IEditorBundle value = (IEditorBundle) this.field.get(bundle);
            if (value != null) {
                // focus on the element itself
                editor.getFocus().setFocusToBundle(value);
                // request rebuilding
                editor.getMenu().queryRebuild();
            } else {
                viewer.sendMessage("§cBad class structure (bundle cannot be null)");
            }
        } catch (IllegalAccessException e) {
            viewer.sendMessage("§cAn unexpected error occurred");
            e.printStackTrace();
        }
    }

    @Override
    public String getInfo(IEditorBundle bundle) throws Exception {
        return ((IEditorBundle) this.field.get(bundle)).getName();
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public ItemStack getIcon(IEditorBundle bundle) throws Exception {
        IEditorBundle o = (IEditorBundle) this.field.get(bundle);
        return o.getPreview();
    }
}

