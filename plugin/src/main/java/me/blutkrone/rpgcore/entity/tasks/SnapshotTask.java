package me.blutkrone.rpgcore.entity.tasks;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.damage.interaction.DamageElement;
import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class SnapshotTask extends BukkitRunnable {

    private final CoreEntity entity;

    public SnapshotTask(CoreEntity entity) {
        this.entity = entity;
    }

    @Override
    public void run() {
        // snapshot basic attributes
        this.entity.getHealth().updateAttributeSnapshots();
        this.entity.getStamina().updateAttributeSnapshots();
        this.entity.getMana().updateAttributeSnapshots();
        // snapshot element attributes
        for (DamageElement element : RPGCore.inst().getDamageManager().getElements()) {
            // snapshot maximum reduction
            double capped = eval(element.getMaxReductionAttribute());
            this.entity.getAttribute(element.getMaxReductionAttribute() + "_SNAPSHOT")
                    .setOverride(capped);
            // snapshot resistances
            this.entity.getAttribute("TOTAL_RESISTANCE_" + element.getId() + "_SNAPSHOT")
                    .setOverride(eval(element.getResistanceAttribute()));
            this.entity.getAttribute("CAPPED_RESISTANCE_" + element.getId() + "_SNAPSHOT")
                    .setOverride(Math.min(capped, eval(element.getResistanceAttribute())));
            // snapshot multipliers
            this.entity.getAttribute("DAMAGE_MULTIPLIER_" + element.getId() + "_SNAPSHOT")
                    .setOverride(eval(element.getMultiplierAttribute()));
            // snapshot damage taken multiplier
            this.entity.getAttribute("DAMAGE_RECEIVED_" + element.getId() + "_SNAPSHOT")
                    .setOverride(eval(element.getReceivedAttribute()));

            this.entity.getAttribute(element.getId() + "_PENETRATION_SNAPSHOT")
                    .setOverride(eval(element.getPenetrationAttribute()));
            this.entity.getAttribute(element.getId() + "_EXTRA_DAMAGE_SNAPSHOT")
                    .setOverride(eval(element.getExtraAttribute()));
            this.entity.getAttribute(element.getId() + "_MINIMUM_SNAPSHOT")
                    .setOverride(eval(element.getMinimumRange()));
            this.entity.getAttribute(element.getId() + "_MAXIMUM_SNAPSHOT")
                    .setOverride(eval(element.getMaximumRange()));
        }
    }

    private double eval(List<String> attributes) {
        double summed = 0d;
        for (String attribute : attributes) {
            summed += eval(attribute);
        }
        return summed;
    }

    private double eval(String attribute) {
        return this.entity.evaluateAttribute(attribute);
    }
}
