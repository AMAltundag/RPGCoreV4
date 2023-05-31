package me.blutkrone.rpgcore.effect.impl;

import me.blutkrone.rpgcore.effect.CoreEffect;
import me.blutkrone.rpgcore.effect.EffectObservation;
import me.blutkrone.rpgcore.hud.editor.bundle.effect.EditorEffectRadiator;
import me.blutkrone.rpgcore.util.collection.WeightedRandomMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class CoreEffectRadiator implements CoreEffect.IEffectPart {

    private float spread_pitch;
    private float spread_yaw;
    private int duration;
    private int count;
    private double length;
    private double offset;
    private double sample;
    private double scatter;

    public CoreEffectRadiator(EditorEffectRadiator editor) {
        this.spread_pitch = (float) editor.spread_pitch;
        this.spread_yaw = (float) editor.spread_yaw;
        this.duration = (int) editor.duration;
        this.count = (int) editor.count;
        this.length = editor.length;
        this.offset = editor.offset;
        this.sample = editor.sample;
        this.scatter = editor.scatter;

        Bukkit.getLogger().info("not implemented (radiator looks off?)");
    }

    @Override
    public List<CoreEffect.ISubPart> process(Location where, WeightedRandomMap<CoreEffectBrush> brush, double scale, EffectObservation viewing) {
        List<CoreEffect.ISubPart> parts = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Location clone = where.clone();
            // randomize the direction
            float pitch = clone.getPitch() + ThreadLocalRandom.current().nextFloat(-spread_pitch, +spread_pitch);
            clone.setPitch(pitch);
            float yaw = clone.getYaw() + ThreadLocalRandom.current().nextFloat(-spread_yaw, +spread_yaw);
            clone.setYaw(yaw);
            // apply original displacement
            clone.add(clone.getDirection().multiply(offset));
            // query a radiator
            parts.add(new Radiator(clone, length * scale, brush.snapshot(), viewing));
        }

        return parts;
    }

    class Radiator implements CoreEffect.ISubPart {

        Location where;
        double length;
        WeightedRandomMap<CoreEffectBrush> brush;
        EffectObservation viewing;
        int cycle;

        Radiator(Location where, double length, WeightedRandomMap<CoreEffectBrush> brush, EffectObservation viewing) {
            this.where = where;
            this.length = length;
            this.brush = brush;
            this.viewing = viewing;
            this.cycle = 0;
        }

        @Override
        public boolean process() {
            double step = length / duration;
            for (int i = 0; i <= step * sample; i++) {
                // identify where the radiator is at
                Vector direction = where.getDirection();
                double move = step * (i / (step * sample));
                where.add(direction.multiply(move));
                Location position = where.clone();
                // apply random offset on the sample point
                if (scatter > 0d) {
                    double _x = ThreadLocalRandom.current().nextDouble(-scatter, +scatter);
                    double _y = ThreadLocalRandom.current().nextDouble(-scatter, +scatter);
                    double _z = ThreadLocalRandom.current().nextDouble(-scatter, +scatter);
                    position.add(new Vector(_x, _y, _z));
                }
                // draw the sphere we created on the screen
                brush.next().show(position, viewing.asList());
            }

            return ++cycle > duration;
        }
    }
}
