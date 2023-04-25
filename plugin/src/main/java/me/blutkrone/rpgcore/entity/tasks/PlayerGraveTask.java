package me.blutkrone.rpgcore.entity.tasks;

import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerGraveTask extends BukkitRunnable {

    private final CorePlayer player;

    public PlayerGraveTask(CorePlayer player) {
        this.player = player;
    }

    @Override
    public void run() {
        this.player.processGraveTimer();
    }
}
