package me.blutkrone.rpgcore.editor.bundle.mechanic;

import me.blutkrone.rpgcore.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.editor.annotation.value.EditorBundle;
import me.blutkrone.rpgcore.editor.annotation.value.EditorList;
import me.blutkrone.rpgcore.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.editor.bundle.modifier.EditorModifierNumber;
import me.blutkrone.rpgcore.editor.constraint.bundle.multi.SelectorConstraint;
import me.blutkrone.rpgcore.skill.mechanic.AbstractCoreMechanic;
import me.blutkrone.rpgcore.skill.mechanic.VelocityMechanic;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorVelocityMechanic extends AbstractEditorMechanic {

    @EditorBundle(name = "Horizontal")
    @EditorTooltip(tooltip = {"Power to move on XZ axis"})
    public EditorModifierNumber horizontal = new EditorModifierNumber();
    @EditorBundle(name = "Vertical")
    @EditorTooltip(tooltip = {"Power to move on Y axis"})
    public EditorModifierNumber vertical = new EditorModifierNumber();
    @EditorList(name = "Anchor", constraint = SelectorConstraint.class)
    @EditorTooltip(tooltip = {"Origin of the velocity change."})
    public List<IEditorBundle> anchor = new ArrayList<>();

    @Override
    public AbstractCoreMechanic build() {
        return new VelocityMechanic(this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.ELYTRA)
                .name("Velocity Mechanic")
                .build();
    }

    @Override
    public String getName() {
        return "Velocity";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Velocity Mechanic");
        instruction.add("Applies a pull towards the anchor.");
        instruction.add("Original target will be the caster.");
        return instruction;
    }
}
