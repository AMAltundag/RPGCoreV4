package me.blutkrone.rpgcore.node.activity;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.api.activity.IActivity;
import me.blutkrone.rpgcore.node.impl.CoreNodeCollectible;

/**
 * This activity doesn't do anything by itself, it is merely
 * meant to observe the collection of a node.
 */
public class ObserveCollectionActivity implements IActivity {

    // which node are we observing.
    private CoreNodeCollectible.CollectibleNodeData watching;
    // how many ticks to observe
    private int timer;

    public ObserveCollectionActivity(CoreNodeCollectible.CollectibleNodeData watching) {
        this.watching = watching;
        this.timer = 40;
    }

    /**
     * Update the focus of this activity.
     *
     * @param watching the activity.
     */
    public void focus(CoreNodeCollectible.CollectibleNodeData watching) {
        this.watching = watching;
        this.timer = 40;
    }

    @Override
    public boolean update() {
        return timer-- <= 0 || !this.watching.isAvailable();
    }

    @Override
    public double getProgress() {
        // apply a pseudo decay effect to hint at faster hitting
        double d = this.watching.getProgress();
        return Math.max(0d, d - (0.2d * (1d - (0d + this.timer / 40d))));
    }

    @Override
    public String getInfoText() {
        String translation = RPGCore.inst().getLanguageManager().getTranslation(this.watching.getMessage());
        return translation.replace("{PROGRESS}", String.format("%s%%", (int) (100 * getProgress())));
    }

    @Override
    public void interrupt() {

    }
}
