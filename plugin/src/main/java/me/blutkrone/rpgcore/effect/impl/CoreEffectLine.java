package me.blutkrone.rpgcore.effect.impl;

import me.blutkrone.rpgcore.editor.bundle.effect.EditorEffectLine;
import me.blutkrone.rpgcore.effect.CoreEffect;
import me.blutkrone.rpgcore.effect.EffectObservation;
import me.blutkrone.rpgcore.util.collection.WeightedRandomMap;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class CoreEffectLine implements CoreEffect.IEffectPart {

    private double length;
    private double scatter;
    private double sample;

    public CoreEffectLine(EditorEffectLine editor) {
        this.length = editor.length;
        this.scatter = editor.scatter;
        this.sample = editor.sample;
    }

    @Override
    public List<CoreEffect.ISubPart> process(Location where, WeightedRandomMap<CoreEffectBrush> brush, double scale, EffectObservation viewing) {
        viewing.forEach((viewers, supersample) -> {
            double length = this.length * scale;
            Location position = where.clone();

            for (double i = 0; i <= (length * sample * supersample); i++) {
                double offset = length * (i / (length * sample * supersample));
                // move along our displacement
                position.add(position.getDirection().multiply(offset));
                // apply random offset on the sample point
                if (this.scatter > 0d) {
                    double _x = ThreadLocalRandom.current().nextDouble(-this.scatter, +this.scatter);
                    double _y = ThreadLocalRandom.current().nextDouble(-this.scatter, +this.scatter);
                    double _z = ThreadLocalRandom.current().nextDouble(-this.scatter, +this.scatter);
                    position.add(new Vector(_x, _y, _z));
                }
                // draw the sphere we created on the screen
                brush.next().show(position, viewers);
            }
        });


        return Collections.emptyList();
    }
}
