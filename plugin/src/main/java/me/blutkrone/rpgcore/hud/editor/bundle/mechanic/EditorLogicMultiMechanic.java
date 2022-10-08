package me.blutkrone.rpgcore.hud.editor.bundle.mechanic;

import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorList;
import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.hud.editor.constraint.bundle.mono.ActionConstraint;
import me.blutkrone.rpgcore.skill.mechanic.MultiMechanic;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorLogicMultiMechanic extends AbstractEditorMechanic {

    @EditorList(name = "Actions", constraint = ActionConstraint.class)
    public List<IEditorBundle> actions = new ArrayList<>();

    @Override
    public MultiMechanic build() {
        return new MultiMechanic(this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.CHEST)
                .name("§fMulti Mechanic")
                .lore("§fContains " + actions.size() + " actions")
                .build();
    }

    @Override
    public String getName() {
        return "Multi Mechanic";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Multi Mechanic");
        instruction.add("Executes multiple actions when invoked.");
        return instruction;
    }
}
