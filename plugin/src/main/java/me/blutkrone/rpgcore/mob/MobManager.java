package me.blutkrone.rpgcore.mob;

import me.blutkrone.rpgcore.hud.editor.index.EditorIndex;
import me.blutkrone.rpgcore.hud.editor.root.mob.EditorCreature;

/**
 * Manage mobs capable of engaging in combat, including basic
 * attacks and access to skill logic.
 */
public class MobManager {
    private EditorIndex<CoreCreature, EditorCreature> index;

    public MobManager() {
        this.index = new EditorIndex<>("mob", EditorCreature.class, EditorCreature::new);
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
