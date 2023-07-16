package me.blutkrone.rpgcore.editor.bundle.other;

import me.blutkrone.rpgcore.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.editor.annotation.value.EditorList;
import me.blutkrone.rpgcore.editor.annotation.value.EditorWrite;
import me.blutkrone.rpgcore.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.editor.constraint.bundle.multi.SelectorConstraint;
import me.blutkrone.rpgcore.editor.constraint.reference.other.LanguageConstraint;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorRequirement implements IEditorBundle {

    @EditorList(name = "Requirement", constraint = SelectorConstraint.class)
    @EditorTooltip(tooltip = {"A best-effort is made to not equip the item", "If still equipped, item will not affect player."})
    public List<IEditorBundle> conditions = new ArrayList<>();
    @EditorWrite(name = "Display", constraint = LanguageConstraint.class)
    @EditorTooltip(tooltip = {"Info language code, tinted on allow/forbid"})
    public String display = "NOTHINGNESS";

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.COMPARATOR)
                .name("Requirement")
                .appendLore("Conditions: X" + this.conditions.size())
                .appendLore("Display: " + this.display)
                .build();
    }

    @Override
    public String getName() {
        return "Requirement";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Requirement");
        instruction.add("A requirement to have access to something.");
        return instruction;
    }
}
