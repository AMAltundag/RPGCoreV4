package me.blutkrone.rpgcore.hud.editor.bundle.mechanic;

import me.blutkrone.rpgcore.hud.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorBundle;
import me.blutkrone.rpgcore.hud.editor.bundle.modifier.EditorModifierNumber;
import me.blutkrone.rpgcore.skill.mechanic.AbstractCoreMechanic;
import me.blutkrone.rpgcore.skill.mechanic.SleepMechanic;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorMobSleepMechanic extends AbstractEditorMechanic {

    @EditorBundle(name = "Duration")
    @EditorTooltip(tooltip = "How long to sleep the action.")
    public EditorModifierNumber duration = new EditorModifierNumber();

    @Override
    public AbstractCoreMechanic build() {
        return new SleepMechanic(this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.RED_BANNER)
                .name("§fSleep")
                .build();
    }

    @Override
    public String getName() {
        return "Sleep";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Sleep Mechanic");
        instruction.add("Pauses the routine which called the mechanic, this will");
        instruction.add("not affect any other routine.");
        return instruction;
    }
}
