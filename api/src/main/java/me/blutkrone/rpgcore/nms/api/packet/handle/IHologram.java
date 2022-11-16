package me.blutkrone.rpgcore.nms.api.packet.handle;

import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

/**
 * Packet handling for holograms.
 */
public interface IHologram {

    int getEntityId();

    void spawn(Player player, Location where);

    void spawn(Player player, double x, double y, double z);

    void destroy(Player player);

    void equip(Player player, EquipmentSlot slot, ItemStack item);

    void name(Player player, BaseComponent[] name);

    void teleport(Player player, Location where);

    void teleport(Player player, double x, double y, double z);

    void rotate(Player player, float yaw);

    void mount(Player player, LivingEntity mount);

    void mount(Player player, int mount);
}
