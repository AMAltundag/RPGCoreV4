package me.blutkrone.rpgcore.editor.bundle.selector;

import me.blutkrone.rpgcore.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.editor.annotation.value.EditorBoolean;
import me.blutkrone.rpgcore.editor.annotation.value.EditorBundle;
import me.blutkrone.rpgcore.editor.bundle.modifier.EditorModifierBoolean;
import me.blutkrone.rpgcore.editor.bundle.modifier.EditorModifierNumber;
import me.blutkrone.rpgcore.skill.selector.AbstractCoreSelector;
import me.blutkrone.rpgcore.skill.selector.RadiusSelector;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorRadiusSelector extends AbstractEditorSelector {

    @EditorBundle(name = "Radius")
    @EditorTooltip(tooltip = "Distance to select within")
    public EditorModifierNumber radius = new EditorModifierNumber();
    @EditorBundle(name = "Total")
    @EditorTooltip(tooltip = "Limits of how many targets to select")
    public EditorModifierNumber total = new EditorModifierNumber();
    @EditorBoolean(name = "Sight")
    @EditorTooltip(tooltip = "Require line-of-sight from source")
    public EditorModifierBoolean sight = new EditorModifierBoolean();

    @Override
    public AbstractCoreSelector build() {
        return new RadiusSelector(this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.ENDER_CHEST)
                .name("Radius Selector")
                .build();
    }

    @Override
    public String getName() {
        return "Radius Selector";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Radius Selector");
        instruction.add("Selects all entities within a given radius.");
        return instruction;
    }
}
