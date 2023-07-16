package me.blutkrone.rpgcore.hologram;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.hologram.impl.StationaryHologram;
import me.blutkrone.rpgcore.util.io.FileUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

/**
 * A manager responsible for creating holograms in the
 * world, either entity holograms (which are locked on
 * the target entity) or world holograms (locked on the
 * given location)
 */
public class HologramManager {

    private final Object thread_sync = new Object();

    private Scoreboard scoreboard;
    // tracker for holograms (grouped by world UUID)
    private Map<String, Map<UUID, StationaryHologram>> holograms = new HashMap<>();
    // tracker for player holograms [thread-unsafe]
    private Map<Player, Map<UUID, StationaryHologram>> tracked = new WeakHashMap<>();
    // thread locker
    private boolean working = false;
    // force a flush of tracked holograms
    private boolean reload = false;

    public HologramManager() {
        this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

        // load holograms from our disk
        File directory = FileUtil.directory("editor/hologram");
        if (directory.exists()) {
            File[] worlds = directory.listFiles();
            if (worlds != null) {
                for (File file : worlds) {
                    String world = file.getName();
                    File[] holograms = file.listFiles();
                    if (holograms != null) {
                        for (File holo_file : holograms) {
                            try {
                                Reader reader = Files.newBufferedReader(holo_file.toPath());
                                StationaryHologram node = RPGCore.inst().getGsonPretty().fromJson(reader, StationaryHologram.class);
                                reader.close();
                                synchronized (this.thread_sync) {
                                    this.holograms.computeIfAbsent(world, (k -> new HashMap<>())).put(node.getId(), node);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }

        // construct holograms on our disk
        Bukkit.getScheduler().runTaskTimer(RPGCore.inst(), () -> {
            if (this.working) {
                return;
            } else {
                this.working = true;
            }

            boolean want_reload = this.reload;

            // snapshot relevant player data
            Map<Player, Location> snapshot = new HashMap<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                snapshot.put(player, player.getLocation());
            }

            // work off the players
            Bukkit.getScheduler().runTaskAsynchronously(RPGCore.inst(), () -> {
                snapshot.forEach((player, where) -> {
                    Map<UUID, StationaryHologram> viewing = this.tracked.computeIfAbsent(player, (k -> new HashMap<>()));
                    Map<UUID, StationaryHologram> server_hologram;
                    synchronized (this.thread_sync) {
                        server_hologram = this.holograms.get(where.getWorld().getName());
                    }

                    if (server_hologram != null && !want_reload) {
                        // re-compute visible holograms
                        Map<UUID, StationaryHologram> viewport = new HashMap<>();
                        Vector v1 = player.getLocation().toVector();
                        server_hologram.forEach((uuid, hologram) -> {
                            if (v1.distanceSquared(hologram.getPosition()) <= 1024) {
                                viewport.put(uuid, hologram);
                            }
                        });
                        // get rid of all holograms no longer visible
                        viewing.entrySet().removeIf(entry -> {
                            // retain hologram if we still got it
                            if (viewport.containsKey(entry.getKey())) {
                                return false;
                            }
                            // delete the hologram from the player
                            entry.getValue().destroy(player);
                            // delete hologram from tracking
                            return true;
                        });
                        // add all newly acquired holograms
                        viewport.forEach((uuid, hologram) -> {
                            if (!viewing.containsKey(uuid)) {
                                // create hologram for the player
                                hologram.update(player);
                                // track hologram for the player
                                viewing.put(uuid, hologram);
                            }
                        });
                    } else {
                        // destruct all holograms that are being viewed
                        viewing.forEach((id, holo) -> {
                            holo.destroy(player);
                        });
                        // mark as no longer viewing
                        viewing.clear();
                    }
                });

                Bukkit.getScheduler().runTask(RPGCore.inst(), () -> {
                    this.working = false;
                    this.reload = false;
                });
            });
        }, 1, 10);
    }

    /**
     * Reload hologram related logic.
     */
    public void reload() {
        this.reload = true;
    }

    /**
     * Create a stationary hologram at the given location.
     *
     * @param where   where to create the hologram.
     * @param content the content of the hologram.
     * @param locked  prevent auto rotation
     */
    public void createHologram(Location where, String content, boolean locked) {
        if (where.getWorld() == null) {
            throw new IllegalArgumentException("World cannot be null!");
        }
        // create and register the hologram
        StationaryHologram hologram = new StationaryHologram(where, content, locked);
        synchronized (this.thread_sync) {
            this.holograms.computeIfAbsent(where.getWorld().getName(), (k -> new HashMap<>())).put(hologram.getId(), hologram);
        }

        // serialize to disk for persistence
        File file = FileUtil.file("editor/hologram/" + where.getWorld().getName(), hologram.getId() + ".rpgcore");
        file.getParentFile().mkdirs();
        try (FileWriter fw = new FileWriter(file, Charset.forName("UTF-8"))) {
            RPGCore.inst().getGsonPretty().toJson(hologram, fw);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Delete the hologram the closest to the player.
     *
     * @param player whose holograms do we check
     */
    public boolean deleteHologram(Player player) {
        Map<UUID, StationaryHologram> holograms;
        synchronized (thread_sync) {
            holograms = this.holograms.get(player.getWorld().getName());
        }

        if (holograms != null && !holograms.isEmpty()) {
            // search for the closest hologram
            Vector v2 = player.getLocation().toVector();
            StationaryHologram closest_hologram = null;
            double closest_distance = 0d;
            for (StationaryHologram hologram : holograms.values()) {
                double dist_current = hologram.getPosition().distanceSquared(v2);
                // limit to holograms with 16 blocks
                if (dist_current <= 256) {
                    // pick only closest hologram
                    if (closest_hologram == null || dist_current < closest_distance) {
                        closest_hologram = hologram;
                        closest_distance = dist_current;
                    }
                }
            }

            // if we got it, destruct it.
            if (closest_hologram != null) {
                holograms.remove(closest_hologram.getId());
                File file = FileUtil.file("editor/hologram/" + player.getWorld().getName(), closest_hologram.getId() + ".rpgcore");
                file.getParentFile().mkdirs();
                file.delete();
            }
        }

        return false;
    }

    /**
     * The scoreboard to be used by the hologram management.
     *
     * @return the scoreboard we are using.
     */
    public Scoreboard getScoreboard() {
        if (this.scoreboard == null) {
            ScoreboardManager scoreboard_manager = Bukkit.getScoreboardManager();
            if (scoreboard_manager != null) {
                this.scoreboard = scoreboard_manager.getNewScoreboard();
            } else {
                throw new NullPointerException("Scoreboard Manager not initialized!");
            }
        }

        return this.scoreboard;
    }

    /**
     * Assign a glow color to the entity.
     *
     * @param uuid  what entity to update
     * @param color what color to use, RESET removes color.
     */
    public void setGlowColor(UUID uuid, ChatColor color) {
        Team team = getScoreboard().getTeam(uuid.toString());
        if (team == null) {
            team = getScoreboard().registerNewTeam(uuid.toString());
            team.addEntry(uuid.toString());
        }
        team.setColor(color);
    }

    /**
     * Show or hide basic name tag of an entity.
     *
     * @param uuid what entity to update
     * @param flag hide (true) or show (false)
     */
    public void setHideName(UUID uuid, boolean flag) {
        Team team = getScoreboard().getTeam(uuid.toString());
        if (team == null) {
            team = getScoreboard().registerNewTeam(uuid.toString());
            team.addEntry(uuid.toString());
        }
        team.setOption(Team.Option.NAME_TAG_VISIBILITY, flag
                ? Team.OptionStatus.NEVER : Team.OptionStatus.ALWAYS);
    }
}
