package me.blutkrone.rpgcore.effect.impl;

import me.blutkrone.rpgcore.effect.CoreEffect;
import me.blutkrone.rpgcore.effect.EffectObservation;
import me.blutkrone.rpgcore.hud.editor.bundle.effect.EditorEffectWait;
import me.blutkrone.rpgcore.util.collection.WeightedRandomMap;
import org.bukkit.Location;

import java.util.Collections;
import java.util.List;

public class CoreEffectWait implements CoreEffect.IEffectPart {

    // how many ticks to wait
    public int time;

    public CoreEffectWait(EditorEffectWait editor) {
        this.time = (int) editor.time;
    }

    @Override
    public List<CoreEffect.ISubPart> process(Location where, WeightedRandomMap<CoreEffectBrush> brush, double scale, EffectObservation viewing) {
        // wait does not do anything
        return Collections.emptyList();
    }
}
