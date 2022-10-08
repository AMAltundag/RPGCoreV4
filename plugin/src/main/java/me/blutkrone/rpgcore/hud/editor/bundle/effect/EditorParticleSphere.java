package me.blutkrone.rpgcore.hud.editor.bundle.effect;

import me.blutkrone.rpgcore.effect.CoreEffect;
import me.blutkrone.rpgcore.effect.impl.CoreParticleSphere;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorNumber;
import me.blutkrone.rpgcore.hud.editor.root.other.EditorEffect;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorParticleSphere implements EditorEffect.IEditorEffectBundle {

    @EditorNumber(name = "Scatter", minimum = 0d)
    public double scatter = 0.5d;
    @EditorNumber(name = "Sample", minimum = 0d)
    public double sample = 1d;
    @EditorNumber(name = "Inner", minimum = 0d)
    public double min_radius = 2d;
    @EditorNumber(name = "Outer", minimum = 0d)
    public double max_radius = 3d;

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
        instructions.add("§fParticle Sphere");
        instructions.add("Spawns random particles in a spherical shape");
        return instructions;
    }

    @Override
    public CoreEffect.IEffectPart build() {
        return new CoreParticleSphere(this);
    }
}
