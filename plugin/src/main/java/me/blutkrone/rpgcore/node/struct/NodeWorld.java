package me.blutkrone.rpgcore.node.struct;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.util.io.FileUtil;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

/**
 * A construction that keeps track of nodes for a single world.
 */
public class NodeWorld {
    // nodes identified by their ID
    private Map<UUID, NodeActive> node_by_id = new HashMap<>();
    // node structure to ease up search
    private Map<Long, List<NodeActive>> node_by_chunk = new HashMap<>();
    // name of the world we are backing up
    private String world_name;

    /**
     * A tracker for a world, which contains information about
     * all nodes on a world.
     *
     * @param world_name the name of the world we are linked with.
     */
    public NodeWorld(String world_name) {
        this.world_name = world_name;
    }

    /**
     * Request a tick on every node present on this world, provided
     * that the world associated with it is loaded.
     * <p>
     * This still happens, even without players in the world.
     * <p>
     * Execution is threaded, no assurance is made about when the
     * execution will finish.
     */
    public void tick() {
        // make sure we got a world to work with
        World world = Bukkit.getWorld(this.world_name);
        if (world == null) {
            return;
        }
        // create a snapshot on our main-thread
        Map<UUID, NodeActive> snapshot = new HashMap<>(this.node_by_id);
        List<Player> players = new ArrayList<>(world.getPlayers());

        // asynchronous await to feed back our data
        Bukkit.getScheduler().runTaskAsynchronously(RPGCore.inst(), () -> {
            Map<NodeActive, List<Player>> result = new HashMap<>();
            snapshot.forEach((id, node) -> {
                Vector vector = new Vector(node.getX(), node.getY(), node.getZ());
                int radius = node.getNode().getEngageRadius();
                List<Player> valid = new ArrayList<>();
                for (Player player : players) {
                    if (player.getLocation().toVector().distance(vector) <= radius) {
                        valid.add(player);
                    }
                }
                result.put(node, valid);
            });

            // invoke the callback on the main thread again
            Bukkit.getScheduler().runTask(RPGCore.inst(), () -> {
                result.forEach((node, observing) -> {
                    node.getNode().tick(world, node, observing);
                });
            });
        });
    }

    /**
     * Create a node of a certain type, before invoking this method
     * always check if this is a valid space. Nodes created like this
     * will always be saved to the disk.
     *
     * @param x    position
     * @param y    position
     * @param z    position
     * @param node "type:id"
     */
    public void create(int x, int y, int z, String node) {
        // create an identifier that isn't in use
        UUID uuid = UUID.randomUUID();
        while (this.node_by_id.containsKey(uuid)) {
            uuid = UUID.randomUUID();
        }
        // track the node which we are using
        NodeActive active_node = new NodeActive(uuid, x, y, z, node);
        this.node_by_id.put(uuid, active_node);
        long id = (((long) ((x >> 4))) << 32) | (z >> 4) & 0xFFFFFFFFL;
        this.node_by_chunk.computeIfAbsent(id, (k -> new ArrayList<>()))
                .add(active_node);
        // identify where to dump the node
        File file = FileUtil.file("editor/node/" + this.world_name, uuid + ".rpgcore");
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        // keep track of the node
        try (FileWriter fw = new FileWriter(file, Charset.forName("UTF-8"))) {
            RPGCore.inst().getGsonPretty().toJson(active_node, fw);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Register a node instance, this will NOT save the node to the disk.
     *
     * @param active the node to register.
     * @return true if we could register
     */
    public boolean register(NodeActive active) {
        if (this.node_by_id.containsKey(active.getID())) {
            return false;
        }

        // register node by the ID
        this.node_by_id.put(active.getID(), active);
        // register node by the quick-search
        long id = (((long) ((active.getX() >> 4))) << 32) | (active.getZ() >> 4) & 0xFFFFFFFFL;
        this.node_by_chunk.computeIfAbsent(id, (k -> new ArrayList<>())).add(active);
        // inform about successful registration
        return true;
    }

    /**
     * Retrieve all nodes within the radius of the given location, the search
     * cannot extend beyond a radius of 128.
     *
     * @param x      position
     * @param y      position
     * @param z      position
     * @param radius distance to search
     * @return all nodes within distance
     */
    public List<NodeActive> getNodesNear(int x, int y, int z, int radius) {
        List<NodeActive> output = new ArrayList<>();
        // vector instance to ease up distance search
        Vector origin = new Vector(x, y, z);
        int distSq = radius * radius;
        // chunk specific parameters
        int chunks = 1 + (radius >> 4);
        // search for all nodes within distance
        for (int i = -chunks; i <= +chunks; i++) {
            for (int j = -chunks; j <= +chunks; j++) {
                long id = (((long) ((x >> 4) + i)) << 32) | ((z >> 4) + j) & 0xFFFFFFFFL;
                List<NodeActive> candidates = this.node_by_chunk.get(id);
                if (candidates != null) {
                    for (NodeActive candidate : candidates) {
                        Vector target = new Vector(candidate.getX(), candidate.getY(), candidate.getZ());
                        if (target.distanceSquared(origin) <= distSq) {
                            output.add(candidate);
                        }
                    }
                }
            }
        }
        // offer up our random output selection
        return output;
    }

    /**
     * Destruct a certain node.
     *
     * @param uuid identifier of the node.
     */
    public void destruct(UUID uuid) {
        // drop the node from the ID
        NodeActive removed = this.node_by_id.remove(uuid);
        if (removed == null) {
            return;
        }
        // drop the node from the chunk index
        long id = (((long) ((removed.getX() >> 4))) << 32) | ((removed.getZ() >> 4)) & 0xFFFFFFFFL;
        List<NodeActive> nodes = this.node_by_chunk.get(id);
        if (nodes != null) {
            nodes.remove(removed);
        }
        // clean the data of the node
        NodeData data = removed.getData();
        if (data != null) {
            data.abandon();
        }
        // delete the file of the node
        File file = FileUtil.file("editor/node/" + this.world_name, removed.getID() + ".rpgcore");
        try {
            file.getParentFile().mkdirs();
            file.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * If the given entity is associated with a node, retrieve that
     * node. Otherwise offers null.
     *
     * @param entity which entity to inspect.
     * @return the node backing up the given entity.
     */
    public NodeActive getNode(Entity entity) {
        List<MetadataValue> metadata = entity.getMetadata("rpgcore-node");
        if (metadata.isEmpty()) {
            return null;
        }
        String uuid = metadata.get(0).asString();
        return this.node_by_id.get(UUID.fromString(uuid));
    }
}
