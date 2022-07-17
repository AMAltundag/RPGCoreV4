package me.blutkrone.rpgcore.hologram.impl;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.Pair;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import me.blutkrone.rpgcore.RPGCore;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * referenced https://github.com/unldenis/Hologram-Lib/blob/master/src/main/java/com/github/unldenis/hologram/packet/IPackets.java for
 * establishing a protocol.
 */
public class Hologram {

    // data watchers to annotate entity metadata
    private static WrappedDataWatcher.WrappedDataWatcherObject EFFECT_MASK = new WrappedDataWatcher.WrappedDataWatcherObject(0, WrappedDataWatcher.Registry.get(Byte.class));
    private static WrappedDataWatcher.WrappedDataWatcherObject NAME_VISIBLE = new WrappedDataWatcher.WrappedDataWatcherObject(3, WrappedDataWatcher.Registry.get(Boolean.class));
    private static WrappedDataWatcher.WrappedDataWatcherObject ENTITY_NAME = new WrappedDataWatcher.WrappedDataWatcherObject(2, WrappedDataWatcher.Registry.getChatComponentSerializer(true));
    private static WrappedDataWatcher.WrappedDataWatcherObject ARMOR_STAND = new WrappedDataWatcher.WrappedDataWatcherObject(15, WrappedDataWatcher.Registry.get(Byte.class));

    // distinctive ID just for this hologram
    private int id;

    public Hologram() {
        this.id = RPGCore.inst().getVolatileManager().getNextEntityId();
    }

    public void spawn(Player viewer, Location where) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.SPAWN_ENTITY_LIVING);
        packet.getIntegers().write(0, this.id);
        packet.getIntegers().write(1, 1); // entity type
        packet.getIntegers().write(2, 1); // extra data

        packet.getUUIDs().write(0, UUID.randomUUID());
        packet.getDoubles().write(0, where.getX());
        packet.getDoubles().write(1, where.getY());
        packet.getDoubles().write(2, where.getZ());

        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(viewer, packet);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public void spawn(Player viewer, double x, double y, double z) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.SPAWN_ENTITY_LIVING);
        packet.getIntegers().write(0, this.id);
        packet.getIntegers().write(1, 1); // entity type
        packet.getIntegers().write(2, 1); // extra data

        packet.getUUIDs().write(0, UUID.randomUUID());
        packet.getDoubles().write(0, x);
        packet.getDoubles().write(1, y);
        packet.getDoubles().write(2, z);

        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(viewer, packet);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public void destroy(Player viewer) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);
        packet.getIntLists().write(0, Collections.singletonList(this.id));

        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(viewer, packet);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public void equipment(Player viewer, EnumWrappers.ItemSlot slot, ItemStack item) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_EQUIPMENT);
        packet.getIntegers().write(0, this.id);
        List<Pair<EnumWrappers.ItemSlot, ItemStack>> pairs = new ArrayList<>();
        pairs.add(new Pair<>(slot, item));
        packet.getSlotStackPairLists().write(0, pairs);

        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(viewer, packet);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public void name(Player viewer, BaseComponent[] message) {
        Object nms_message = RPGCore.inst().getVolatileManager().adaptComponent(message);

        PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
        packet.getIntegers().write(0, this.id);
        WrappedDataWatcher watcher = new WrappedDataWatcher();

        watcher.setObject(EFFECT_MASK, (byte) 0x20); // make invisible
        watcher.setObject(NAME_VISIBLE, true); // always show name
        watcher.setObject(ENTITY_NAME, Optional.of(nms_message)); // updated name
        watcher.setObject(ARMOR_STAND, (byte) (0x01 | 0x04 | 0x08 | 0x10));

        packet.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects());

        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(viewer, packet);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public void teleport(Player viewer, Location where) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_TELEPORT);
        packet.getIntegers().write(0, this.id);
        packet.getDoubles().write(0, where.getX());
        packet.getDoubles().write(1, where.getY());
        packet.getDoubles().write(2, where.getZ());
        packet.getBytes().write(0, (byte) (where.getYaw() * 256F / 360F));
        packet.getBytes().write(1, (byte) (where.getPitch() * 256F / 360F));
        packet.getBooleans().write(0, false);

        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(viewer, packet);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public void rotate(Player viewer, float yaw) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_LOOK);
        packet.getIntegers().write(0, this.id);
        packet.getBytes()
                .write(0, (byte) (yaw * 256F / 360F))
                .write(1, (byte) 0);
        packet.getBooleans().write(0, true);

        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(viewer, packet);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public void mount(Player viewer, LivingEntity mount) {

        PacketContainer packet = new PacketContainer(PacketType.Play.Server.MOUNT);
        packet.getIntegers().write(0, mount.getEntityId());
        packet.getIntegerArrays().write(0, new int[]{this.id});

        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(viewer, packet);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public void mount(Player viewer, int mount) {

        PacketContainer packet = new PacketContainer(PacketType.Play.Server.MOUNT);
        packet.getIntegers().write(0, mount);
        packet.getIntegerArrays().write(0, new int[]{this.id});

        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(viewer, packet);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
