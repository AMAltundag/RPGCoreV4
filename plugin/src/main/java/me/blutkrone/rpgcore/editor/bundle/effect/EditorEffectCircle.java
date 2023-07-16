package me.blutkrone.rpgcore.editor.bundle.effect;

import me.blutkrone.rpgcore.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.editor.annotation.value.EditorNumber;
import me.blutkrone.rpgcore.editor.root.other.EditorEffect;
import me.blutkrone.rpgcore.effect.CoreEffect;
import me.blutkrone.rpgcore.effect.impl.CoreEffectCircle;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorEffectCircle implements EditorEffect.IEditorEffectBundle {
    @EditorNumber(name = "Scatter")
    @EditorTooltip(tooltip = "Randomize particle position by this much")
    public double scatter = 0d;
    @EditorNumber(name = "Sample")
    @EditorTooltip(tooltip = "Multiplier to how many particles are used")
    public double sample = 0d;
    @EditorNumber(name = "Minimum")
    @EditorTooltip(tooltip = "Minimum radius of the circle")
    public double minimum_diameter = 0d;
    @EditorNumber(name = "Maximum")
    @EditorTooltip(tooltip = "Maximum radius of the circle")
    public double maximum_diameter = 0d;

    public EditorEffectCircle() {
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.BOOKSHELF)
                .name("§aCircle Effect")
                .appendLore("§fScatter: " + scatter)
                .appendLore("§fSample: " + sample)
                .appendLore("§fMinimum: " + minimum_diameter)
                .appendLore("§fMaximum: " + maximum_diameter)
                .build();
    }

    @Override
    public String getName() {
        return "Circle";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Circle Effect");
        instruction.add("Renders a circle, if minimum and maximum are close");
        instruction.add("Enough we trace the edge instead.");
        return instruction;
    }

    @Override
    public CoreEffect.IEffectPart build() {
        return new CoreEffectCircle(this);
    }
}
