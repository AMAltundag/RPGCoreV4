package me.blutkrone.rpgcore.hud.editor.bundle.loot;

import me.blutkrone.rpgcore.hud.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorNumber;
import me.blutkrone.rpgcore.mob.loot.AbstractCoreLoot;
import me.blutkrone.rpgcore.mob.loot.ExpCoreLoot;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorLootExperience extends AbstractEditorLoot {

    @EditorNumber(name = "Amount", minimum = 0d)
    @EditorTooltip(tooltip = {
            "Grants an amount of experience",
            "Experience granted is level scaled"
    })
    public double experience = 0d;

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
        instruction.add("Grants experience to the killer, experience scaling");
        instruction.add("Applies based on level difference.");
        return instruction;
    }
}