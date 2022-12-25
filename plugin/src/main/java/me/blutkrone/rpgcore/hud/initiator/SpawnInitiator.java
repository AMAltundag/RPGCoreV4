package me.blutkrone.rpgcore.hud.initiator;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.api.roster.IRosterInitiator;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.menu.NavigationMenu;
import me.blutkrone.rpgcore.minimap.MapMarker;
import me.blutkrone.rpgcore.util.Utility;
import me.blutkrone.rpgcore.util.io.ConfigWrapper;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class SpawnInitiator implements IRosterInitiator {

    private List<Spawnpoint> spawnpoints = new ArrayList<>();

    public SpawnInitiator(ConfigWrapper config) {
        config.forEachUnder("spawn-camp-choices", (path, root) -> {
            spawnpoints.add(new Spawnpoint(root.getSection(path)));
        });
    }

    @Override
    public int priority() {
        return 2;
    }

    @Override
    public boolean initiate(CorePlayer player) {
        // check if a spawnpoint selection is mandatory
        if (player.getRespawnPosition() != null) {
            // prevent getting stuck after logging off during character creation
            if (player.getLoginPosition() != null) {
                double distSq = Utility.distanceSqOrWorld(player.getLoginPosition(), RPGCore.inst().getDataManager().getPreLoginPosition());
                if (distSq <= 1d) {
                    player.setLoginPosition(player.getRespawnPosition());
                }
            }

            return false;
        }
        // ensure that the spawn-camp locations are valid
        this.spawnpoints.removeIf(point -> {
            Location where = point.getWhere();
            return where == null || where.getWorld() == null;
        });

        if (this.spawnpoints.isEmpty()) {
            // require at least 1 spawnpoint
            Bukkit.getScheduler().runTask(RPGCore.inst(), () -> {
                player.getEntity().kickPlayer("§cYou've been kicked by: §fRPGCore\n\n§cIllegal Config: 'spawn-camp-choices'");
            });
        } else if (this.spawnpoints.size() == 1) {
            // with 1 spawnpoint we can auto-set spawns
            Location where = this.spawnpoints.iterator().next().getWhere();
            player.setRespawnPosition(where);
            player.setLoginPosition(where);
        } else {
            // otherwise we can pick from the menu
            Bukkit.getScheduler().runTask(RPGCore.inst(), () -> {
                try {
                    NavigationMenu.SpawnCartography cartography = new NavigationMenu.SpawnCartography(player.getLocation());
                    for (SpawnInitiator.Spawnpoint spawnpoint : this.spawnpoints) {
                        cartography.addMarker(new MapMarker(spawnpoint.getWhere(), spawnpoint.getDescriptionLC(), 0d));
                    }
                    cartography.finish(player.getEntity());
                } catch (Exception ex) {
                    player.getEntity().kickPlayer("§cYou've been kicked by: §fRPGCore\n\n§cReferenced Illegal MiniMap");
                }
            });
        }

        return true;
    }

    public class Spawnpoint {
        private Supplier<Location> where;
        private Location where_cached;
        private String description;

        Spawnpoint(ConfigWrapper config) {
            this.where = config.getLazyLocation("position");
            this.description = config.getString("description");
        }

        public String getDescriptionLC() {
            return description;
        }

        public Location getWhere() {
            if (this.where_cached == null) {
                this.where_cached = this.where.get();
                this.where = null;
            }

            return this.where_cached;
        }
    }
}