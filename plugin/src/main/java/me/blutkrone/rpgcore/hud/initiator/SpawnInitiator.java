package me.blutkrone.rpgcore.hud.initiator;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.api.roster.IRosterInitiator;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.util.io.ConfigWrapper;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

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
        if (player.getRespawnPosition() == null) {
            int i = RPGCore.inst().getRandom().nextInt(this.spawnpoints.size());
            Location where = this.spawnpoints.get(i).getWhere();
            player.setRespawnPosition(where);
            player.setLoginPosition(where);
            player.getEntity().sendMessage("§cA random spawn-location was allocated (menu still WIP)");
        }

        return false;
    }

    /**
     * All spawnpoints known.
     *
     * @return
     */
    public List<Spawnpoint> getSpawnpoints() {
        return spawnpoints;
    }

    public class Spawnpoint {
        private Supplier<Location> where;
        private Location where_cached;
        private ItemStack icon;

        Spawnpoint(ConfigWrapper config) {
            this.where = config.getLazyLocation("position");
            this.icon = RPGCore.inst().getLanguageManager()
                    .getAsItem(config.getString("description"))
                    .build();
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