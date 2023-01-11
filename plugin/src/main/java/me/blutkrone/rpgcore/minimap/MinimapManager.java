package me.blutkrone.rpgcore.minimap;

import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.util.io.ConfigWrapper;
import me.blutkrone.rpgcore.util.io.FileUtil;
import me.blutkrone.rpgcore.util.world.ChunkIdentifier;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.*;

public class MinimapManager {

    // a region uses a certain minimap loadout
    private Map<ChunkIdentifier, List<MapRegion>> map_regions_cached = new HashMap<>();
    private Map<String, MapRegion> map_regions_listed = new HashMap<>();
    // a marker indicates a certain location on a map
    private Map<ChunkIdentifier, List<MapMarker>> map_markers_cached = new HashMap<>();
    private List<MapMarker> map_markers_listed = new ArrayList<>();

    public MinimapManager() {
        try {
            ConfigWrapper config = FileUtil.asConfigYML(FileUtil.file("minimap.yml"));

            // load the map regions we got
            config.forEachUnder("minimap-regions", (path, root) -> {
                map_regions_listed.put(path, new MapRegion(path, root.getSection(path)));
            });
            // load the map markers we got
            config.forEachUnder("compass-markers", (path, root) -> {
                map_markers_listed.add(new MapMarker(root.getSection(path)));
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Grab the region associated with the given ID
     *
     * @param region region identifier
     * @return region we found
     */
    public MapRegion getRegion(String region) {
        return this.map_regions_listed.get(region);
    }

    /**
     * Retrieve the markers visible to the given player.
     *
     * @return markers visible to player
     */
    public List<MapMarker> getMarkersOf(Player bukkit, CorePlayer core) {
        // cache markers from the chunk-indexed cache
        List<MapMarker> candidates = map_markers_cached.computeIfAbsent(new ChunkIdentifier(bukkit.getLocation()), (k -> {
            List<MapMarker> filtered = new ArrayList<>();
            for (MapMarker marker : map_markers_listed) {
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
        // retrieve markers available across all worlds
        List<MapMarker> filtered = new ArrayList<>();
        for (MapMarker candidate : candidates) {
            // ensure the marker is within a passable distance
            double distSq = candidate.getLocation().distanceSquared(bukkit.getLocation());
            if (distSq >= candidate.distance * candidate.distance || distSq <= 9d)
                continue;
            // offer up the marker to the render queue
            filtered.add(candidate);
        }
        // retrieve markers specific to this player
        for (MapMarker candidate : core.getMapMarkers().values()) {
            // ensure the marker shares this world
            if (candidate.getLocation().getWorld() != bukkit.getWorld())
                continue;
            // ensure the marker is within a passable distance
            double distSq = candidate.getLocation().distanceSquared(bukkit.getLocation());
            if (distSq >= candidate.distance * candidate.distance || distSq <= 9d)
                continue;
            // offer up the marker to the render queue
            filtered.add(candidate);
        }
        // sort the markers by their distance towards the player
        filtered.sort(Comparator.comparingDouble((o -> o.getLocation().distanceSquared(bukkit.getLocation()))));
        Collections.reverse(filtered);
        return filtered;
    }

    /**
     * Retrieve which region the player should be seeing
     *
     * @param player whose region are we checking out
     * @return which region to present
     */
    public MapRegion getRegionOf(Player player) {
        // retrieve the approximated regions
        List<MapRegion> candidates = map_regions_cached.computeIfAbsent(new ChunkIdentifier(player.getLocation()), (k -> {
            List<MapRegion> filtered = new ArrayList<>();
            for (MapRegion region : map_regions_listed.values()) {
                // ensure we are contained in the region
                if (!region.world.equals(player.getWorld().getName()))
                    continue;
                if (!region.contains(new ChunkIdentifier(player.getLocation())))
                    continue;
                // this chunk can show the region
                filtered.add(region);
            }
            return filtered;
        }));
        // reduce region set to our limited pool
        List<MapRegion> filtered = new ArrayList<>();
        for (MapRegion candidate : candidates) {
            if (candidate.contains(player.getLocation())) {
                filtered.add(candidate);
            }
        }
        // offer the first hit as the map region
        return filtered.isEmpty() ? null : filtered.iterator().next();
    }

    /**
     * Retrieve which region is anchored at the given location
     *
     * @param location where to sample
     * @return which region to present
     */
    public MapRegion getRegionOf(Location location) {
        // retrieve the approximated regions
        List<MapRegion> candidates = map_regions_cached.computeIfAbsent(new ChunkIdentifier(location), (k -> {
            List<MapRegion> filtered = new ArrayList<>();
            for (MapRegion region : map_regions_listed.values()) {
                // ensure we are contained in the region
                if (!region.world.equals(location.getWorld().getName()))
                    continue;
                if (!region.contains(new ChunkIdentifier(location)))
                    continue;
                // this chunk can show the region
                filtered.add(region);
            }
            return filtered;
        }));
        // reduce region set to our limited pool
        List<MapRegion> filtered = new ArrayList<>();
        for (MapRegion candidate : candidates) {
            if (candidate.contains(location)) {
                filtered.add(candidate);
            }
        }
        // offer the first hit as the map region
        return filtered.isEmpty() ? null : filtered.iterator().next();
    }

}
