package me.blutkrone.rpgcore.data;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * A data bundle is intended to contain information from the same
 * context,
 * <p>
 * A single bundle of data which is meant to hold information
 * from the same context, for the sake of player data held in
 * a persistent manner.
 */
public class DataBundle {
    private List<Object> handle = new ArrayList<>();

    public void addNumber(Number value) {
        handle.add(value);
    }

    public void addBoolean(Boolean value) {
        handle.add(value);
    }

    public void addString(String value) {
        handle.add(value);
    }

    public void addVector(Vector value) {
        handle.add(value);
    }

    public void addLocation(Location value) {
        handle.add(value);
    }

    public Number getNumber(int index) {
        return (Number) this.handle.get(index);
    }

    public Boolean getBoolean(int index) {
        return (Boolean) this.handle.get(index);
    }

    public String getString(int index) {
        return (String) this.handle.get(index);
    }

    public Vector getVector(int index) {
        return (Vector) this.handle.get(index);
    }

    public Location getLocation(int index) {
        return (Location) this.handle.get(index);
    }

    public List<Object> getHandle() {
        return handle;
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
