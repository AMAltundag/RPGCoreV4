package me.blutkrone.rpgcore.job;

import me.blutkrone.rpgcore.hud.editor.index.EditorIndex;
import me.blutkrone.rpgcore.hud.editor.root.job.EditorJob;

public class JobManager {

    // jobs registered to the core
    private EditorIndex<CoreJob, EditorJob> job_index;

    public JobManager() {
        this.job_index = new EditorIndex<>("job", EditorJob.class, EditorJob::new);
    }

    /**
     * The index which tracks jobs
     *
     * @return job index
     */
    public EditorIndex<CoreJob, EditorJob> getIndex() {
        return job_index;
    }
}
