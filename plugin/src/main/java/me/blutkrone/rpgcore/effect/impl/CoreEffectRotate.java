package me.blutkrone.rpgcore.effect.impl;

import me.blutkrone.rpgcore.effect.CoreEffect;
import me.blutkrone.rpgcore.effect.EffectObservation;
import me.blutkrone.rpgcore.hud.editor.bundle.effect.EditorEffectRotate;
import me.blutkrone.rpgcore.util.collection.WeightedRandomMap;
import org.bukkit.Location;

import java.util.Collections;
import java.util.List;

public class CoreEffectRotate implements CoreEffect.IEffectPart {

    private float pitch;
    private float yaw;

    public CoreEffectRotate(EditorEffectRotate editor) {
        this.pitch = (float) editor.pitch;
        this.yaw = (float) editor.yaw;
    }

    @Override
    public List<CoreEffect.ISubPart> process(Location where, WeightedRandomMap<CoreEffectBrush> brush, double scale, EffectObservation viewing) {
        where.setPitch(where.getPitch() + this.pitch);
        where.setYaw(where.getYaw() + this.yaw);
        return Collections.emptyList();
    }
}
