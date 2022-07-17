package me.blutkrone.rpgcore.job;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.hud.editor.root.job.EditorJob;
import org.bukkit.inventory.ItemStack;

/**
 * A job used to scale player power.
 */
public class CoreJob {
    private final String id;
    // if the job is a default choice
    private boolean defaults;
    // itemized job emblem for roster menu
    private ItemStack emblem;
    // itemized primary class weapon
    private ItemStack weapon;

    public CoreJob(String id, EditorJob editor) {
        this.id = id;
        this.defaults = editor.defaults;
        this.emblem = RPGCore.inst().getLanguageManager()
                .getAsItem(editor.lc_emblem)
                .persist("job-id", getId()).build();
        this.weapon = RPGCore.inst().getLanguageManager()
                .getAsItem(editor.lc_weapon)
                .persist("job-id", getId()).build();
    }

    /**
     * Check if the job is a default choice during creation of a
     * new character from the roster menu.
     *
     * @return true if we are a default choice.
     */
    public boolean isDefaults() {
        return defaults;
    }

    /**
     * The ID of this job, this is an immutable value which cannot
     * be modified by the editor.
     *
     * @return unique job identifier.
     */
    public final String getId() {
        return id;
    }

    /**
     * Icon used by the roster to demonstrate the identity while
     * picking a class for a newly created character.
     *
     * @return emblem icon for roster creation menu.
     */
    public ItemStack getEmblemIcon() {
        return emblem;
    }

    /**
     * Icon used by the roster to demonstrate the weapon while
     * picking a class for a newly created character.
     *
     * @return weapon icon for roster creation menu.
     */
    public ItemStack getWeaponIcon() {
        return weapon;
    }
}
