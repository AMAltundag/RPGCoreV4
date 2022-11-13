package me.blutkrone.rpgcore.effect.impl;

import me.blutkrone.rpgcore.effect.CoreEffect;
import me.blutkrone.rpgcore.effect.EffectObservation;
import me.blutkrone.rpgcore.hud.editor.bundle.effect.EditorEffectSphere;
import me.blutkrone.rpgcore.util.collection.WeightedRandomMap;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class CoreEffectSphere implements CoreEffect.IEffectPart {

    private static final double PHI = Math.PI * (3d - Math.sqrt(5d));

    private double scatter;
    private double sample;
    private double minimum;
    private double maximum;

    public CoreEffectSphere(EditorEffectSphere editor) {
        this.scatter = editor.scatter;
        this.sample = editor.sample;
        this.minimum = editor.min_radius;
        this.maximum = editor.max_radius;
    }

    @Override
    public List<CoreEffect.ISubPart> process(Location where, WeightedRandomMap<CoreEffectBrush> brush, double scale, EffectObservation viewing) {
        if (brush.isEmpty()) {
            return Collections.emptyList();
        }

        viewing.forEach((viewers, supersample) -> {
            double minimum = Math.max(0d, Math.min(this.minimum, this.maximum)) * scale;
            double maximum = Math.max(0d, Math.max(this.minimum, this.maximum)) * scale;
            int samples = (int) (this.sample * (4 * Math.PI * maximum * maximum) * supersample);

            for (double i = 0; i < samples; i++) {
                // construct the polar coordinates to work with
                double y = 1d - (i / (samples - 1d)) * 2d;
                double r = Math.sqrt(1d - y * y);
                double t = PHI * i;
                double x = Math.cos(t) * r;
                double z = Math.sin(t) * r;
                // transform into euclidean coordinates
                Vector relative = new Vector(x, y, z).multiply(minimum + (Math.random() * (maximum-minimum)));
                // apply random offset on the sample point
                if (this.scatter > 0d) {
                    double _x = ThreadLocalRandom.current().nextDouble(-this.scatter, +this.scatter);
                    double _y = ThreadLocalRandom.current().nextDouble(-this.scatter, +this.scatter);
                    double _z = ThreadLocalRandom.current().nextDouble(-this.scatter, +this.scatter);
                    relative.add(new Vector(_x, _y, _z));
                }
                // merge with absolute position
                Location absolute = where.clone().add(relative);
                // draw the sphere we created on the screen
                brush.next().show(absolute, viewing.asList());
            }
        });

        return Collections.emptyList();
    }
}
