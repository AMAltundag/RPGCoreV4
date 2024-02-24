package me.blutkrone.rpgcore.menu;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.editor.instruction.InstructionBuilder;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.minimap.v2.MapInfo;
import me.blutkrone.rpgcore.nms.api.menu.IChestMenu;
import me.blutkrone.rpgcore.npc.trait.impl.CoreTravelTrait;
import me.blutkrone.rpgcore.resourcepack.ResourcepackManager;
import me.blutkrone.rpgcore.util.fontmagic.MagicStringBuilder;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NavigationMenu {

    public static class TravelCartography extends Cartography {

        private final CoreTravelTrait trait;

        public TravelCartography(MapInfo minimap, CoreTravelTrait trait) {
            super(minimap);
            this.trait = trait;
        }

        @Override
        public void onClickExact(Location where) {
            // with a tick delay, move to target location
            Bukkit.getScheduler().runTask(RPGCore.inst(), () -> {
                getMenu().getViewer().closeInventory();
                getMenu().getViewer().teleport(where);
            });
        }
    }

    public static class SpawnCartography extends Cartography {

        public SpawnCartography(MapInfo minimap) {
            super(minimap);
        }

        @Override
        public void onClickExact(Location where) {
            CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(getMenu().getViewer());
            core_player.setRespawnPosition(where);
            core_player.setLoginPosition(where);

            Bukkit.getScheduler().runTask(RPGCore.inst(), () -> {
                getMenu().getViewer().closeInventory();
            });
        }
    }

    public static class Cartography extends AbstractCoreMenu {

        // a history of maps rendered
        List<MapInfo> history = new ArrayList<>();
        // icon to use previous map
        ItemStack back_to_before;

        public Cartography(MapInfo minimap) {
            super(6);
            // establish history with just this element
            this.history.add(minimap);
            // control elements
            this.back_to_before = RPGCore.inst().getLanguageManager().getAsItem("back_to_last").build();
        }

        public Cartography(List<MapInfo> minimap) {
            super(6);
            // establish history with just this element
            this.history.addAll(minimap);
            // control elements
            this.back_to_before = RPGCore.inst().getLanguageManager().getAsItem("back_to_last").build();
        }

        /**
         * Called when interacting with an action that doesn't link
         * with another map and has a location defined.
         *
         * @param where What location we interacted with.
         */
        public void onClickExact(Location where) {

        }

        /**
         * Itemize a map action, the action will either open another map
         * or it will invoke the click method with the listed coordinate.
         *
         * @param action The action to itemize
         * @return Itemized action
         */
        public ItemStack itemize(MapInfo.MapAction action) {
            ItemStack icon = language().getAsItem("invisible").build();
            icon = language().getAsItem(action.tooltip).inheritIcon(icon).build();
            if (!action.next_map.equals("")) {
                // link to next map
                IChestMenu.setBrand(icon, RPGCore.inst(), "minimap", action.next_map);
            } else {
                // link to a location
                try {
                    Location location = action.location.get();
                    String location_as_string = location.getWorld().getUID()
                            + " " + location.getX()
                            + " " + location.getY()
                            + " " + location.getZ()
                            + " " + location.getPitch()
                            + " " + location.getYaw();
                    IChestMenu.setBrand(icon, RPGCore.inst(), "minimap", location_as_string);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            return icon;
        }

        @Override
        public void rebuild() {
            ResourcepackManager rpm = RPGCore.inst().getResourcepackManager();
            getMenu().clearItems();

            // grab the current map we work on
            MapInfo active_map = this.history.get(history.size() - 1);

            // base texture
            MagicStringBuilder msb = new MagicStringBuilder();
            msb.shiftToExact(-208);
            msb.append(resourcepack().texture("menu_" + active_map.menu), ChatColor.WHITE);

            // show the icons for the map
            for (MapInfo.MapAction action : active_map.actions) {
                ItemStack icon = itemize(action);
                for (Integer slot : action.slots) {
                    getMenu().setItemAt(slot, icon);
                }
            }

            // append actions to sidebar
            getMenu().setItemAt(53, this.back_to_before);

            InstructionBuilder instructions = new InstructionBuilder();
            instructions.add(RPGCore.inst().getLanguageManager().getTranslationList("instruction_player_map"));
            instructions.apply(msb);

            getMenu().setTitle(msb.compile());
        }

        @Override
        public void click(InventoryClickEvent event) {
            event.setCancelled(true);

            if (event.getClickedInventory() == event.getView().getTopInventory()) {
                if (event.getClick() == ClickType.LEFT && event.getCurrentItem() != null) {
                    if (this.back_to_before.isSimilar(event.getCurrentItem())) {
                        if (this.history.size() != 1) {
                            this.history.remove(this.history.size() - 1);
                        }

                        this.getMenu().queryRebuild();
                    } else {
                        String minimap = IChestMenu.getBrand(event.getCurrentItem(), RPGCore.inst(), "minimap", null);
                        if (minimap != null) {
                            MapInfo next_map_to_show = RPGCore.inst().getMinimapManager().getMapInfo(minimap);
                            if (next_map_to_show != null) {
                                this.history.add(next_map_to_show);
                                this.getMenu().queryRebuild();
                            } else {
                                try {
                                    String[] split = minimap.split(" ");
                                    World world = Bukkit.getWorld(UUID.fromString(split[0]));
                                    double x = Double.parseDouble(split[1]);
                                    double y = Double.parseDouble(split[2]);
                                    double z = Double.parseDouble(split[3]);
                                    float pitch = Float.parseFloat(split[4]);
                                    float yaw = Float.parseFloat(split[5]);

                                    this.onClickExact(new Location(world, x, y, z, yaw, pitch));
                                } catch (Exception ignored) {
                                    // ignored
                                }
                            }
                        }
                    }
                }

            }
        }

        @Override
        public boolean isTrivial() {
            return true;
        }
    }
}
