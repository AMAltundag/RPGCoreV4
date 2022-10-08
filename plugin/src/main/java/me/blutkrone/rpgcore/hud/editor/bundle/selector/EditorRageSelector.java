package me.blutkrone.rpgcore.hud.editor.bundle.selector;

import me.blutkrone.rpgcore.skill.selector.AbstractCoreSelector;
import me.blutkrone.rpgcore.skill.selector.RageSelector;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorRageSelector extends AbstractEditorSelector {

    @Override
    public AbstractCoreSelector build() {
        return new RageSelector(this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.ENDER_CHEST)
                .name("Rage Selector")
                .build();
    }

    @Override
    public String getName() {
        return "Rage Selector";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Rage Selector");
        instruction.add("Selects rage holder, maps to nothing if we do not");
        instruction.add("or cannot have a rage target.");
        return instruction;
    }
}
