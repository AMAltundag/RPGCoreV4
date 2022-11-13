package me.blutkrone.rpgcore.effect;

import me.blutkrone.rpgcore.effect.impl.CoreEffectBrush;
import me.blutkrone.rpgcore.effect.impl.CoreEffectWait;
import me.blutkrone.rpgcore.util.collection.WeightedRandomMap;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

public class ActiveEffect {
    public boolean debug;


    // scale of the effect
    private double scale;
    // players who observe the effect
    private EffectObservation viewers;
    // position related information
    private Location absolute;
    // stall further execution based on this
    private int delay = 0;
    // what parts need to be processed
    private List<CoreEffect.IEffectPart> parts = new ArrayList<>();
    // the current effect brush
    private WeightedRandomMap<CoreEffectBrush> brush;
    // sub-segments we've initialized
    private List<CoreEffect.ISubPart> children = new ArrayList<>();

    public ActiveEffect(Location where, double scale, EffectObservation viewers, List<CoreEffect.IEffectPart> parts) {
        this.absolute = where.clone();
        this.scale = scale;
        this.viewers = viewers;
        this.parts.addAll(parts);
        this.brush = new WeightedRandomMap<>();
    }

    public ActiveEffect(Location where, double scale, EffectObservation viewers, List<CoreEffect.IEffectPart> parts, WeightedRandomMap<CoreEffectBrush> brush) {
        this.absolute = where.clone();
        this.scale = scale;
        this.viewers = viewers;
        this.parts.addAll(parts);
        this.brush = brush;
    }

    /**
     * Update the effect, this returns true when the
     * effect is finished.
     *
     * @return whether we are finished.
     */
    public boolean update() {
        // update the child segments
        children.removeIf(CoreEffect.ISubPart::process);
        // stall execution while we got a delay
        if (delay > 0) {
            delay = delay - 1;
            return false;
        }
        // finish task if no parts left
        if (parts.isEmpty() && children.isEmpty()) {
            return true;
        }
        // poll parts until empty or delay
        while (!parts.isEmpty()) {
            CoreEffect.IEffectPart header = parts.remove(0);
            // delay will stall further execution
            if (header instanceof CoreEffectWait) {
                delay = ((CoreEffectWait) header).time;
                return false;
            }
            // invoke the component we got
            List<CoreEffect.ISubPart> parts = header.process(absolute, brush, scale, viewers);
            if (parts != null) {
                children.addAll(parts);
            }
        }
        // we still have something to do
        return false;
    }
}
