package me.blutkrone.rpgcore.hud.editor.bundle.effect;

import me.blutkrone.rpgcore.effect.CoreEffect;
import me.blutkrone.rpgcore.effect.impl.CoreEffectRotor;
import me.blutkrone.rpgcore.hud.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorNumber;
import me.blutkrone.rpgcore.hud.editor.root.other.EditorEffect;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorEffectRotor implements EditorEffect.IEditorEffectBundle {

    @EditorNumber(name = "Start", minimum = 0d)
    @EditorTooltip(tooltip = "Original rotation in degrees")
    public double start;
    @EditorNumber(name = "Angle", minimum = 0d)
    @EditorTooltip(tooltip = "Total angle to spin in degrees")
    public double angle;
    @EditorNumber(name = "Radius", minimum = 0d)
    @EditorTooltip(tooltip = "Radius we rotate around")
    public double radius;
    @EditorNumber(name = "Duration", minimum = 0d)
    @EditorTooltip(tooltip = "Over how many ticks to complete all rotations")
    public double duration;
    @EditorNumber(name = "Scatter")
    @EditorTooltip(tooltip = "Randomize particle position by this much")
    public double scatter = 0d;
    @EditorNumber(name = "Sample")
    @EditorTooltip(tooltip = "Multiplier to how many particles are used")
    public double sample = 0d;

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.BOOKSHELF)
                .name("§aRotor Effect")
                .appendLore("§fStart:" + start)
                .appendLore("§fAngle: " + angle)
                .appendLore("§fRadius: " + radius)
                .appendLore("§fDuration: " + duration)
                .appendLore("§fSample: " + sample)
                .appendLore("§fScatter: " + scatter)
                .build();
    }

    @Override
    public String getName() {
        return "Rotor";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instructions = new ArrayList<>();
        instructions.add("§fRotor Effect");
        instructions.add("Particle trail moving in a circle");
        return instructions;
    }

    @Override
    public CoreEffect.IEffectPart build() {
        return new CoreEffectRotor(this);
    }
}
