package me.blutkrone.rpgcore.nms.api.packet.handle;

import me.blutkrone.rpgcore.nms.api.packet.grouping.IBundledPacket;
import me.blutkrone.rpgcore.nms.api.packet.wrapper.VolatileStyle;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;

/**
 * Packet handling for holograms.
 */
public interface ITextDisplay {

    /**
     * Update the message shown by component
     *
     * @param message
     * @param locked
     * @param styles
     * @return Packet for bundling
     */
    IBundledPacket message(BaseComponent[] message, boolean locked, VolatileStyle... styles);

    /**
     * The entity ID we are tied with
     *
     * @return
     */
    int getEntityId();

    /**
     * Show to a player
     *
     * @param where
     * @return Packet for bundling
     */
    IBundledPacket spawn(Location where);

    /**
     * Show to a player
     *
     * @param x
     * @param y
     * @param z
     * @param pitch
     * @param yaw
     * @return Packet for bundling
     */
    IBundledPacket spawn(double x, double y, double z, float pitch, float yaw);

    /**
     * Hide from a player
     *
     * @return Packet for bundling
     */
    IBundledPacket destroy();

    /**
     * Teleport to a location
     *
     * @param where
     * @return Packet for bundling
     */
    IBundledPacket teleport(Location where);

    /**
     * Teleport to a location
     *
     * @param x
     * @param y
     * @param z
     * @return Packet for bundling
     */
    IBundledPacket teleport(double x, double y, double z);

    /**
     * Rotation on entity level
     *
     * @param yaw
     * @return Packet for bundling
     */
    IBundledPacket rotate(float yaw);

    /**
     * Make this display mounted to another entity
     *
     * @param mount
     * @return Packet for bundling
     */
    IBundledPacket mount(LivingEntity mount);

    /**
     * Make this display mounted to another entity
     *
     * @param mount
     * @return Packet for bundling
     */
    IBundledPacket mount(int mount);

    /**
     * Interpolation of translation, rotation and scale over the given duration
     * starting after a delay.
     *
     * @param delay
     * @param duration
     * @return Packet for bundling
     */
    IBundledPacket interpolation(int delay, int duration);

    /**
     * Displace the entity
     *
     * @param x
     * @param y
     * @param z
     * @return Packet for bundling
     */
    IBundledPacket translation(double x, double y, double z);

    /**
     * Apply a rotation before applying the translation
     *
     * @param rotation
     * @return Packet for bundling
     */
    IBundledPacket rotateBeforeTranslation(Quaternionf rotation);

    /**
     * Apply a rotation after applying the translation
     *
     * @param rotation
     * @return Packet for bundling
     */
    IBundledPacket rotateAfterTranslation(Quaternionf rotation);

    /**
     * Size multiplier of the display
     *
     * @param x
     * @param y
     * @param z
     * @return Packet for bundling
     */
    IBundledPacket scale(double x, double y, double z);

    /**
     * Deploy a transformation as one package.
     *
     * @param duration
     * @param transformation
     * @return Packet for bundling
     */
    IBundledPacket transform(int duration, Transformation transformation);

    /**
     * Displace entity by the given factor, note that this at most supports an
     * offset of 8 blocks, but it should look smooth. Use teleport for shorter
     * distance travel.
     *
     * @param x
     * @param y
     * @param z
     * @return Packet for bundling
     */
    IBundledPacket move(double x, double y, double z);
}
