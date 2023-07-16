package me.blutkrone.rpgcore.hud.world;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.dungeon.IDungeonInstance;
import me.blutkrone.rpgcore.dungeon.instance.EditorDungeonInstance;
import me.blutkrone.rpgcore.dungeon.structure.AbstractDungeonStructure;
import me.blutkrone.rpgcore.editor.index.EditorIndex;
import me.blutkrone.rpgcore.editor.root.IEditorRoot;
import me.blutkrone.rpgcore.menu.AbstractPickListMenu;
import me.blutkrone.rpgcore.nms.api.packet.handle.IHighlight;
import me.blutkrone.rpgcore.node.struct.AbstractNode;
import me.blutkrone.rpgcore.node.struct.NodeActive;
import me.blutkrone.rpgcore.node.struct.NodeWorld;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Handle the World and RPGCore integration, do note that this
 * is <b>ONLY</b> for updating the integration. The handling of
 * the integration is done by their respective managers.
 */
public class WorldIntegrationManager implements Listener {

    private static ItemStack CREATE_COLLECTIBLE = ItemBuilder
            .of(Material.BUNDLE)
            .flag(ItemFlag.values())
            .name("§fCreate Collectible")
            .appendLore("§fCreate a resource that players can harvest.")
            .build();
    private static ItemStack CREATE_BOX = ItemBuilder
            .of(Material.CHEST)
            .flag(ItemFlag.values())
            .name("§fCreate Box")
            .appendLore("§fCreate a box of randomized items")
            .build();
    private static ItemStack CREATE_SPAWNER = ItemBuilder
            .of(Material.SPAWNER)
            .flag(ItemFlag.values())
            .name("§fCreate Spawner")
            .appendLore("§fCreate a spawnpoint for mobs")
            .build();
    private static ItemStack CREATE_HOTSPOT = ItemBuilder
            .of(Material.MAP)
            .flag(ItemFlag.values())
            .name("§fCreate Hotspot")
            .appendLore("§fCreate a hotspot which marks quest locations")
            .build();
    private static ItemStack CREATE_NPC = ItemBuilder
            .of(Material.PLAYER_HEAD)
            .flag(ItemFlag.values())
            .name("§fCreate NPC")
            .appendLore("§fCreate a spawnpoint for an NPC")
            .build();
    private static ItemStack CREATE_GATE = ItemBuilder
            .of(Material.END_PORTAL_FRAME)
            .flag(ItemFlag.values())
            .name("§fCreate Dungeon Gate")
            .appendLore("§fCreate an entrance for dungeons")
            .build();
    private static ItemStack RPGCORE_INTEGRATION_TOOL = ItemBuilder.of(Material.BLAZE_ROD)
            .name("§fRPGCore World Tool")
            .appendLore("§f[Shift+RMB] to use the tool")
            .appendLore("")
            .appendLore("§cYou must be an admin to use this!")
            .appendLore("§cCannot be used in active dungeons!")
            .build();

    public WorldIntegrationManager() {
        RPGCore.inst().getLogger().info("not implemented (node creation needs proper structure)");

        // handle highlighting as deemed necessary
        Bukkit.getScheduler().runTaskTimer(RPGCore.inst(), () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                NodeWorld node_world = RPGCore.inst().getNodeManager().getNodeWorld(player.getWorld());
                if (node_world == null) {
                    continue;
                }
                // disable highlights if not using tool
                if (!isUsingTool(player)) {
                    continue;
                }
                // hint at nodes within range
                Block where = player.getLocation().getBlock();
                List<NodeActive> nodes = node_world.getNodesNear(where.getX(), where.getY(), where.getZ(), 32);
                for (NodeActive node : nodes) {
                    IHighlight highlight = node.getHighlight();
                    highlight.enable(player);
                    Bukkit.getScheduler().runTaskLater(RPGCore.inst(), () -> {
                        highlight.disable(player);
                    }, 10);
                }
            }
        }, 1, 20);

        Bukkit.getPluginManager().registerEvents(this, RPGCore.inst());
    }

    /**
     * The item to interact with the world tool.
     *
     * @return World tool item.
     */
    public ItemStack getTool() {
        return WorldIntegrationManager.RPGCORE_INTEGRATION_TOOL;
    }

    /**
     * Check if player is using the world tool, and is allowed
     * to do so.
     *
     * @param player Who to check
     * @return Whether we are using the tool.
     */
    public boolean isUsingTool(Player player) {
        return player.hasPermission("rpg.admin")
                && getTool().isSimilar(player.getInventory().getItemInMainHand());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    void onWorldTool(PlayerInteractEvent event) {
        // do not double call
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        // ensure we can use the tool
        if (!isUsingTool(event.getPlayer())) {
            return;
        }
        // must be a shift-right click
        if (!event.getPlayer().isSneaking()) {
            return;
        }
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR) {
            return;
        }

        IDungeonInstance instance = RPGCore.inst().getDungeonManager().getInstance(event.getPlayer().getWorld());
        if (instance == null) {
            NodeWorld node_world = RPGCore.inst().getNodeManager().getOrCreateNodeWorld(event.getPlayer().getWorld());

            // prepare a menu that handles the editing
            AbstractPickListMenu picklist = new AbstractPickListMenu() {
                @Override
                public boolean onPick(String _choice) {
                    String[] choices = _choice.split("#");
                    if (choices[0].equalsIgnoreCase("delete")) {
                        node_world.destruct(UUID.fromString(choices[1]));
                        getMenu().getViewer().sendMessage("§cA node has been deleted!");
                    } else if (choices[0].equalsIgnoreCase("create")) {
                        EditorIndex<? extends AbstractNode, ? extends IEditorRoot> index = null;
                        if (choices[1].equalsIgnoreCase("collectible")) {
                            index = RPGCore.inst().getNodeManager().getIndexCollectible();
                        } else if (choices[1].equalsIgnoreCase("box")) {
                            index = RPGCore.inst().getNodeManager().getIndexBox();
                        } else if (choices[1].equalsIgnoreCase("spawner")) {
                            index = RPGCore.inst().getNodeManager().getIndexSpawner();
                        } else if (choices[1].equalsIgnoreCase("npc")) {
                            index = RPGCore.inst().getNPCManager().getIndex();
                        } else if (choices[1].equalsIgnoreCase("hotspot")) {
                            index = RPGCore.inst().getNodeManager().getIndexHotspot();
                        } else if (choices[1].equalsIgnoreCase("gate")) {
                            index = RPGCore.inst().getNodeManager().getIndexGate();
                        }

                        int x = Integer.parseInt(choices[2]);
                        int y = Integer.parseInt(choices[3]);
                        int z = Integer.parseInt(choices[4]);

                        if (index != null) {
                            AbstractPickListMenu picklist = new AbstractPickListMenu() {
                                @Override
                                public boolean onPick(String choice) {
                                    node_world.create(x, y, z, choices[1] + ":" + choice);
                                    return true;
                                }

                                @Override
                                public boolean highlight(String choice) {
                                    return false;
                                }
                            };

                            for (String s : index.getKeys()) {
                                ItemStack preview = index.get(s).getPreview();
                                preview = ItemBuilder.of(preview.clone()).name(s).build();
                                picklist.addToList(preview, s);
                            }

                            Bukkit.getScheduler().runTaskLater(RPGCore.inst(), () -> {
                                picklist.finish(getMenu().getViewer());
                            }, 2);
                        }
                    }

                    // close menu afterward
                    return true;
                }

                @Override
                public boolean highlight(String choice) {
                    return false;
                }
            };

            // collect nearby nodes to offer removal
            Block block = event.getClickedBlock();
            if (block == null) {
                Location source = event.getPlayer().getLocation();
                List<NodeActive> nodes_nearby = node_world.getNodesNear(source.getBlockX(), source.getBlockY(), source.getBlockZ(), 10);
                nodes_nearby.sort(Comparator.comparingDouble(node -> new Vector(node.getX(), node.getY(), node.getZ()).distanceSquared(source.toVector())));
                // allow choice to delete
                for (NodeActive node : nodes_nearby) {
                    ItemStack icon = ItemBuilder.of(node.getNode().getPreview().clone())
                            .appendLore(String.format("§fWhere: x:%s y:%s z:%s", node.getX(), node.getY(), node.getZ()))
                            .appendLore(String.format("§fDistance: %.1f", new Vector(node.getX(), node.getY(), node.getZ()).distance(source.toVector())))
                            .appendLore(String.format("§fInternal: %s", node.getRawNode()))
                            .appendLore("§cClick to delete node!").build();
                    picklist.addToList(icon, "delete#" + node.getID().toString());
                }
            } else {
                List<NodeActive> nodes_nearby = node_world.getNodesNear(block.getX(), block.getY(), block.getZ(), 3);
                if (nodes_nearby.size() == 0) {
                    // create new node
                    picklist.addToList(CREATE_BOX, "create#box#" + block.getX() + "#" + block.getY() + "#" + block.getZ());
                    picklist.addToList(CREATE_COLLECTIBLE, "create#collectible#" + block.getX() + "#" + block.getY() + "#" + block.getZ());
                    picklist.addToList(CREATE_GATE, "create#gate#" + block.getX() + "#" + block.getY() + "#" + block.getZ());
                    picklist.addToList(CREATE_HOTSPOT, "create#hotspot#" + block.getX() + "#" + block.getY() + "#" + block.getZ());
                    picklist.addToList(CREATE_NPC, "create#npc#" + block.getX() + "#" + block.getY() + "#" + block.getZ());
                    picklist.addToList(CREATE_SPAWNER, "create#spawner#" + block.getX() + "#" + block.getY() + "#" + block.getZ());
                } else {
                    // allow choice to delete
                    nodes_nearby = node_world.getNodesNear(block.getX(), block.getY(), block.getZ(), 10);
                    nodes_nearby.sort(Comparator.comparingDouble(node -> new Vector(node.getX(), node.getY(), node.getZ()).distanceSquared(block.getLocation().toVector())));

                    for (NodeActive node : nodes_nearby) {
                        ItemStack icon = ItemBuilder.of(node.getNode().getPreview().clone())
                                .appendLore(String.format("§fWhere: x:%s y:%s z:%s", node.getX(), node.getY(), node.getZ()))
                                .appendLore(String.format("§fDistance: %.1f", new Vector(node.getX(), node.getY(), node.getZ()).distance(block.getLocation().toVector())))
                                .appendLore(String.format("§fInternal: %s", node.getRawNode()))
                                .appendLore("", "§cClick to delete node!").build();
                        picklist.addToList(icon, "delete#" + node.getID().toString());
                    }
                }
            }

            // show the menu if necessary
            if (picklist.hasChoices()) {
                picklist.finish(event.getPlayer());
            }
        } else if (instance instanceof EditorDungeonInstance) {
            // ensure we got a block interaction
            if (event.getClickedBlock() != null) {
                // search for a structure we can remove
                Location where = event.getClickedBlock().getLocation();
                EditorDungeonInstance editing = (EditorDungeonInstance) instance;
                for (EditorDungeonInstance.ActiveStructure structure : editing.getStructures()) {
                    if (structure.data.removeIf(datum -> datum.where.toVector().equals(where.toVector()))) {
                        event.getPlayer().sendMessage("§cA structure has been removed!");
                        return;
                    }
                }
                // allow placing a structure here
                AbstractPickListMenu picklist = new AbstractPickListMenu() {
                    @Override
                    public boolean onPick(String choice) {
                        // append location to the relevant structure
                        for (EditorDungeonInstance.ActiveStructure structure : editing.getStructures()) {
                            if (structure.structure.getSyncId().equalsIgnoreCase(choice)) {
                                structure.data.add(new AbstractDungeonStructure.StructureData(structure.structure, where));
                            }
                        }
                        // close menu afterward
                        return true;
                    }

                    @Override
                    public boolean highlight(String choice) {
                        return false;
                    }
                };
                editing.getStructures().forEach(structure -> {
                    picklist.addToList(structure.structure.getIcon(), structure.structure.getSyncId());
                });
                picklist.finish(event.getPlayer());
            }
        } else {
            event.getPlayer().sendMessage("§cYou cannot do this with an active dungeon!");
        }
    }
}