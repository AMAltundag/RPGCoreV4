package me.blutkrone.rpgcore.hud.editor.bundle.mechanic;

import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorList;
import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.hud.editor.constraint.bundle.mono.BranchConstraint;
import me.blutkrone.rpgcore.skill.mechanic.AbstractCoreMechanic;
import me.blutkrone.rpgcore.skill.mechanic.BranchMechanic;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorLogicBranchMechanic extends AbstractEditorMechanic {

    @EditorList(name = "Branches", constraint = BranchConstraint.class)
    public List<IEditorBundle> branches = new ArrayList<>();

    @Override
    public AbstractCoreMechanic build() {
        return new BranchMechanic(this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.CHEST)
                .name("Branch")
                .appendLore("Total of " + branches.size() + " branches")
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
