package me.blutkrone.rpgcore.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.UUID;

public class EntityReference<K extends Entity> {

    private UUID uuid;
    private Reference<K> reference;

    public EntityReference(K entity) {
        this.uuid = entity.getUniqueId();
        this.reference = new WeakReference<>(entity);
    }

    public EntityReference() {
    }

    /**
     * Fetch the entity, do note that this may be null should
     * the entity have despawned.
     *
     * @return The entity referenced
     */
    public K get() {
        if (this.uuid == null || this.reference == null) {
            return null;
        }
        // retrieve entity, or refresh reference
        K entity = this.reference.get();
        if (entity == null) {
            entity = (K) Bukkit.getEntity(this.uuid);
            this.reference = new WeakReference<>(entity);
        }
        // ensure that entity is still valid
        if (entity != null && !entity.isValid()) {
            this.reference = null;
            this.uuid = null;
            entity = null;
        }
        // offer entity
        return entity;
    }
}
