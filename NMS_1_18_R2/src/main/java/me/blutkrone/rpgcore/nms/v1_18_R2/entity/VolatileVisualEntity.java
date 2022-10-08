package me.blutkrone.rpgcore.nms.v1_18_R2.entity;

import me.blutkrone.rpgcore.nms.api.entity.IEntityVisual;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.core.Vector3f;
import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.decoration.EntityArmorStand;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.level.World;
import net.minecraft.world.phys.Vec3D;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_18_R2.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_18_R2.util.CraftChatMessage;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class VolatileVisualEntity extends EntityArmorStand implements IEntityVisual {

    public VolatileVisualEntity(World world, boolean small) {
        super(EntityTypes.c, world);
        // disable generic collision
        super.collides = false;
        // set as invisible
        super.persistentInvisibility = true;
        super.b(5, true);
        // prevent from taking damage
        super.m(true);
        // shrink to smaller size
        super.t(true);
        super.a(small);
        // the arms shouldn't show up
        super.r(false);
        // make arms point straight down
        super.c(new Vector3f(0f, 0f, 0f));
        super.d(new Vector3f(0f, 0f, 0f));
        // prevent any form of velocity or physics
        super.e(true);
        // do show the base armor plate
        super.s(false);
        // prevent entity persistence
        super.persist = false;
    }

    @Override
    public void remove() {
        super.ah();
    }

    @Override
    public void setName(String name) {
        if (name != null && name.length() > 256) {
            name = name.substring(0, 256);
        }

        super.a(CraftChatMessage.fromStringOrNull(name));
        super.n(true);
    }

    @Override
    public void setName(BaseComponent[] name) {
        // update the entity name
        if (name.length == 0) {
            setName("");
        } else {
            String json = ComponentSerializer.toString(name);
            super.a(CraftChatMessage.fromJSON(json));
            super.n(true);
        }
    }

    @Override
    public void setItem(EquipmentSlot slot, ItemStack item) {
        if (slot == EquipmentSlot.HAND) {
            super.setItemSlot(EnumItemSlot.a, CraftItemStack.asNMSCopy(item), true);
        } else if (slot == EquipmentSlot.OFF_HAND) {
            super.setItemSlot(EnumItemSlot.b, CraftItemStack.asNMSCopy(item), true);
        } else if (slot == EquipmentSlot.FEET) {
            super.setItemSlot(EnumItemSlot.c, CraftItemStack.asNMSCopy(item), true);
        } else if (slot == EquipmentSlot.LEGS) {
            super.setItemSlot(EnumItemSlot.d, CraftItemStack.asNMSCopy(item), true);
        } else if (slot == EquipmentSlot.CHEST) {
            super.setItemSlot(EnumItemSlot.e, CraftItemStack.asNMSCopy(item), true);
        } else if (slot == EquipmentSlot.HEAD) {
            super.setItemSlot(EnumItemSlot.f, CraftItemStack.asNMSCopy(item), true);
        }
    }

    @Override
    public ItemStack getItem(EquipmentSlot slot) {
        if (slot == EquipmentSlot.HAND) {
            return CraftItemStack.asBukkitCopy(super.b(EnumItemSlot.a));
        } else if (slot == EquipmentSlot.OFF_HAND) {
            return CraftItemStack.asBukkitCopy(super.b(EnumItemSlot.b));
        } else if (slot == EquipmentSlot.FEET) {
            return CraftItemStack.asBukkitCopy(super.b(EnumItemSlot.c));
        } else if (slot == EquipmentSlot.LEGS) {
            return CraftItemStack.asBukkitCopy(super.b(EnumItemSlot.d));
        } else if (slot == EquipmentSlot.CHEST) {
            return CraftItemStack.asBukkitCopy(super.b(EnumItemSlot.e));
        } else if (slot == EquipmentSlot.HEAD) {
            return CraftItemStack.asBukkitCopy(super.b(EnumItemSlot.f));
        } else {
            return new ItemStack(Material.AIR);
        }
    }

    @Override
    public void move(double x, double y, double z) {
        super.p();
        super.a(x, y, z, 0f, 0f);
        super.l(0f);
    }

    @Override
    public void move(double x, double y, double z, float pitch, float yaw) {
        super.p();
        super.a(x, y, z, pitch, yaw);
        super.l(yaw);
    }

    @Override
    public Vector getLocation() {
        return new Vector(super.dc(), super.de(), super.di());
    }

    @Override
    public org.bukkit.entity.Entity getRiding() {
        Entity vehicle = super.cN();
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
    public boolean isActive() {
        return Bukkit.getEntity(super.cm()) != null;
    }

    @Override
    public org.bukkit.entity.Entity asBukkit() {
        return getBukkitEntity();
    }

    @Override
    public EnumInteractionResult a(EntityHuman entityhuman, Vec3D vec3d, EnumHand enumhand) {
        Bukkit.getLogger().severe("INTERACT FAIL");
        // never allow the stand being equipped
        return EnumInteractionResult.e;
    }
}