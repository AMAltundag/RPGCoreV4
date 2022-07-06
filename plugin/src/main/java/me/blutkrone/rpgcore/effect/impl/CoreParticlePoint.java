package me.blutkrone.rpgcore.effect.impl;

import me.blutkrone.rpgcore.effect.CoreEffect;
import me.blutkrone.rpgcore.hud.editor.bundle.EditorParticlePoint;
import me.blutkrone.rpgcore.util.collection.WeightedRandomMap;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class CoreParticlePoint implements CoreEffect.IEffectPart {

    private double scatter;
    private int sample;

    public CoreParticlePoint(EditorParticlePoint point) {
        this.scatter = point.scatter;
        this.sample = (int) point.sample;
    }

    @Override
    public void process(Location where, Vector offset, WeightedRandomMap<CoreParticleBrush> brush, double scale, List<Player> viewing) {
        if (brush.isEmpty()) {
            return;
        }

        for (int i = 0; i < this.sample; i++) {
            // identify relative location
            Vector relative = offset.clone();
            if (this.scatter > 0d) {
                double x = ThreadLocalRandom.current().nextDouble(-this.scatter, +this.scatter);
                double y = ThreadLocalRandom.current().nextDouble(-this.scatter, +this.scatter);
                double z = ThreadLocalRandom.current().nextDouble(-this.scatter, +this.scatter);
                relative.add(new Vector(x, y, z));
            }
            relative.multiply(scale);
            // merge with absolute position
            Location absolute = where.clone().add(relative);
            // pop the particle on the brush
            brush.next().show(absolute, viewing);
        }
    }
}
