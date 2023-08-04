package me.blutkrone.rpgcore.util.world;

import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.Arrays;
import java.util.UUID;

public class BlockIdentifier {

    private final UUID world;
    private final int x, y, z;

    public BlockIdentifier(Location location) {
        this.world = location.getWorld().getUID();
        this.x = location.getBlockX();
        this.y = location.getBlockY();
        this.z = location.getBlockZ();
    }

    public BlockIdentifier(Block location) {
        this.world = location.getWorld().getUID();
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
    }

    public UUID getWorld() {
        return world;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof BlockIdentifier
                && ((BlockIdentifier) obj).world.equals(this.world)
                && ((BlockIdentifier) obj).x == this.x
                && ((BlockIdentifier) obj).y == this.y
                && ((BlockIdentifier) obj).z == this.z;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(new Object[]{this.world, this.x, this.y, this.z});
    }
}
