package me.blutkrone.rpgcore.hud.editor.bundle.selector;

import me.blutkrone.rpgcore.skill.selector.AbstractCoreSelector;
import me.blutkrone.rpgcore.skill.selector.NoneSelector;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorNoneSelector extends AbstractEditorSelector {

    @Override
    public AbstractCoreSelector build() {
        return new NoneSelector(this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.GLASS)
                .name("None Selector")
                .build();
    }

    @Override
    public String getName() {
        return "None Selector";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("None Selector");
        instruction.add("If there are targets, maps to no targets.");
        instruction.add("Otherwise we map to just the context holder.");
        instruction.add("");
        instruction.add("Intended for conditions, not for 'selecting' per-se.");
        return instruction;
    }
}
