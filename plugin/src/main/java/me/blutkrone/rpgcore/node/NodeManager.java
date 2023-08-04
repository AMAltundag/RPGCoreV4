package me.blutkrone.rpgcore.node;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.editor.index.EditorIndex;
import me.blutkrone.rpgcore.editor.root.node.*;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.node.impl.*;
import me.blutkrone.rpgcore.node.struct.NodeActive;
import me.blutkrone.rpgcore.node.struct.NodeWorld;
import me.blutkrone.rpgcore.quest.CoreQuest;
import me.blutkrone.rpgcore.quest.task.AbstractQuestTask;
import me.blutkrone.rpgcore.quest.task.impl.CoreQuestTaskVisit;
import me.blutkrone.rpgcore.util.io.FileUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NodeManager implements Listener {
    // nodes organized into world templates
    private Map<String, NodeWorld> nodes_by_world = new HashMap<>();
    // indexes for node implementations
    private EditorIndex<CoreNodeBox, EditorNodeBox> index_box;
    private EditorIndex<CoreNodeSpawner, EditorNodeSpawner> index_spawner;
    private EditorIndex<CoreNodeCollectible, EditorNodeCollectible> index_collectible;
    private EditorIndex<CoreNodeHotspot, EditorNodeHotspot> index_hotspot;
    private EditorIndex<CoreNodeGate, EditorNodeGate> index_gate;

    public NodeManager() {
        // load all indexes in the world
        this.index_box = new EditorIndex<>("box", EditorNodeBox.class, EditorNodeBox::new);
        this.index_spawner = new EditorIndex<>("spawner", EditorNodeSpawner.class, EditorNodeSpawner::new);
        this.index_collectible = new EditorIndex<>("collectible", EditorNodeCollectible.class, EditorNodeCollectible::new);
        this.index_hotspot = new EditorIndex<>("hotspot", EditorNodeHotspot.class, EditorNodeHotspot::new);
        this.index_gate = new EditorIndex<>("gate", EditorNodeGate.class, EditorNodeGate::new);

        // load all nodes which we got in memory (including of unloaded worlds.)
        try {
            File parent = FileUtil.directory("editor/node").getParentFile();
            if (!parent.exists()) {
                parent.mkdirs();
            }
            for (File node_directory : FileUtil.directory("editor/node").listFiles()) {
                String world = node_directory.getName();
                for (File node_file : node_directory.listFiles()) {
                    Reader reader = Files.newBufferedReader(node_file.toPath());
                    NodeActive node = RPGCore.inst().getGsonPretty().fromJson(reader, NodeActive.class);
                    reader.close();
                    this.nodes_by_world.computeIfAbsent(world, NodeWorld::new).register(node);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Bukkit.getScheduler().runTaskTimer(RPGCore.inst(), () -> {
            // trigger all nodes known on the server
            nodes_by_world.forEach((id, world) -> world.tick());
            // update the quests that need us to visit a node
            for (Player player : Bukkit.getOnlinePlayers()) {
                CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(player);
                if (core_player != null) {
                    NodeWorld node_world = getNodeWorld(player.getWorld());
                    if (node_world != null) {
                        Location location = player.getLocation();
                        for (String id : core_player.getActiveQuestIds()) {
                            CoreQuest quest = RPGCore.inst().getQuestManager().getIndexQuest().get(id);
                            AbstractQuestTask task = quest.getCurrentTask(core_player);
                            if (task instanceof CoreQuestTaskVisit) {
                                List<NodeActive> nodes = node_world.getNodesNear(location.getBlockX(), location.getBlockY(), location.getBlockZ(), ((CoreQuestTaskVisit) task).getDistance());
                                for (NodeActive node : nodes) {
                                    task.updateQuest(core_player, node.getRawNode());
                                }
                            }
                        }
                    }
                }
            }
        }, 1, 60);

        Bukkit.getPluginManager().registerEvents(this, RPGCore.inst());
    }

    /**
     * This will flush the data of active nodes, and re-generate
     * them at the next opportunity.
     */
    public void reload() {
        for (NodeWorld node_world : this.nodes_by_world.values()) {
            node_world.reset();
        }
    }

    /**
     * Fetch the layout of nodes on the given world.
     *
     * @param world World to check
     * @return The layout of nodes in the world
     */
    public NodeWorld getNodeWorld(World world) {
        return this.nodes_by_world.get(world.getName());
    }

    /**
     * Fetch the layout of nodes on the given world.
     *
     * @param world World to check
     * @return The layout of nodes in the world
     */
    public NodeWorld getOrCreateNodeWorld(World world) {
        return this.nodes_by_world.computeIfAbsent(world.getName(), NodeWorld::new);
    }

    /**
     * An index for nodes granting certain content.
     *
     * @return an index to configure
     */
    public EditorIndex<CoreNodeBox, EditorNodeBox> getIndexBox() {
        return index_box;
    }

    /**
     * An index for nodes granting certain content.
     *
     * @return an index to configure
     */
    public EditorIndex<CoreNodeCollectible, EditorNodeCollectible> getIndexCollectible() {
        return index_collectible;
    }

    /**
     * An index for nodes granting certain content.
     *
     * @return an index to configure
     */
    public EditorIndex<CoreNodeSpawner, EditorNodeSpawner> getIndexSpawner() {
        return index_spawner;
    }

    /**
     * An index for nodes for quest progress
     *
     * @return an index to configure
     */
    public EditorIndex<CoreNodeHotspot, EditorNodeHotspot> getIndexHotspot() {
        return index_hotspot;
    }

    /**
     * An index for nodes for dungeon gates
     *
     * @return an index to configure
     */
    public EditorIndex<CoreNodeGate, EditorNodeGate> getIndexGate() {
        return index_gate;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    void onInteractNodeRightClick(PlayerInteractEntityEvent e) {
        // ensure we are not using world tool
        if (RPGCore.inst().getWorldIntegrationManager().isUsingTool(e.getPlayer())) {
            return;
        }
        // only one click should be detected
        if (e.getHand() != EquipmentSlot.HAND) {
            return;
        }
        // check if there is a node associated
        NodeWorld node_world = this.nodes_by_world.get(e.getPlayer().getWorld().getName());
        if (node_world == null) {
            return;
        }
        // retrieve the node from our trace
        NodeActive node = node_world.getNode(e.getRightClicked());
        if (node == null) {
            return;
        }
        // do not actually process a node interaction
        e.setCancelled(true);
        // otherwise we will interact with the node
        node.getNode().right(e.getPlayer().getWorld(), node, e.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    void onInteractNodeLeftClick(EntityDamageByEntityEvent e) {
        // only check player interactions
        if (!(e.getDamager() instanceof Player)) {
            return;
        }
        // ensure we are not using world tool
        if (RPGCore.inst().getWorldIntegrationManager().isUsingTool(((Player) e.getDamager()))) {
            return;
        }
        // check if there is a node associated
        NodeWorld node_world = this.nodes_by_world.get(e.getDamager().getWorld().getName());
        if (node_world == null) {
            return;
        }
        // retrieve the node from our trace
        NodeActive node = node_world.getNode(e.getEntity());
        if (node == null) {
            return;
        }
        // do not actually process a node interaction
        e.setCancelled(true);
        // otherwise we will interact with the node
        node.getNode().left(e.getDamager().getWorld(), node, ((Player) e.getDamager()));
    }
}