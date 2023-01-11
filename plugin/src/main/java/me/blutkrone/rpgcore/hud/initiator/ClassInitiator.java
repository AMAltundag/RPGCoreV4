package me.blutkrone.rpgcore.hud.initiator;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.api.roster.IRosterInitiator;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.hud.editor.index.IndexAttachment;
import me.blutkrone.rpgcore.job.CoreJob;
import me.blutkrone.rpgcore.menu.JobInitiationMenu;
import me.blutkrone.rpgcore.resourcepack.ResourcePackManager;
import me.blutkrone.rpgcore.util.io.ConfigWrapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ClassInitiator implements IRosterInitiator {

    public IndexAttachment<CoreJob, List<CoreJob>> attachment_starter_jobs
            = RPGCore.inst().getJobManager().getIndexJob().createFiltered(CoreJob::isDefaults);

    public ClassInitiator(ConfigWrapper config) {
    }

    @Override
    public int priority() {
        return 3;
    }

    @Override
    public boolean initiate(CorePlayer player) {
        ResourcePackManager rpm = RPGCore.inst().getResourcePackManager();

        // only offer this if we got no jobs listed
        if (player.getJob() != null)
            return false;

        // shuffle job list to avoid favourites
        List<CoreJob> options = new ArrayList<>(attachment_starter_jobs.get());
        Collections.shuffle(options);

        // process single-choice options
        if (options.size() == 0) {
            return false;
        } else if (options.size() == 1) {
            player.setJob(options.get(0).getId());
            return false;
        }

        // offer up menu to pick jobs from
        new JobInitiationMenu(options).finish(player.getEntity());

        return true;
    }
}
