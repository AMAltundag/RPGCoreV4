package me.blutkrone.rpgcore.data;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A data bundle is intended to contain information from the same
 * context,
 * <br>
 * A single bundle of data which is meant to hold information
 * from the same context, for the sake of player data held in
 * a persistent manner.
 */
public class DataBundle {
    private List<String> handle = new ArrayList<>();

    public DataBundle(String... defaults) {
        this.handle.addAll(Arrays.asList(defaults));
    }

    public DataBundle() {
    }

    public void addNumber(Number value) {
        this.handle.add(String.valueOf(value));
    }

    public void addBoolean(Boolean value) {
        this.handle.add(String.valueOf(value));
    }

    public void addString(String value) {
        this.handle.add(String.valueOf(value));
    }

    public void addLocation(Location value) {
        this.handle.add(String.format("%s;%s;%s;%s;%s;%s",
                value.getWorld().getName(), value.getX(), value.getY(), value.getZ(), value.getPitch(), value.getYaw()));
    }

    public Number getNumber(int index) {
        return Double.parseDouble(this.handle.get(index));
    }

    public Boolean getBoolean(int index) {
        return Boolean.parseBoolean(this.handle.get(index));
    }

    public String getString(int index) {
        return this.handle.get(index);
    }

    public Location getLocation(int index) {
        try {
            String[] split = this.handle.get(index).split("\\;");
            World world = Bukkit.getWorld(split[0]);
            if (world == null) {
                return null;
            }

            double x = Double.parseDouble(split[1]);
            double y = Double.parseDouble(split[2]);
            double z = Double.parseDouble(split[3]);
            float pitch = Float.parseFloat(split[4]);
            float yaw = Float.parseFloat(split[5]);
            return new Location(world, x, y, z, yaw, pitch);
        } catch (Exception ex) {
            return null;
        }
    }

    public List<String> getHandle() {
        return this.handle;
    }

    public boolean isEmpty() {
        return this.handle.isEmpty();
    }

    public int size() {
        return this.handle.size();
    }

    @Override
    public String toString() {
        return handle.toString();
    }
}
