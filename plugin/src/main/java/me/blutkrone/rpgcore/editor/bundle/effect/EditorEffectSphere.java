package me.blutkrone.rpgcore.editor.bundle.effect;

import me.blutkrone.rpgcore.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.editor.annotation.value.EditorNumber;
import me.blutkrone.rpgcore.editor.root.other.EditorEffect;
import me.blutkrone.rpgcore.effect.CoreEffect;
import me.blutkrone.rpgcore.effect.impl.CoreEffectSphere;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorEffectSphere implements EditorEffect.IEditorEffectBundle {

    @EditorNumber(name = "Inner", minimum = 0d)
    @EditorTooltip(tooltip = "Inner radius of the sphere")
    public double min_radius = 2d;
    @EditorNumber(name = "Outer", minimum = 0d)
    @EditorTooltip(tooltip = "Outer radius of the sphere")
    public double max_radius = 3d;
    @EditorNumber(name = "Scatter")
    @EditorTooltip(tooltip = "Randomize particle position by this much")
    public double scatter = 0d;
    @EditorNumber(name = "Sample")
    @EditorTooltip(tooltip = "Multiplier to how many particles are used")
    public double sample = 1d;

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.BOOKSHELF)
                .name("§aParticle Sphere")
                .appendLore("§fSample: " + (int) (sample))
                .appendLore("§fScatter: " + scatter)
                .appendLore("§fInner: " + min_radius)
                .appendLore("§fOuter: " + max_radius)
                .build();
    }

    @Override
    public String getName() {
        return "Sphere";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instructions = new ArrayList<>();
        instructions.add("§fSphere Effect");
        instructions.add("Spawns particles in a spherical shape");
        return instructions;
    }

    @Override
    public CoreEffect.IEffectPart build() {
        return new CoreEffectSphere(this);
    }
}
