package me.blutkrone.rpgcore.editor.bundle.selector;

import me.blutkrone.rpgcore.skill.selector.AbstractCoreSelector;
import me.blutkrone.rpgcore.skill.selector.SelfSelector;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorSelfSelector extends AbstractEditorSelector {

    @Override
    public AbstractCoreSelector build() {
        return new SelfSelector(this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.ENDER_CHEST)
                .name("Hostile Selector")
                .build();
    }

    @Override
    public String getName() {
        return "Hostile Selector";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Hostile Selector");
        instruction.add("Filters targets to hostiles. Does not affect non-entities.");
        return instruction;
    }
}
