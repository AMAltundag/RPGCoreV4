package me.blutkrone.external.juliarn.npc.modifier;

import me.blutkrone.external.juliarn.npc.AbstractPlayerNPC;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

/**
 * A modifier for modifying the rotation of a player.
 */
public class RotationModifier extends AbstractNPCModifier {

    private final AbstractPlayerNPC npc;

    /**
     * A modifier for modifying the rotation of a player.
     *
     * @param npc The npc this modifier is for.
     */
    public RotationModifier(@NotNull AbstractPlayerNPC npc) {
        this.npc = npc;
    }

    /**
     * Rotate to face the exact pitch/yaw.
     *
     * @param pitch new rotation
     * @param yaw   new rotation
     * @return this instance, for chaining
     */
    public RotationModifier exact(float pitch, float yaw) {
        return (RotationModifier) super.queue(this.npc.packet().look(pitch, yaw));
    }

    /**
     * Rotate to look at the targets eyes.
     *
     * @param entity target to look at
     * @return this instance, for chaining
     */
    public RotationModifier target(LivingEntity entity) {
        double x = entity.getEyeLocation().getX() - (this.npc.location().getX());
        double y = entity.getEyeLocation().getY() - (this.npc.location().getY() + 1.66d);
        double z = entity.getEyeLocation().getZ() - (this.npc.location().getZ());
        double r = Math.sqrt(Math.pow(z, 2) + Math.pow(y, 2) + Math.pow(z, 2));

        float yaw = (float) (-Math.atan2(x, z) / Math.PI * 180D);
        yaw = yaw < 0 ? yaw + 360 : yaw;
        float pitch = (float) (-Math.asin(y / r) / Math.PI * 180D);

        return (RotationModifier) super.queue(this.npc.packet().look(pitch, yaw));
    }

    /**
     * Rotate to look at the given location.
     *
     * @param where target to look at
     * @return this instance, for chaining
     */
    public RotationModifier target(Location where) {
        double x = where.getX() - (this.npc.location().getX());
        double y = where.getY() - (this.npc.location().getY() + 1.66d);
        double z = where.getZ() - (this.npc.location().getZ());
        double r = Math.sqrt(Math.pow(z, 2) + Math.pow(y, 2) + Math.pow(z, 2));

        float yaw = (float) (-Math.atan2(x, z) / Math.PI * 180D);
        yaw = yaw < 0 ? yaw + 360 : yaw;
        float pitch = (float) (-Math.asin(y / r) / Math.PI * 180D);

        return (RotationModifier) super.queue(this.npc.packet().look(pitch, yaw));
    }
}
