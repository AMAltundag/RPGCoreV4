package me.blutkrone.rpgcore.hud.editor.bundle.selector;

import me.blutkrone.rpgcore.hud.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorList;
import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.hud.editor.constraint.bundle.multi.SelectorConstraint;
import me.blutkrone.rpgcore.skill.selector.AbstractCoreSelector;
import me.blutkrone.rpgcore.skill.selector.AndSelector;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorAndSelector extends AbstractEditorSelector {

    @EditorList(name = "Conditions", constraint = SelectorConstraint.class)
    @EditorTooltip(tooltip = "Selectors to fulfil")
    public List<IEditorBundle> conditions = new ArrayList<>();

    @Override
    public AbstractCoreSelector build() {
        return new AndSelector(this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.COMPARATOR)
                .name("AND Selector")
                .build();
    }

    @Override
    public String getName() {
        return "AND Selector";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("AND Selector");
        instruction.add("Process the input targets sequentially, if any");
        instruction.add("Target remains we are mapped to the context holder");
        return instruction;
    }
}
