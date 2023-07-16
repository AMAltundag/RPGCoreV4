package me.blutkrone.rpgcore.menu;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.editor.instruction.InstructionBuilder;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.minimap.MapMarker;
import me.blutkrone.rpgcore.minimap.MapRegion;
import me.blutkrone.rpgcore.nms.api.menu.IChestMenu;
import me.blutkrone.rpgcore.npc.trait.impl.CoreTravelTrait;
import me.blutkrone.rpgcore.resourcepack.ResourcePackManager;
import me.blutkrone.rpgcore.util.ItemBuilder;
import me.blutkrone.rpgcore.util.fontmagic.MagicStringBuilder;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NavigationMenu {

    private static Map<Pos, Pos> IMAGE_TO_SLOT = new HashMap<>();
    private static Map<Pos, List<Pos>> SLOT_TO_IMAGE = new HashMap<>();

    static {
        // map textures to slots
        int[] indexed = {0, 0, 1, 1, 2, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6, 6, 7, 7, 8, 8};

        for (int i = 0; i < 20; i++) {
            IMAGE_TO_SLOT.put(new Pos(i, 3), new Pos(indexed[i], 1));
            IMAGE_TO_SLOT.put(new Pos(i, 4), new Pos(indexed[i], 1));
            IMAGE_TO_SLOT.put(new Pos(i, 5), new Pos(indexed[i], 2));
            IMAGE_TO_SLOT.put(new Pos(i, 6), new Pos(indexed[i], 2));
            IMAGE_TO_SLOT.put(new Pos(i, 7), new Pos(indexed[i], 3));
            IMAGE_TO_SLOT.put(new Pos(i, 8), new Pos(indexed[i], 3));
            IMAGE_TO_SLOT.put(new Pos(i, 9), new Pos(indexed[i], 3));
            IMAGE_TO_SLOT.put(new Pos(i, 10), new Pos(indexed[i], 4));
            IMAGE_TO_SLOT.put(new Pos(i, 11), new Pos(indexed[i], 4));
            IMAGE_TO_SLOT.put(new Pos(i, 12), new Pos(indexed[i], 5));
            IMAGE_TO_SLOT.put(new Pos(i, 13), new Pos(indexed[i], 5));
        }
        // map slots to textures
        IMAGE_TO_SLOT.forEach((slot, image) -> {
            SLOT_TO_IMAGE.computeIfAbsent(image, (k -> new ArrayList<>())).add(slot);
        });
    }

    public static class TravelCartography extends Cartography {

        private final CoreTravelTrait trait;

        public TravelCartography(MapRegion region, Location position, CoreTravelTrait trait) {
            super(region, position);
            this.trait = trait;
        }

        @Override
        public ItemStack itemize(MapMarker marker) {
            CorePlayer player = RPGCore.inst().getEntityManager().getPlayer(getMenu().getViewer());
            int cost = 0, have = 0;

            Location location = marker.getLocation();
            if (this.trait.getCurrency() != null) {
                cost = (int) (trait.getMultiplier() * this.getMenu().getViewer().getLocation().distance(location));
                have = player.getBankedItems().getOrDefault(trait.getCurrency(), 0);
            }

            ItemBuilder builder = language().getAsItem(marker.marker,
                    trait.getCurrency() == null ? "" : cost + language().getTranslation("travel_currency_" + trait.getCurrency()),
                    String.format("%s/%s/%s", location.getBlockX(), location.getBlockY(), location.getBlockZ()),
                    cost <= have ? ChatColor.GREEN : ChatColor.RED,
                    cost <= have ? ChatColor.WHITE : ChatColor.GRAY);
            builder.inheritIcon(RPGCore.inst().getLanguageManager().getAsItem("invisible").build());
            return builder.build();
        }

        @Override
        public void onClickMarker(MapMarker marker) {
            CorePlayer player = RPGCore.inst().getEntityManager().getPlayer(getMenu().getViewer());

            int cost = 0, have = 0;
            if (this.trait.getCurrency() != null) {
                cost = (int) (trait.getMultiplier() * this.getMenu().getViewer().getLocation().distance(marker.getLocation()));
                have = player.getBankedItems().getOrDefault(trait.getCurrency(), 0);
            }

            // cannot travel if we cannot afford it.
            if (cost > have) {
                String message = RPGCore.inst().getLanguageManager().getTranslation("cannot_afford_travel");
                getMenu().getViewer().sendMessage(message);
                return;
            }

            // consume the cost of travelling
            if (cost > 0) {
                player.getBankedItems().merge(trait.getCurrency(), cost, (a, b) -> a - b);
            }

            // with a tick delay, move to target location
            Bukkit.getScheduler().runTask(RPGCore.inst(), () -> {
                getMenu().getViewer().closeInventory();
                getMenu().getViewer().teleport(marker.getLocation());
            });
        }
    }

    public static class SpawnCartography extends Cartography {

        public SpawnCartography(Location position) {
            super(position);
        }

        @Override
        public void onClickMarker(MapMarker marker) {
            CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(getMenu().getViewer());
            core_player.setRespawnPosition(marker.getLocation());
            core_player.setLoginPosition(marker.getLocation());

            Bukkit.getScheduler().runTask(RPGCore.inst(), () -> {
                getMenu().getViewer().closeInventory();
            });
        }
    }

    public static class Cartography extends AbstractCoreMenu {

        // parameters about the map viewport
        MapRegion region;
        int tileX, limitX;
        int tileY, limitY;
        // maps chest slot to a map offset
        Map<Pos, List<Pos>> slot_to_tile = new HashMap<>();
        // cache for markers
        List<MapMarker> markers = new ArrayList<>();
        // offset on the marker list
        int marker_offset = 0;
        // control elements
        ItemStack page_back;
        ItemStack page_next;

        public Cartography(MapRegion region, Location position) {
            super(6);
            ResourcePackManager rpm = RPGCore.inst().getResourcePackManager();

            // ensure there is a position to inspect
            this.region = region;
            if (this.region == null) {
                throw new IllegalArgumentException("Position contains no minimap!");
            }
            // ensure that there is a matching map
            if (!rpm.hasParameter("map-" + this.region.map + "-width")) {
                throw new IllegalArgumentException("Map '" + this.region.map + " does not exist!");
            }
            // compute tile placement within relative space
            double relX = position.getBlockX() - this.region.x1;
            double lenX = this.region.x2 - this.region.x1;
            double relZ = position.getBlockZ() - this.region.z1;
            double lenZ = this.region.z2 - this.region.z1;
            double imgW = rpm.parameter("map-" + this.region.map + "-width").doubleValue();
            double imgH = rpm.parameter("map-" + this.region.map + "-height").doubleValue();
            // track the anchor tile we are using
            this.tileX = (int) (((int) Math.ceil(imgW / 8)) * (relX / lenX));
            this.tileY = (int) (((int) Math.ceil(imgH / 8)) * (relZ / lenZ));
            this.limitX = ((int) Math.ceil(imgW / 8)) - 10;
            this.limitY = ((int) Math.ceil(imgH / 8)) - 5;
            // control elements
            this.page_back = RPGCore.inst().getLanguageManager().getAsItem("viewport_left").build();
            this.page_next = RPGCore.inst().getLanguageManager().getAsItem("viewport_right").build();
        }

        public Cartography(Location position) {
            this(RPGCore.inst().getMinimapManager().getRegionOf(position), position);
        }

        /**
         * Register a marker on this map, which will be
         * selectable from the map.
         *
         * @param marker the marker to be added.
         */
        public void addMarker(MapMarker marker) {
            this.markers.add(marker);
        }

        /**
         * Called when interacting with a marker on the map, marker
         * interactions only detect shift-left clicks.
         *
         * @param marker the marker we've provided
         */
        public void onClickMarker(MapMarker marker) {

        }

        /**
         * Itemize a marker at the given position.
         *
         * @param marker LC we are transforming
         * @return output item
         */
        public ItemStack itemize(MapMarker marker) {
            ItemBuilder builder = RPGCore.inst().getLanguageManager().getAsItem(marker.marker);
            return builder.build();
        }

        /**
         * Places the focus on the marker, this may change
         * the region.
         *
         * @param marker the marker we're focusing on.
         */
        public void focusOnMarker(MapMarker marker) {
            ResourcePackManager rpm = RPGCore.inst().getResourcePackManager();

            // ensure there is a region for us
            Location position = marker.getLocation();
            MapRegion region = RPGCore.inst().getMinimapManager().getRegionOf(position);
            if (region == null) {
                String message = RPGCore.inst().getLanguageManager().getTranslation("marker_without_region");
                getMenu().getViewer().sendMessage(message);
                return;
            }
            // ensure that there is a matching map
            if (!rpm.hasParameter("map-" + region.map + "-width")) {
                return;
            }

            // adjust the map to the relevant region
            this.region = region;
            double relX = position.getBlockX() - this.region.x1;
            double lenX = this.region.x2 - this.region.x1;
            double relZ = position.getBlockZ() - this.region.z1;
            double lenZ = this.region.z2 - this.region.z1;
            double imgW = rpm.parameter("map-" + this.region.map + "-width").doubleValue();
            double imgH = rpm.parameter("map-" + this.region.map + "-height").doubleValue();
            // track the anchor tile we are using
            this.tileX = (int) (((int) Math.ceil(imgW / 8)) * (relX / lenX));
            this.tileY = (int) (((int) Math.ceil(imgH / 8)) * (relZ / lenZ));
            this.limitX = ((int) Math.ceil(imgW / 8)) - 10;
            this.limitY = ((int) Math.ceil(imgH / 8)) - 5;
        }

        @Override
        public void rebuild() {
            ResourcePackManager rpm = RPGCore.inst().getResourcePackManager();
            getMenu().clearItems();
            slot_to_tile.clear();

            // clamp into area constraint
            tileX = Math.max(10, Math.min(limitX, tileX));
            tileY = Math.max(5, Math.min(limitY, tileY));

            // base texture
            MagicStringBuilder msb = new MagicStringBuilder();
            msb.shiftToExact(-208);
            msb.append(resourcepack().texture("menu_cartography"), ChatColor.WHITE);

            // put together the minimap
            for (int i = 0; i < 11; i++) {
                msb.shiftToExact(0);
                for (int j = 0; j < 20; j++) {
                    // ensure the texture actually exists
                    String texture = String.format("map_%s_%s_%s_%s", region.map, tileX + j - 10, tileY + i - 5, i + 3);
                    if (rpm.textures().containsKey(texture)) {
                        // render an 8x8 slice of the map
                        msb.append(rpm.texture(texture), ChatColor.WHITE);
                        // ensure there is a position to match
                        Pos position_in_chest = IMAGE_TO_SLOT.get(new Pos(j, i + 3));
                        if (position_in_chest == null) {
                            continue;
                        }
                        // allow to trace back to the exact slot we've got
                        slot_to_tile.computeIfAbsent(position_in_chest, (k -> new ArrayList<>()))
                                .add(new Pos(tileX + j - 10, tileY + i - 5));
                    } else {
                        // map tile is out of bounds
                        msb.advance(8);
                    }
                }
            }

            // scroller for links to the hotbar
            if (this.markers.size() <= 9) {
                for (int i = 0; i < this.markers.size(); i++) {
                    ItemStack item = itemize(this.markers.get(i));
                    IChestMenu.setBrand(item, RPGCore.inst(), "shortcut", String.valueOf(this.marker_offset + i));
                    getMenu().setItemAt(i, item);
                }
            } else {
                getMenu().setItemAt(0, this.page_back);
                getMenu().setItemAt(8, this.page_next);
                for (int i = 0; i < 7; i++) {
                    try {
                        ItemStack item = itemize(this.markers.get(this.marker_offset + i));
                        IChestMenu.setBrand(item, RPGCore.inst(), "shortcut", String.valueOf(this.marker_offset + i));
                        getMenu().setItemAt(1 + i, item);
                    } catch (Exception ex) {
                        // ignored
                    }
                }
            }

            // identify marker specific locations
            for (int k = 0; k < this.markers.size(); k++) {
                MapMarker marker = this.markers.get(k);
                // ensure there is a position to inspect
                MapRegion marker_region = RPGCore.inst().getMinimapManager().getRegionOf(marker.getLocation());
                if (marker_region == this.region && rpm.hasParameter("map-" + marker_region.map + "-width")) {
                    // compute tile placement within relative space
                    double relX = marker.getLocation().getBlockX() - marker_region.x1;
                    double lenX = marker_region.x2 - marker_region.x1;
                    double relZ = marker.getLocation().getBlockZ() - marker_region.z1;
                    double lenZ = marker_region.z2 - marker_region.z1;
                    double imgW = rpm.parameter("map-" + marker_region.map + "-width").doubleValue();
                    double imgH = rpm.parameter("map-" + marker_region.map + "-height").doubleValue();
                    // track the anchor tile we are using
                    int markerX = (int) (((int) Math.ceil(imgW / 8)) * (relX / lenX));
                    int markerY = (int) (((int) Math.ceil(imgH / 8)) * (relZ / lenZ));
                    // check if marker is within viewport and highlight it
                    if (markerX >= (tileX - 10) && markerX <= (tileX + 19 - 5)) {
                        if (markerY >= (tileY - 5) && markerY <= (tileY + 10 - 5)) {
                            int dX = markerX - tileX + 10;
                            int dY = markerY - tileY + 5 + 3;

                            for (int i = -2; i <= +2; i++) {
                                for (int j = -2; j <= +2; j++) {
                                    Pos where = IMAGE_TO_SLOT.get(new Pos(dX + i, dY + j));
                                    if (where != null) {
                                        // check if we can add a marker 0to this slot
                                        if (!getMenu().getItemAt((where.y * 9) + where.x).getType().isAir()) {
                                            continue;
                                        }
                                        // place an itemized marker on the location
                                        ItemStack item = itemize(marker);
                                        ItemBuilder.of(item).inheritIcon(RPGCore.inst().getLanguageManager().getAsItem("invisible").build()).build();
                                        IChestMenu.setBrand(item, RPGCore.inst(), "marker", String.valueOf(k));
                                        getMenu().setItemAt((where.y * 9) + where.x, item);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            InstructionBuilder instructions = new InstructionBuilder();
            instructions.add(RPGCore.inst().getLanguageManager().getTranslationList("instruction_player_map"));
            instructions.apply(msb);

            getMenu().setTitle(msb.compile());
        }

        @Override
        public void click(InventoryClickEvent event) {
            event.setCancelled(true);

            if (event.getClickedInventory() == event.getView().getTopInventory()) {
                if (event.getClick() == ClickType.LEFT) {
                    // check against shortcut icons
                    if (this.page_next.isSimilar(event.getCurrentItem())) {
                        this.marker_offset = Math.max(0, this.marker_offset - 1);
                    } else if (this.page_back.isSimilar(event.getCurrentItem())) {
                        this.marker_offset = Math.min(this.markers.size() - 7, this.marker_offset + 1);
                    } else {
                        String id = IChestMenu.getBrand(event.getCurrentItem(), RPGCore.inst(), "shortcut", "");
                        if (!id.isEmpty()) {
                            focusOnMarker(this.markers.get(Integer.parseInt(id)));
                        } else {
                            // shift to the given position
                            List<Pos> updated = this.slot_to_tile.get(new Pos(event.getSlot() % 9, event.getSlot() / 9));
                            if (updated != null && !updated.contains(new Pos(this.tileX, this.tileY))) {
                                Pos next = updated.iterator().next();
                                this.tileX = next.x;
                                this.tileY = next.y;
                            }
                        }
                    }
                } else if (event.getClick() == ClickType.SHIFT_LEFT) {
                    // check against a marker position
                    String id = IChestMenu.getBrand(event.getCurrentItem(), RPGCore.inst(), "marker", "");
                    if (!id.isEmpty()) {
                        MapMarker marker = this.markers.get(Integer.parseInt(id));
                        if (marker != null) {
                            onClickMarker(marker);
                        }
                    }
                }

                Bukkit.getScheduler().runTask(RPGCore.inst(), this::rebuild);
            }
        }

        @Override
        public boolean isTrivial() {
            return true;
        }
    }

    static class Pos {
        private final int x;
        private final int y;

        Pos(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public int hashCode() {
            return x * 32 + y;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Pos
                    && ((Pos) obj).x == x
                    && ((Pos) obj).y == y;
        }
    }
}
