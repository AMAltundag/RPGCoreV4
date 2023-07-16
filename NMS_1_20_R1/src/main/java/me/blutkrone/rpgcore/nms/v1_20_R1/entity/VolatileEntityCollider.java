package me.blutkrone.rpgcore.nms.v1_20_R1.entity;

import me.blutkrone.rpgcore.nms.api.entity.IEntityCollider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.level.Level;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

public class VolatileEntityCollider extends Slime implements IEntityCollider {

    public VolatileEntityCollider(Level world) {
        super(EntityType.SLIME, world);

        super.persistentInvisibility = true;
        super.setSharedFlag(5, true);
        super.setNoAi(true);
        super.setNoGravity(true);
        super.setSize(2, true);
        super.persist = false;
    }

    /**
     * Retrieve an entity linked to a collider, this will return
     * nothing if there is no linked entity or if we are not dealing
     * with a collider.
     *
     * @param entity who are we inspecting
     * @return a possibly linked entity
     */
    public static UUID getLinkedEntity(Entity entity) {
        if (entity == null) {
            return null;
        }
        PersistentDataContainer data = entity.getPersistentDataContainer();
        String uuid = data.get(new NamespacedKey("rpgcore", "linked-entity"), PersistentDataType.STRING);
        if (uuid == null) {
            return null;
        }
        return UUID.fromString(uuid);
    }

    @Override
    public void link(Entity entity) {
        org.bukkit.entity.Slime slime = (org.bukkit.entity.Slime) this.getBukkitEntity();
        if (slime != null) {
            PersistentDataContainer data = slime.getPersistentDataContainer();
            data.set(new NamespacedKey("rpgcore", "linked-entity"), PersistentDataType.STRING, entity.getUniqueId().toString());
        }
    }

    @Override
    public void move(Location location) {
        org.bukkit.entity.Slime slime = (org.bukkit.entity.Slime) this.getBukkitEntity();
        if (slime != null) {
            slime.teleport(location);
        }
    }

    @Override
    public void highlight(int time) {
        org.bukkit.entity.Slime slime = (org.bukkit.entity.Slime) this.getBukkitEntity();
        if (slime != null) {
            slime.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 30, 1, false, false, false));
        }
    }

    @Override
    public void resize(int size) {
        org.bukkit.entity.Slime slime = (org.bukkit.entity.Slime) this.getBukkitEntity();
        if (slime != null) {
            slime.setSize(size);
        }
    }

    @Override
    public void destroy() {
        org.bukkit.entity.Slime slime = (org.bukkit.entity.Slime) this.getBukkitEntity();
        if (slime != null) {
            slime.remove();
        }
    }

    @Override
    public boolean isActive() {
        org.bukkit.entity.Slime slime = (org.bukkit.entity.Slime) this.getBukkitEntity();
        return slime != null && slime.isValid();
    }
}
