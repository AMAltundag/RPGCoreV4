package me.blutkrone.rpgcore.effect.impl;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.editor.bundle.effect.EditorEffectModel;
import me.blutkrone.rpgcore.effect.CoreEffect;
import me.blutkrone.rpgcore.effect.EffectObservation;
import me.blutkrone.rpgcore.util.collection.WeightedRandomMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Collections;
import java.util.List;

public class CoreEffectModel implements CoreEffect.IEffectPart {

    private ItemStack model;
    private int duration;
    private double scatter;
    private double size;

    public CoreEffectModel(EditorEffectModel editor) {
        this.model = editor.model.build();
        this.duration = (int) editor.duration;
        this.scatter = editor.scatter;
        this.size = editor.size;
    }

    @Override
    public List<CoreEffect.ISubPart> process(Location where, WeightedRandomMap<CoreEffectBrush> brush, double scale, EffectObservation viewing) {
        Location anchor = where.clone();
        if (scale > 0.01d) {
            anchor.add(Math.random() * scale * scatter, Math.random() * scale * scatter, Math.random() * scale * scatter);
        }
        return Collections.singletonList(new Model(anchor, duration, size * scale));
    }

    class Model implements CoreEffect.ISubPart {
        Location where;

        ItemDisplay visual;
        int duration;
        float scale;

        Model(Location where, int duration, double scale) {
            this.where = where;
            this.visual = null;
            this.duration = duration;
            this.scale = (float) scale;
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
                    this.visual = (ItemDisplay) where.getWorld().spawnEntity(where, EntityType.ITEM_DISPLAY);
                    this.visual.setTransformation(new Transformation(new Vector3f(), new Quaternionf(), new Vector3f(scale), new Quaternionf()));
                    this.visual.setBillboard(Display.Billboard.FIXED);
                    this.visual.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.FIXED);
                    this.visual.setItemStack(model);
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
