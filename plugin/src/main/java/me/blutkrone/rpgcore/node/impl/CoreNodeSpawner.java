package me.blutkrone.rpgcore.node.impl;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.editor.root.node.EditorNodeSpawner;
import me.blutkrone.rpgcore.entity.entities.CoreMob;
import me.blutkrone.rpgcore.mob.CoreCreature;
import me.blutkrone.rpgcore.nms.api.mob.IEntityBase;
import me.blutkrone.rpgcore.node.struct.AbstractNode;
import me.blutkrone.rpgcore.node.struct.NodeActive;
import me.blutkrone.rpgcore.node.struct.NodeData;
import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class CoreNodeSpawner extends AbstractNode {

    private int count;
    private int cooldown;
    private int level;
    private double leash;
    private List<String> mobs;

    public CoreNodeSpawner(String id, EditorNodeSpawner editor) {
        super(id, (int) editor.radius, editor.getPreview());
        this.count = (int) editor.count;
        this.cooldown = (int) editor.cooldown;
        this.level = (int) editor.level;
        this.leash = ((int) editor.leash) * 16d;
        this.mobs = new ArrayList<>(editor.mobs);
    }

    public static void leash(UUID uuid, Location where, double leash) {
        CoreMob mob = RPGCore.inst().getEntityManager().getMob(uuid);
        // ignore leash while in death sequence
        if (mob == null) {
            return;
        }
        // do not leash while in death sequence
        IEntityBase base = mob.getBase();
        if (base == null || base.isInDeathSequence()) {
            return;
        }
        // check how long the leash should be
        LivingEntity handle = mob.getEntity();
        double dist = handle.getLocation().distance(where);
        double my_leash = leash;
        if (mob.getBase().getRageEntity() != null) {
            my_leash = my_leash * 1.5d;
        }
        // update force teleport tracking
        if (dist < my_leash) {
            return;
        }
        // reset the creature since the leash kicked in
        mob.getHealth().recoverBy(mob.getHealth().getSnapshotMaximum());
        mob.getBase().resetRage();
        // decide on a location to retreat to
        Location home = computeRandomSpawnpoint(handle.getWorld(), where, 8d, 6);
        if (home == null) {
            home = where;
        }
        // move back to valid spawnpoint
        if (dist >= 24d) {
            // too far away, just teleport back
            handle.teleport(home);
        } else {
            // walk request, if failed we can teleport
            if (!mob.getBase().walkTo(home, 1d)) {
                handle.teleport(home);
            }
        }
    }

    public static List<UUID> spawn(int count, List<String> mobs, Location where, int level, double leash) {
        List<UUID> output = new ArrayList<>();
        World world = where.getWorld();

        if (count <= 1) {
            // roll a random mob to spawn
            String mob_id = mobs.get(ThreadLocalRandom.current().nextInt(mobs.size()));
            CoreCreature creature = RPGCore.inst().getMobManager().getIndex().get(mob_id);
            // track the mobs for respawning
            where = where.clone().setDirection(new Vector(Math.random() * 2 - 1, 0d, Math.random() * 2 - 1));
            CoreMob spawned = creature.spawn(where, level);
            if (spawned != null) {
                if (leash > 0d) {
                    spawned.setStrollLeash(leash, where);
                }
                output.add(spawned.getUniqueId());
            }
        } else {
            // spawn a pack of mobs at the location
            for (int i = 0; i < count; i++) {
                // grab a random position to spawn from
                Location spawnpoint = computeRandomSpawnpoint(world, where, 6d, 4);
                if (spawnpoint != null) {
                    // roll a random mob to spawn
                    String mob_id = mobs.get(ThreadLocalRandom.current().nextInt(mobs.size()));
                    CoreCreature creature = RPGCore.inst().getMobManager().getIndex().get(mob_id);
                    // track the mobs for respawning
                    where = where.clone().setDirection(new Vector(Math.random() * 2 - 1, 0d, Math.random() * 2 - 1));
                    CoreMob spawned = creature.spawn(where, level);
                    if (spawned != null) {
                        if (leash > 0d) {
                            spawned.setStrollLeash(leash, where);
                        }
                        output.add(spawned.getUniqueId());
                    }
                }
            }
        }

        return output;
    }

    private static Location computeRandomSpawnpoint(World world, Location around, double spread, int attempts) {
        around = around.clone().add(0d, spread / 3d, 0d);

        for (int i = 0; i < attempts; i++) {
            // throw a ray-cast down at an angle to find a spawn position
            Vector v = new Vector(Math.random() * 2 - 1, Math.random() * -1d, Math.random() * 2 - 1).normalize();
            RayTraceResult hit = world.rayTraceBlocks(around, v, spread, FluidCollisionMode.NEVER, true);
            if (hit == null) {
                continue;
            }
            if (hit.getHitBlock() == null) {
                continue;
            }
            if (hit.getHitBlockFace() != BlockFace.UP) {
                continue;
            }
            // ensure there is space above the block
            Block block = hit.getHitBlock();
            if (!block.getRelative(0, 1, 0).isPassable()) {
                continue;
            }
            if (!block.getRelative(0, 2, 0).isPassable()) {
                continue;
            }
            if (!block.getRelative(0, 3, 0).isPassable()) {
                continue;
            }
            // we can spawn a mob at this location
            Location spawnpoint = block.getLocation();
            spawnpoint.add(0.5d, 1d, 0.5d);
            // offer the location
            return spawnpoint;
        }

        return null;
    }

    @Override
    public void tick(World world, NodeActive active, List<Player> players) {
        Location where = new Location(world, active.getX(), active.getY(), active.getZ());

        // skip if we cannot spawn anything
        if (this.mobs.isEmpty() || this.count <= 0) {
            return;
        }
        // initialize the data for the mob
        MobData data = (MobData) active.getData();
        if (data == null) {
            active.setData(data = new MobData());
        }
        // validate the data we are tracking
        data.validate();
        // handle leash logic of the creatures
        if (this.leash > 0d) {
            for (UUID uuid : data.mobs) {
                leash(uuid, where, this.leash);
            }
        }
        // while any mobs are spawned, retain cooldown
        if (!data.mobs.isEmpty()) {
            data.cooldown = RPGCore.inst().getTimestamp() + cooldown;
            return;
        }
        // wait until cooldown does expire
        if (data.cooldown > RPGCore.inst().getTimestamp()) {
            return;
        }
        // require players in range to spawn mobs
        if (players.isEmpty()) {
            return;
        }

        // spawn in the mobs
        List<UUID> spawned = spawn(count, mobs, where, level, leash);
        if (!spawned.isEmpty()) {
            data.mobs.addAll(spawned);
            data.cooldown = RPGCore.inst().getTimestamp() + cooldown;
        }
    }

    @Override
    public void right(World world, NodeActive active, Player player) {

    }

    @Override
    public void left(World world, NodeActive active, Player player) {

    }

    private class MobData extends NodeData {

        Set<UUID> mobs = new HashSet<>();
        int cooldown = 0;

        private void validate() {
            mobs.removeIf(uuid -> {
                CoreMob mob = RPGCore.inst().getEntityManager().getMob(uuid);
                return mob == null || mob.getBase() == null;
            });
        }

        @Override
        public void abandon() {
            for (UUID mob : mobs) {
                // delete the mob from the core
                Entity entity = Bukkit.getEntity(mob);
                if (entity != null) {
                    entity.remove();
                }
                // unregister from the core
                RPGCore.inst().getEntityManager().unregister(mob);
            }
            // clear the spawned creatures
            mobs.clear();
        }
    }
}
