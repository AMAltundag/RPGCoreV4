package me.blutkrone.rpgcore.editor.bundle.effect;

import me.blutkrone.rpgcore.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.editor.annotation.value.EditorNumber;
import me.blutkrone.rpgcore.editor.root.other.EditorEffect;
import me.blutkrone.rpgcore.effect.CoreEffect;
import me.blutkrone.rpgcore.effect.impl.CoreEffectRadiator;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorEffectRadiator implements EditorEffect.IEditorEffectBundle {

    @EditorNumber(name = "Pitch", minimum = 0d)
    @EditorTooltip(tooltip = "How much a spread with pitch angle")
    public double spread_pitch;
    @EditorNumber(name = "Yaw", minimum = 0d)
    @EditorTooltip(tooltip = "How much a spread with yaw angle")
    public double spread_yaw;
    @EditorNumber(name = "Duration", minimum = 0d)
    @EditorTooltip(tooltip = "Over how many ticks do we complete a line")
    public double duration;
    @EditorNumber(name = "Count", minimum = 0d)
    @EditorTooltip(tooltip = "How many lines do we render")
    public double count;
    @EditorNumber(name = "Length", minimum = 0d)
    @EditorTooltip(tooltip = "The length of each line")
    public double length;
    @EditorNumber(name = "Offset", minimum = 0d)
    @EditorTooltip(tooltip = "Initial offset of the lines")
    public double offset;
    @EditorNumber(name = "Scatter")
    @EditorTooltip(tooltip = "Randomize particle position by this much")
    public double scatter = 0d;
    @EditorNumber(name = "Sample")
    @EditorTooltip(tooltip = "Multiplier to how many particles are used")
    public double sample = 0d;

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.BOOKSHELF)
                .name("§aRadiator Effect")
                .appendLore("§fPitch:" + spread_pitch)
                .appendLore("§fYaw: " + spread_yaw)
                .appendLore("§fDuration: " + duration)
                .appendLore("§fCount: " + count)
                .appendLore("§fLength: " + length)
                .appendLore("§fOffset: " + offset)
                .appendLore("§fSample: " + sample)
                .appendLore("§fScatter: " + scatter)
                .build();
    }

    @Override
    public String getName() {
        return "Radiator";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instructions = new ArrayList<>();
        instructions.add("§fRadiator Effect");
        instructions.add("Particles pushed into facing direction");
        return instructions;
    }

    @Override
    public CoreEffect.IEffectPart build() {
        return new CoreEffectRadiator(this);
    }
}
