package me.blutkrone.rpgcore.hud.editor.bundle.selector;

import me.blutkrone.rpgcore.skill.selector.AbstractCoreSelector;
import me.blutkrone.rpgcore.skill.selector.HostileSelector;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorHostileSelector extends AbstractEditorSelector {

    @Override
    public AbstractCoreSelector build() {
        return new HostileSelector(this);
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
