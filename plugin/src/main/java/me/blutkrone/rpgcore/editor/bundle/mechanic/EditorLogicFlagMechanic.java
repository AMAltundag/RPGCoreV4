package me.blutkrone.rpgcore.editor.bundle.mechanic;

import me.blutkrone.rpgcore.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.editor.annotation.value.EditorBundle;
import me.blutkrone.rpgcore.editor.bundle.modifier.EditorModifierNumber;
import me.blutkrone.rpgcore.editor.bundle.modifier.EditorModifierString;
import me.blutkrone.rpgcore.skill.mechanic.AbstractCoreMechanic;
import me.blutkrone.rpgcore.skill.mechanic.LogicFlagMechanic;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorLogicFlagMechanic extends AbstractEditorMechanic {

    @EditorBundle(name = "Flag")
    @EditorTooltip(tooltip = {"The flag to be acquired."})
    public EditorModifierString flag = new EditorModifierString("NOTHINGNESS");
    @EditorBundle(name = "Duration")
    @EditorTooltip(tooltip = {"Duration to hold the flag."})
    public EditorModifierNumber time = new EditorModifierNumber(80);

    @Override
    public AbstractCoreMechanic build() {
        return new LogicFlagMechanic(this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.CHEST)
                .name("§fFlag Mechanic")
                .name("§fFlag: " + flag.base_value)
                .name("§fDuration: " + time.base_value)
                .build();
    }

    @Override
    public String getName() {
        return "Flag Mechanic";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Flag Mechanic");
        instruction.add("Assigns a flag to all entities, the flag will expire");
        instruction.add("After a certain duration passed.");
        return instruction;
    }
}
