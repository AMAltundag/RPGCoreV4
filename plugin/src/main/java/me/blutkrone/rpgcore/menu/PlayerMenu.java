package me.blutkrone.rpgcore.menu;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.minimap.MapMarker;
import me.blutkrone.rpgcore.nms.api.menu.IChestMenu;
import me.blutkrone.rpgcore.util.collection.TreeGraph;
import me.blutkrone.rpgcore.util.fontmagic.MagicStringBuilder;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PlayerMenu extends AbstractCoreMenu {

    private TreeGraph.TreeNode<String> current;
    private Map<String, List<String>> custom_options;

    public PlayerMenu(TreeGraph<String> structure, Map<String, List<String>> custom_options) {
        super(6);
        this.current = structure.getRoot();
        this.custom_options = custom_options;
    }

    @Override
    public void rebuild() {
        if (current.getChildren().size() == 2) {
            rebuildAs2();
        } else if (current.getChildren().size() == 3) {
            rebuildAs3();
        } else if (current.getChildren().size() == 4) {
            rebuildAs4();
        } else if (current.getChildren().size() == 5) {
            rebuildAs5();
        } else if (current.getChildren().size() == 6) {
            rebuildAs6();
        } else {
            getMenu().stalled(() -> getMenu().getViewer().closeInventory());
        }
    }

    @Override
    public void click(InventoryClickEvent event) {
        event.setCancelled(true);
        String menu = IChestMenu.getBrand(event.getCurrentItem(), RPGCore.inst(), "player-menu", null);
        if (menu != null) {
            TreeGraph.TreeNode<String> clicked = current.find(menu::equalsIgnoreCase);
            if (clicked.getChildren().isEmpty()) {
                // process the node itself
                getMenu().stalled(() -> process(clicked.getData(), getMenu().getViewer()));
            } else if (clicked.getChildren().size() == 1) {
                // process the child below
                getMenu().stalled(() -> process(clicked.getChildren().get(0).getData(), getMenu().getViewer()));
            } else {
                // show a selection of child nodes
                this.current = clicked;
                this.getMenu().queryRebuild();
            }
        }
    }

    /*
     * Process an action for the player who clicked the
     * given cortex button.
     *
     * @param action what action we are processing.
     */
    public void process(String action, Player player) {
        if (player == null || !player.isValid()) {
            return;
        }

        if (this.custom_options.containsKey(action)) {
            // customization done by users
            List<String> strings = this.custom_options.get(action);
            for (String string : strings) {
                // substitute player placeholder
                string = string.replace("%player%", getMenu().getViewer().getName());
                // process backend as a command
                if (string.startsWith("!")) {
                    // run on the player
                    Bukkit.dispatchCommand(getMenu().getViewer(), string);
                } else {
                    // run on the console
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), string);
                }
            }
        } else if ("job".equalsIgnoreCase(action)) {
            // passive trees provided by your job
            CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(player);
            if (core_player.getJob() != null) {
                List<String> trees = core_player.getJob().getPassiveTree();
                if (trees.size() == 1) {
                    new PassiveMenu(trees.get(0)).finish(player);
                } else {
                    new JobTreeMenu().finish(player);
                }
            } else {
                Bukkit.getLogger().severe("Unable to resolve Job: " + core_player.getRawJob());
            }
        } else if ("profession".equalsIgnoreCase(action)) {
            // passive trees provided by your job
            if (RPGCore.inst().getJobManager().getIndexProfession().getAll().size() != 0) {
                new ProfessionTreeMenu().finish(player);
            }
        } else if ("skill".equalsIgnoreCase(action)) {
            // configure skillbar
            RPGCore.inst().getHUDManager().getSkillMenu().open(player);
        } else if ("equip".equalsIgnoreCase(action)) {
            // equipment menu
            RPGCore.inst().getHUDManager().getEquipMenu().open(player);
        } else if ("cartography".equalsIgnoreCase(action)) {
            // map of the current area
            NavigationMenu.Cartography cartography = new NavigationMenu.Cartography(player.getLocation());
            CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(player);
            if (core_player != null) {
                // grab the markers on the map
                List<MapMarker> markers = RPGCore.inst().getMinimapManager()
                        .getMarkersOf(player, core_player);
                // track the markers on the map
                for (MapMarker marker : markers) {
                    if (marker.getLocation().distance(player.getLocation()) <= marker.distance) {
                        cartography.addMarker(marker);
                    }
                }
            }
            cartography.finish(player);
        } else if ("logout".equalsIgnoreCase(action)) {
            // logout of current character
            RPGCore.inst().getEntityManager().unregister(player.getUniqueId());
        } else if ("quest".equalsIgnoreCase(action)) {
            // quest log/overview for player
            new QuestMenu.Journal().finish(player);
        } else if ("guild".equalsIgnoreCase(action)) {
            // guild, members, invites and benefits
            player.sendMessage("not implemented (guild menu)");
        } else if ("friends".equalsIgnoreCase(action)) {
            // add friend, remove friend & more info
            player.sendMessage("not implemented (friends menu)");
        } else if ("party".equalsIgnoreCase(action)) {
            // create, invite, join to party
            player.sendMessage("not implemented (party menu)");
        } else if ("mail".equalsIgnoreCase(action)) {
            // mailbox for the player
            player.sendMessage("not implemented (mail menu)");
        } else if ("dungeon".equalsIgnoreCase(action)) {
            // list of dungeons in current area (grayed out others)
            player.sendMessage("not implemented (dungeons menu)");
        } else if ("teleport".equalsIgnoreCase(action)) {
            // teleportation book that allows warping to hotspots
            player.sendMessage("not implemented (teleport menu)");
        }
    }

    /*
     * Itemize an action so we can identify what it is.
     *
     * @param selection an action we want itemized
     * @return itemized action
     */
    private ItemStack itemize(String selection) {
        ItemStack build = language().getAsItem("lc_player_menu_" + selection).build();
        IChestMenu.setBrand(build, RPGCore.inst(), "player-menu", selection);
        return build;
    }

    /*
     * Assistance function to render a certain number of panels
     * on the menu.
     */
    private void rebuildAs2() {
        this.getMenu().clearItems();

        // build basic background
        MagicStringBuilder msb = new MagicStringBuilder();
        msb.shiftToExact(-208);
        msb.append(resourcepack().texture("menu_cortex_2"), ChatColor.WHITE);

        // extract menus we are working on
        String menu1 = this.current.getChildren().get(0).getData();
        String menu2 = this.current.getChildren().get(1).getData();

        // update menu design
        msb.shiftToExact(0).append(resourcepack().texture("cortex_large_player_" + menu1 + "_0", "cortex_large_default_0"), ChatColor.WHITE);
        msb.shiftToExact(0).append(resourcepack().texture("cortex_large_player_" + menu2 + "_1", "cortex_large_default_1"), ChatColor.WHITE);

        // place clickable items
        Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26)
                .forEach(i -> this.getMenu().setItemAt(i, itemize(menu1)));
        Arrays.asList(27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53)
                .forEach(i -> this.getMenu().setItemAt(i, itemize(menu2)));

        this.getMenu().setTitle(msb.compile());
    }


    /*
     * Assistance function to render a certain number of panels
     * on the menu.
     */
    private void rebuildAs3() {
        this.getMenu().clearItems();

        // build basic background
        MagicStringBuilder msb = new MagicStringBuilder();
        msb.shiftToExact(-208);
        msb.append(resourcepack().texture("menu_cortex_3"), ChatColor.WHITE);

        // extract menus we are working on
        String menu1 = this.current.getChildren().get(0).getData();
        String menu2 = this.current.getChildren().get(1).getData();
        String menu3 = this.current.getChildren().get(2).getData();

        // update menu design
        msb.shiftToExact(0).append(resourcepack().texture("cortex_large_player_" + menu1 + "_0", "cortex_large_default_0"), ChatColor.WHITE);
        msb.shiftToExact(0).append(resourcepack().texture("cortex_medium_player_" + menu2 + "_1", "cortex_medium_default_1"), ChatColor.WHITE);
        msb.shiftToExact(81).append(resourcepack().texture("cortex_medium_player_" + menu3 + "_1", "cortex_medium_default_1"), ChatColor.WHITE);

        // place clickable items
        Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26)
                .forEach(i -> this.getMenu().setItemAt(i, itemize(menu1)));
        Arrays.asList(27, 28, 29, 30, 36, 37, 38, 39, 45, 46, 47, 48)
                .forEach(i -> this.getMenu().setItemAt(i, itemize(menu2)));
        Arrays.asList(32, 33, 34, 35, 41, 42, 43, 44, 50, 51, 52, 53)
                .forEach(i -> this.getMenu().setItemAt(i, itemize(menu3)));

        this.getMenu().setTitle(msb.compile());
    }


    /*
     * Assistance function to render a certain number of panels
     * on the menu.
     */
    private void rebuildAs4() {
        this.getMenu().clearItems();

        // build basic background
        MagicStringBuilder msb = new MagicStringBuilder();
        msb.shiftToExact(-208);
        msb.append(resourcepack().texture("menu_cortex_4"), ChatColor.WHITE);

        // extract menus we are working on
        String menu1 = this.current.getChildren().get(0).getData();
        String menu2 = this.current.getChildren().get(1).getData();
        String menu3 = this.current.getChildren().get(2).getData();
        String menu4 = this.current.getChildren().get(3).getData();

        // update menu design
        msb.shiftToExact(0).append(resourcepack().texture("cortex_medium_player_" + menu1 + "_0", "cortex_medium_default_ß"), ChatColor.WHITE);
        msb.shiftToExact(81).append(resourcepack().texture("cortex_medium_player_" + menu2 + "_0", "cortex_medium_default_0"), ChatColor.WHITE);
        msb.shiftToExact(0).append(resourcepack().texture("cortex_medium_player_" + menu3 + "_1", "cortex_medium_default_1"), ChatColor.WHITE);
        msb.shiftToExact(81).append(resourcepack().texture("cortex_medium_player_" + menu4 + "_1", "cortex_medium_default_1"), ChatColor.WHITE);

        // place clickable items
        Arrays.asList(0, 1, 2, 3, 9, 10, 11, 12, 18, 19, 20, 21)
                .forEach(i -> this.getMenu().setItemAt(i, itemize(menu1)));
        Arrays.asList(5, 6, 7, 8, 14, 15, 16, 17, 23, 24, 25, 26)
                .forEach(i -> this.getMenu().setItemAt(i, itemize(menu2)));
        Arrays.asList(27, 28, 29, 30, 36, 37, 38, 39, 45, 46, 47, 48)
                .forEach(i -> this.getMenu().setItemAt(i, itemize(menu3)));
        Arrays.asList(32, 33, 34, 35, 41, 42, 43, 44, 50, 51, 52, 53)
                .forEach(i -> this.getMenu().setItemAt(i, itemize(menu4)));

        this.getMenu().setTitle(msb.compile());
    }


    /*
     * Assistance function to render a certain number of panels
     * on the menu.
     */
    private void rebuildAs5() {
        this.getMenu().clearItems();

        // build basic background
        MagicStringBuilder msb = new MagicStringBuilder();
        msb.shiftToExact(-208);
        msb.append(resourcepack().texture("menu_cortex_5"), ChatColor.WHITE);

        // extract menus we are working on
        String menu1 = this.current.getChildren().get(0).getData();
        String menu2 = this.current.getChildren().get(1).getData();
        String menu3 = this.current.getChildren().get(2).getData();
        String menu4 = this.current.getChildren().get(3).getData();
        String menu5 = this.current.getChildren().get(4).getData();

        // update menu design
        msb.shiftToExact(0).append(resourcepack().texture("cortex_medium_player_" + menu1 + "_0", "cortex_medium_default_0"), ChatColor.WHITE);
        msb.shiftToExact(0).append(resourcepack().texture("cortex_medium_player_" + menu2 + "_1", "cortex_medium_default_1"), ChatColor.WHITE);
        msb.shiftToExact(81).append(resourcepack().texture("cortex_small_player_" + menu3 + "_0", "cortex_small_default_0"), ChatColor.WHITE);
        msb.shiftToExact(81).append(resourcepack().texture("cortex_small_player_" + menu4 + "_1", "cortex_small_default_1"), ChatColor.WHITE);
        msb.shiftToExact(81).append(resourcepack().texture("cortex_small_player_" + menu5 + "_2", "cortex_small_default_2"), ChatColor.WHITE);

        // place clickable items
        Arrays.asList(0, 1, 2, 3, 9, 10, 11, 12, 18, 19, 20, 21)
                .forEach(i -> this.getMenu().setItemAt(i, itemize(menu1)));
        Arrays.asList(27, 28, 29, 30, 36, 37, 38, 39, 45, 46, 47, 48)
                .forEach(i -> this.getMenu().setItemAt(i, itemize(menu2)));
        Arrays.asList(5, 6, 7, 8, 14, 15, 16, 17)
                .forEach(i -> this.getMenu().setItemAt(i, itemize(menu3)));
        Arrays.asList(23, 24, 25, 26, 32, 33, 34, 35)
                .forEach(i -> this.getMenu().setItemAt(i, itemize(menu4)));
        Arrays.asList(41, 42, 43, 44, 50, 51, 52, 53)
                .forEach(i -> this.getMenu().setItemAt(i, itemize(menu5)));

        this.getMenu().setTitle(msb.compile());
    }


    /*
     * Assistance function to render a certain number of panels
     * on the menu.
     */
    private void rebuildAs6() {
        this.getMenu().clearItems();

        // build basic background
        MagicStringBuilder msb = new MagicStringBuilder();
        msb.shiftToExact(-208);
        msb.append(resourcepack().texture("menu_cortex_6"), ChatColor.WHITE);

        // extract menus we are working on
        String menu1 = this.current.getChildren().get(0).getData();
        String menu2 = this.current.getChildren().get(1).getData();
        String menu3 = this.current.getChildren().get(2).getData();
        String menu4 = this.current.getChildren().get(3).getData();
        String menu5 = this.current.getChildren().get(4).getData();
        String menu6 = this.current.getChildren().get(5).getData();

        // update menu design
        msb.shiftToExact(0).append(resourcepack().texture("cortex_small_player_" + menu1 + "_0", "cortex_small_default_0"), ChatColor.WHITE);
        msb.shiftToExact(0).append(resourcepack().texture("cortex_small_player_" + menu2 + "_1", "cortex_small_default_1"), ChatColor.WHITE);
        msb.shiftToExact(0).append(resourcepack().texture("cortex_small_player_" + menu3 + "_2", "cortex_small_default_2"), ChatColor.WHITE);
        msb.shiftToExact(81).append(resourcepack().texture("cortex_small_player_" + menu4 + "_0", "cortex_small_default_0"), ChatColor.WHITE);
        msb.shiftToExact(81).append(resourcepack().texture("cortex_small_player_" + menu5 + "_1", "cortex_small_default_1"), ChatColor.WHITE);
        msb.shiftToExact(81).append(resourcepack().texture("cortex_small_player_" + menu6 + "_2", "cortex_small_default_2"), ChatColor.WHITE);

        // place clickable items
        Arrays.asList(0, 1, 2, 3, 9, 10, 11, 12)
                .forEach(i -> this.getMenu().setItemAt(i, itemize(menu1)));
        Arrays.asList(18, 19, 20, 21, 27, 28, 29, 30)
                .forEach(i -> this.getMenu().setItemAt(i, itemize(menu2)));
        Arrays.asList(36, 37, 38, 39, 45, 46, 47, 48)
                .forEach(i -> this.getMenu().setItemAt(i, itemize(menu3)));
        Arrays.asList(5, 6, 7, 8, 14, 15, 16, 17)
                .forEach(i -> this.getMenu().setItemAt(i, itemize(menu4)));
        Arrays.asList(23, 24, 25, 26, 32, 33, 34, 35)
                .forEach(i -> this.getMenu().setItemAt(i, itemize(menu5)));
        Arrays.asList(41, 42, 43, 44, 50, 51, 52, 53)
                .forEach(i -> this.getMenu().setItemAt(i, itemize(menu6)));

        this.getMenu().setTitle(msb.compile());
    }
}
