package me.blutkrone.rpgcore.nms.v1_19_R1.entity;

import me.blutkrone.rpgcore.nms.api.entity.IEntityCollider;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.monster.EntitySlime;
import net.minecraft.world.level.World;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Slime;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

public class VolatileEntityCollider extends EntitySlime implements IEntityCollider {

    public VolatileEntityCollider(World world) {
        super(EntityTypes.aG, world);

        super.s(true); // no ai
        super.e(true); // no gravity
        super.d(true); // silent
        super.persistentInvisibility = true; // invisible
        super.b(5, true); // invisible
        super.a(2); // size
        super.persist = false; // persist
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
        Slime slime = (Slime) this.getBukkitEntity();
        if (slime != null) {
            PersistentDataContainer data = slime.getPersistentDataContainer();
            data.set(new NamespacedKey("rpgcore", "linked-entity"), PersistentDataType.STRING, entity.getUniqueId().toString());
        }
    }

    @Override
    public void move(Location location) {
        Slime slime = (Slime) this.getBukkitEntity();
        if (slime != null) {
            slime.teleport(location);
        }
    }

    @Override
    public void highlight(int time) {
        Slime slime = (Slime) this.getBukkitEntity();
        if (slime != null) {
            slime.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 30, 1, false, false, false));
        }
    }

    @Override
    public void resize(int size) {
        Slime slime = (Slime) this.getBukkitEntity();
        if (slime != null) {
            slime.setSize(size);
        }
    }

    @Override
    public void destroy() {
        Slime slime = (Slime) this.getBukkitEntity();
        if (slime != null) {
            slime.remove();
        }
    }

    @Override
    public boolean isActive() {
        Slime slime = (Slime) this.getBukkitEntity();
        return slime != null && slime.isValid();
    }
}
