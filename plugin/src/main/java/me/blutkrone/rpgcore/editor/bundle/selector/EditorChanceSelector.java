package me.blutkrone.rpgcore.editor.bundle.selector;

import me.blutkrone.rpgcore.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.editor.annotation.value.EditorBundle;
import me.blutkrone.rpgcore.editor.bundle.modifier.EditorModifierNumber;
import me.blutkrone.rpgcore.skill.selector.AbstractCoreSelector;
import me.blutkrone.rpgcore.skill.selector.ChanceSelector;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorChanceSelector extends AbstractEditorSelector {

    @EditorBundle(name = "Chance")
    @EditorTooltip(tooltip = "Chance to be retained in percent")
    public EditorModifierNumber chance = new EditorModifierNumber(1d);

    @Override
    public AbstractCoreSelector build() {
        return new ChanceSelector(this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.ENDER_CHEST)
                .name("Chance Selector")
                .build();
    }

    @Override
    public String getName() {
        return "Chance Selector";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Chance Selector");
        instruction.add("Chance for a target to be retained");
        return instruction;
    }
}
