package me.blutkrone.rpgcore.minimap.v2;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.api.social.IPartySnapshot;
import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.minimap.MapMarker;
import me.blutkrone.rpgcore.nms.api.block.ChunkOutline;
import me.blutkrone.rpgcore.node.struct.NodeActive;
import me.blutkrone.rpgcore.node.struct.NodeWorld;
import me.blutkrone.rpgcore.npc.CoreNPC;
import me.blutkrone.rpgcore.npc.trait.AbstractCoreTrait;
import me.blutkrone.rpgcore.npc.trait.impl.CoreQuestTrait;
import me.blutkrone.rpgcore.quest.CoreQuest;
import me.blutkrone.rpgcore.quest.task.AbstractQuestTask;
import me.blutkrone.rpgcore.util.io.ConfigWrapper;
import me.blutkrone.rpgcore.util.io.FileUtil;
import me.blutkrone.rpgcore.util.world.BlockIdentifier;
import me.blutkrone.rpgcore.util.world.ChunkIdentifier;
import org.bukkit.*;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.*;

/**
 * A handler for minimaps, we can access a boolean[][] array that
 * will outline the surroundings of a player relative to where they
 * are currently located at.
 * <br>
 * This minimap will work anywhere, iE it can also be more smoothly
 * merged with markers etc and is completely unrelated to pre-drawn
 * world maps.
 */
public class MinimapManagerV2 {

    public static final int CHUNKS_PER_TICK = 4;
    public static final int MAXIMUM_CHUNK_CACHE = 1024;
    public static final int MAXIMUM_MAP_CACHE = 128;
    public static final int MINIMAP_SIZE_BLOCK = 36;
    public static final int MINIMAP_SIZE_CHUNK = 3;

    // synchronization object for thread safety
    private final Object SYNC = new Object();
    // queue of chunks that have to be mapped
    private final Set<ChunkIdentifier> outline_queued = new LinkedHashSet<>();
    // map of outlines, null values while processing
    private final Map<ChunkIdentifier, ChunkOutline> outline_finished = new LinkedHashMap<>();
    // map of block to the minimap grid around it
    private final Map<BlockIdentifier, boolean[][]> minimap_cache = new LinkedHashMap<>();

    // MapMarker indicates a certain location on a map
    private Map<ChunkIdentifier, List<MapMarker>> marker_cache = new HashMap<>();
    private List<MapMarker> marker_mixed = new ArrayList<>();

    // MapInfo contains information to render a map
    private Map<String, MapInfo> id_to_map = new HashMap<>();

    // MapInfo anchors to be applied
    private Map<ChunkIdentifier, List<MapAnchor>> anchor_cache = new HashMap<>();
    private List<MapAnchor> anchor_mixed = new ArrayList<>();

    public MinimapManagerV2() {
        // load configuration
        try {
            ConfigWrapper config = FileUtil.asConfigYML(FileUtil.file("minimap.yml"));
            this.marker_mixed = config.getObjectList("markers", MapMarker::new);
            this.anchor_mixed = config.getObjectList("anchors", MapAnchor::new);
            this.id_to_map = config.getObjectMap("maps", MapInfo::new);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // warn about illegal anchors
        this.anchor_mixed.removeIf(anchor -> {
            for (String path : anchor.path) {
                if (!id_to_map.containsKey(path)) {
                    Bukkit.getLogger().severe(String.format("Removed minimap anchor because path '%s' does not exist!", path));
                    return true;
                }
            }

            return false;
        });

        // handle timer appropriately
        Bukkit.getScheduler().runTaskTimer(RPGCore.inst(), () -> {
            // request new chunks to be mapped
            this.requestNewChunks();
            // work off chunks that were mapped
            this.processQueuedChunks();
            // restrain maximum cache size
            this.restrainCacheSize();
        }, 1, 1);
    }

    /**
     * Retrieve the map info object the closest to the player, note that
     * this may be null.
     *
     * @param player Whose closest map do we want
     * @return Map information, should there be one
     */
    public MapAnchor getAnchorNearby(Player player) {
        // grab all the anchors within chunk range
        ChunkIdentifier identifier = new ChunkIdentifier(player.getLocation());
        List<MapAnchor> candidates = this.anchor_cache.computeIfAbsent(identifier, (k -> {
            List<MapAnchor> output = new ArrayList<>();
            for (MapAnchor anchor : this.anchor_mixed) {
                if (anchor.distance(player) <= 32d) {
                    output.add(anchor);
                }
            }
            return output;
        }));
        // find the closest anchor
        MapAnchor closest_anchor = null;
        double closest_distance = Double.MAX_VALUE;
        for (MapAnchor next_anchor : candidates) {
            double next_distance = next_anchor.distance(player);
            if (closest_anchor == null || next_distance < closest_distance) {
                closest_anchor = next_anchor;
                closest_distance = next_distance;
            }
        }
        // offer up what we found
        return closest_anchor;
    }

    /*
     * Scan an outline at the given location, should we be focusing a chunk that
     * wasn't scanned yet the return value will be false.
     *
     * @param world Where to check
     * @param x Where to check
     * @param y Where to check
     * @param z Where to check
     * @return Block at position is solid
     */
    private boolean getOutlineAt(World world, int x, int y, int z) {
        synchronized (SYNC) {
            // identify the outline we have
            ChunkIdentifier identifier = new ChunkIdentifier(world, x >> 4, z >> 4);
            ChunkOutline outline = outline_finished.remove(identifier);
            if (outline == null) {
                return false;
            }
            // update popularity tracker
            outline_finished.put(identifier, outline);
            // offer up data in the outline
            return outline.get(x & 0xF, y, z & 0xF);
        }
    }

    /**
     * Retrieve a map info object.
     *
     * @param info What maps info do we want
     * @return Information on the map, if it exists
     */
    public MapInfo getMapInfo(String info) {
        return this.id_to_map.get(info);
    }

    /**
     * Retrieve the markers visible to the given player.
     *
     * @return markers visible to player
     */
    public List<MapMarker> getMarkersOf(Player bukkit, CorePlayer core) {
        List<MapMarker> markers_for_player = new ArrayList<>();

        // markers that are fixed into the world
        List<MapMarker> candidates = marker_cache.computeIfAbsent(new ChunkIdentifier(bukkit.getLocation()), (k -> {
            List<MapMarker> filtered = new ArrayList<>();
            for (MapMarker marker : marker_mixed) {
                // ensure the marker shares this world
                if (marker.getLocation().getWorld() != bukkit.getWorld())
                    continue;
                // verify (rough) distance matching
                double distance = bukkit.getLocation().distance(marker.getLocation()) + 20d;
                if (distance > marker.distance)
                    continue;
                // this chunk can show the marker
                filtered.add(marker);
            }
            return filtered;
        }));
        for (MapMarker candidate : candidates) {
            // ensure the marker is within a passable distance
            double distSq = candidate.getLocation().distanceSquared(bukkit.getLocation());
            if (distSq >= candidate.distance * candidate.distance || distSq <= 9d)
                continue;
            // offer up the marker to the render queue
            markers_for_player.add(candidate);
        }

        // players that share our world
        IPartySnapshot party = RPGCore.inst().getSocialManager().getGroupHandler().getPartySnapshot(core);
        if (party != null) {
            for (OfflinePlayer player : party.getAllMembers()) {
                if (player instanceof Player && ((Player) player).getWorld() == bukkit.getWorld()) {
                    if (((Player) player).getWorld() == bukkit.getWorld()) {
                        markers_for_player.add(new MapMarker(((Player) player).getLocation(), "party_player"));
                    }
                }
            }
        }

        // highlights for active quests
        List<String> quests = core.getActiveQuestIds();
        for (int i = 0; i < quests.size() && i < 3; i++) {
            CoreQuest quest = RPGCore.inst().getQuestManager().getIndexQuest().get(quests.get(i));
            AbstractQuestTask task = quest.getCurrentTask(core);
            if (task != null) {
                List<Location> hints = task.getHints(core, bukkit);
                if (hints != null) {
                    hints = hints.subList(0, Math.min(hints.size(), 10));
                    for (Location hint : hints) {
                        markers_for_player.add(new MapMarker(hint, "quest_active_" + (i+1)));
                    }
                }
            } else if (quest.getRewardNPC() != null) {
                NodeWorld node_world = RPGCore.inst().getNodeManager().getNodeWorld(bukkit.getWorld());
                if (node_world != null) {
                    List<NodeActive> nodes = node_world.getNodesOfType("npc:" + quest.getRewardNPC());
                    for (NodeActive node : nodes) {
                        Location location = new Location(bukkit.getWorld(), node.getX(), node.getY(), node.getZ());
                        markers_for_player.add(new MapMarker(location, "quest_reward_" + (i+1)));
                    }
                }
            }
        }

        // highlights for NPCs that can give us quests
        Location player_location = bukkit.getLocation();
        NodeWorld node_world = RPGCore.inst().getNodeManager().getNodeWorld(bukkit.getWorld());
        for (NodeActive nearby : node_world.getNodesNear(player_location.getBlockX(), player_location.getBlockY(), player_location.getBlockZ(), 60)) {
            if (nearby.getNode() instanceof CoreNPC) {
                List<AbstractCoreTrait> traits = ((CoreNPC) nearby.getNode()).getAvailableTraits(bukkit);
                for (AbstractCoreTrait trait : traits) {
                    if (trait instanceof CoreQuestTrait) {
                        if (!((CoreQuestTrait) trait).getQuestAvailable(core).isEmpty()) {
                            Location location = new Location(bukkit.getWorld(), nearby.getX(), nearby.getY(), nearby.getZ());
                            markers_for_player.add(new MapMarker(location, "quest_available"));
                        }
                    }
                }
            }
        }

        // remember mobs we have dealt damage against in the last 10 seconds
        for (CoreEntity tracked : core.getEntitiesOnMinimap()) {
            Location where = tracked.getLocation();
            if (where != null && where.getWorld() == bukkit.getWorld()) {
                markers_for_player.add(new MapMarker(where, "tracked"));
            }
        }

        // sort the markers by their distance towards the player
        markers_for_player.sort(Comparator.comparingDouble((o -> o.getLocation().distanceSquared(bukkit.getLocation()))));
        Collections.reverse(markers_for_player);
        return markers_for_player;
    }

    /**
     * This method should be called asynchronously, our intent is to
     * generate a two-dimensional array to outline the area.
     * <br>
     * We only outline blocks relative to the eye-height of the
     * player, cast towards the outer boundary.
     * <br>
     * Nullable if not ready
     *
     * @param location Where to cast from
     * @return The area mapped to an appropriate outline
     */
    public boolean[][] buildMapAround(Location location) {
        if (location == null) {
            return null;
        }

        // use from minimap cache if possible
        BlockIdentifier identifier = new BlockIdentifier(location);
        synchronized (SYNC) {
            boolean[][] outline = minimap_cache.remove(identifier);
            if (outline != null) {
                minimap_cache.put(identifier, outline);
                return outline;
            }
        }

        // ensure that area is fully outlined
        for (int i = -MINIMAP_SIZE_CHUNK; i <= +MINIMAP_SIZE_CHUNK; i++) {
            for (int j = -MINIMAP_SIZE_CHUNK; j <= +MINIMAP_SIZE_CHUNK; j++) {
                synchronized (SYNC) {
                    ChunkOutline outline = outline_finished.get(new ChunkIdentifier(location).withOffset(i, j));
                    if (outline == null) {
                        return null;
                    }
                }
            }
        }

        World world = location.getWorld();
        if (world != null) {
            int center_x = location.getBlockX();
            int center_y = location.getBlockY() + (-1 * world.getMinHeight());
            int center_z = location.getBlockZ();
            int size = MINIMAP_SIZE_BLOCK;

            // maximized holds all blocks on our grid
            boolean[][] maximize = new boolean[size][size];
            for (int offset_x = 0; offset_x < size; offset_x++) {
                for (int offset_y = 0; offset_y < size; offset_y++) {
                    int rX = center_x - (size/2) + offset_x;
                    int rY = center_z - (size/2) + offset_y;

                    if (getOutlineAt(world, rX, center_y, rY)) {
                        // facing against a wall
                        maximize[offset_y][offset_x] = true;
                    } else if (!getOutlineAt(world, rX, center_y-1, rY)) {
                        // falling down
                        maximize[offset_y][offset_x] = true;
                    }
                }
            }

            // minimize holds only exposed surfaces
            boolean[][] minimize = new boolean[size][size];
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    if (!maximize[i][j]) {
                        continue;
                    }

                    try {
                        if (!maximize[i+1][j]) {
                            minimize[i][j] = true;
                        } else if (!maximize[i-1][j]) {
                            minimize[i][j] = true;
                        } else if (!maximize[i][j+1]) {
                            minimize[i][j] = true;
                        } else if (!maximize[i][j-1]) {
                            minimize[i][j] = true;
                        }
                    } catch (Exception ignored) {
                        // ignored
                    }
                }
            }

            minimap_cache.put(identifier, minimize);
            return minimize;
        } else {
            throw new NullPointerException("Location has no world!");
        }
    }

    /*
     * Restrain maximum size of minimap cache, this will drop elements from the
     * cache based on when they were last utilised.
     */
    private void restrainCacheSize() {
        // restrain how many chunk outlines we keep track of
        int maximum_chunk_cache = MAXIMUM_CHUNK_CACHE + (Bukkit.getOnlinePlayers().size() * 128);
        synchronized (SYNC) {
            Iterator<Map.Entry<ChunkIdentifier, ChunkOutline>> popularity = this.outline_finished.entrySet().iterator();
            while (outline_finished.size() > maximum_chunk_cache) {
                popularity.next();
                popularity.remove();
            }
        }
        // restrain how many minimap outlines we keep track of
        int maximum_map_cache = MAXIMUM_MAP_CACHE + (Bukkit.getOnlinePlayers().size() * 16);
        synchronized (SYNC) {
            Iterator<Map.Entry<BlockIdentifier, boolean[][]>> popularity = this.minimap_cache.entrySet().iterator();
            while (minimap_cache.size() > maximum_map_cache) {
                popularity.next();
                popularity.remove();
            }
        }
    }

    /*
     * Work off the queued chunk outline requests, a backlog may be created
     * should too many chunks be requested at once.
     */
    private void processQueuedChunks() {
        int chunks_per_tick = (int) (CHUNKS_PER_TICK + Math.sqrt(Bukkit.getOnlinePlayers().size()));
        for (int i = 0; i < chunks_per_tick; i++) {
            // pull the next chunk we want outlined
            ChunkIdentifier next;
            synchronized (SYNC) {
                if (outline_queued.isEmpty()) {
                    return;
                }

                Iterator<ChunkIdentifier> iterator = outline_queued.iterator();
                next = iterator.next();
                iterator.remove();
            }
            // populate with the chunk information
            ChunkOutline outline = RPGCore.inst().getVolatileManager().getChunkOutline(next.asChunk());
            synchronized (SYNC) {
                outline_finished.put(next, outline);
            }
        }
    }

    /*
     * Queue all chunks within approximately 50 blocks of all players into
     * the minimap buffer
     */
    private void requestNewChunks() {
        Set<ChunkIdentifier> player_overlap = new HashSet<>();
        // deploy requests for minimap outline
        for (Player player : Bukkit.getOnlinePlayers()) {
            Chunk chunk = player.getLocation().getChunk();
            ChunkIdentifier identifier = new ChunkIdentifier(chunk);
            // do not queue twice for players gathered together
            if (!player_overlap.add(identifier)) {
                continue;
            }
            // request all nearby chunks to be populated
            for (int i = -4; i <= +4; i++) {
                for (int j = -4; j < +4; j++) {
                    ChunkIdentifier identifier_offset = identifier.withOffset(i, j);
                    synchronized (SYNC) {
                        // element was already computed
                        if (outline_finished.containsKey(identifier_offset)) {
                            continue;
                        }
                        // element is already queued up
                        if (outline_queued.contains(identifier_offset)) {
                            continue;
                        }
                        // can append to queue
                        outline_queued.add(identifier_offset);
                    }
                }
            }
        }
    }
}
