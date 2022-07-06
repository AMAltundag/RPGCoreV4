package me.blutkrone.rpgcore.util.collection;

import java.util.Map;
import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;

public class WeightedRandomMap<E> {
    private NavigableMap<Double, E> map = new TreeMap<>();
    private Random random;
    private double total = 0;

    public WeightedRandomMap() {
        this(new Random());
    }

    public WeightedRandomMap(Random random) {
        this.random = random;
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public void clear() {
        map = new TreeMap<>();
        total = 0d;
    }

    public WeightedRandomMap<E> copy() {
        WeightedRandomMap<E> copy = new WeightedRandomMap<>();
        copy.map.putAll(this.map);
        copy.total = this.total;
        return copy;
    }

    public void add(double weight, E object) {
        if (weight <= 0) return;
        total += weight;
        map.put(total, object);
    }

    public void add(E object, Double weight) {
        if (weight <= 0) return;
        total += weight;
        map.put(total, object);
    }

    public E next() {
        double value = (random.nextFloat() * total); // Can also use floating-point weights
        return map.ceilingEntry(value).getValue();
    }

    public E nextSafe() {
        try {
            double value = (random.nextFloat() * total); // Can also use floating-point weights
            return map.ceilingEntry(value).getValue();
        } catch (Exception ignored) {
            return null;
        }
    }

    public Map.Entry<Double, E> nextEntry() {
        double value = (random.nextFloat() * total); // Can also use floating-point weights
        return map.ceilingEntry(value);
    }

    public E next(Random random) {
        double value = (random.nextFloat() * total); // Can also use floating-point weights
        return map.ceilingEntry(value).getValue();
    }

    public E next(double random) {
        double value = (random * total); // Can also use floating-point weights
        return map.ceilingEntry(value).getValue();
    }

    public Map.Entry<Double, E> nextEntry(Random random) {
        double value = (random.nextFloat() * total); // Can also use floating-point weights
        return map.ceilingEntry(value);
    }
}
