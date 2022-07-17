package me.blutkrone.rpgcore.effect;

import me.blutkrone.rpgcore.hud.editor.index.EditorIndex;
import me.blutkrone.rpgcore.hud.editor.root.other.EditorEffect;

public class EffectManager {
    private EditorIndex<CoreEffect, EditorEffect> effect_index;

    public EffectManager() {
        this.effect_index = new EditorIndex<>("effect", EditorEffect.class, EditorEffect::new);
    }

    /**
     * An index which holds every single managed effect on the server.
     *
     * @return the effect index.
     */
    public EditorIndex<CoreEffect, EditorEffect> getIndex() {
        return effect_index;
    }
}
