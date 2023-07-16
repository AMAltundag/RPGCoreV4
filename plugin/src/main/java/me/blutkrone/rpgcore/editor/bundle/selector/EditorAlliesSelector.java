package me.blutkrone.rpgcore.editor.bundle.selector;

import me.blutkrone.rpgcore.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.editor.annotation.value.EditorBundle;
import me.blutkrone.rpgcore.editor.bundle.modifier.EditorModifierBoolean;
import me.blutkrone.rpgcore.editor.bundle.modifier.EditorModifierNumber;
import me.blutkrone.rpgcore.skill.selector.AbstractCoreSelector;
import me.blutkrone.rpgcore.skill.selector.AlliesSelector;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorAlliesSelector extends AbstractEditorSelector {

    @EditorBundle(name = "Radius")
    @EditorTooltip(tooltip = "Distance to select within")
    public EditorModifierNumber radius = new EditorModifierNumber();
    @EditorBundle(name = "Total")
    @EditorTooltip(tooltip = "Limits of how many targets to select")
    public EditorModifierNumber total = new EditorModifierNumber();
    @EditorBundle(name = "Sight")
    @EditorTooltip(tooltip = "Require line-of-sight from source")
    public EditorModifierBoolean sight = new EditorModifierBoolean();

    @Override
    public AbstractCoreSelector build() {
        return new AlliesSelector(this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.ENDER_CHEST)
                .name("Allies Selector")
                .build();
    }

    @Override
    public String getName() {
        return "Allies Selector";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Allies Selector");
        instruction.add("Selects friendlies within a radius, do note that");
        instruction.add("entities may neither be friendly nor hostile.");
        return instruction;
    }
}
