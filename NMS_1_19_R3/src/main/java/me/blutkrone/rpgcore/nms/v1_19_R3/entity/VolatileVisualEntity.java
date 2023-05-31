package me.blutkrone.rpgcore.nms.v1_19_R3.entity;

import me.blutkrone.rpgcore.nms.api.entity.IEntityVisual;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.core.Rotations;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.Level;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_19_R3.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_19_R3.util.CraftChatMessage;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class VolatileVisualEntity extends ArmorStand implements IEntityVisual {

    public VolatileVisualEntity(Level world, boolean small) {
        super(EntityType.ARMOR_STAND, world);

        // disable generic collision
        super.collides = false;
        // set as invisible
        super.persistentInvisibility = true;
        super.setSharedFlag(5, true);
        // prevent from taking damage
        super.setInvulnerable(true);
        // shrink to smaller size
        super.setMarker(true);
        super.setSmall(small);
        // the arms shouldn't show up
        super.setShowArms(false);
        // make arms point straight down
        super.setLeftArmPose(new Rotations(0f, 0f, 0f));
        super.setRightArmPose(new Rotations(0f, 0f, 0f));
        // prevent any form of velocity or physics
        super.setNoGravity(true);
        // do show the base armor plate
        super.setNoBasePlate(false);
        // prevent entity persistence
        super.persist = false;
    }

    @Override
    public void move(double x, double y, double z) {

    }

    @Override
    public void move(double x, double y, double z, float pitch, float yaw) {

    }

    @Override
    public Vector getLocation() {
        return new Vector(super.getX(), super.getY(), super.getZ());
    }

    @Override
    public Entity getRiding() {
        net.minecraft.world.entity.Entity vehicle = super.getVehicle();
        if (vehicle == null) {
            return null;
        }
        return vehicle.getBukkitEntity();
    }

    @Override
    public void setRiding(LivingEntity entity) {
        entity.addPassenger(this.getBukkitEntity());
    }

    @Override
    public void setName(String name) {
        if (name != null && name.length() > 256) {
            name = name.substring(0, 256);
        }

        super.setCustomName(CraftChatMessage.fromStringOrNull(name));
        super.setCustomNameVisible(true);
    }

    @Override
    public void setName(BaseComponent[] name) {
        // update the entity name
        if (name.length == 0) {
            setName("");
        } else {
            String json = ComponentSerializer.toString(name);
            super.setCustomName(CraftChatMessage.fromJSON(json));
            super.setCustomNameVisible(true);
        }
    }

    @Override
    public void remove() {
        super.discard();
    }

    @Override
    public void setItem(EquipmentSlot slot, ItemStack item) {
        if (slot == EquipmentSlot.HAND) {
            super.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, CraftItemStack.asNMSCopy(item), true);
        } else if (slot == EquipmentSlot.OFF_HAND) {
            super.setItemSlot(net.minecraft.world.entity.EquipmentSlot.OFFHAND, CraftItemStack.asNMSCopy(item), true);
        } else if (slot == EquipmentSlot.FEET) {
            super.setItemSlot(net.minecraft.world.entity.EquipmentSlot.FEET, CraftItemStack.asNMSCopy(item), true);
        } else if (slot == EquipmentSlot.LEGS) {
            super.setItemSlot(net.minecraft.world.entity.EquipmentSlot.LEGS, CraftItemStack.asNMSCopy(item), true);
        } else if (slot == EquipmentSlot.CHEST) {
            super.setItemSlot(net.minecraft.world.entity.EquipmentSlot.CHEST, CraftItemStack.asNMSCopy(item), true);
        } else if (slot == EquipmentSlot.HEAD) {
            super.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD, CraftItemStack.asNMSCopy(item), true);
        }
    }

    @Override
    public ItemStack getItem(EquipmentSlot slot) {
        if (slot == EquipmentSlot.HAND) {
            return CraftItemStack.asBukkitCopy(super.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND));
        } else if (slot == EquipmentSlot.OFF_HAND) {
            return CraftItemStack.asBukkitCopy(super.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.OFFHAND));
        } else if (slot == EquipmentSlot.FEET) {
            return CraftItemStack.asBukkitCopy(super.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.FEET));
        } else if (slot == EquipmentSlot.LEGS) {
            return CraftItemStack.asBukkitCopy(super.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.LEGS));
        } else if (slot == EquipmentSlot.CHEST) {
            return CraftItemStack.asBukkitCopy(super.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST));
        } else if (slot == EquipmentSlot.HEAD) {
            return CraftItemStack.asBukkitCopy(super.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD));
        } else {
            return new ItemStack(Material.AIR);
        }
    }

    @Override
    public boolean isActive() {
        return Bukkit.getEntity(super.getUUID()) != null;
    }

    @Override
    public Entity asBukkit() {
        return getBukkitEntity();
    }
}