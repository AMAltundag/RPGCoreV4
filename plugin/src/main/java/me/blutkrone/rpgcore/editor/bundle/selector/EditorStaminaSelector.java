package me.blutkrone.rpgcore.editor.bundle.selector;

import me.blutkrone.rpgcore.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.editor.annotation.value.EditorBoolean;
import me.blutkrone.rpgcore.editor.annotation.value.EditorBundle;
import me.blutkrone.rpgcore.editor.bundle.modifier.EditorModifierBoolean;
import me.blutkrone.rpgcore.editor.bundle.modifier.EditorModifierNumber;
import me.blutkrone.rpgcore.skill.selector.AbstractCoreSelector;
import me.blutkrone.rpgcore.skill.selector.StaminaSelector;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorStaminaSelector extends AbstractEditorSelector {

    @EditorBundle(name = "Minimum")
    @EditorTooltip(tooltip = "Minimum amount of the resource")
    public EditorModifierNumber minimum = new EditorModifierNumber(1);
    @EditorBundle(name = "Maximum")
    @EditorTooltip(tooltip = "Maximum amount of the resource")
    public EditorModifierNumber maximum = new EditorModifierNumber(99999);
    @EditorBoolean(name = "Percentage")
    @EditorTooltip(tooltip = "Process as percentage")
    public EditorModifierBoolean percentage = new EditorModifierBoolean();

    @Override
    public AbstractCoreSelector build() {
        return new StaminaSelector(this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.BUCKET)
                .name("Stamina Selector")
                .build();
    }

    @Override
    public String getName() {
        return "Stamina Selector";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Stamina Selector");
        instruction.add("Filter by the remaining resource of an entity.");
        return instruction;
    }
}
