package me.blutkrone.rpgcore.editor.bundle.selector;

import me.blutkrone.rpgcore.skill.selector.AbstractCoreSelector;
import me.blutkrone.rpgcore.skill.selector.TargetSelector;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorTargetSelector extends AbstractEditorSelector {

    @Override
    public AbstractCoreSelector build() {
        return new TargetSelector(this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.TARGET)
                .name("Target Selector")
                .build();
    }

    @Override
    public String getName() {
        return "Target Selector";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Target Selector");
        instruction.add("Selects rage holder for mobs, focus for players.");
        return instruction;
    }
}
