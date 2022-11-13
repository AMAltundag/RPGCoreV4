package me.blutkrone.rpgcore.effect;

import me.blutkrone.rpgcore.RPGCore;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * A wrapper to aid with observation.
 */
public class EffectObservation {

    private Map<Integer, List<Player>> observing = new HashMap<>();

    public EffectObservation(Location anchor) {
        List<Player> observing = RPGCore.inst().getEntityManager().getObserving(anchor);
        for (Player player : observing) {
            int intensity = RPGCore.inst().getEffectManager().getIntensity(player);
            if (intensity <= 10d) {
                this.observing.computeIfAbsent(0, (k -> new ArrayList<>())).add(player);
            } else {
                this.observing.computeIfAbsent((int) Math.log10(intensity), (k -> new ArrayList<>())).add(player);
            }
        }
    }

    public EffectObservation(List<Player> observing) {
        for (Player player : observing) {
            int intensity = RPGCore.inst().getEffectManager().getIntensity(player);
            if (intensity <= 10d) {
                this.observing.computeIfAbsent(0, (k -> new ArrayList<>())).add(player);
            } else {
                this.observing.computeIfAbsent((int) Math.log10(intensity), (k -> new ArrayList<>())).add(player);
            }
        }
    }

    public EffectObservation(Player observing) {
        int intensity = RPGCore.inst().getEffectManager().getIntensity(observing);
        if (intensity <= 10d) {
            this.observing.computeIfAbsent(0, (k -> new ArrayList<>())).add(observing);
        } else {
            this.observing.computeIfAbsent((int) Math.log10(intensity), (k -> new ArrayList<>())).add(observing);
        }
    }

    /**
     * Work off the observation.
     *
     * @param consumer players mapped to a separate "sample" multiplier
     */
    public void forEach(BiConsumer<List<Player>, Double> consumer) {
        observing.forEach((factor, players) -> {
            consumer.accept(players, Math.pow(0.9d, factor));
        });
    }

    /**
     * Work off the observation.
     *
     * @param consumer players without "sample" multiplier
     */
    public void forEach(Consumer<Player> consumer) {
        for (List<Player> players : observing.values()) {
            players.forEach(consumer);
        }
    }

    /**
     * Flattens into a list.
     *
     * @return observers
     */
    public List<Player> asList() {
        List<Player> viewers = new ArrayList<>();
        for (List<Player> players : observing.values()) {
            viewers.addAll(players);
        }
        return viewers;
    }
}