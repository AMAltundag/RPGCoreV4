package me.blutkrone.rpgcore.effect.impl;

import me.blutkrone.rpgcore.effect.CoreEffect;
import me.blutkrone.rpgcore.effect.EffectObservation;
import me.blutkrone.rpgcore.hud.editor.bundle.effect.EditorEffectAudio;
import me.blutkrone.rpgcore.util.collection.WeightedRandomMap;
import org.bukkit.Location;
import org.bukkit.Sound;

import java.util.Collections;
import java.util.List;

/**
 * An effect which can emit a sound to the entities who
 * are viewing it.
 */
public class CoreEffectAudio implements CoreEffect.IEffectPart {
    private String sound;
    private float pitch;
    private float volume;

    public CoreEffectAudio(EditorEffectAudio editor) {
        this.sound = editor.sound.replace("_", ".");
        this.pitch = (float) editor.pitch;
        this.volume = (float) editor.volume;
    }

    @Override
    public List<CoreEffect.ISubPart> process(Location where, WeightedRandomMap<CoreEffectBrush> brush, double scale, EffectObservation viewing) {
        try {
            // play the exact sound if available
            Sound exact_sound = Sound.valueOf(this.sound);
            viewing.forEach((player) -> {
                player.playSound(where, exact_sound, this.pitch, this.volume);
            });
        } catch (Exception e) {
            // otherwise try a custom sound
            viewing.forEach((player) -> {
                player.playSound(where, this.sound, this.pitch, this.volume);
            });
        }

        return Collections.emptyList();
    }
}
