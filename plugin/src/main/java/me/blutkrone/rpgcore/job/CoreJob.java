package me.blutkrone.rpgcore.job;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.editor.root.job.EditorJob;
import me.blutkrone.rpgcore.nms.api.menu.IChestMenu;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A job granting a passive tree to a player.
 */
public class CoreJob {
    private final String id;
    // if the job is a default choice
    private boolean defaults;
    // itemized job emblem for roster menu
    private ItemStack emblem;
    // itemized primary class weapon
    private ItemStack weapon;
    // relevant passive trees we are unlocking
    private List<String> passive_tree;
    // jobs we can advance into
    private List<String> advancements;

    public CoreJob(String id, EditorJob editor) {
        this.id = id;
        this.defaults = editor.defaults;
        this.emblem = RPGCore.inst().getLanguageManager()
                .getAsItem(editor.lc_emblem).build();
        this.weapon = RPGCore.inst().getLanguageManager()
                .getAsItem(editor.lc_weapon)
                .persist("job-id", getId()).build();
        IChestMenu.setBrand(this.emblem, RPGCore.inst(), "job-id", getId());
        IChestMenu.setBrand(this.weapon, RPGCore.inst(), "job-id", getId());
        this.passive_tree = editor.passive_tree.stream()
                .map(String::toLowerCase)
                .filter(string -> !string.startsWith("skill_"))
                .collect(Collectors.toList());
        this.advancements = new ArrayList<>(editor.advanced_jobs);
    }

    /**
     * Jobs which we can advance into.
     *
     * @return jobs to advance into.
     */
    public List<CoreJob> getAdvancements() {
        List<CoreJob> jobs = new ArrayList<>();
        for (String advancement : this.advancements) {
            jobs.add(RPGCore.inst().getJobManager().getIndexJob().get(advancement));
        }
        return jobs;
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

    /**
     * The passive trees which are unlocked by the job.
     *
     * @return the relevant passive trees.
     */
    public List<String> getPassiveTree() {
        return passive_tree;
    }
}
