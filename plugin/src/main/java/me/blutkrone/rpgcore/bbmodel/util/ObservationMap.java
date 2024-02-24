package me.blutkrone.rpgcore.bbmodel.util;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.bbmodel.active.component.LocationSnapshot;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class ObservationMap {

    private Map<Player, Boolean> observing = new ConcurrentHashMap<>();

    /**
     * Invoked should we start observing.
     *
     * @param player Who changed their state.
     */
    public abstract void whenStart(Player player);

    /**
     * Invoked should we stop observing.
     *
     * @param player Who changed their state.
     */
    public abstract void whenFinish(Player player);

    /**
     * Invoked if we've already started to observe, and
     * keep observing.
     *
     * @param player Who retained their active state.
     */
    public void whenUpdate(Player player) {

    }

    /**
     * Retrieve everyone who is observing.
     *
     * @return Observation flags
     */
    public Map<Player, Boolean> getObserving() {
        return observing;
    }

    /**
     * Make everyone unobserve
     */
    public void terminate() {
        for (Map.Entry<Player, Boolean> entry : this.observing.entrySet()) {
            if (entry.getValue()) {
                whenFinish(entry.getKey());
            }
        }

        this.observing.clear();
    }

    /**
     * Update the observation mappings.
     *
     * @param snapshot
     * @param distance
     */
    public void update(LocationSnapshot snapshot, int distance) {
        distance = distance*distance;

        // update observation state where necessary
        for (Player player : Bukkit.getOnlinePlayers()) {
            Location player_location = RPGCore.inst().getEntityManager().getLastLocation(player.getUniqueId());
            if (player_location != null) {
                if (observing.getOrDefault(player, false) && snapshot.distSq(player_location) > distance) {
                    this.observing.put(player, false);
                    whenFinish(player);
                } else if (!observing.getOrDefault(player, false) && snapshot.distSq(player_location) < distance) {
                    this.observing.put(player, true);
                    whenStart(player);
                } else if (observing.getOrDefault(player, false)) {
                    whenUpdate(player);
                }
            }
        }
    }
}
