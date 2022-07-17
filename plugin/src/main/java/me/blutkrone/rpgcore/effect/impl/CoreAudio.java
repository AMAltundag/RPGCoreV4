package me.blutkrone.rpgcore.effect.impl;

import me.blutkrone.rpgcore.effect.CoreEffect;
import me.blutkrone.rpgcore.hud.editor.bundle.effect.EditorAudio;
import me.blutkrone.rpgcore.util.collection.WeightedRandomMap;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.List;

/**
 * An effect which can emit a sound to the entities who
 * are viewing it.
 */
public class CoreAudio implements CoreEffect.IEffectPart {
    private String sound;
    private float pitch;
    private float volume;

    public CoreAudio(EditorAudio editor) {
        this.sound = editor.sound;
        this.pitch = (float) editor.pitch;
        this.volume = (float) editor.volume;
    }

    @Override
    public void process(Location where, Vector offset, WeightedRandomMap<CoreParticleBrush> brush, double scale, List<Player> viewing) {
        // identify exact position
        offset = offset.clone().multiply(scale);
        where = where.clone().add(offset);

        try {
            // play the exact sound if available
            Sound exact_sound = Sound.valueOf(this.sound);
            for (Player player : viewing) {
                player.playSound(where, exact_sound, this.pitch, this.volume);
            }
        } catch (Exception e) {
            // otherwise try a custom sound
            for (Player player : viewing) {
                player.playSound(where, this.sound, this.pitch, this.volume);
            }
        }
    }
}
