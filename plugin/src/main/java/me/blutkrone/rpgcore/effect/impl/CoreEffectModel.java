package me.blutkrone.rpgcore.effect.impl;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.effect.CoreEffect;
import me.blutkrone.rpgcore.effect.EffectObservation;
import me.blutkrone.rpgcore.hud.editor.bundle.effect.EditorEffectModel;
import me.blutkrone.rpgcore.nms.api.entity.IEntityVisual;
import me.blutkrone.rpgcore.util.collection.WeightedRandomMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;

public class CoreEffectModel implements CoreEffect.IEffectPart {

    private ItemStack model;
    private int duration;
    private double scatter;

    public CoreEffectModel(EditorEffectModel editor) {
        this.model = editor.model.build();
        this.duration = (int) editor.duration;
        this.scatter = editor.scatter;
    }

    @Override
    public List<CoreEffect.ISubPart> process(Location where, WeightedRandomMap<CoreEffectBrush> brush, double scale, EffectObservation viewing) {
        Location anchor = where.clone();
        if (scale > 0.01d) {
            anchor.add(Math.random()*scale*scatter, Math.random()*scale*scatter, Math.random()*scale*scatter);
        }
        return Collections.singletonList(new Model(anchor, duration));
    }

    class Model implements CoreEffect.ISubPart {
        Location where;

        IEntityVisual visual;
        int duration;

        Model(Location where, int duration) {
            this.where = where;
            this.visual = null;
            this.duration = duration;
        }

        @Override
        public boolean process() {
            // handle initialization
            if (this.visual == null) {
                create();
                return false;
            }
            // wait until duration expires
            if (this.duration-- > 0) {
                return false;
            }
            // destruct and abandon task
            destruct();
            return true;
        }

        private void create() {
            Bukkit.getScheduler().runTask(RPGCore.inst(), () -> {
                if (this.visual == null) {
                    this.visual = RPGCore.inst().getVolatileManager().createVisualEntity(where, true);
                    this.visual.setItem(EquipmentSlot.HAND, model);
                }
            });
        }

        private void destruct() {
            Bukkit.getScheduler().runTask(RPGCore.inst(), () -> {
                if (this.visual != null) {
                    this.visual.remove();
                    this.visual = null;
                }
            });
        }
    }
}
