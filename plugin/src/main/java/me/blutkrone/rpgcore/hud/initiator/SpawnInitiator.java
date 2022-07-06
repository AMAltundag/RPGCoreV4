package me.blutkrone.rpgcore.hud.initiator;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.api.roster.IRosterInitiator;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.util.io.ConfigWrapper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

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
            Bukkit.getConsoleSender().sendMessage("§aSETTING SPAWNPOINT");
            int i = RPGCore.inst().getRandom().nextInt(this.spawnpoints.size());
            Location where = this.spawnpoints.get(i).where;
            player.setRespawnPosition(where);
            player.setLoginPosition(where);
            player.getEntity().sendMessage("§cA random spawn-location was allocated (menu still WIP)");
        }

        return false;
    }

    class Spawnpoint {
        private final Location where;
        private final ItemStack icon;

        Spawnpoint(ConfigWrapper config) {
            this.where = config.getLocation("position");
            this.icon = RPGCore.inst().getLanguageManager()
                    .getAsItem(config.getString("description"))
                    .build();
        }
    }
}