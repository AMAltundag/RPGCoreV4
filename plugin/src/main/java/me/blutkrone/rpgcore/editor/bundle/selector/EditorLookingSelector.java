package me.blutkrone.rpgcore.editor.bundle.selector;

import me.blutkrone.rpgcore.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.editor.annotation.value.EditorBoolean;
import me.blutkrone.rpgcore.editor.annotation.value.EditorBundle;
import me.blutkrone.rpgcore.editor.bundle.modifier.EditorModifierBoolean;
import me.blutkrone.rpgcore.editor.bundle.modifier.EditorModifierNumber;
import me.blutkrone.rpgcore.skill.selector.AbstractCoreSelector;
import me.blutkrone.rpgcore.skill.selector.LookingSelector;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorLookingSelector extends AbstractEditorSelector {

    @EditorBundle(name = "Distance")
    @EditorTooltip(tooltip = "Distance to throw a cast at.")
    public EditorModifierNumber distance = new EditorModifierNumber(8d);
    @EditorBoolean(name = "Surface")
    @EditorTooltip(tooltip = {"Tries to hug the top of a block"})
    public EditorModifierBoolean surface = new EditorModifierBoolean();

    @Override
    public AbstractCoreSelector build() {
        return new LookingSelector(this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.GLASS)
                .name("Looking Selector")
                .build();
    }

    @Override
    public String getName() {
        return "Looking";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Looking Selector");
        instruction.add("First will throw a ray-trace to find what we are looking at.");
        instruction.add("If ray-trace hits nothing, traces again looking lower each");
        instruction.add("Time, until we hit a target.");
        instruction.add("");
        instruction.add("If no location can be found, maps to nothing.");
        instruction.add("");
        instruction.add("This will always generate a location, not an entity.");
        instruction.add("");
        instruction.add("Entities are traced from eye-position, otherwise from location.");
        return instruction;
    }
}
