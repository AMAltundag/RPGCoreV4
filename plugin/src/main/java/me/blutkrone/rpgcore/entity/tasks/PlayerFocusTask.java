package me.blutkrone.rpgcore.entity.tasks;

import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.entity.focus.FocusType;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerFocusTask extends BukkitRunnable {

    private final CorePlayer player;

    public PlayerFocusTask(CorePlayer player) {
        this.player = player;
    }

    @Override
    public void run() {
        // validate our current tracking information
        Location location = this.player.getLocation();
        this.player.getFocusTracker().validate(location);
        // update our looking-at focus
        CoreEntity target = this.player.rayCastTarget(16d);
        this.player.getFocusTracker().update(FocusType.LOOKING, target);
    }
}
