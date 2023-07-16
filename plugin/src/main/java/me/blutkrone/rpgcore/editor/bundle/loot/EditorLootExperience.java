package me.blutkrone.rpgcore.editor.bundle.loot;

import me.blutkrone.rpgcore.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.editor.annotation.value.EditorList;
import me.blutkrone.rpgcore.editor.annotation.value.EditorNumber;
import me.blutkrone.rpgcore.editor.constraint.reference.index.AttributeConstraint;
import me.blutkrone.rpgcore.mob.loot.AbstractCoreLoot;
import me.blutkrone.rpgcore.mob.loot.ExpCoreLoot;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorLootExperience extends AbstractEditorLoot {

    @EditorNumber(name = "Amount", minimum = 0d)
    @EditorTooltip(tooltip = {"Grants an amount of experience", "Experience granted is level scaled"})
    public double experience = 0d;
    @EditorList(name = "Scaling", constraint = AttributeConstraint.class)
    @EditorTooltip(tooltip = {"Attributes from killer which can scale exp", "Attribute 'exp_multi_kill' works for all kills"})
    public List<String> scaling_attributes = new ArrayList<>();

    @Override
    public AbstractCoreLoot build() {
        return new ExpCoreLoot(this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.EXPERIENCE_BOTTLE)
                .name("§fLoot Exp")
                .appendLore("§fAmount: " + ((int) this.experience))
                .build();
    }

    @Override
    public String getName() {
        return "Loot Exp";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Loot Exp");
        instruction.add("Grants experience to the killer, exp is level scaled.");
        return instruction;
    }
}
