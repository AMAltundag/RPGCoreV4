package me.blutkrone.rpgcore.dungeon.structure;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.dungeon.instance.ActiveDungeonInstance;
import me.blutkrone.rpgcore.dungeon.instance.EditorDungeonInstance;
import me.blutkrone.rpgcore.entity.entities.CoreMob;
import me.blutkrone.rpgcore.hud.editor.bundle.dungeon.EditorDungeonSpawner;
import me.blutkrone.rpgcore.hud.editor.bundle.selector.AbstractEditorSelector;
import me.blutkrone.rpgcore.node.impl.CoreNodeSpawner;
import me.blutkrone.rpgcore.skill.selector.AbstractCoreSelector;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Spawn mobs at the location, if count is greater 1 the mobs
 * are spawned with a bit of distance.
 */
public class SpawnerStructure extends AbstractDungeonStructure<SpawnerStructure.DungeonSpawn> {

    private final List<AbstractCoreSelector> activation;

    private List<String> mobs;
    private int level;
    private int leash;
    private int count;
    private int concurrent;
    private int despawn;
    private int cooldown;

    public SpawnerStructure(EditorDungeonSpawner editor) {
        super(editor);
        this.activation = AbstractEditorSelector.unwrap(editor.activation);
        this.mobs = new ArrayList<>(editor.mobs);
        this.level = (int) editor.level;
        this.leash = (int) editor.leash;
        this.count = (int) editor.count;
        this.cooldown = (int) editor.cooldown;
        this.concurrent = (int) editor.concurrent;
        this.despawn = (int) (editor.despawn * editor.despawn);
        this.despawn = (int) Math.max(this.despawn, super.getRange()*super.getRange());
    }

    public class DungeonSpawn {
        private List<UUID> active = new ArrayList<>();
        private int remaining;
        private int cooldown;

        public DungeonSpawn(int count) {
            this.remaining = count;
        }
    }

    @Override
    public void update(ActiveDungeonInstance instance, List<StructureData<DungeonSpawn>> where) {
        // spawn rules should update once per second
        if (RPGCore.inst().getTimestamp() % 20 != 0 || this.mobs.isEmpty()) {
            return;
        }


        where.removeIf(structure -> {
            if (structure.data != null) {
                // [0] keep the cooldown up-to-date
                structure.data.cooldown -= 20;
                if (!structure.data.active.isEmpty()) {
                    structure.data.cooldown = cooldown;
                }

                // [1] accredit kills, discard despawns
                structure.data.active.removeIf(uuid -> {
                    if (RPGCore.inst().getEntityManager().getMob(uuid) != null) {
                        return false;
                    }

                    if (instance.getMobKills().contains(uuid)) {
                        structure.data.remaining -= 1;
                    }

                    return true;
                });

                // [2] enforce leashing range
                if (this.leash > 0d) {
                    for (UUID uuid : structure.data.active) {
                        CoreNodeSpawner.leash(uuid, structure.where, this.leash);
                    }
                }

                // [3] re-populate against de-spawned mobs
                if (structure.data.active.isEmpty() && structure.data.remaining > 0 && structure.activated && structure.data.cooldown <= 0) {
                    if (!AbstractCoreSelector.doSelect(activation, structure.context, Collections.singletonList(new IOrigin.SnapshotOrigin(structure.where))).isEmpty()) {
                        structure.data.active.addAll(CoreNodeSpawner.spawn(Math.min(structure.data.remaining, this.concurrent), mobs, structure.where, level, leash));
                        for (UUID uuid : structure.data.active) {
                            CoreMob mob = RPGCore.inst().getEntityManager().getMob(uuid);
                            instance.getTemplate().getSpawnsAttributes().forEach((attribute, factor) -> {
                                mob.getAttribute(attribute).create(factor);
                            });
                        }
                    }
                }

                // [4] remove if structure is done
                return structure.data.remaining == 0;
            } else if (structure.activated) {
                // [5] initial spawning process
                if (!AbstractCoreSelector.doSelect(activation, structure.context, Collections.singletonList(new IOrigin.SnapshotOrigin(structure.where))).isEmpty()) {
                    structure.data = new DungeonSpawn(count);
                    structure.data.active.addAll(CoreNodeSpawner.spawn(Math.min(structure.data.remaining, this.concurrent), mobs, structure.where, level, leash));
                    for (UUID uuid : structure.data.active) {
                        CoreMob mob = RPGCore.inst().getEntityManager().getMob(uuid);
                        instance.getTemplate().getSpawnsAttributes().forEach((attribute, factor) -> {
                            mob.getAttribute(attribute).create(factor);
                        });
                    }
                }
            }

            return false;
        });

        // [6] despawn based on anchor location
        if (this.despawn > 0) {
            List<Location> players = new ArrayList<>();
            for (Player player : instance.getPlayers(true)) {
                players.add(player.getLocation());
            }
            Map<UUID, Location> spawned = new HashMap<>();
            for (StructureData<DungeonSpawn> structure : where) {
                if (structure.data != null) {
                    for (UUID uuid : structure.data.active) {
                        spawned.put(uuid, structure.where);
                    }
                }
            }

            Bukkit.getScheduler().runTaskAsynchronously(RPGCore.inst(), () -> {
                // only retain the mobs that would despawn
                spawned.entrySet().removeIf(entry -> {
                    for (Location player : players) {
                        if (player.distanceSquared(entry.getValue()) <= this.despawn) {
                            return true;
                        }
                    }
                    return false;
                });
                // despawn the mobs on the main thread
                Bukkit.getScheduler().runTask(RPGCore.inst(), () -> {
                    for (UUID uuid : spawned.keySet()) {
                        Entity entity = Bukkit.getEntity(uuid);
                        if (entity != null) {
                            entity.remove();
                        }
                        RPGCore.inst().getEntityManager().unregister(uuid);
                    }
                });
            });
        }
    }

    @Override
    public void update(EditorDungeonInstance instance, List<StructureData<DungeonSpawn>> where) {
        if (RPGCore.inst().getTimestamp() % 20 == 0) {
            for (StructureData<?> structure : where) {
                if (structure.highlight == null) {
                    int x = structure.where.getBlockX();
                    int y = structure.where.getBlockY();
                    int z = structure.where.getBlockZ();
                    structure.highlight = RPGCore.inst().getVolatileManager().getPackets().highlight(x, y, z);
                }

                List<Player> watching = RPGCore.inst().getEntityManager().getObserving(structure.where);
                watching.removeIf(player -> player.getLocation().distance(structure.where) > 32);
                for (Player player : watching) {
                    structure.highlight.enable(player);
                    Bukkit.getScheduler().runTaskLater(RPGCore.inst(), () -> {
                        structure.highlight.disable(player);
                    }, 10);
                }
            }
        }
    }
}
