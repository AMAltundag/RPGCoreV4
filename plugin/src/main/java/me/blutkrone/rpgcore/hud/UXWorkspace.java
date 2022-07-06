package me.blutkrone.rpgcore.hud;

import me.blutkrone.rpgcore.util.fontmagic.MagicStringBuilder;

public class UXWorkspace {
    // actionbar specific content
    private final MagicStringBuilder actionbar;
    // bossbar specific content
    private final MagicStringBuilder bossbar;

    public UXWorkspace() {
        this.actionbar = new MagicStringBuilder();
        this.bossbar = new MagicStringBuilder();
    }

    /**
     * Contents drawn to the actionbar.
     *
     * @return string builder for the actionbar.
     */
    public MagicStringBuilder actionbar() {
        return actionbar;
    }

    /**
     * Contents drawn to the bossbar.
     *
     * @return string builder for the bossbar.
     */
    public MagicStringBuilder bossbar() {
        return bossbar;
    }
}
