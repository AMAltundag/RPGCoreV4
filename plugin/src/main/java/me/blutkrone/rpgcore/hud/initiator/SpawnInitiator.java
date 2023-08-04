package me.blutkrone.rpgcore.hud.initiator;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.api.roster.IRosterInitiator;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.menu.NavigationMenu;
import me.blutkrone.rpgcore.minimap.v2.MapInfo;
import me.blutkrone.rpgcore.util.io.ConfigWrapper;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.function.Supplier;

public class SpawnInitiator implements IRosterInitiator {

    // just apply a spawnpoint directly
    private Supplier<Location> fixed_spawnpoint = null;
    // allow user to pick a spawnpoint
    private String dynamic_spawnpoint = null;

    public SpawnInitiator(ConfigWrapper config) {
        if (config.isString("spawnpoint")) {
            this.dynamic_spawnpoint = config.getString("spawnpoint");
        } else {
            this.fixed_spawnpoint = config.getLazyLocation("spawnpoint");
        }
    }

    @Override
    public int priority() {
        return 2;
    }

    @Override
    public boolean initiate(CorePlayer player) {
        // only pick spawnpoint if none was picked
        if (player.getRespawnPosition() != null) {
            // // prevent getting stuck after logging off during character creation
            // if (player.getLoginPosition() != null) {
            //     double distSq = Utility.distanceSqOrWorld(player.getLoginPosition(), RPGCore.inst().getDataManager().getPreLoginPosition());
            //     if (distSq <= 1d) {
            //         player.setLoginPosition(player.getRespawnPosition());
            //     }
            // }
            return false;
        }

        // check for a minimap directly
        if (this.dynamic_spawnpoint != null) {
            MapInfo minimap = RPGCore.inst().getMinimapManager().getMapInfo(this.dynamic_spawnpoint);
            if (minimap != null) {
                // allow player to pick their spawnpoint
                Bukkit.getScheduler().runTask(RPGCore.inst(), () -> {
                    try {
                        NavigationMenu.SpawnCartography cartography = new NavigationMenu.SpawnCartography(minimap);
                        cartography.finish(player.getEntity());
                    } catch (Exception ex) {
                        player.getEntity().kickPlayer("§cYou've been kicked by: §fRPGCore\n\n§cReferenced Illegal MiniMap");
                    }
                });
            } else {
                // linked minimap does not exist
                Bukkit.getScheduler().runTask(RPGCore.inst(), () -> {
                    player.getEntity().kickPlayer("§cYou've been kicked by: §fRPGCore\n\n§cIllegal Config: 'spawnpoint'");
                });
            }
        } else {
            try {
                Location location = this.fixed_spawnpoint.get();
                if (location.getWorld() != null) {
                    // apply a fixed spawnpoint
                    player.setRespawnPosition(location);
                    player.setLoginPosition(location);
                } else {
                    // spawnpoint doesn't exist
                    Bukkit.getScheduler().runTask(RPGCore.inst(), () -> {
                        player.getEntity().kickPlayer("§cYou've been kicked by: §fRPGCore\n\n§cIllegal Config: 'spawnpoint'");
                    });
                }
            } catch (Exception e) {
                // spawnpoint doesn't exist
                Bukkit.getScheduler().runTask(RPGCore.inst(), () -> {
                    player.getEntity().kickPlayer("§cYou've been kicked by: §fRPGCore\n\n§cIllegal Config: 'spawnpoint'");
                });
            }
        }

        return true;
    }
}