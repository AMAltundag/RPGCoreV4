package me.blutkrone.rpgcore.nms.api.packet.handle;

import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;

/**
 * Packet handling for holograms.
 */
public interface ITextDisplay {

    /**
     * The entity ID we are tied with
     *
     * @return
     */
    int getEntityId();

    /**
     * Show to a player
     *
     * @param player
     * @param where
     */
    void spawn(Player player, Location where);

    /**
     * Show to a player
     *
     * @param player
     * @param x
     * @param y
     * @param z
     * @param pitch
     * @param yaw
     */
    void spawn(Player player, double x, double y, double z, float pitch, float yaw);

    /**
     * Hide from a player
     *
     * @param player
     */
    void destroy(Player player);

    /**
     * Update the message shown by component
     *
     * @param player
     * @param message
     * @param shadow
     * @param locked
     */
    void message(Player player, BaseComponent[] message, boolean shadow, boolean locked);

    /**
     * Teleport to a location
     *
     * @param player
     * @param where
     */
    void teleport(Player player, Location where);

    /**
     * Teleport to a location
     *
     * @param player
     * @param x
     * @param y
     * @param z
     */
    void teleport(Player player, double x, double y, double z);

    /**
     * Rotation on entity level
     *
     * @param player
     * @param yaw
     */
    void rotate(Player player, float yaw);

    /**
     * Make this display mounted to another entity
     * @param player
     * @param mount
     */
    void mount(Player player, LivingEntity mount);

    /**
     * Make this display mounted to another entity
     *
     * @param player
     * @param mount
     */
    void mount(Player player, int mount);

    /**
     * Interpolation of translation, rotation and scale over the given duration
     * starting after a delay.
     *
     * @param player
     * @param delay
     * @param duration
     */
    void interpolation(Player player, int delay, int duration);

    /**
     * Displace the entity
     *
     * @param player
     * @param x
     * @param y
     * @param z
     */
    void translation(Player player, double x, double y, double z);

    /**
     * Apply a rotation before applying the translation
     *
     * @param player
     * @param rotation
     */
    void rotateBeforeTranslation(Player player, Quaternionf rotation);

    /**
     * Apply a rotation after applying the translation
     *
     * @param player
     * @param rotation
     */
    void rotateAfterTranslation(Player player, Quaternionf rotation);

    /**
     * Size multiplier of the display
     *
     * @param player
     * @param x
     * @param y
     * @param z
     */
    void scale(Player player, double x, double y, double z);

    /**
     * Deploy a transformation as one package.
     *
     * @param player
     * @param duration
     * @param transformation
     */
    void transform(Player player, int duration, Transformation transformation);
}
