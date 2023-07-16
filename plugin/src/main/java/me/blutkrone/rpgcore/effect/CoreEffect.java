package me.blutkrone.rpgcore.effect;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.editor.root.other.EditorEffect;
import me.blutkrone.rpgcore.effect.impl.CoreEffectBrush;
import me.blutkrone.rpgcore.util.collection.WeightedRandomMap;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

public class CoreEffect {

    private String id;
    private List<IEffectPart> parts;

    public CoreEffect(String id, EditorEffect editor) {
        this.id = id;
        this.parts = new ArrayList<>();
        for (IEditorBundle effect : editor.effects) {
            if (effect instanceof EditorEffect.IEditorEffectBundle) {
                this.parts.add(((EditorEffect.IEditorEffectBundle) effect).build());
            }
        }
    }

    /**
     * Identifier of this effect.
     *
     * @return unique identifier of the effect.
     */
    public String getId() {
        return id;
    }

    /**
     * Show an effect to the given entities.
     *
     * @param where anchor within absolute space
     * @return linked effect task
     */
    public BukkitTask show(Location where) {
        ActiveEffect active = new ActiveEffect(where, 1d, new EffectObservation(where), this.parts);
        // query a task to handle the effect
        return new BukkitRunnable() {
            @Override
            public void run() {
                if (active.update()) {
                    cancel();
                }
            }
        }.runTaskTimerAsynchronously(RPGCore.inst(), 0L, 1L);
    }

    /**
     * Show an effect to the given entities.
     *
     * @param where anchor within absolute space
     * @param scale additional size scaling for effect
     */
    public void show(Location where, double scale) {
        ActiveEffect active = new ActiveEffect(where, scale, new EffectObservation(where), this.parts);
        // query a task to handle the effect
        new BukkitRunnable() {
            @Override
            public void run() {
                if (active.update()) {
                    cancel();
                }
            }
        }.runTaskTimerAsynchronously(RPGCore.inst(), 0L, 1L);
    }

    /**
     * Show an effect to the given entities.
     *
     * @param where anchor within absolute space
     */
    public void show(Location where, Player player) {
        ActiveEffect active = new ActiveEffect(where, 1d, new EffectObservation(player), this.parts);
        // query a task to handle the effect
        new BukkitRunnable() {
            @Override
            public void run() {
                if (active.update()) {
                    cancel();
                }
            }
        }.runTaskTimerAsynchronously(RPGCore.inst(), 0L, 1L);
    }

    /**
     * A part of an effect sequence.
     */
    public interface IEffectPart {

        /**
         * Process a part of an effect.
         *
         * @param where   anchor within absolute space
         * @param brush   a "brush" for particle effects
         * @param scale   effect scaling ratio
         * @param viewing who is viewing the effect
         */
        List<ISubPart> process(Location where, WeightedRandomMap<CoreEffectBrush> brush, double scale, EffectObservation viewing);
    }

    /**
     * A sub-part of an effect, this should be anchored
     * to the location of the owing part.
     */
    public interface ISubPart {
        /**
         * Process the part, if finished returns true
         *
         * @return whether we are finished.
         */
        boolean process();
    }
}
