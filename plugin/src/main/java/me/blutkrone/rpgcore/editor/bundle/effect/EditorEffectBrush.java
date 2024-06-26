package me.blutkrone.rpgcore.editor.bundle.effect;

import me.blutkrone.rpgcore.editor.annotation.EditorHideWhen;
import me.blutkrone.rpgcore.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.editor.annotation.value.EditorColor;
import me.blutkrone.rpgcore.editor.annotation.value.EditorNumber;
import me.blutkrone.rpgcore.editor.annotation.value.EditorWrite;
import me.blutkrone.rpgcore.editor.constraint.enums.MaterialConstraint;
import me.blutkrone.rpgcore.editor.constraint.enums.ParticleConstraint;
import me.blutkrone.rpgcore.editor.root.other.EditorEffect;
import me.blutkrone.rpgcore.effect.CoreEffect;
import me.blutkrone.rpgcore.effect.impl.CoreEffectBrush;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorEffectBrush implements EditorEffect.IEditorEffectBundle {

    @EditorNumber(name = "Weight", minimum = 0)
    @EditorTooltip(tooltip = "Chance to roll this particle effect among others.")
    public double weighting = 1d;
    @EditorWrite(name = "Particle", constraint = ParticleConstraint.class)
    @EditorTooltip(tooltip = "Which particle type to spawn.")
    public Particle particle = Particle.SMALL_FLAME;
    @EditorNumber(name = "Speed", minimum = 0)
    @EditorTooltip(tooltip = "Speed the particle is to move with.")
    public double speed = 0.03d;
    @EditorNumber(name = "Amount", minimum = 0)
    @EditorTooltip(tooltip = "Number of particles, does not affect all particles.")
    public double amount = 1;
    @EditorWrite(name = "Material", constraint = MaterialConstraint.class)
    @EditorTooltip(tooltip = "Material to use for the particle.")
    @EditorHideWhen(field = "particle", value = {"ITEM_CRACK", "BLOCK_CRACK", "BLOCK_DUST", "FALLING_DUST"}, invert = true)
    public Material material = Material.STONE;
    @EditorNumber(name = "Model")
    @EditorTooltip(tooltip = "Custom model to use for item particles")
    @EditorHideWhen(field = "particle", value = {"ITEM_CRACK"}, invert = true)
    public double model = 0;
    @EditorColor(name = "Color")
    @EditorTooltip(tooltip = "Color to use for the particle.")
    @EditorHideWhen(field = "particle", value = {"REDSTONE", "SPELL_MOB", "SPELL_MOB_AMBIENT", "NOTE"}, invert = true)
    public String color = "FFFFFF";

    public EditorEffectBrush() {
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.BOOKSHELF)
                .name("§aParticle Brush")
                .appendLore("§fParticle: " + this.particle)
                .appendLore("§fWeight: " + String.format("%.3f", weighting))
                .build();
    }

    @Override
    public String getName() {
        return "Brush";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Brush Effect");
        instruction.add("Adds this particle to our current brush.");
        return instruction;
    }

    @Override
    public CoreEffect.IEffectPart build() {
        return new CoreEffectBrush(this);
    }
}
