package me.blutkrone.rpgcore.effect;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.hud.editor.index.EditorIndex;
import me.blutkrone.rpgcore.hud.editor.root.other.EditorEffect;
import me.blutkrone.rpgcore.nms.api.packet.handle.IBlockMutator;
import me.blutkrone.rpgcore.nms.api.packet.IVolatilePackets;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class EffectManager {
    private final Object sync_intensity = new Object();

    // index tracking effects
    private EditorIndex<CoreEffect, EditorEffect> effect_index;
    // maps players to their visible disguises
    private Map<UUID, Collection<ActiveBlockDisguise>> block_disguise;
    // particle intensity (approximate) of a player
    private Map<UUID, NavigableMap<Integer, Integer>> intensity = new HashMap<>();

    public EffectManager() {
        this.effect_index = new EditorIndex<>("effect", EditorEffect.class, EditorEffect::new);
        this.block_disguise = new ConcurrentHashMap<>();

        Bukkit.getScheduler().runTaskTimerAsynchronously(RPGCore.inst(), () -> {
            block_disguise.entrySet().removeIf(entry -> {
                Collection<ActiveBlockDisguise> disguises = entry.getValue();
                Player player = Bukkit.getPlayer(entry.getKey());
                if (player == null) {
                    return true;
                }
                disguises.removeIf(disguise -> disguise.delete);
                if (disguises.isEmpty()) {
                    return true;
                }

                // consume one tick of the relevant duration
                disguises.forEach(disguise -> disguise.duration -= 1);

                // identify disguises that overlap
                Map<Vector, List<ActiveBlockDisguise>> organized = new HashMap<>();
                for (ActiveBlockDisguise disguise : disguises) {
                    organized.computeIfAbsent(new Vector(disguise.x, disguise.y, disguise.z),
                            (k -> new ArrayList<>())).add(disguise);
                }

                // identify all disguises to update
                Map<Vector, Material> updates = new HashMap<>();
                organized.forEach((where, options) -> {
                    // identify the active disguise
                    ActiveBlockDisguise active = null;
                    for (ActiveBlockDisguise option : options) {
                        if (option.duration <= 0) {
                            option.delete = true;
                        } else {
                            active = option;
                        }
                    }
                    // process the active disguise
                    if (active == null) {
                        // strip the disguise away
                        updates.put(where, null);
                    } else {
                        // check if disguise needs to go out
                        if (!active.sent) {
                            updates.put(where, active.disguise);
                            active.sent = true;
                        }
                        // clear the flag off other disguises
                        for (ActiveBlockDisguise option : options) {
                            if (option != active) {
                                option.sent = false;
                            }
                        }
                    }
                });

                // organize into relevant chunk organization
                Map<Vector, Map<Vector, Material>> packet_prepared = new HashMap<>();
                updates.forEach((position, material) -> {
                    // unwrap position
                    int x = position.getBlockX();
                    int y = position.getBlockY();
                    int z = position.getBlockZ();
                    // identify the positions
                    Vector chunk_position = new Vector(x >> 4, y >> 4, z >> 4);
                    Vector local_position = new Vector(x, y, z);
                    // track the information we got
                    packet_prepared.computeIfAbsent(chunk_position, (k -> new HashMap<>())).put(local_position, material);
                });

                // construct appropriate packets
                Bukkit.getScheduler().runTask(RPGCore.inst(), () -> {
                    World world = player.getWorld();
                    IVolatilePackets packets = RPGCore.inst().getVolatileManager().getPackets();

                    packet_prepared.forEach((chunk, changes) -> {
                        IBlockMutator mutator = packets.blocks(world, chunk.getBlockX(), chunk.getBlockY(), chunk.getBlockZ());
                        changes.forEach((position, material) -> {
                            // query a block change packet
                            Material applied = mutator.mutate(position.getBlockX() % 16, position.getBlockY() % 16, position.getBlockZ() % 16, material);
                            // scatter some particles on change
                            for (int i = 0; i < 4; i++) {
                                Location location = position.toLocation(player.getWorld());
                                location.add(new Vector(0.5d, 0.5d, 0.5d));
                                location.add(new Vector(Math.random() * 2 - 1, Math.random() * 2 - 1, Math.random() * 2 - 1).multiply(1.5));
                                player.spawnParticle(Particle.BLOCK_CRACK, location, 1, applied.createBlockData());
                            }
                        });
                        mutator.dispatch(player);
                    });
                });

                // retain the effect manager
                return false;
            });
        }, 1, 1);
        Bukkit.getScheduler().runTaskTimer(RPGCore.inst(), () -> {
            intensity.entrySet().removeIf(entry -> Bukkit.getPlayer(entry.getKey()) == null);
        }, 0, 100);
    }

    /**
     * Intensity refers to how many particle effects the player
     * is currently dealing with, falls off over time.
     *
     * @param player    whose intensity to update
     * @param intensity the intensity of the player
     */
    public void addIntensity(Player player, int intensity) {
        synchronized (sync_intensity) {
            this.intensity.computeIfAbsent(player.getUniqueId(), (k) -> new TreeMap<>())
                    .merge(RPGCore.inst().getTimestamp(), intensity, (a, b) -> a + b);
        }
    }

    /**
     * Intensity refers to how many particle effects the player
     * is currently dealing with, falls off over time.
     *
     * @param player whose intensity to check
     * @return approximate particle density player has, approximately
     * one particle call per
     */
    public int getIntensity(Player player) {
        synchronized (sync_intensity) {
            NavigableMap<Integer, Integer> approximation = this.intensity.get(player.getUniqueId());
            if (approximation == null || approximation.isEmpty()) {
                return 0;
            }
            // only sample the past 6 seconds
            int timestamp = RPGCore.inst().getTimestamp();
            approximation.subMap(0, timestamp - 120).clear();
            // contribute density (older = less value)
            double total = 0;
            for (Map.Entry<Integer, Integer> sample : approximation.entrySet()) {
                double delta = timestamp - sample.getValue();
                double value = 1d - (delta / 120d);
                total += sample.getValue() * value;
            }
            // delete the total value we have
            return (int) total;
        }
    }

    /**
     * An index which holds every single managed effect on the server.
     *
     * @return the effect index.
     */
    public EditorIndex<CoreEffect, EditorEffect> getIndex() {
        return effect_index;
    }

    /**
     * Request a packet-based disguise of a block, respecting the shape of the said
     * block. Block disguise is not guaranteed to apply instantly.
     *
     * @param player   who wants the disguise
     * @param location where to put the disguise
     * @param disguise the material to disguise into
     * @param duration how long to wait
     * @param particle scatter particles on transform
     */
    public void disguise(Player player, Block location, Material disguise, int duration, boolean particle) {
        this.block_disguise.computeIfAbsent(player.getUniqueId(), (k -> new ConcurrentLinkedQueue<>()))
                .add(new ActiveBlockDisguise(location, disguise, duration, particle));
    }

    /**
     * Disguise that is active.
     */
    private class ActiveBlockDisguise {
        // where the disguise is and what it is
        private int x, y, z;
        private Material disguise;
        // ticks remaining on disguise
        private int duration;
        // whether user was given the disguise
        private boolean sent;
        // deletes at next pass
        private boolean delete;
        // whether to show particles on transform
        private boolean particle;

        ActiveBlockDisguise(Block location, Material disguise, int duration, boolean particle) {
            this.x = location.getX();
            this.y = location.getY();
            this.z = location.getZ();
            this.disguise = disguise;
            this.duration = duration;
            this.particle = particle;
        }
    }
}
