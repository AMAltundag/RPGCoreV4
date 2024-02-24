package me.blutkrone.rpgcore.nms.api.packet.handle;

import me.blutkrone.rpgcore.nms.api.packet.grouping.IBundledPacket;
import me.blutkrone.rpgcore.nms.api.packet.wrapper.VolatileBillboard;
import me.blutkrone.rpgcore.nms.api.packet.wrapper.VolatileDisplay;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;

public interface IItemDisplay {

    IBundledPacket item(ItemStack item, VolatileBillboard billboard, VolatileDisplay display);

    int getEntityId();

    IBundledPacket spawn(Location where);

    IBundledPacket spawn(double x, double y, double z);

    IBundledPacket destroy();

    IBundledPacket teleport(Location where);

    IBundledPacket teleport(double x, double y, double z);

    IBundledPacket mount(LivingEntity mount);

    IBundledPacket mount(int mount);

    IBundledPacket move(double x, double y, double z);

    /**
     * Interpolation of translation, rotation and scale over the given duration
     * starting after a delay.
     *
     * @param delay
     * @param duration
     */
    IBundledPacket interpolation(int delay, int duration);

    /**
     * Displace the entity
     *
     * @param x
     * @param y
     * @param z
     */
    IBundledPacket translation(double x, double y, double z);

    /**
     * Apply a rotation before applying the translation
     *
     * @param rotation
     */
    IBundledPacket rotateBeforeTranslation(Quaternionf rotation);

    /**
     * Apply a rotation after applying the translation
     *
     * @param rotation
     */
    IBundledPacket rotateAfterTranslation(Quaternionf rotation);

    /**
     * Size multiplier of the display
     *
     * @param x
     * @param y
     * @param z
     */
    IBundledPacket scale(double x, double y, double z);

    /**
     * Deploy a transformation as one package.
     *
     * @param duration
     * @param transformation
     */
    IBundledPacket transform(int duration, Transformation transformation);
}
