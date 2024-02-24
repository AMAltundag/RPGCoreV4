package me.blutkrone.rpgcore.nms.api.packet.handle;

import me.blutkrone.rpgcore.nms.api.packet.grouping.IDispatchPacket;
import me.blutkrone.rpgcore.nms.api.packet.wrapper.*;
import org.bukkit.Location;
import org.bukkit.entity.Pose;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

/**
 * Packet handling for player NPCs.
 */
public interface IPlayerNPC {

    int getEntityId();

    IDispatchPacket animation(VolatileAnimation animation);

    IDispatchPacket equipment(EquipmentSlot slot, ItemStack item);

    IDispatchPacket status(VolatileStatus... status);

    IDispatchPacket pose(Pose pose);

    IDispatchPacket skin(VolatileSkin... layers);

    IDispatchPacket look(float pitch, float yaw);

    IDispatchPacket teleport(Location where, boolean grounded);

    IDispatchPacket info(VolatileInfoAction action, VolatileGameProfile profile);

    IDispatchPacket spawn(Location where);

    IDispatchPacket destroy();
}
