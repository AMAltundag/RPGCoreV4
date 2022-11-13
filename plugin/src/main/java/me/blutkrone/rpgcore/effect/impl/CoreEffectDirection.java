package me.blutkrone.rpgcore.effect.impl;

import me.blutkrone.rpgcore.effect.CoreEffect;
import me.blutkrone.rpgcore.effect.EffectObservation;
import me.blutkrone.rpgcore.hud.editor.bundle.effect.EditorEffectDirection;
import me.blutkrone.rpgcore.util.collection.WeightedRandomMap;
import org.bukkit.Location;

import java.util.Collections;
import java.util.List;

public class CoreEffectDirection implements CoreEffect.IEffectPart {

    private float pitch;
    private float yaw;

    public CoreEffectDirection(EditorEffectDirection editor) {
        this.pitch = (float) editor.pitch;
        this.yaw = (float) editor.yaw;
    }

    @Override
    public List<CoreEffect.ISubPart> process(Location where, WeightedRandomMap<CoreEffectBrush> brush, double scale, EffectObservation viewing) {
        where.setPitch(this.pitch);
        where.setYaw(this.yaw);
        return Collections.emptyList();
    }
}
