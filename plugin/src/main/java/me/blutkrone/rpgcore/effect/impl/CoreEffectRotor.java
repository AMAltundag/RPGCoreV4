package me.blutkrone.rpgcore.effect.impl;

import me.blutkrone.rpgcore.editor.bundle.effect.EditorEffectRotor;
import me.blutkrone.rpgcore.effect.CoreEffect;
import me.blutkrone.rpgcore.effect.EffectObservation;
import me.blutkrone.rpgcore.util.Utility;
import me.blutkrone.rpgcore.util.collection.WeightedRandomMap;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class CoreEffectRotor implements CoreEffect.IEffectPart {

    private float start;
    private float angle;
    private double radius;
    private int duration;
    private double sample;
    private double scatter;

    public CoreEffectRotor(EditorEffectRotor editor) {
        this.start = (float) editor.start;
        this.angle = (float) editor.angle;
        this.radius = editor.radius;
        this.duration = (int) editor.duration;
        this.sample = editor.sample;
        this.scatter = editor.scatter;
    }

    @Override
    public List<CoreEffect.ISubPart> process(Location where, WeightedRandomMap<CoreEffectBrush> brush, double scale, EffectObservation viewing) {
        return Arrays.asList(new Rotor(where.clone(), brush.snapshot(), viewing, start));
    }

    class Rotor implements CoreEffect.ISubPart {

        Location where;
        WeightedRandomMap<CoreEffectBrush> brush;
        EffectObservation viewing;
        float angle;
        int cycle;
        int points;

        public Rotor(Location where, WeightedRandomMap<CoreEffectBrush> brush, EffectObservation viewing, float angle) {
            this.where = where;
            this.brush = brush;
            this.viewing = viewing;
            this.angle = angle;
            this.points = (int) (Math.PI * radius * radius * sample);
            this.cycle = 0;
        }

        @Override
        public boolean process() {
            double rotation = CoreEffectRotor.this.angle / duration;
            int samples = points / duration;

            for (int i = 0; i < samples; i++) {
                angle += rotation / samples;
                double theta = Math.toRadians(angle);
                double x = Math.cos(theta) * radius;
                double y = Math.sin(theta) * radius;
                Vector relative = new Vector(x, 0d, y);
                relative = Utility.drawOnPlane(where, relative, 0d);
                // apply random offset on the sample point
                if (scatter > 0d) {
                    double _x = ThreadLocalRandom.current().nextDouble(-scatter, +scatter);
                    double _y = ThreadLocalRandom.current().nextDouble(-scatter, +scatter);
                    double _z = ThreadLocalRandom.current().nextDouble(-scatter, +scatter);
                    relative.add(new Vector(_x, _y, _z));
                }
                // extract relative direction for particle
                Vector directional = relative.clone().subtract(where.toVector()).normalize();
                Location absolute = relative.toLocation(where.getWorld());
                absolute.setDirection(directional);
                // draw the sphere we created on the screen
                brush.next().show(absolute, viewing.asList());
            }

            return cycle++ > duration;
        }
    }
}
