package me.blutkrone.rpgcore.hud.ux;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.api.hud.IUXComponent;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.hud.UXWorkspace;
import me.blutkrone.rpgcore.minimap.MapMarker;
import me.blutkrone.rpgcore.minimap.MapRegion;
import me.blutkrone.rpgcore.resourcepack.ResourcePackManager;
import me.blutkrone.rpgcore.util.Utility;
import me.blutkrone.rpgcore.util.io.ConfigWrapper;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NavigatorComponent implements IUXComponent<NavigatorComponent.Snapshot> {
    // black list of minimap arrangement
    private static final Set<Integer> MAP_CIRCLE_TRIM = new HashSet<>(Arrays.asList(
            -40, -39, -31, -33, -32, -23, 23, 32, 33, 39, 31, 40
    ));
    // starting indices for the map
    private int minimap_start_at;
    private int position_center;

    public NavigatorComponent(ConfigWrapper section) {
        // offset parameters
        minimap_start_at = section.getInt("interface-offset.minimap-start-at");
        position_center = section.getInt("interface-offset.minimap-position");
    }

    @Override
    public int getPriority() {
        return 5;
    }

    @Override
    public Snapshot prepare(CorePlayer core_player, Player bukkit_player) {
        List<MapMarker> map_marker = RPGCore.inst().getMinimapManager().getMarkersOf(bukkit_player, core_player);
        MapRegion map_region = RPGCore.inst().getMinimapManager().getRegionOf(bukkit_player);
        Location position = bukkit_player.getLocation().clone();
        return new Snapshot(map_marker, map_region, position);
    }

    @Override
    public void populate(CorePlayer core_player, Player bukkit_player, UXWorkspace workspace, Snapshot prepared) {
        ResourcePackManager rpm = RPGCore.inst().getResourcePackManager();
        // where to start rendering the component
        int render_point = core_player.getSettings().screen_width - rpm.texture("static_navigator_front_N").width - 10;
        // draw the background of our navigator
        workspace.bossbar().shiftToExact(render_point);
        workspace.bossbar().append(rpm.texture("static_navigator_back"));

        // draw the minimap slices on screen
        if (prepared.map_region != null && prepared.map_region.contains(prepared.position)
                && rpm.hasParameter("map-" + prepared.map_region.map + "-width")) {
            // identify the middle piece
            double relX = prepared.position.getBlockX() - prepared.map_region.x1;
            double lenX = prepared.map_region.x2 - prepared.map_region.x1;
            double relZ = prepared.position.getBlockZ() - prepared.map_region.z1;
            double lenZ = prepared.map_region.z2 - prepared.map_region.z1;
            double imgW = rpm.parameter("map-" + prepared.map_region.map + "-width").doubleValue();
            double imgH = rpm.parameter("map-" + prepared.map_region.map + "-height").doubleValue();
            int tileX = (int) (((int) Math.ceil(imgW / 8)) * (relX / lenX));
            int tileY = (int) (((int) Math.ceil(imgH / 8)) * (relZ / lenZ));

            // put together the minimap
            for (int i = -4; i <= +4; i++) {
                workspace.bossbar().shiftToExact(render_point + minimap_start_at);
                for (int j = -4; j <= +4; j++) {
                    try {
                        if (NavigatorComponent.MAP_CIRCLE_TRIM.contains(i * 9 + j)) {
                            workspace.bossbar().advance(8);
                        } else {
                            workspace.bossbar().append(rpm.texture(String.format("map_%s_%s_%s_%s", prepared.map_region.map, tileX + j, tileY + i, (i + 4))));
                        }
                    } catch (Exception e) {
                        workspace.bossbar().advance(8);
                    }
                }
            }
        }

        // draw the foreground of our navigator
        workspace.bossbar().shiftToExact(render_point);
        // draw a front based on camera rotation
        float yaw = (prepared.position.getYaw() - 180f) % 360f;
        if (yaw < 0) yaw += 360f;
        if (0 <= yaw && yaw < 22.5) {
            workspace.bossbar().append(rpm.texture("static_navigator_front_N"));
        } else if (22.5 <= yaw && yaw < 67.5) {
            workspace.bossbar().append(rpm.texture("static_navigator_front_NE"));
        } else if (67.5 <= yaw && yaw < 112.5) {
            workspace.bossbar().append(rpm.texture("static_navigator_front_E"));
        } else if (112.5 <= yaw && yaw < 157.5) {
            workspace.bossbar().append(rpm.texture("static_navigator_front_SE"));
        } else if (157.5 <= yaw && yaw < 202.5) {
            workspace.bossbar().append(rpm.texture("static_navigator_front_S"));
        } else if (202.5 <= yaw && yaw < 247.5) {
            workspace.bossbar().append(rpm.texture("static_navigator_front_SW"));
        } else if (247.5 <= yaw && yaw < 292.5) {
            workspace.bossbar().append(rpm.texture("static_navigator_front_W"));
        } else if (292.5 <= yaw && yaw < 337.5) {
            workspace.bossbar().append(rpm.texture("static_navigator_front_NW"));
        } else if (337.5 <= yaw && yaw < 360.0) {
            workspace.bossbar().append(rpm.texture("static_navigator_front_N"));
        } else {
            workspace.bossbar().append(rpm.texture("static_navigator_front_N"));
        }

        // text info about our current position
        String location = String.format("x:%s y:%s z:%s", prepared.position.getBlockX(), prepared.position.getBlockY(), prepared.position.getBlockZ());
        workspace.bossbar().shiftCentered(render_point + position_center, Utility.measure(location));
        workspace.bossbar().append(location, "hud_navigator_text");
    }

    class Snapshot {
        final List<MapMarker> map_marker;
        final MapRegion map_region;
        final Location position;

        Snapshot(List<MapMarker> map_marker, MapRegion map_region, Location position) {
            this.map_marker = map_marker;
            this.map_region = map_region;
            this.position = position;
        }
    }
}

