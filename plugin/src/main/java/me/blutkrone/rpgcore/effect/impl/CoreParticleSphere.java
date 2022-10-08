package me.blutkrone.rpgcore.effect.impl;

import me.blutkrone.rpgcore.effect.CoreEffect;
import me.blutkrone.rpgcore.hud.editor.bundle.effect.EditorParticleSphere;
import me.blutkrone.rpgcore.util.collection.WeightedRandomMap;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class CoreParticleSphere implements CoreEffect.IEffectPart {

    private static final double PHI = Math.PI * (3d - Math.sqrt(5d));

    private double scatter;
    private int sample;
    private double minimum;
    private double maximum;

    public CoreParticleSphere(EditorParticleSphere editor) {
        this.scatter = editor.scatter;
        this.sample = (int) editor.sample;
        this.minimum = editor.min_radius;
        this.maximum = editor.max_radius;
    }

    @Override
    public void process(Location where, Vector offset, WeightedRandomMap<CoreParticleBrush> brush, double scale, List<Player> viewing) {
        if (brush.isEmpty()) {
            return;
        }

        double minimum = Math.max(0d, Math.min(this.minimum, this.maximum));
        double maximum = Math.max(0d, Math.max(this.minimum, this.maximum));

        int samples = (int) (this.sample * (4 * Math.PI * maximum * maximum) * Math.sqrt(Math.max(1d, maximum - minimum)));
        for (double i = 0; i < samples; i++) {
            // construct the polar coordinates to work with
            double y = 1d - (i / (samples - 1d)) * 2d;
            double r = Math.sqrt(1d - y * y);
            double t = PHI * i;
            double x = Math.cos(t) * r;
            double z = Math.sin(t) * r;
            // transform into euclidean coordinates
            Vector relative = new Vector(x, y, z).multiply(scale).multiply(minimum + (Math.random() * (maximum-minimum)));
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
            brush.next().show(absolute, viewing);
        }
    }
}
