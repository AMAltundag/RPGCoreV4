package me.blutkrone.rpgcore.entity.tasks;

import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import me.blutkrone.rpgcore.util.Utility;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * A task which is dedicated to translate attributes from the core
 * to bukkit specific attributes.
 */
public class CoreToBukkitAttributeTask extends BukkitRunnable {

    private final CoreEntity entity;

    private double last_attack_speed = -1.337;
    private double last_move_speed = -1.337;

    public CoreToBukkitAttributeTask(CoreEntity entity) {
        this.entity = entity;
    }

    @Override
    public void run() {
        // ensure that we got a valid bukkit entity
        LivingEntity bukkit_entity = this.entity.getEntity();
        if (bukkit_entity == null)
            return;

        // action speed serves as a multiplier to other speed modifiers
        double action_speed = Math.max(0d, 1d + this.entity.evaluateAttribute("ACTION_SPEED"));

        // adjust bukkit attack speed modifier
        double attack_speed = this.entity.evaluateAttribute("ATTACK_SPEED") * action_speed;
        if (Math.abs(attack_speed - last_attack_speed) > 0.001d) {
            AttributeInstance attribute = bukkit_entity.getAttribute(org.bukkit.attribute.Attribute.GENERIC_ATTACK_SPEED);
            if (attack_speed < 0d) {
                Utility.swapModifier(attribute, "rpgcore_attack_speed", 1 / (1d - attack_speed), AttributeModifier.Operation.MULTIPLY_SCALAR_1);
            } else if (attack_speed > 0d) {
                Utility.swapModifier(attribute, "rpgcore_attack_speed", attack_speed, AttributeModifier.Operation.MULTIPLY_SCALAR_1);
            } else {
                Utility.removeModifier(attribute, "rpgcore_attack_speed");
            }

            last_attack_speed = attack_speed;
        }

        // adjust bukkit movement speed modifier
        double move_speed = this.entity.evaluateAttribute("MOVE_SPEED") * action_speed;
        if (Math.abs(move_speed - last_move_speed) > 0.001d) {
            AttributeInstance attribute = bukkit_entity.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MOVEMENT_SPEED);
            if (move_speed < 0d) {
                Utility.swapModifier(attribute, "rpgcore_move_speed", 1 / (1d - move_speed), AttributeModifier.Operation.MULTIPLY_SCALAR_1);
            } else if (move_speed > 0d) {
                Utility.swapModifier(attribute, "rpgcore_move_speed", move_speed, AttributeModifier.Operation.MULTIPLY_SCALAR_1);
            } else {
                Utility.removeModifier(attribute, "rpgcore_move_speed");
            }

            last_move_speed = move_speed;
        }

        // fixate bukkit resources that are backend only
        if (bukkit_entity instanceof Player) {
            ((Player) bukkit_entity).setFoodLevel(20);
            ((Player) bukkit_entity).setHealth(20d);
        }
    }
}
