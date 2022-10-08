package me.blutkrone.rpgcore.hud.editor.bundle.mechanic;

import me.blutkrone.rpgcore.hud.editor.annotation.EditorCategory;
import me.blutkrone.rpgcore.hud.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorBoolean;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorBundle;
import me.blutkrone.rpgcore.hud.editor.bundle.modifier.EditorModifierNumber;
import me.blutkrone.rpgcore.skill.mechanic.AbstractCoreMechanic;
import me.blutkrone.rpgcore.skill.mechanic.BarrierMechanic;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorMobBarrierMechanic extends AbstractEditorMechanic {

    @EditorCategory(icon = Material.SHIELD, info = "Barrier")
    @EditorBundle(name = "Capacity")
    @EditorTooltip(tooltip = {"How much damage to break the barrier"})
    public EditorModifierNumber damage = new EditorModifierNumber();
    @EditorCategory(icon = Material.BARRIER, info = "Failure")
    @EditorBundle(name = "Countdown")
    @EditorTooltip(tooltip = {"Time limit to deal necessary damage"})
    public EditorModifierNumber countdown = new EditorModifierNumber();
    @EditorBundle(name = "Failure")
    @EditorTooltip(tooltip = {"Executed in case of failure"})
    public EditorLogicMultiMechanic failure = new EditorLogicMultiMechanic();
    @EditorBoolean(name = "Terminate")
    @EditorTooltip(tooltip = {"Kill logic thread upon failure"})
    public boolean terminate_when_failed = true;

    @Override
    public AbstractCoreMechanic build() {
        return new BarrierMechanic(this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.SHIELD)
                .name("§fMob Barrier")
                .build();
    }

    @Override
    public String getName() {
        return "Mob Barrier";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Mob Barrier");
        instruction.add("Stalls execution of thread until the given amount");
        instruction.add("of damage has been dealt. Failure can only happen");
        instruction.add("should there be a time limit.");
        instruction.add("");
        instruction.add("§cNever create multiple concurrent barriers!");
        return instruction;
    }
}
