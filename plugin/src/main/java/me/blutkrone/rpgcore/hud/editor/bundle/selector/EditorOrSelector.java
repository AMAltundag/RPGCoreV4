package me.blutkrone.rpgcore.hud.editor.bundle.selector;

import me.blutkrone.rpgcore.hud.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorList;
import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.hud.editor.constraint.bundle.multi.SelectorConstraint;
import me.blutkrone.rpgcore.skill.selector.AbstractCoreSelector;
import me.blutkrone.rpgcore.skill.selector.OrSelector;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorOrSelector extends AbstractEditorSelector {

    @EditorList(name = "Conditions", constraint = SelectorConstraint.class)
    @EditorTooltip(tooltip = "Selectors to fulfil")
    public List<IEditorBundle> conditions = new ArrayList<>();

    @Override
    public AbstractCoreSelector build() {
        return new OrSelector(this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.GLASS)
                .name("OR Selector")
                .build();
    }

    @Override
    public String getName() {
        return "OR Selector";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("ORSelector");
        instruction.add("Process the input targets in isolation, if any");
        instruction.add("Target fulfill any of the conditions we are mapped");
        instruction.add("To the context holder");
        return instruction;
    }
}
