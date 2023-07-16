package me.blutkrone.rpgcore.editor.bundle.other;

import me.blutkrone.rpgcore.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.editor.annotation.value.EditorBundle;
import me.blutkrone.rpgcore.editor.annotation.value.EditorList;
import me.blutkrone.rpgcore.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.editor.bundle.mechanic.EditorLogicMultiMechanic;
import me.blutkrone.rpgcore.editor.constraint.bundle.multi.SelectorConstraint;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorBranch implements IEditorBundle {

    @EditorBundle(name = "Mechanic")
    @EditorTooltip(tooltip = {"Mechanic executed when entering branch."})
    public EditorLogicMultiMechanic mechanic = new EditorLogicMultiMechanic();
    @EditorList(name = "Condition", constraint = SelectorConstraint.class)
    @EditorTooltip(tooltip = {"Condition to enter this branch."})
    public List<IEditorBundle> condition = new ArrayList<>();

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.OAK_DOOR)
                .name("§fBranch")
                .appendLore("")
                .build();
    }

    @Override
    public String getName() {
        return "Branch";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Branch");
        instruction.add("When multiple branches exist, only the first branch");
        instruction.add("Where the condition is met is entered.");
        return instruction;
    }
}
