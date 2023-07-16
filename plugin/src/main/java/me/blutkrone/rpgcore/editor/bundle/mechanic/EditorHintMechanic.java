package me.blutkrone.rpgcore.editor.bundle.mechanic;

import me.blutkrone.rpgcore.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.editor.annotation.value.EditorBundle;
import me.blutkrone.rpgcore.editor.annotation.value.EditorWrite;
import me.blutkrone.rpgcore.editor.bundle.modifier.EditorModifierNumber;
import me.blutkrone.rpgcore.editor.constraint.reference.other.LanguageConstraint;
import me.blutkrone.rpgcore.skill.mechanic.AbstractCoreMechanic;
import me.blutkrone.rpgcore.skill.mechanic.HintMechanic;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorHintMechanic extends AbstractEditorMechanic {

    @EditorWrite(name = "Hint", constraint = LanguageConstraint.class)
    @EditorTooltip(tooltip = {"Hint shown when focused by other players.", "§cThis is a language code, NOT plaintext."})
    public String lc_hint = "NOTHINGNESS";
    @EditorBundle(name = "Duration")
    @EditorTooltip(tooltip = "How long the hint will persist.")
    public EditorModifierNumber duration = new EditorModifierNumber(60);

    @Override
    public AbstractCoreMechanic build() {
        return new HintMechanic(this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.CHEST)
                .name("Hint")
                .appendLore("Hint: " + lc_hint)
                .appendLore("Duration: " + duration.base_value)
                .build();
    }

    @Override
    public String getName() {
        return "Hint";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Hint");
        instruction.add("A hint is shown when focused by players, the hint will");
        instruction.add("Override previous hints.");
        return instruction;
    }
}
