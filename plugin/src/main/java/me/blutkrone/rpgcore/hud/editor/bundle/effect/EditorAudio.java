package me.blutkrone.rpgcore.hud.editor.bundle.effect;

import me.blutkrone.rpgcore.effect.CoreEffect;
import me.blutkrone.rpgcore.effect.impl.CoreAudio;
import me.blutkrone.rpgcore.hud.editor.annotation.EditorName;
import me.blutkrone.rpgcore.hud.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorNumber;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorWrite;
import me.blutkrone.rpgcore.hud.editor.constraint.enums.SoundConstraint;
import me.blutkrone.rpgcore.hud.editor.root.other.EditorEffect;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

@EditorName(name = "Audio")
@EditorTooltip(tooltip = "An attribute mapped to a numeric value")
public class EditorAudio implements EditorEffect.IEditorEffectBundle {
    @EditorWrite(name = "Sound", constraint = SoundConstraint.class)
    @EditorTooltip(tooltip = {"Which sound to use"})
    public String sound = "ENTITY_GHAST_SCREAM";
    @EditorNumber(name = "Pitch", minimum = -1d, maximum = +1d)
    @EditorTooltip(tooltip = "Pitch of the sound to play.")
    public double pitch = 0d;
    @EditorNumber(name = "Volume")
    @EditorTooltip(tooltip = "Volume of the sound to play")
    public double volume = 0d;

    public EditorAudio() {
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.BOOKSHELF)
                .name("§aSound")
                .appendLore("§fSound: " + sound)
                .appendLore("§fVolume: " + String.format("%.1f", this.volume))
                .appendLore("§fPitch: " + String.format("%.1f", this.pitch))
                .build();
    }

    @Override
    public String getName() {
        return "Sound";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Sound Effect");
        instruction.add("A sound effect to play.");
        return instruction;
    }

    @Override
    public CoreEffect.IEffectPart build() {
        return new CoreAudio(this);
    }
}
