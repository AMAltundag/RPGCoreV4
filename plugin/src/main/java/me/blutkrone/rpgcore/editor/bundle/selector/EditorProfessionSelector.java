package me.blutkrone.rpgcore.editor.bundle.selector;

import me.blutkrone.rpgcore.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.editor.annotation.value.EditorBundle;
import me.blutkrone.rpgcore.editor.bundle.modifier.EditorModifierNumber;
import me.blutkrone.rpgcore.editor.bundle.modifier.EditorModifierString;
import me.blutkrone.rpgcore.skill.selector.AbstractCoreSelector;
import me.blutkrone.rpgcore.skill.selector.ProfessionSelector;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorProfessionSelector extends AbstractEditorSelector {

    @EditorBundle(name = "Profession")
    @EditorTooltip(tooltip = {"Which profession to check"})
    public EditorModifierString profession = new EditorModifierString("nothingness");
    @EditorBundle(name = "Minimum")
    @EditorTooltip(tooltip = {"Minimum level allowed"})
    public EditorModifierNumber minimum = new EditorModifierNumber(0);
    @EditorBundle(name = "Maximum")
    @EditorTooltip(tooltip = "Maximum level allowed")
    public EditorModifierNumber maximum = new EditorModifierNumber(999);

    @Override
    public AbstractCoreSelector build() {
        return new ProfessionSelector(this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.ANVIL)
                .name("Profession Selector")
                .build();
    }

    @Override
    public String getName() {
        return "Profession Selector";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Profession Selector");
        instruction.add("Filter for an entity with a certain profession level.");
        return instruction;
    }
}

