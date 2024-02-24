package me.blutkrone.rpgcore.hud.ux;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.api.hud.IUXComponent;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.hud.UXWorkspace;
import me.blutkrone.rpgcore.minimap.MapMarker;
import me.blutkrone.rpgcore.minimap.v2.MinimapManagerV2;
import me.blutkrone.rpgcore.resourcepack.ResourcepackManager;
import me.blutkrone.rpgcore.resourcepack.generation.component.hud.AbstractTexture;
import me.blutkrone.rpgcore.util.Utility;
import me.blutkrone.rpgcore.util.io.ConfigWrapper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
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
        Location position = bukkit_player.getLocation().clone();
        return new Snapshot(map_marker, position);
    }

    @Override
    public void populate(CorePlayer core_player, Player bukkit_player, UXWorkspace workspace, Snapshot prepared) {
        int size = MinimapManagerV2.MINIMAP_SIZE_BLOCK;
        ResourcepackManager rpm = RPGCore.inst().getResourcepackManager();
        // where to start rendering the component
        int render_point = core_player.getSettings().screen_width - rpm.texture("static_navigator_front_N").width - 10;
        // draw the background of our navigator
        workspace.bossbar().shiftToExact(render_point);
        workspace.bossbar().append(rpm.texture("static_navigator_back"));
        // dispatch request for minimap
        if (prepared.map_anchor != null) {
            boolean[][] layer_below = RPGCore.inst().getMinimapManager().buildMapAround(prepared.map_anchor.clone().add(0, -1, 0));
            boolean[][] layer_equal = RPGCore.inst().getMinimapManager().buildMapAround(prepared.map_anchor);
            boolean[][] layer_above = RPGCore.inst().getMinimapManager().buildMapAround(prepared.map_anchor.clone().add(0, +1, 0));
            if (layer_below != null && layer_equal != null && layer_above != null) {
                for (int i = 0; i < size; i++) {
                    for (int j = 0; j < size; j++) {
                        if (layer_equal[i][j]) {
                            workspace.bossbar().shiftToExact(render_point+minimap_start_at+(j*2)).append(rpm.texture("minimap_pixel_0_" + i));
                        } else if (layer_above[i][j]) {
                            workspace.bossbar().shiftToExact(render_point+minimap_start_at+(j*2)).append(rpm.texture("minimap_pixel_1_" + i));
                        } else if (layer_below[i][j]) {
                            workspace.bossbar().shiftToExact(render_point+minimap_start_at+(j*2)).append(rpm.texture("minimap_pixel_2_" + i));
                        }
                    }
                }
            }
        }

        // overlay above map but below icons
        workspace.bossbar().shiftToExact(render_point);
        workspace.bossbar().append(rpm.texture("static_navigator_overlay"));

        // render the relevant markers on the map
        if (prepared.map_anchor != null) {
            int start_x = prepared.map_anchor.getBlockZ() - (size/2);
            int start_z = prepared.map_anchor.getBlockX() - (size/2);
            int close_x = prepared.map_anchor.getBlockZ() + (size/2);
            int close_z = prepared.map_anchor.getBlockX() + (size/2);
            for (MapMarker marker : prepared.map_marker) {
                AbstractTexture texture = rpm.texture("marker_" + marker.marker + "_0");
                // marker position on an absolute space
                int marker_x = Math.max(start_x, Math.min(close_x, marker.getLocation().getBlockZ()));
                int marker_z = Math.max(start_z, Math.min(close_z, marker.getLocation().getBlockX()));
                // marker position on an relative space
                marker_x = marker_x - start_x;
                marker_z = marker_z - start_z;
                // relative space clamping to not render outside of map
                marker_x = Math.max(texture.height/8, Math.min(marker_x, size - (texture.height/8)));
                marker_z = Math.max(texture.width/8, Math.min(marker_z, size - (texture.width/8)));

                // semi-transparent if Y difference is too much
                if (Math.abs(marker.getLocation().getY() - prepared.map_anchor.getY()) <= 16d) {
                    texture = rpm.texture("marker_" + marker.marker + "_" + marker_x);
                } else {
                    texture = rpm.texture("marker_transparent_" + marker.marker + "_" + marker_x);
                }
                // render marker at appropriate location
                if (texture != null) {
                    workspace.bossbar()
                            .shiftToExact(render_point+minimap_start_at+(marker_z*2))
                            .retreat(texture.width/2)
                            .append(texture);
                } else {
                    Bukkit.getLogger().severe("Missing marker texture for: " + marker.marker);
                }
            }
        }

        // draw the foreground of our navigator
        workspace.bossbar().shiftToExact(render_point);
        // draw a front based on camera rotation
        float yaw = (prepared.position.getYaw() - 180f) % 360f;
        if (yaw < 0) {
            yaw += 360f;
        }
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

    static class Snapshot {
        final List<MapMarker> map_marker;
        final Location position;
        final Location map_anchor;

        Snapshot(List<MapMarker> map_marker, Location position) {
            this.map_marker = map_marker;
            this.position = position;
            Block block = this.position.getBlock();
            for (int i = 0; i < 4 && !block.getRelative(BlockFace.DOWN).getType().isSolid(); i++) {
                block = block.getRelative(BlockFace.DOWN);
            }
            if (block.getRelative(BlockFace.DOWN).getType().isSolid()) {
                this.map_anchor = block.getLocation();
            } else {
                this.map_anchor = null;
            }
        }
    }
}