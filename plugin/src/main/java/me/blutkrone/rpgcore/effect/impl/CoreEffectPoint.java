package me.blutkrone.rpgcore.effect.impl;

import me.blutkrone.rpgcore.editor.bundle.effect.EditorEffectPoint;
import me.blutkrone.rpgcore.effect.CoreEffect;
import me.blutkrone.rpgcore.effect.EffectObservation;
import me.blutkrone.rpgcore.util.collection.WeightedRandomMap;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class CoreEffectPoint implements CoreEffect.IEffectPart {

    private double scatter;
    private double sample;

    public CoreEffectPoint(EditorEffectPoint point) {
        this.scatter = point.scatter;
        this.sample = point.sample;
    }

    @Override
    public List<CoreEffect.ISubPart> process(Location where, WeightedRandomMap<CoreEffectBrush> brush, double scale, EffectObservation viewing) {
        if (brush.isEmpty()) {
            return Collections.emptyList();
        }

        viewing.forEach((viewers, supersample) -> {
            double real_sample = this.sample * supersample;
            if (real_sample <= 1d) {
                real_sample = Math.random() < real_sample ? 1d : 0d;
            }

            for (int i = 0; i < real_sample; i++) {
                // identify relative location
                Vector relative = new Vector();
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
                brush.next().show(absolute, viewers);
            }
        });

        return Collections.emptyList();
    }
}
