package me.blutkrone.rpgcore.effect.impl;

import me.blutkrone.rpgcore.effect.ActiveEffect;
import me.blutkrone.rpgcore.effect.CoreEffect;
import me.blutkrone.rpgcore.effect.EffectObservation;
import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.hud.editor.bundle.effect.EditorEffectRepeat;
import me.blutkrone.rpgcore.hud.editor.root.other.EditorEffect;
import me.blutkrone.rpgcore.util.collection.WeightedRandomMap;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CoreEffectRepeat implements CoreEffect.IEffectPart {

    private List<CoreEffect.IEffectPart> parts = new ArrayList<>();
    private int cycles;
    private double expansion;

    public CoreEffectRepeat(EditorEffectRepeat editor) {
        for (IEditorBundle bundle : editor.parts) {
            this.parts.add(((EditorEffect.IEditorEffectBundle) bundle).build());
        }
        this.cycles = (int) editor.cycles;
        this.expansion = editor.expansion_per_cycle;
    }

    @Override
    public List<CoreEffect.ISubPart> process(Location where, WeightedRandomMap<CoreEffectBrush> brush, double scale, EffectObservation viewing) {
        return Collections.singletonList(new RepeatPart(where, brush, scale, viewing));
    }

    private class RepeatPart implements CoreEffect.ISubPart {
        // cache of where we've pulled a snapshot
        private final Location where;
        private final WeightedRandomMap<CoreEffectBrush> brush;
        private final double scale;
        private final EffectObservation viewing;
        // a full cycle that we want to repeat
        private ActiveEffect working;
        // how many cycles were completed
        private int cycle = 0;

        RepeatPart(Location where, WeightedRandomMap<CoreEffectBrush> brush, double scale, EffectObservation viewing) {
            this.where = where.clone();
            this.brush = brush.snapshot();
            this.scale = scale;
            this.viewing = viewing;
        }

        @Override
        public boolean process() {
            // work off the active queried parts
            if (this.working != null) {
                // work off the cycle we are in
                if (this.working.update()) {
                    // clear the effect we updated
                    this.working = null;
                }
                return false;
            }
            // check if we can create another cycle
            if (this.cycle >= cycles) {
                return true;
            }
            // query another cycle
            this.working = new ActiveEffect(where, scale * (1d*(expansion*cycle)), viewing, parts, brush.snapshot());
            this.cycle += 1;
            this.working.debug = true;
            return false;
        }
    }
}
