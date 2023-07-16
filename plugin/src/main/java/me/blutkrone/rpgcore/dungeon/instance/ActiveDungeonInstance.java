package me.blutkrone.rpgcore.dungeon.instance;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.api.entity.IEntityEffect;
import me.blutkrone.rpgcore.attribute.AttributeModifier;
import me.blutkrone.rpgcore.dungeon.CoreDungeon;
import me.blutkrone.rpgcore.dungeon.IDungeonInstance;
import me.blutkrone.rpgcore.dungeon.structure.AbstractDungeonStructure;
import me.blutkrone.rpgcore.dungeon.structure.BlockStructure;
import me.blutkrone.rpgcore.dungeon.structure.SpawnpointStructure;
import me.blutkrone.rpgcore.dungeon.structure.TreasureStructure;
import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import me.blutkrone.rpgcore.entity.entities.CoreMob;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.menu.AbstractCoreMenu;
import me.blutkrone.rpgcore.util.io.FileUtil;
import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockVector;

import java.io.File;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class ActiveDungeonInstance implements IDungeonInstance {

    private final String world;
    private final CoreDungeon template;
    private final List<StructureTracker> structures;

    private Map<String, Double> score;
    private Map<UUID, Location> checkpoints;
    private List<String> invited;
    private Set<Long> chunk_processed;
    private int empty_ticks;
    private List<Location> spawnpoints;
    private Set<UUID> mob_kills;
    private Map<UUID, TreasureStructure.ActiveTreasure> treasures;

    public ActiveDungeonInstance(String id, CoreDungeon template) {
        RPGCore.inst().getLogger().info("not implemented (Dungeon entrance UX)");
        RPGCore.inst().getLogger().info("not implemented (Location selection)");

        // prepare a dungeon world
        this.world = id;
        String template_dir = "dungeon" + File.separator + template.getId();
        FileUtil.copyDirectory(FileUtil.directory(template_dir), new File(Bukkit.getWorldContainer() + File.separator + this.world));
        World world = new WorldCreator(this.world).seed(0).environment(World.Environment.NORMAL).type(WorldType.FLAT).generateStructures(false).createWorld();
        if (world == null) {
            throw new NullPointerException("Could not create world!");
        }
        world.setKeepSpawnInMemory(false);
        world.setAutoSave(false);
        world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        world.setGameRule(GameRule.DO_ENTITY_DROPS, false);
        world.setGameRule(GameRule.DO_FIRE_TICK, false);
        world.setGameRule(GameRule.DO_MOB_LOOT, false);
        world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        world.setGameRule(GameRule.DO_TILE_DROPS, false);
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        world.setGameRule(GameRule.KEEP_INVENTORY, true);
        world.setGameRule(GameRule.MOB_GRIEFING, false);
        world.setGameRule(GameRule.SHOW_DEATH_MESSAGES, false);
        world.setGameRule(GameRule.SPECTATORS_GENERATE_CHUNKS, false);
        world.setGameRule(GameRule.DISABLE_RAIDS, true);
        world.setGameRule(GameRule.DO_INSOMNIA, false);
        world.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, false);
        world.setGameRule(GameRule.DO_PATROL_SPAWNING, false);
        world.setGameRule(GameRule.DO_TRADER_SPAWNING, false);

        // template related information
        this.template = template;
        this.structures = new ArrayList<>();
        this.spawnpoints = new ArrayList<>();
        this.mob_kills = new HashSet<>();

        for (AbstractDungeonStructure<?> structure : template.getStructures().values()) {
            if (structure instanceof SpawnpointStructure) {
                for (BlockVector vector : structure.getWhere()) {
                    this.spawnpoints.add(vector.toLocation(getWorld()));
                }
            }

            this.structures.add(new StructureTracker(this, structure));
        }

        // runtime information
        this.score = new HashMap<>();
        this.checkpoints = new HashMap<>();
        this.invited = new ArrayList<>();
        this.chunk_processed = new HashSet<>();
        this.treasures = new HashMap<>();
    }

    /**
     * Treasures which have been activated for the
     * players.
     *
     * @return Activated dungeon treasures.
     */
    public Map<UUID, TreasureStructure.ActiveTreasure> getTreasures() {
        return treasures;
    }

    /**
     * Count mobs slain in the dungeon, this excludes secondary
     * spawns.
     *
     * @return UUID of slain mobs.
     */
    public Set<UUID> getMobKills() {
        return mob_kills;
    }

    /**
     * Check if the given chunk should be given a pass
     * for hiding blocks
     *
     * @param chunk Chunk to check
     * @return Hidden chunk
     */
    public boolean canHideChunk(Chunk chunk) {
        return this.chunk_processed.add((((long) chunk.getX()) << 32) | chunk.getZ());
    }

    /**
     * Structures activated within the dungeon.
     *
     * @return dungeon structure.
     */
    public List<StructureTracker> getStructures() {
        return structures;
    }

    /**
     * Score mapping for everyone who participated.
     *
     * @return Score for participating entities.
     */
    public Map<String, Double> getScore() {
        return score;
    }

    /**
     * Apply a checkpoint for the player, if they perish or
     * disconnect they will rejoin at the checkpoint.
     *
     * @param player     Whose checkpoint to update
     * @param checkpoint Updated checkpoint
     */
    public void setCheckpoint(Player player, Location checkpoint) {
        this.checkpoints.put(player.getUniqueId(), checkpoint);
    }

    @Override
    public CoreDungeon getTemplate() {
        return this.template;
    }

    @Override
    public Location getCheckpoint(Player player) {
        return this.checkpoints.computeIfAbsent(player.getUniqueId(), (uuid) -> {
            if (this.spawnpoints.isEmpty()) {
                player.sendMessage("§cDungeon has no spawnpoint setup, using 0/130/0 instead!");
                return new BlockVector(0, 130, 0).toLocation(getWorld());
            }

            return this.spawnpoints.get(ThreadLocalRandom.current().nextInt(this.spawnpoints.size()));
        });
    }

    @Override
    public World getWorld() {
        return Bukkit.getWorld(this.world);
    }

    @Override
    public boolean update() {
        // re-apply player effects
        if (RPGCore.inst().getTimestamp() % 100 == 0) {
            for (Player player : getPlayers(true)) {
                CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(player);
                if (core_player != null) {
                    if (core_player.getStatusEffects().containsKey("RPG_DUNGEON_EFFECT")) {
                        core_player.getStatusEffects().put("RPG_DUNGEON_EFFECT", new DungeonEffect(
                                core_player, getWorld(), template.getPlayerAttributes()));
                    }
                }
            }
        }

        // once per second re-count living mobs
        if (RPGCore.inst().getTimestamp() % 20 == 0) {
            this.score.keySet().removeIf(key -> key.endsWith("_alive"));
            Map<String, Integer> totals = new HashMap<>();
            int total = 0;
            for (LivingEntity entity : getWorld().getLivingEntities()) {
                CoreMob mob = RPGCore.inst().getEntityManager().getMob(entity);
                if (mob != null) {
                    totals.merge(mob.getTemplate().getId(), 1, (a, b) -> a + b);
                    total += 1;
                }
            }
            this.score.put("total_alive", 0d + total);
            totals.forEach((id, counted) -> {
                this.score.put(id + "_alive", 0d + counted);
            });
        }

        // asynchronous uniform structure activation evaluation
        if (RPGCore.inst().getTimestamp() % 10 == 0) {
            List<Location> players = new ArrayList<>();
            for (Player player : getPlayers(true)) {
                players.add(player.getLocation());
            }

            List<AbstractDungeonStructure.StructureData> snapshot = new ArrayList<>();
            for (StructureTracker structure : this.structures) {
                for (AbstractDungeonStructure.StructureData datum : structure.data) {
                    snapshot.add(datum);
                }
            }

            Bukkit.getScheduler().runTaskAsynchronously(RPGCore.inst(), () -> {
                List<AbstractDungeonStructure.StructureData> activate = new ArrayList<>();
                List<AbstractDungeonStructure.StructureData> deactivate = new ArrayList<>();

                for (AbstractDungeonStructure.StructureData data : snapshot) {
                    double distance = Double.MAX_VALUE;
                    for (Location player : players) {
                        distance = Math.min(distance, player.distance(data.where));
                    }

                    if (distance < data.structure.getRange()) {
                        activate.add(data);
                    } else {
                        deactivate.add(data);
                    }
                }

                Bukkit.getScheduler().runTask(RPGCore.inst(), () -> {
                    for (AbstractDungeonStructure.StructureData datum : activate) {
                        datum.activated = true;
                    }
                    for (AbstractDungeonStructure.StructureData datum : deactivate) {
                        datum.activated = false;
                    }
                });
            });
        }

        // pull in invited players
        this.invited.removeIf(name -> {
            Player player = Bukkit.getPlayer(name);
            if (player != null && AbstractCoreMenu.isUsingTrivialMenu(player)) {
                CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(player);
                if (core_player != null && core_player.isInitiated()) {
                    player.teleport(getCheckpoint(player));
                    return true;
                }
            }

            return false;
        });

        // update the structures
        structures.removeIf(structure -> {
            structure.structure.update(this, structure.data);
            return structure.data.isEmpty();
        });

        // abandon if empty for more then 10s
        if (getPlayers(false).isEmpty()) {
            if (++this.empty_ticks >= 200) {
                Bukkit.unloadWorld(this.getWorld(), false);
                return true;
            }
        } else {
            this.empty_ticks = 0;
        }

        return false;
    }

    @Override
    public void invite(List<String> players) {
        this.invited.addAll(players);
        this.empty_ticks = 0;
    }

    /**
     * A structure which actively tracks structures while
     * the dungeon instance is active.
     */
    public static class StructureTracker {
        public AbstractDungeonStructure structure;
        public List<AbstractDungeonStructure.StructureData> data;
        public Map<Long, List<Location>> hidden;

        StructureTracker(ActiveDungeonInstance instance, AbstractDungeonStructure<?> structure) {
            this.structure = structure;
            this.data = new ArrayList<>();
            for (BlockVector vector : structure.getWhere()) {
                Location location = vector.toLocation(instance.getWorld());
                this.data.add(new AbstractDungeonStructure.StructureData(structure, location));
            }

            this.hidden = new HashMap<>();
            if (this.structure.isHidden()) {
                // initial pass collects primary locations
                for (AbstractDungeonStructure.StructureData datum : this.data) {
                    long x = datum.where.getBlockX();
                    long z = datum.where.getBlockZ();
                    long chunk = ((x >> 4) << 32) | (z >> 4);
                    this.hidden.computeIfAbsent(chunk, (k -> new ArrayList<>()))
                            .add(datum.where);
                }
                // second pass to find proliferation of block structure
                if (this.structure instanceof BlockStructure) {
                    Map<BlockVector, List<BlockVector>> proliferated = ((BlockStructure) this.structure).getProliferated();
                    for (List<BlockVector> vectors : proliferated.values()) {
                        for (BlockVector vector : vectors) {
                            long x = vector.getBlockX();
                            long z = vector.getBlockZ();
                            long chunk = ((x >> 4) << 32) | (z >> 4);
                            this.hidden.computeIfAbsent(chunk, (k -> new ArrayList<>()))
                                    .add(vector.toLocation(instance.getWorld()));
                        }
                    }
                }
            }
        }
    }

    /**
     * A dungeon effect is a permanent effect which
     * lasts so long the dungeon is active.
     */
    public static class DungeonEffect implements IEntityEffect {

        private CoreEntity entity;
        private UUID world;
        private List<AttributeModifier> modifiers;

        DungeonEffect(CoreEntity entity, World world, Map<String, Double> effect) {
            this.world = world.getUID();
            this.modifiers = new ArrayList<>();
            this.entity = entity;
            effect.forEach((attribute, factor) -> {
                AttributeModifier modifier = entity.getAttribute(attribute).create(factor);
                this.modifiers.add(modifier);
            });
        }

        @Override
        public boolean tickEffect(int delta) {
            World world = entity.getWorld();
            if (world == null || !world.getUID().equals(this.world)) {
                for (AttributeModifier modifier : modifiers) {
                    modifier.setExpired();
                }
                return true;
            }

            return false;
        }

        @Override
        public int getStacks() {
            return 1;
        }

        @Override
        public int getDuration() {
            return 99999;
        }

        @Override
        public String getIcon() {
            return null;
        }

        @Override
        public long getLastUpdated() {
            return 0;
        }

        @Override
        public boolean isDebuff() {
            return false;
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public void manipulate(int stack, int duration, boolean override) {

        }
    }
}
