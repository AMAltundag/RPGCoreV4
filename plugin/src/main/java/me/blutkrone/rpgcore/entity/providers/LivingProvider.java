package me.blutkrone.rpgcore.entity.providers;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.api.entity.EntityProvider;
import me.blutkrone.rpgcore.editor.bundle.entity.EditorVanillaCreatureProvider;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.BoundingBox;

/**
 * A provider capable of providing us with a simple
 * vanilla entity, without any mutations.
 */
public class LivingProvider implements EntityProvider {

    private EntityType type;
    private ItemStack helmet;
    private ItemStack chestplate;
    private ItemStack leggings;
    private ItemStack boots;
    private ItemStack main_hand;
    private ItemStack off_hand;
    private boolean hidden;

    public LivingProvider(EntityType type) {
        this.type = type;
    }

    public LivingProvider(EditorVanillaCreatureProvider editor) {
        this.type = editor.type;
        this.helmet = editor.helmet.isDefault() ? null : editor.helmet.build();
        this.chestplate = editor.chestplate.isDefault() ? null : editor.chestplate.build();
        this.leggings = editor.leggings.isDefault() ? null : editor.leggings.build();
        this.boots = editor.boots.isDefault() ? null : editor.boots.build();
        this.main_hand = editor.main_hand.isDefault() ? null : editor.main_hand.build();
        this.off_hand = editor.off_hand.isDefault() ? null : editor.off_hand.build();
        this.hidden = editor.hidden;
    }

    @Override
    public LivingEntity create(Location where, Object... args) {
        try {
            LivingEntity entity = RPGCore.inst().getVolatileManager().spawnEntity(this.type, where).getBukkitHandle();
            EntityEquipment equipment = entity.getEquipment();
            if (equipment != null) {
                if (this.helmet != null) {
                    equipment.setHelmet(this.helmet, true);
                    equipment.setHelmetDropChance(0f);
                }
                if (this.chestplate != null) {
                    equipment.setChestplate(this.chestplate, true);
                    equipment.setChestplateDropChance(0f);
                }
                if (this.leggings != null) {
                    equipment.setLeggings(this.leggings, true);
                    equipment.setLeggingsDropChance(0f);
                }
                if (this.boots != null) {
                    equipment.setBoots(this.boots, true);
                    equipment.setBootsDropChance(0f);
                }
                if (this.main_hand != null) {
                    equipment.setItemInMainHand(this.main_hand, true);
                    equipment.setItemInMainHandDropChance(0f);
                }
                if (this.off_hand != null) {
                    equipment.setItemInOffHand(this.off_hand, true);
                    equipment.setItemInOffHandDropChance(0f);
                }
            }
            entity.setSilent(true);
            entity.setPersistent(false);
            entity.setRemoveWhenFarAway(false);
            if (this.hidden) {
                entity.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 9999999, 1, false, false, false));
            }

            // force an injection of last known locations
            RPGCore.inst().getEntityManager().getLastLocation().put(entity.getUniqueId(), entity.getLocation());

            return entity;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public BoundingBox getBounds(LivingEntity entity) {
        return entity.getBoundingBox().clone();
    }

    @Override
    public Location getHeadLocation(LivingEntity entity) {
        return entity.getEyeLocation();
    }
}
