package me.blutkrone.rpgcore.editor.bundle.mechanic;

import me.blutkrone.rpgcore.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.editor.annotation.value.EditorBundle;
import me.blutkrone.rpgcore.editor.bundle.modifier.EditorModifierNumber;
import me.blutkrone.rpgcore.skill.mechanic.AbstractCoreMechanic;
import me.blutkrone.rpgcore.skill.mechanic.ThrustMechanic;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorThrustMechanic extends AbstractEditorMechanic {

    @EditorBundle(name = "Power")
    @EditorTooltip(tooltip = {"Power to thrust forward with"})
    public EditorModifierNumber power = new EditorModifierNumber();
    @EditorBundle(name = "Drag")
    @EditorTooltip(tooltip = {"High drag for slow directional change"})
    public EditorModifierNumber drag = new EditorModifierNumber();

    @Override
    public AbstractCoreMechanic build() {
        return new ThrustMechanic(this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.ELYTRA)
                .name("Thrust Mechanic")
                .build();
    }

    @Override
    public String getName() {
        return "Thrust";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Thrust Mechanic");
        instruction.add("Thrusts forward where the player looks.");
        instruction.add("Negative numbers to thrust backwards.");
        instruction.add("Drag of zero adds thrust, instead of overriding.");
        return instruction;
    }
}
