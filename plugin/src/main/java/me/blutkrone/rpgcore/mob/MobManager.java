package me.blutkrone.rpgcore.mob;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.entity.entities.CoreMob;
import me.blutkrone.rpgcore.hud.editor.index.EditorIndex;
import me.blutkrone.rpgcore.hud.editor.root.mob.EditorCreature;
import org.bukkit.Bukkit;

/**
 * Manage mobs capable of engaging in combat, including basic
 * attacks and access to skill logic.
 */
public class MobManager {
    private EditorIndex<CoreCreature, EditorCreature> index;

    public MobManager() {
        this.index = new EditorIndex<>("mob", EditorCreature.class, EditorCreature::new);

        Bukkit.getScheduler().runTaskTimer(RPGCore.inst(), () -> {
            // every 10 minutes scan for corrupted entities
            RPGCore.inst().getEntityManager().getHandleUnsafe().entrySet().removeIf(entry -> {
                if (entry.getValue() instanceof CoreMob) {
                    return entry.getValue().getEntity() == null;
                } else {
                    return false;
                }
            });
        }, 20, 6000);
    }

    /**
     * The index tracking templates for mobs.
     *
     * @return index for templates for mob
     */
    public EditorIndex<CoreCreature, EditorCreature> getIndex() {
        return index;
    }
}
