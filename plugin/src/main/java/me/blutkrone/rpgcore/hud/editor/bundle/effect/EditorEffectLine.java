package me.blutkrone.rpgcore.hud.editor.bundle.effect;

import me.blutkrone.rpgcore.effect.CoreEffect;
import me.blutkrone.rpgcore.effect.impl.CoreEffectLine;
import me.blutkrone.rpgcore.hud.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorNumber;
import me.blutkrone.rpgcore.hud.editor.root.other.EditorEffect;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorEffectLine implements EditorEffect.IEditorEffectBundle {
    @EditorNumber(name = "Length")
    @EditorTooltip(tooltip = "How long a line we should have")
    public double length = 0d;
    @EditorNumber(name = "Scatter")
    @EditorTooltip(tooltip = "Randomize particle position by this much")
    public double scatter = 0d;
    @EditorNumber(name = "Sample")
    @EditorTooltip(tooltip = "Multiplier to how many particles are used")
    public double sample = 0d;

    public EditorEffectLine() {
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.BOOKSHELF)
                .name("§aLine Effect")
                .appendLore("§fLength: " + length)
                .appendLore("§fScatter: " + scatter)
                .appendLore("§fSample: " + sample)
                .build();
    }

    @Override
    public String getName() {
        return "Line";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Line Effect");
        instruction.add("Draw a line of particles");
        return instruction;
    }

    @Override
    public CoreEffect.IEffectPart build() {
        return new CoreEffectLine(this);
    }
}
