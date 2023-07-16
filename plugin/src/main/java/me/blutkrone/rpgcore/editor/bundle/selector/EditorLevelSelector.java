package me.blutkrone.rpgcore.editor.bundle.selector;

import me.blutkrone.rpgcore.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.editor.annotation.value.EditorBundle;
import me.blutkrone.rpgcore.editor.bundle.modifier.EditorModifierNumber;
import me.blutkrone.rpgcore.skill.selector.AbstractCoreSelector;
import me.blutkrone.rpgcore.skill.selector.LevelSelector;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorLevelSelector extends AbstractEditorSelector {

    @EditorBundle(name = "Minimum")
    @EditorTooltip(tooltip = {"Minimum level allowed"})
    public EditorModifierNumber minimum = new EditorModifierNumber(0);
    @EditorBundle(name = "Maximum")
    @EditorTooltip(tooltip = "Maximum level allowed")
    public EditorModifierNumber maximum = new EditorModifierNumber(999);

    @Override
    public AbstractCoreSelector build() {
        return new LevelSelector(this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.ENDER_CHEST)
                .name("Level Selector")
                .build();
    }

    @Override
    public String getName() {
        return "Level Selector";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Level Selector");
        instruction.add("Filters to entities within level range.");
        return instruction;
    }
}
