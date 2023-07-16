package me.blutkrone.rpgcore.job;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.editor.index.EditorIndex;
import me.blutkrone.rpgcore.editor.root.job.EditorJob;
import me.blutkrone.rpgcore.editor.root.job.EditorProfession;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.menu.JobAdvancementMenu;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;

public class JobManager {

    // jobs registered to the core
    private EditorIndex<CoreJob, EditorJob> job_index;
    private EditorIndex<CoreProfession, EditorProfession> profession_index;

    public JobManager() {
        this.job_index = new EditorIndex<>("job", EditorJob.class, EditorJob::new);
        this.profession_index = new EditorIndex<>("profession", EditorProfession.class, EditorProfession::new);

        Bukkit.getScheduler().runTaskTimer(RPGCore.inst(), () -> {
            // check for players who need to pick their advanced job
            for (Player player : Bukkit.getOnlinePlayers()) {
                // ensure we can show a menu
                if (player.getOpenInventory().getType() != InventoryType.CRAFTING) {
                    continue;
                }
                // ensure player is logged on and initiated
                CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(player);
                if (core_player == null || !core_player.isInitiated()) {
                    continue;
                }
                // ensure we want to advance
                if (core_player.checkForTag("job_advancement_waiting")) {
                    continue;
                }
                // ensure that we have advancement options
                CoreJob job = core_player.getJob();
                if (job == null || job.getAdvancements().isEmpty()) {
                    continue;
                }
                // show advancement menu
                new JobAdvancementMenu().finish(player);
            }
        }, 20, 20);
    }

    /**
     * The index which tracks jobs
     *
     * @return job index
     */
    public EditorIndex<CoreJob, EditorJob> getIndexJob() {
        return this.job_index;
    }

    /**
     * The index which tracks professions
     *
     * @return profession index
     */
    public EditorIndex<CoreProfession, EditorProfession> getIndexProfession() {
        return this.profession_index;
    }
}
