package me.blutkrone.rpgcore.effect;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.effect.impl.CoreParticleBrush;
import me.blutkrone.rpgcore.effect.impl.CoreWait;
import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.hud.editor.root.other.EditorEffect;
import me.blutkrone.rpgcore.util.collection.WeightedRandomMap;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

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
     * @param where   anchor within absolute space
     * @param scale   scale ratio of the effect
     * @param viewing who is viewing the effect
     * @return linked effect task
     */
    public BukkitTask show(Location where, double scale, List<Player> viewing) {
        // query a task to handle the effect
        return new BukkitRunnable() {
            // position related information
            Location absolute = where.clone();
            Vector relative = new Vector();
            // stall further execution based on this
            int delay = 0;
            // what parts need to be processed
            List<IEffectPart> parts = new ArrayList<>(CoreEffect.this.parts);
            // the current effect brush
            WeightedRandomMap<CoreParticleBrush> brush = new WeightedRandomMap<>();

            @Override
            public void run() {
                // stall execution while we got a delay
                if (delay > 0) {
                    delay = delay - 1;
                    return;
                }
                // finish task if no parts left
                if (parts.isEmpty()) {
                    cancel();
                    return;
                }
                // poll parts until empty or delay
                while (!parts.isEmpty()) {
                    IEffectPart header = parts.remove(0);
                    // delay will stall further execution
                    if (header instanceof CoreWait) {
                        delay = ((CoreWait) header).time;
                        return;
                    }
                    // invoke the component we got
                    header.process(absolute, relative, brush, scale, viewing);
                }
            }
        }.runTaskTimerAsynchronously(RPGCore.inst(), 0L, 1L);
    }

    /**
     * A sub-part of an effect.
     */
    public interface IEffectPart {

        /**
         * Process a part of an effect.
         *
         * @param where   anchor within absolute space
         * @param offset  local displacement of effect
         * @param scale   effect scaling ratio
         * @param viewing who is viewing the effect
         */
        void process(Location where, Vector offset, WeightedRandomMap<CoreParticleBrush> brush, double scale, List<Player> viewing);
    }
}
