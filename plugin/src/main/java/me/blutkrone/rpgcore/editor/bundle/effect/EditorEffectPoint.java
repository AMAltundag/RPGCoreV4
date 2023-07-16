package me.blutkrone.rpgcore.editor.bundle.effect;

import me.blutkrone.rpgcore.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.editor.annotation.value.EditorNumber;
import me.blutkrone.rpgcore.editor.root.other.EditorEffect;
import me.blutkrone.rpgcore.effect.CoreEffect;
import me.blutkrone.rpgcore.effect.impl.CoreEffectPoint;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorEffectPoint implements EditorEffect.IEditorEffectBundle {
    @EditorNumber(name = "Scatter")
    @EditorTooltip(tooltip = "Randomize particle position by this much")
    public double scatter = 0d;
    @EditorNumber(name = "Sample")
    @EditorTooltip(tooltip = "Multiplier to how many particles are used")
    public double sample = 0d;

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.BOOKSHELF)
                .name("§aPoint Effect")
                .appendLore("§fSample: " + (int) (sample))
                .appendLore("§fScatter: " + scatter)
                .build();
    }

    @Override
    public String getName() {
        return "Point";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instructions = new ArrayList<>();
        instructions.add("§fPoint Effect");
        instructions.add("Spawns random particles around a point");
        return instructions;
    }

    @Override
    public CoreEffect.IEffectPart build() {
        return new CoreEffectPoint(this);
    }
}
