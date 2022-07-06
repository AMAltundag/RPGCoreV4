package me.blutkrone.rpgcore.nms.api.entity;

import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

/**
 * A light-weight entity that shouldn't stress the server.
 */
public interface IEntityVisual {

    /**
     * Teleport to the given coordinate.
     *
     * @param x target coordinate x
     * @param y target coordinate y
     * @param z target coordinate z
     */
    void move(double x, double y, double z);

    /**
     * Teleport to the given coordinate.
     *
     * @param x target coordinate x
     * @param y target coordinate y
     * @param z target coordinate z
     */
    void move(double x, double y, double z, float pitch, float yaw);

    /**
     * Retrieve the location of this visual entity.
     *
     * @return the location of our location.
     */
    Vector getLocation();

    /**
     * The entity which the visual entity mounted.
     *
     * @return who we are riding, or null.
     */
    Entity getRiding();

    /**
     * Mount the visual entity atop the given entity.
     *
     * @param entity who to mount, null to delete.
     */
    void setRiding(LivingEntity entity);

    /**
     * Update the name of the entity.
     *
     * @param name updated name, empty to hide.
     */
    void setName(String name);

    /**
     * Update the name of the entity.
     *
     * @param name updated name, empty to hide.
     */
    void setName(BaseComponent[] name);

    /**
     * Destroy this entity.
     */
    void remove();

    /**
     * Occupy an equipment slot with a certain item.
     *
     * @param slot the slot to populate
     * @param item the item to fill with
     */
    void setItem(EquipmentSlot slot, ItemStack item);

    /**
     * Occupy an equipment slot with a certain item.
     *
     * @param slot the slot to populate
     */
    ItemStack getItem(EquipmentSlot slot);

    /**
     * Check if the entity is still active on the
     * server.
     *
     * @return true if we are still valid and alive.
     */
    boolean isActive();

    /**
     * Fetch the bukkit entity backing us up.
     *
     * @return backing bukkit entity.
     */
    Entity asBukkit();
}
