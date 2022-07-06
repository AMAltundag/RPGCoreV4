package me.blutkrone.rpgcore.resourcepack;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.util.ThrowingRunnable;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class ResourcePackThreadWorker {

    // which tasks are still pending
    private List<Worker> working = new ArrayList<>();

    /**
     * Queue something to be processed by this worker.
     *
     * @param async    true to run on non-primary thread
     * @param runnable the actual worker we got
     */
    public void add(boolean async, ThrowingRunnable runnable) {
        working.add(new Worker(runnable, async));
    }

    /**
     * Kick off the working process.
     */
    public void work() {
        // interrupt if we have nothing left
        if (working.isEmpty()) {
            return;
        }
        // pop one task from our query
        Worker header = working.remove(0);
        // prepare the task worker
        BukkitRunnable slave = new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    // have the worker run the task
                    header.worker.run();
                    // schedule for the next task
                    work();
                } catch (Throwable throwable) {
                    // execution terminated due to error
                    throwable.printStackTrace();
                }
            }
        };
        // execute the task on the relevant thread
        if (header.async) {
            slave.runTaskAsynchronously(RPGCore.inst());
        } else {
            slave.runTask(RPGCore.inst());
        }
    }

    /*
     * A wrapper about a worker job.
     */
    private class Worker {
        final ThrowingRunnable worker;
        final boolean async;

        private Worker(ThrowingRunnable worker, boolean async) {
            this.worker = worker;
            this.async = async;
        }
    }
}
