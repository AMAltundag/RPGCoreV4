package me.blutkrone.rpgcore.hud.editor.bundle.selector;

import me.blutkrone.rpgcore.hud.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorBundle;
import me.blutkrone.rpgcore.hud.editor.bundle.modifier.EditorModifierString;
import me.blutkrone.rpgcore.skill.selector.AbstractCoreSelector;
import me.blutkrone.rpgcore.skill.selector.FlagSelector;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorFlagSelector extends AbstractEditorSelector {

    @EditorBundle(name = "Wanted")
    @EditorTooltip(tooltip = "Flag wanted on the entity")
    public EditorModifierString flag = new EditorModifierString();

    @Override
    public AbstractCoreSelector build() {
        return new FlagSelector(this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.ENDER_CHEST)
                .name("Flag Selector")
                .build();
    }

    @Override
    public String getName() {
        return "Flag Selector";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Flag Selector");
        instruction.add("Selects all entities who have the given flag.");
        return instruction;
    }
}
