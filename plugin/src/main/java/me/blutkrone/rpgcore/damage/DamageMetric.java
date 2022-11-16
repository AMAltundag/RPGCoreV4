package me.blutkrone.rpgcore.damage;

import java.util.*;

public class DamageMetric {

    private NavigableMap<Long, List<Double>> metrics = new TreeMap<>();
    private double[] cached = null;

    /**
     * Whether we have any samples on the damage metric.
     *
     * @return if we have any samples.
     */
    public boolean isEmpty() {
        return this.metrics.isEmpty();
    }

    /**
     * Provides a range marking the largest and lowest variance of
     * damage that was inflicted in the past 8 seconds.
     *
     * @return damage metric, as range
     */
    public double[] getAsRange() {
        // keep a cache of sampled damage
        if (this.cached == null) {
            double minimum = Double.MAX_VALUE;
            double maximum = 0;

            for (List<Double> samples : metrics.values()) {
                for (double sample : samples) {
                    minimum = Math.min(sample, minimum);
                    maximum = Math.max(sample, maximum);
                }
            }

            this.cached = new double[]{minimum, maximum};
        }
        // offer up the cached value
        return this.cached;
    }

    /**
     * Track an instance of damage.
     *
     * @param target who was inflicted with damage
     * @param damage how much damage was inflicted
     */
    public void track(UUID target, double damage) {
        // only track the past 8 seconds of damage
        this.metrics.subMap(0L, System.currentTimeMillis() - 8000L).clear();
        // stamp a data point into our metric
        this.metrics.computeIfAbsent(System.currentTimeMillis(), (k -> new ArrayList<>())).add(damage);
        // clear cache since we got new damage
        this.cached = null;
    }
}
