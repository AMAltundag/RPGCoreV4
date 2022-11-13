package me.blutkrone.rpgcore.effect.impl;

import me.blutkrone.rpgcore.effect.CoreEffect;
import me.blutkrone.rpgcore.effect.EffectObservation;
import me.blutkrone.rpgcore.hud.editor.bundle.effect.EditorEffectForward;
import me.blutkrone.rpgcore.util.collection.WeightedRandomMap;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.Collections;
import java.util.List;

public class CoreEffectForward implements CoreEffect.IEffectPart {

    public double length;

    public CoreEffectForward(EditorEffectForward editor) {
        this.length = editor.length;
    }

    @Override
    public List<CoreEffect.ISubPart> process(Location where, WeightedRandomMap<CoreEffectBrush> brush, double scale, EffectObservation viewing) {
        Vector direction = where.getDirection();
        where.add(direction.multiply(length * scale));
        return Collections.emptyList();
    }
}
