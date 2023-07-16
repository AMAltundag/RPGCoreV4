package me.blutkrone.rpgcore.editor.bundle.mechanic;

import me.blutkrone.rpgcore.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.editor.annotation.value.EditorBundle;
import me.blutkrone.rpgcore.editor.bundle.modifier.EditorModifierBoolean;
import me.blutkrone.rpgcore.editor.bundle.modifier.EditorModifierNumber;
import me.blutkrone.rpgcore.editor.bundle.modifier.EditorModifierString;
import me.blutkrone.rpgcore.skill.mechanic.AbstractCoreMechanic;
import me.blutkrone.rpgcore.skill.mechanic.StackMechanic;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorStackMechanic extends AbstractEditorMechanic {

    @EditorBundle(name = "Stack")
    @EditorTooltip(tooltip = "Manipulate the number of stacks of the effect.")
    public EditorModifierNumber stack = new EditorModifierNumber(0d);
    @EditorBundle(name = "Duration")
    @EditorTooltip(tooltip = "Manipulate the duration of the effect.")
    public EditorModifierNumber duration = new EditorModifierNumber(0d);
    @EditorBundle(name = "Effect")
    @EditorTooltip(tooltip = "ID of the effect to manipulate")
    public EditorModifierString effect = new EditorModifierString("unknown");
    @EditorBundle(name = "Override")
    @EditorTooltip(tooltip = {"Either overrides or adds to value", "Override ignores negative values"})
    public EditorModifierBoolean override = new EditorModifierBoolean();

    @Override
    public AbstractCoreMechanic build() {
        return new StackMechanic(this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.PAPER)
                .name("§fStack Mechanic")
                .build();
    }

    @Override
    public String getName() {
        return "Stack";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Stack Mechanic");
        instruction.add("This will manipulate any effect on the entity, but cannot");
        instruction.add("Create a new effect by itself.");
        instruction.add("");
        instruction.add("This does NOT respect stack limits!");
        return instruction;
    }
}
