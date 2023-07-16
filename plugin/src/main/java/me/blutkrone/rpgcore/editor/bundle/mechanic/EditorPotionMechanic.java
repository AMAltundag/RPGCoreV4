package me.blutkrone.rpgcore.editor.bundle.mechanic;

import me.blutkrone.rpgcore.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.editor.annotation.value.EditorBundle;
import me.blutkrone.rpgcore.editor.annotation.value.EditorWrite;
import me.blutkrone.rpgcore.editor.bundle.modifier.EditorModifierNumber;
import me.blutkrone.rpgcore.editor.constraint.enums.PotionTypeConstraint;
import me.blutkrone.rpgcore.skill.mechanic.AbstractCoreMechanic;
import me.blutkrone.rpgcore.skill.mechanic.PotionMechanic;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class EditorPotionMechanic extends AbstractEditorMechanic {

    @EditorWrite(name = "Type", constraint = PotionTypeConstraint.class)
    @EditorTooltip(tooltip = "What potion type to apply.")
    public String type = "BLINDNESS";
    @EditorBundle(name = "Amplifier")
    @EditorTooltip(tooltip = "Amplifier of effect.")
    public EditorModifierNumber amplifier = new EditorModifierNumber();
    @EditorBundle(name = "Duration")
    @EditorTooltip(tooltip = "Duration of effect.")
    public EditorModifierNumber duration = new EditorModifierNumber();

    @Override
    public AbstractCoreMechanic build() {
        return new PotionMechanic(this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.LINGERING_POTION)
                .name("§fPotion Mechanic")
                .build();
    }

    @Override
    public String getName() {
        return "Potion Mechanic";
    }

    @Override
    public List<String> getInstruction() {
        return null;
    }
}
