package me.blutkrone.rpgcore.effect.impl;

import me.blutkrone.rpgcore.effect.CoreEffect;
import me.blutkrone.rpgcore.effect.EffectObservation;
import me.blutkrone.rpgcore.hud.editor.bundle.effect.EditorEffectCircle;
import me.blutkrone.rpgcore.util.collection.WeightedRandomMap;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class CoreEffectCircle implements CoreEffect.IEffectPart {

    private static final double PHI = Math.PI * (3d - Math.sqrt(5d));

    private double scatter;
    private double sample;
    private double minimum_diameter;
    private double maximum_diameter;

    public CoreEffectCircle(EditorEffectCircle editor) {
        this.scatter = editor.scatter;
        this.sample = editor.sample;
        this.minimum_diameter = Math.min(editor.maximum_diameter, editor.minimum_diameter);
        this.maximum_diameter = Math.max(editor.maximum_diameter, editor.minimum_diameter);
    }

    /*
     * Fibonacci spiral based circle.
     *
     * @param where anchor within absolute space
     * @param brush particles to render
     * @param scale effect scaling ratio
     * @param viewing who is viewing the effect
     */
    private void fiboCircle(Location where, WeightedRandomMap<CoreEffectBrush> brush, double scale, EffectObservation viewing) {
        viewing.forEach((viewers, supersample) -> {
            double minimum = this.minimum_diameter * scale;
            double maximum = this.maximum_diameter * scale;
            double surface = (Math.PI * maximum * maximum) - (Math.PI * minimum * minimum);
            int density = (int) (this.sample * surface * supersample);

            for (int i = 0; i < density; i++) {
                double theta = i * PHI;
                double r = Math.sqrt(i) / Math.sqrt(density);
                if (Math.abs(r*maximum) >= minimum) {
                    // identify the point on the disk
                    double x = r * maximum * Math.cos(theta);
                    double y = r * maximum * Math.sin(theta);

                    // transform into euclidean coordinates
                    Vector relative = new Vector(x, 0, y);
                    // apply random offset on the sample point
                    if (this.scatter > 0d) {
                        double _x = ThreadLocalRandom.current().nextDouble(-this.scatter, +this.scatter);
                        double _y = ThreadLocalRandom.current().nextDouble(-this.scatter, +this.scatter);
                        double _z = ThreadLocalRandom.current().nextDouble(-this.scatter, +this.scatter);
                        relative.add(new Vector(_x, _y, _z));
                    }
                    // map into planar space
                    Location absolute = where.clone().add(relative);
                    absolute.setDirection(relative.normalize());
                    // draw the sphere we created on the screen
                    brush.next().show(absolute, viewers);
                }
            }
        });
    }

    private void edgeCircle(Location where, WeightedRandomMap<CoreEffectBrush> brush, double scale, EffectObservation viewing) {
        viewing.forEach((viewers, supersample) -> {
            double radius = this.maximum_diameter * scale;
            int density = (int) (Math.PI * radius * radius * sample * supersample);
            for (int i = 0; i < density; i++) {
                // construct a circle
                double theta = ((i * 1d) / (density * 1D)) * Math.PI * 2;
                double x = Math.cos(theta) * radius;
                double y = Math.sin(theta) * radius;
                // transform into euclidean coordinates
                Vector relative = new Vector(x, 0, y);
                // apply random offset on the sample point
                if (this.scatter > 0d) {
                    double _x = ThreadLocalRandom.current().nextDouble(-this.scatter, +this.scatter);
                    double _y = ThreadLocalRandom.current().nextDouble(-this.scatter, +this.scatter);
                    double _z = ThreadLocalRandom.current().nextDouble(-this.scatter, +this.scatter);
                    relative.add(new Vector(_x, _y, _z));
                }
                // map into planar space
                Location absolute = where.clone().add(relative);
                absolute.setDirection(relative.normalize());
                // draw the sphere we created on the screen
                brush.next().show(absolute, viewers);
            }
        });
    }

    @Override
    public List<CoreEffect.ISubPart> process(Location where, WeightedRandomMap<CoreEffectBrush> brush, double scale, EffectObservation viewing) {
        if (Math.abs(maximum_diameter-minimum_diameter) > 0.01d) {
            fiboCircle(where, brush, scale, viewing);
        } else {
            edgeCircle(where, brush, scale, viewing);
        }

        return Collections.emptyList();
    }
}
