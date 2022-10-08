package me.blutkrone.rpgcore.hud.editor.bundle.selector;

import me.blutkrone.rpgcore.skill.selector.AbstractCoreSelector;
import me.blutkrone.rpgcore.skill.selector.FriendlySelector;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorFriendlySelector extends AbstractEditorSelector {

    @Override
    public AbstractCoreSelector build() {
        return new FriendlySelector(this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.ENDER_CHEST)
                .name("Friendly Selector")
                .build();
    }

    @Override
    public String getName() {
        return "Friendly Selector";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Friendly Selector");
        instruction.add("Filters targets to friendlies. Does not affect non-entities.");
        return instruction;
    }
}
