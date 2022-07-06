package me.blutkrone.rpgcore.hud.editor.bundle;

import me.blutkrone.rpgcore.effect.CoreEffect;
import me.blutkrone.rpgcore.effect.impl.CoreParticlePoint;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorNumber;
import me.blutkrone.rpgcore.hud.editor.root.EditorEffect;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorParticlePoint implements EditorEffect.IEditorEffectBundle {

    @EditorNumber(name = "Scatter", minimum = 0d)
    public double scatter;
    @EditorNumber(name = "Sample", minimum = 0d)
    public double sample;

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.BOOKSHELF)
                .name("§aParticle Point")
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
        instructions.add("§fParticle Point");
        instructions.add("Spawns random particles around a point");
        return instructions;
    }

    @Override
    public CoreEffect.IEffectPart build() {
        return new CoreParticlePoint(this);
    }
}
