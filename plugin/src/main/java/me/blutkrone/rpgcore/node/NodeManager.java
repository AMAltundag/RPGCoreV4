package me.blutkrone.rpgcore.node;

import com.github.juliarn.npc.event.PlayerNPCInteractEvent;
import com.google.gson.stream.JsonReader;
import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.command.impl.ToolCommand;
import me.blutkrone.rpgcore.hud.editor.index.EditorIndex;
import me.blutkrone.rpgcore.hud.editor.root.node.EditorNodeBox;
import me.blutkrone.rpgcore.hud.editor.root.node.EditorNodeCollectible;
import me.blutkrone.rpgcore.hud.editor.root.node.EditorNodeSpawner;
import me.blutkrone.rpgcore.node.impl.CoreNodeBox;
import me.blutkrone.rpgcore.node.impl.CoreNodeCollectible;
import me.blutkrone.rpgcore.node.impl.CoreNodeSpawner;
import me.blutkrone.rpgcore.node.struct.NodeActive;
import me.blutkrone.rpgcore.node.struct.NodeData;
import me.blutkrone.rpgcore.node.struct.NodeWorld;
import me.blutkrone.rpgcore.npc.CoreNPC;
import me.blutkrone.rpgcore.util.io.FileUtil;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class NodeManager implements Listener {
    // nodes organized into world templates
    private Map<String, NodeWorld> nodes_by_world = new HashMap<>();
    // indexes for node implementations
    private EditorIndex<CoreNodeBox, EditorNodeBox> index_box;
    private EditorIndex<CoreNodeSpawner, EditorNodeSpawner> index_spawner;
    private EditorIndex<CoreNodeCollectible, EditorNodeCollectible> index_collectible;

    public NodeManager() {
        // load all indexes in the world
        this.index_box = new EditorIndex<>("box", EditorNodeBox.class, EditorNodeBox::new);
        this.index_spawner = new EditorIndex<>("spawner", EditorNodeSpawner.class, EditorNodeSpawner::new);
        this.index_collectible = new EditorIndex<>("collectible", EditorNodeCollectible.class, EditorNodeCollectible::new);

        // load all nodes which we got in memory (including of unloaded worlds.)
        try {
            File parent = FileUtil.directory("editor/node").getParentFile();
            if (!parent.exists()) {
                parent.mkdirs();
            }
            for (File node_directory : FileUtil.directory("editor/node").listFiles()) {
                String world = node_directory.getName();
                for (File node_file : node_directory.listFiles()) {
                    NodeActive node = RPGCore.inst().getGson().fromJson(new JsonReader(new FileReader(node_file)), NodeActive.class);
                    this.nodes_by_world.computeIfAbsent(world, NodeWorld::new).register(node);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // trigger all nodes known on the server
        Bukkit.getScheduler().runTaskTimer(RPGCore.inst(), () -> {
            nodes_by_world.forEach((id, world) -> {
                world.tick();
            });
        }, 1, 60);

        Bukkit.getScheduler().runTaskTimer(RPGCore.inst(), () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                // check if we got a node world setup
                NodeWorld node_world = nodes_by_world.get(player.getWorld().getName());
                if (node_world == null) {
                    continue;
                }
                // ensure we got a player who can use admin tools
                if (!player.hasPermission("rpg.admin")) {
                    continue;
                }
                // identify if we got a tool that we can use
                String tool = ToolCommand.getTool(player.getEquipment().getItemInMainHand());
                if (tool == null) {
                    continue;
                }
                // all nodes within range should glow
                Block where = player.getLocation().getBlock();
                List<NodeActive> nodes = node_world.getNodesNear(where.getX(), where.getY(), where.getZ(), 32);
                for (NodeActive node : nodes) {
                    NodeData data = node.getData();
                    if (data != null) {
                        data.highlight(30);
                    }
                }
            }
        }, 1, 20);

        Bukkit.getPluginManager().registerEvents(this, RPGCore.inst());
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

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    void onCreateNodeWithTool(PlayerInteractEvent e) {
        // only admins can create a node
        if (!e.getPlayer().hasPermission("rpg.admin")) {
            return;
        }
        // tool editing only cares about one hand
        if (e.getClickedBlock() == null) {
            return;
        }
        // only one click should be detected
        if (e.getHand() != EquipmentSlot.HAND) {
            return;
        }
        // only allow to create above
        if (e.getBlockFace() != BlockFace.UP) {
            return;
        }
        // identify what type of tool we got
        String tool = ToolCommand.getTool(e.getItem());
        if (tool == null) {
            return;
        }
        tool = tool.replace(" ", ":");
        // ensure we can create a node here
        Block where = e.getClickedBlock().getRelative(BlockFace.UP);
        NodeWorld node_world = this.nodes_by_world.computeIfAbsent(e.getPlayer().getWorld().getName(), NodeWorld::new);
        if (!node_world.getNodesNear(where.getX(), where.getY(), where.getZ(), 4).isEmpty()) {
            e.getPlayer().sendMessage("§cCreation failed, another node is too close by!");
            return;
        }
        // actually create the node we are dealing with
        node_world.create(where.getX(), where.getY(), where.getZ(), tool);
        e.getPlayer().sendMessage("§cA node has been created!");
    }

    @EventHandler(priority = EventPriority.LOWEST)
    void onInteractNodeNPC(PlayerNPCInteractEvent e) {
        // only one click should be detected
        if (e.getHand() != PlayerNPCInteractEvent.Hand.MAIN_HAND) {
            return;
        }

        // if using a tool, destroy the engaged node
        if (e.getPlayer().hasPermission("rpg.admin")) {
            ItemStack item = e.getPlayer().getInventory().getItemInMainHand();
            String tool = ToolCommand.getTool(item);
            if (tool != null) {
                // check if there is a node associated
                NodeWorld node_world = this.nodes_by_world.get(e.getPlayer().getWorld().getName());
                if (node_world != null) {
                    UUID origin = RPGCore.inst().getNPCManager().getOrigin(e.getNPC());
                    if (origin != null) {
                        node_world.destruct(origin);
                        e.getPlayer().sendMessage("§cA node has been destroyed!");
                    }
                }

                return;
            }
        }

        // delegate the interaction of the node if we got one
        CoreNPC design = RPGCore.inst().getNPCManager().getDesign(e.getNPC());
        if (design != null) {
            design.interact(e);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    void onInteractNodeRightClick(PlayerInteractEntityEvent e) {
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
        // if using a tool, destroy the engaged node
        if (e.getPlayer().hasPermission("rpg.admin")) {
            ItemStack item = e.getPlayer().getInventory().getItemInMainHand();
            String tool = ToolCommand.getTool(item);
            if (tool != null) {
                node_world.destruct(node.getID());
                e.getPlayer().sendMessage("§cA node has been destroyed!");
                return;
            }
        }
        // otherwise we will interact with the node
        node.getNode().right(e.getPlayer().getWorld(), node, e.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    void onInteractNodeLeftClick(EntityDamageByEntityEvent e) {
        // only check player interactions
        if (!(e.getDamager() instanceof Player)) {
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
        // if using a tool, destroy the engaged node
        if (e.getDamager().hasPermission("rpg.admin")) {
            ItemStack item = ((Player) e.getDamager()).getInventory().getItemInMainHand();
            String tool = ToolCommand.getTool(item);
            if (tool != null) {
                node_world.destruct(node.getID());
                e.getDamager().sendMessage("§cA node has been destroyed!");
                return;
            }
        }
        // otherwise we will interact with the node
        node.getNode().left(e.getDamager().getWorld(), node, ((Player) e.getDamager()));
    }
}