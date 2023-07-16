package me.blutkrone.rpgcore.editor.bundle.selector;

import me.blutkrone.rpgcore.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.editor.annotation.value.EditorBundle;
import me.blutkrone.rpgcore.editor.bundle.modifier.EditorModifierNumber;
import me.blutkrone.rpgcore.skill.selector.AbstractCoreSelector;
import me.blutkrone.rpgcore.skill.selector.OffsetSelector;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorOffsetSelector extends AbstractEditorSelector {

    @EditorBundle(name = "Forward")
    @EditorTooltip(tooltip = {"Move forward relative to direction", "Applies on XYZ axis."})
    public EditorModifierNumber forward = new EditorModifierNumber();
    @EditorBundle(name = "Upward")
    @EditorTooltip(tooltip = "Move up or down on the Y axis")
    public EditorModifierNumber upward = new EditorModifierNumber();

    @Override
    public AbstractCoreSelector build() {
        return new OffsetSelector(this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.ENDER_CHEST)
                .name("Offset Selector")
                .build();
    }

    @Override
    public String getName() {
        return "Offset Selector";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Offset Selector");
        instruction.add("Displace targets, entities turn to locations.");
        return instruction;
    }
}
