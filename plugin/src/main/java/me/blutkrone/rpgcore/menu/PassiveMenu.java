package me.blutkrone.rpgcore.menu;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.hud.editor.index.EditorIndex;
import me.blutkrone.rpgcore.hud.editor.root.passive.EditorPassiveTree;
import me.blutkrone.rpgcore.item.CoreItem;
import me.blutkrone.rpgcore.nms.api.menu.IChestMenu;
import me.blutkrone.rpgcore.passive.CorePassiveNode;
import me.blutkrone.rpgcore.passive.CorePassiveTree;
import me.blutkrone.rpgcore.resourcepack.ResourcePackManager;
import me.blutkrone.rpgcore.util.ItemBuilder;
import me.blutkrone.rpgcore.util.Utility;
import me.blutkrone.rpgcore.util.fontmagic.MagicStringBuilder;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Before the menu is opened, please check against integrity
 * first and provide a reset if deemed necessary.
 */
public class PassiveMenu extends AbstractCoreMenu {

    // whether we are in node selection mode
    boolean node_select;
    // tool to assist with selection mode
    NodeSelector node_select_help;
    // what tree we are processing
    private String tree;

    public PassiveMenu(String tree) {
        super(6);
        this.tree = tree;
        this.node_select_help = new NodeSelector();
    }

    @Override
    public void rebuild() {
        if (this.node_select) {
            node_select_help.rebuildAsSelector();
            return;
        }

        ResourcePackManager rpm = RPGCore.inst().getResourcePackManager();

        this.getMenu().clearItems();
        MagicStringBuilder msb = new MagicStringBuilder();

        CorePlayer player = RPGCore.inst().getEntityManager().getPlayer(getMenu().getViewer());
        CorePassiveTree tree = RPGCore.inst().getPassiveManager().getTreeIndex().get(this.tree);
        tree.ensureIntegrity(player);

        Set<Long> allocated = player.getAllocated(tree.getId());
        int have = player.getPassivePoints().getOrDefault(tree.getPoint(), 0);
        int refund = player.getPassiveRefunds().getOrDefault(tree.getPoint(), 0);
        int used = allocated.size();

        // draw frame based on the tree type
        msb.shiftToExact(-208);
        msb.append(resourcepack().texture("menu_passive_" + tree.getMenuDesign()), ChatColor.WHITE);

        // information about passive points
        String template_string = language().getTranslation("passive_info_string");
        template_string = template_string.replace("{USED}", "" + used);
        template_string = template_string.replace("{HAVE}", "" + have);
        template_string = template_string.replace("{REFUNDS}", "" + refund);

        String[] split = template_string.split("#");
        msb.shiftToExact(0).append(split[0], "passive_tree_info");
        if (split.length == 2) {
            msb.shiftToExact(160 - Utility.measure(split[1]));
            msb.append(split[1], "passive_tree_info");
        }
        msb.shiftToExact(-208);

        // viewport we are looking from
        long viewport_raw = player.getPassiveViewport().getOrDefault(tree.getId(), 0L);
        int x = (int) (viewport_raw >> 32);
        int y = (int) (viewport_raw);

        // reset viewport if invalid
        if (!isValidViewport(tree, x, y)) {
            player.getPassiveViewport().put(tree.getId(), 0L);
            x = 0;
            y = 0;
        }

        // dump all nodes in our viewport
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 6; j++) {
                int dX = x - 4 + i;
                int dY = y - 2 + j;
                long encoded = (((long) dX) << 32) | (dY & 0xffffffffL);

                CorePassiveNode node = tree.getNode(dX, dY);
                if (node != null) {
                    // build the bitmask for path connectivity
                    int coded = 0b0;
                    coded |= (tree.hasNode(dX, dY + 1)) ? 0b0100 : 0b0000; // 8 | ^
                    coded |= (tree.hasNode(dX, dY - 1)) ? 0b1000 : 0b0000; // 4 | v
                    coded |= (tree.hasNode(dX + 1, dY)) ? 0b0010 : 0b0000; // 2 | >
                    coded |= (tree.hasNode(dX - 1, dY)) ? 0b0001 : 0b0000; // 1 | <
                    // count how many nodes are linked
                    int total_links = 0;
                    for (long maybe : tree.getGraph().getNodesLinkedToPath(dX, dY)) {
                        if (allocated.contains(maybe)) {
                            total_links += 1;
                        }
                    }

                    if (node instanceof CorePassiveNode.Path) {
                        // render the path based on links
                        if (total_links == 0) {
                            msb.shiftToExact(-1 + 18 * i);
                            msb.append(rpm.texture("passive_path_unreachable_" + j + "_" + coded), ChatColor.WHITE);
                        } else if (total_links == 1) {
                            msb.shiftToExact(-1 + 18 * i);
                            msb.append(rpm.texture("passive_path_reachable_" + j + "_" + coded), ChatColor.WHITE);
                        } else if (total_links >= 2) {
                            msb.shiftToExact(-1 + 18 * i);
                            msb.append(rpm.texture("passive_path_active_" + j + "_" + coded), ChatColor.WHITE);
                        }
                    } else {
                        // handle socketed items
                        ItemStack item = null;
                        if (node.isSocket()) {
                            item = player.getPassiveSocketed(tree.getId(), encoded);
                            if (item != null && item.getType().isAir()) {
                                item = null;
                            }
                        }
                        // handle node based item
                        if (item == null) {
                            if (allocated.contains(encoded)) {
                                item = node.getAllocatedIcon().clone();
                            } else {
                                // path node that was allocated
                                item = node.getUnallocated().clone();
                            }
                        }
                        // render the item
                        getMenu().setItemAt((j * 9) + i, item);
                    }
                } else if (encoded == 0) {
                    getMenu().setItemAt((j * 9) + i, ItemBuilder.of(Material.BARRIER)
                            .name("§cEntry Point Missing")
                            .appendLore("§fCreate a node here, it will always be allocated.")
                            .appendLore("§fExpand your passive tree from around here.")
                            .appendLore("§f")
                            .appendLore("§f[Shift-Right] to open editor (Only Admins!)")
                            .build());
                }
            }
        }

        getMenu().setTitle(msb.compile());
    }

    @Override
    public void click(InventoryClickEvent event) {
        if (this.node_select) {
            node_select_help.clickAsSelector(event);
            return;
        }

        if (event.getClickedInventory() != event.getView().getTopInventory()) {
            return;
        }

        event.setCancelled(true);

        CorePlayer player = RPGCore.inst().getEntityManager().getPlayer(getMenu().getViewer());
        CorePassiveTree tree = RPGCore.inst().getPassiveManager().getTreeIndex().get(this.tree);
        Set<Long> allocated = player.getAllocated(tree.getId());
        int have = player.getPassivePoints().getOrDefault(tree.getPoint(), 0);
        int refund = player.getPassiveRefunds().getOrDefault(tree.getPoint(), 0);
        int used = allocated.size();
        long viewport_raw = player.getPassiveViewport().getOrDefault(tree.getId(), 0L);
        int x = (int) (viewport_raw >> 32) - 4 + (event.getSlot() % 9);
        int y = (int) (viewport_raw) - 2 + (event.getSlot() / 9);
        long position = (((long) x) << 32) | (y & 0xffffffffL);

        if (event.getClick() == ClickType.LEFT) {
            CorePassiveNode node = tree.getNode(x, y);

            if (node != null && node.isSocket() && allocated.contains(position)) {
                // handle socketing related logic
                ItemStack bukkit_socketed = event.getCurrentItem();
                CoreItem core_socketed = RPGCore.inst().getItemManager().getItemFrom(bukkit_socketed).orElse(null);
                ItemStack bukkit_cursor = event.getCursor();
                CoreItem core_cursor = RPGCore.inst().getItemManager().getItemFrom(bukkit_cursor).orElse(null);

                if (core_socketed != null && (bukkit_cursor == null || bukkit_cursor.getType().isAir())) {
                    // take out what is socketed on the tree
                    player.setPassiveSocketed(tree.getId(), position, null);
                    event.setCursor(node.getAllocatedIcon());
                    event.setCancelled(false);
                } else if (core_cursor != null) {
                    // ensure that item is compatible
                    if (node.canSocket(core_cursor)) {
                        // item we are holding is going to be socketed
                        player.setPassiveSocketed(tree.getId(), position, bukkit_cursor.clone());
                        event.setCancelled(false);
                        // if not a swap, do not take out the inventory design
                        if (core_socketed == null) {
                            event.setCurrentItem(new ItemStack(Material.AIR));
                        }
                    } else {
                        String message = RPGCore.inst().getLanguageManager().getTranslation("cannot_socket_here");
                        event.getWhoClicked().sendMessage(message);
                    }
                }
            } else {
                if (tree.getGraph().isNotPath(position)) {
                    // handle allocation if necessary
                    if (!allocated.contains(position)) {
                        if (have > used) {
                            boolean has_adjacent = false;
                            for (long connected : tree.getGraph().getAdjacentCollapsed(position)) {
                                if (allocated.contains(connected)) {
                                    has_adjacent = true;
                                }
                            }

                            if (has_adjacent) {
                                allocated.add(position);
                                getMenu().stalled(this::rebuild);
                            }
                        }
                    }
                }

                // correct for axis locking
                if (tree.isLockedX()) {
                    x = 0;
                    position = (((long) x) << 32) | (y & 0xffffffffL);
                }
                if (tree.isLockedY()) {
                    y = 0;
                    position = (((long) x) << 32) | (y & 0xffffffffL);
                }
                // update viewport on tree
                if (isValidViewport(tree, x, y)) {
                    player.getPassiveViewport().put(tree.getId(), position);
                    getMenu().stalled(this::rebuild);
                }
            }
        } else if (event.getClick() == ClickType.RIGHT) {
            // try remove allocated passive point
            if (allocated.contains(position) && refund > 0) {
                if (position != 0) {
                    Set<Long> without = new HashSet<>(allocated);
                    without.remove(position);
                    if (tree.getGraph().checkConnectivity(without)) {
                        allocated.remove(position);
                        player.getPassiveRefunds().merge(tree.getPoint(), -1, (a, b) -> a + b);
                    }
                }
            }
            // correct for axis locking
            if (tree.isLockedX()) {
                x = 0;
                position = (((long) x) << 32) | (y & 0xffffffffL);
            }
            if (tree.isLockedY()) {
                y = 0;
                position = (((long) x) << 32) | (y & 0xffffffffL);
            }
            // update viewport on tree
            if (isValidViewport(tree, x, y)) {
                player.getPassiveViewport().put(tree.getId(), position);
                getMenu().stalled(this::rebuild);
            }
        } else if (event.getClick() == ClickType.SHIFT_LEFT) {
            if (event.getWhoClicked().isOp()) {
                // open editor to work within
                long __position = position;
                getMenu().stalled(() -> {
                    node_select = true;
                    node_select_help.offset = 0;
                    node_select_help.position = __position;
                    rebuild();
                });
            }
        }
    }

    @Override
    public void close(InventoryCloseEvent event) {
        Bukkit.getScheduler().runTask(RPGCore.inst(), () -> {
            CorePlayer player = RPGCore.inst().getEntityManager().getPlayer(getMenu().getViewer());
            if (player != null) {
                player.updatePassiveTree();
            }
        });
    }

    /**
     * Validate the viewport, the viewport is valid if we would see anything
     * on the screen.
     *
     * @return whether the viewport is valid or not.
     */
    private boolean isValidViewport(CorePassiveTree tree, int x, int y) {
        // verify if viewport respects axis locking rules
        if (tree.isLockedX() && x != 0) {
            return false;
        }
        if (tree.isLockedY() && y != 0) {
            return false;
        }

        // viewport is valid if we can see any node from there.
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 6; j++) {
                CorePassiveNode node = tree.getNode(x - 4 + i, y - 2 + j);
                if (node != null) {
                    return true;
                }
            }
        }

        return false;
    }

    /*
     * Wrapper to assist with node selection, we use this instead
     * of an isolated menu for smoother editing.
     */
    class NodeSelector {
        List<ItemStack> preview;
        int offset;
        long position;

        NodeSelector() {
            this.preview = new ArrayList<>();
            // default element to create a path
            ItemStack path = ItemBuilder.of(Material.NETHER_BRICK)
                    .name("§fCreate Path")
                    .appendLore("§fCreate a connective path")
                    .appendLore("§fCan connect 'real' nodes")
                    .build();
            IChestMenu.setBrand(path, RPGCore.inst(), "passive-edit-admin", "path");
            this.preview.add(path);
            // default element to delete a node
            ItemStack delete = ItemBuilder.of(Material.BARRIER)
                    .name("§cDelete Node")
                    .appendLore("§fDelete the node you click on")
                    .build();
            IChestMenu.setBrand(delete, RPGCore.inst(), "passive-edit-admin", "delete");
            this.preview.add(delete);
            // dump of all other passive nodes
            for (CorePassiveNode node : RPGCore.inst().getPassiveManager().getNodeIndex().getAll()) {
                if (!(node instanceof CorePassiveNode.Path)) {
                    ItemStack icon = node.getAllocatedIcon();
                    IChestMenu.setBrand(icon, RPGCore.inst(), "passive-edit-admin", node.getId());
                    this.preview.add(icon);
                }
            }
        }

        /*
         * Rebuild method, for node selection
         */
        void rebuildAsSelector() {
            getMenu().clearItems();

            // base texture
            MagicStringBuilder msb = new MagicStringBuilder();
            msb.shiftToExact(-208);
            msb.append(resourcepack().texture("menu_scroller_grid"), ChatColor.WHITE);

            for (int i = 0; i < 6; i++) {
                for (int j = 0; j < 8; j++) {
                    int k = i * 9 + j;
                    int m = i * 8 + j;
                    ItemStack icon = preview.size() > ((offset * 8) + m) ? preview.get((offset * 8) + m) : null;
                    if (icon != null) {
                        getMenu().setItemAt(k, icon);
                    }
                }
            }

            // render scroll-bar for the viewport
            msb.shiftToExact(150);
            if (preview.size() <= 54) {
                msb.append(resourcepack().texture("pointer_huge_0"), ChatColor.WHITE);
            } else if (preview.size() <= 54 * 2) {
                double length = Math.ceil(preview.size() / 8d) - 6;
                double ratio = offset / length;

                msb.append(resourcepack().texture("pointer_medium_" + (int) (100 * ratio)), ChatColor.WHITE);
            } else if (preview.size() <= 54 * 3) {
                double length = Math.ceil((preview.size() / 8d)) - 6;
                double ratio = offset / length;

                msb.append(resourcepack().texture("pointer_small_" + (int) (100 * ratio)), ChatColor.WHITE);
            } else {
                double length = Math.ceil((preview.size() / 8d)) - 6;
                double ratio = offset / length;

                msb.append(resourcepack().texture("pointer_tiny_" + (int) (100 * ratio)), ChatColor.WHITE);
            }

            getMenu().setTitle(msb.compile());
        }

        /*
         * Click method, for node selection
         *
         * @param event
         */
        void clickAsSelector(InventoryClickEvent event) {
            event.setCancelled(true);

            if (event.getView().getTopInventory() == event.getClickedInventory()) {
                if (event.getSlot() == 8) {
                    // scroll up by one
                    offset = Math.max(0, offset - 1);
                    getMenu().queryRebuild();
                } else if (event.getSlot() == 17) {
                    // scroll to top
                    offset = 0;
                    getMenu().queryRebuild();
                } else if (event.getSlot() == 26) {
                    // ignore other clicks
                } else if (event.getSlot() == 35) {
                    // ignore other clicks
                } else if (event.getSlot() == 44) {
                    // scroll to bottom
                    offset = (preview.size() / 8) - 6;
                    getMenu().queryRebuild();
                } else if (event.getSlot() == 53) {
                    // scroll down by one
                    int floor = Math.max(0, (preview.size() / 8) - 6);
                    offset = Math.min(floor, offset + 1);
                    getMenu().queryRebuild();
                } else {
                    String id = IChestMenu.getBrand(event.getCurrentItem(), RPGCore.inst(), "passive-edit-admin", "");
                    if (id != null && !id.isEmpty()) {
                        // apply the changes
                        apply(id);
                        // rebuild the menu
                        getMenu().stalled(() -> {
                            node_select = false;
                            rebuild();
                        });
                    }
                }
            }
        }

        private void apply(String operation) {
            EditorIndex<CorePassiveTree, EditorPassiveTree> index = RPGCore.inst().getPassiveManager().getTreeIndex();
            EditorPassiveTree editor = index.edit(tree);

            if (operation.equalsIgnoreCase("path")) {
                // delete whatever node was before
                for (List<Long> longs : editor.layout.values()) {
                    longs.remove(position);
                }
                // add back as a path node
                editor.layout.computeIfAbsent("path", (k -> new ArrayList<>())).add(position);
            } else if (operation.equalsIgnoreCase("delete")) {
                // delete whatever node was before
                for (List<Long> longs : editor.layout.values()) {
                    longs.remove(position);
                }
            } else {
                // delete whatever node was before
                for (List<Long> longs : editor.layout.values()) {
                    longs.remove(position);
                }
                // add back as a real node
                editor.layout.computeIfAbsent(operation, (k -> new ArrayList<>())).add(position);
            }

            // update integrity flag on tree
            editor.integrity = System.currentTimeMillis();
            // push the changes we made on the core
            index.update(tree, editor.build(tree));

            // save changes made to disk
            try {
                // todo keep a version control of our changes
                Bukkit.getLogger().severe("not implemented (backup before save)");
                // apply the actual saving
                editor.save();
                // inform about having saved
                getMenu().getViewer().sendMessage("§aSaved '" + tree + "' to disk!");
            } catch (IOException e) {
                getMenu().getViewer().sendMessage("§cSomething went wrong while saving!");
                e.printStackTrace();
            }
        }
    }
}
