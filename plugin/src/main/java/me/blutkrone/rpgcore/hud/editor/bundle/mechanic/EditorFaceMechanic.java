package me.blutkrone.rpgcore.hud.editor.bundle.mechanic;

import me.blutkrone.rpgcore.hud.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorList;
import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.hud.editor.constraint.bundle.multi.SelectorConstraint;
import me.blutkrone.rpgcore.skill.mechanic.AbstractCoreMechanic;
import me.blutkrone.rpgcore.skill.mechanic.FaceMechanic;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorFaceMechanic extends AbstractEditorMechanic {

    @EditorList(name = "Where", constraint = SelectorConstraint.class)
    @EditorTooltip(tooltip = "Where should targets look at")
    public List<IEditorBundle> where = new ArrayList<>();

    @Override
    public AbstractCoreMechanic build() {
        return new FaceMechanic(this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.COMPASS)
                .name("Face Mechanic")
                .build();
    }

    @Override
    public String getName() {
        return "Face";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Face Mechanic");
        instruction.add("All targets will face a random target from of our selection.");
        return instruction;
    }
}
