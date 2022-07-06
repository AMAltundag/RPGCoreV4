package me.blutkrone.rpgcore.util.world;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.*;

public class ChunkIdentifier {
    private final UUID world;
    private final int x;
    private final int z;

    /**
     * An explicitly defined chunk identifier
     *
     * @param world the world we are within
     * @param x     chunk coordinate
     * @param z     chunk coordinate
     */
    public ChunkIdentifier(World world, int x, int z) {
        this.world = world.getUID();
        this.x = x;
        this.z = z;
    }

    /**
     * An explicitly defined chunk identifier
     *
     * @param world the world we are within
     * @param x     chunk coordinate
     * @param z     chunk coordinate
     */
    public ChunkIdentifier(UUID world, int x, int z) {
        this.world = world;
        this.x = x;
        this.z = z;
    }

    /**
     * A chunk identifier from a location
     *
     * @param location the location we are in
     */
    public ChunkIdentifier(Location location) {
        Chunk chunk = location.getChunk();
        this.world = chunk.getWorld().getUID();
        this.x = chunk.getX();
        this.z = chunk.getZ();
    }

    /**
     * A chunk identifier from a chunk
     *
     * @param chunk the chunk we are in
     */
    public ChunkIdentifier(Chunk chunk) {
        this.world = chunk.getWorld().getUID();
        this.x = chunk.getX();
        this.z = chunk.getZ();
    }

    /**
     * Identifier of the world we are backing up.
     *
     * @return
     */
    public UUID getWorld() {
        return world;
    }

    /**
     * Recover this instance into a chunk reference
     *
     * @return the chunk backing us up
     */
    public Chunk asChunk() {
        World world = Bukkit.getWorld(this.world);
        if (world == null) return null;
        return world.getChunkAt(x, z);
    }

    /**
     * Fetch the entities active within the chunk
     *
     * @return a collection of active entities
     */
    public Collection<CoreEntity> getEntitiesInChunk() {
        Chunk chunk = asChunk();
        if (chunk == null) return Collections.emptyList();
        Queue<CoreEntity> entities = new LinkedList<>();
        for (Entity entity : chunk.getEntities()) {
            if (entity instanceof LivingEntity) {
                CoreEntity core_entity = RPGCore.inst().getEntityManager().getEntity(entity.getUniqueId());
                if (core_entity != null) entities.add(core_entity);
            }
        }
        return entities;
    }

    /**
     * A new chunk identifier instance, with an offset on the x/z axis
     *
     * @param x offset on the X axis
     * @param z offset on the Z axis
     * @return a chunk identifier relative to this one
     */
    public ChunkIdentifier withOffset(int x, int z) {
        return new ChunkIdentifier(this.world, this.x + x, this.z + z);
    }

    /**
     * Chunk coordinate, transform to location by left-shifting by 4 units
     *
     * @return chunk coordinate
     */
    public int getX() {
        return x;
    }

    /**
     * Chunk coordinate, transform to location by left-shifting by 4 units
     *
     * @return chunk coordinate
     */
    public int getZ() {
        return z;
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash = hash * 17 + x;
        hash = hash * 17 + z;
        hash = hash * 17 + world.hashCode();
        return hash;
    }

    @Override
    public String toString() {
        return String.format("chunk{x=%s,z=%s}", this.x, this.z);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ChunkIdentifier)) return false;
        ChunkIdentifier other = (ChunkIdentifier) obj;
        return other.x == this.x && other.z == this.z && other.world.equals(this.world);
    }
}
