package me.blutkrone.rpgcore.hud.editor.bundle.modifier;

import me.blutkrone.rpgcore.hud.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorWrite;
import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.hud.editor.constraint.other.StringConstraint;
import me.blutkrone.rpgcore.skill.modifier.CoreModifierString;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorModifierString implements IEditorBundle {

    @EditorWrite(name = "Default Value", constraint = StringConstraint.class)
    @EditorTooltip(tooltip = "Inherent value of modifier")
    public String base_value = "undefined";

    public EditorModifierString(String base_value) {
        this.base_value = base_value;
    }

    public EditorModifierString() {
    }

    public CoreModifierString build() {
        return new CoreModifierString(this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.PAPER)
                .name("Modifier String")
                .appendLore("Default Value: " + base_value)
                .build();
    }

    @Override
    public String getName() {
        return "Modifier";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Modifier String");
        instruction.add("Evaluates into a string.");
        return instruction;
    }
}
