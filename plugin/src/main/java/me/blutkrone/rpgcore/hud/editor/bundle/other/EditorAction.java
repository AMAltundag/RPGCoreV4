package me.blutkrone.rpgcore.hud.editor.bundle.other;

import me.blutkrone.rpgcore.hud.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorList;
import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.hud.editor.constraint.bundle.multi.MechanicConstraint;
import me.blutkrone.rpgcore.hud.editor.constraint.bundle.multi.SelectorConstraint;
import me.blutkrone.rpgcore.skill.behaviour.CoreAction;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorAction implements IEditorBundle {

    @EditorList(name = "Mechanics", constraint = MechanicConstraint.class)
    @EditorTooltip(tooltip = "Something that'll happen to a target")
    public List<IEditorBundle> mechanics = new ArrayList<>();
    @EditorList(name = "Selectors", constraint = SelectorConstraint.class)
    @EditorTooltip(tooltip = {"After passing thorough all selectors, we got our targets"})
    public List<IEditorBundle> selectors = new ArrayList<>();

    public CoreAction build() {
        return new CoreAction(this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.CHEST)
                .name("§fEditor Action")
                .appendLore("§fContains " + mechanics.size() + " mechanics")
                .appendLore("§fContains " + selectors.size() + " selectors")
                .build();
    }

    @Override
    public String getName() {
        return "Action";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Action");
        instruction.add("The mechanics apply to the final subset of targets");
        instruction.add("provided by the selectors");
        return instruction;
    }
}
