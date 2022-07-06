package me.blutkrone.rpgcore.effect;

import me.blutkrone.rpgcore.hud.editor.EditorIndex;
import me.blutkrone.rpgcore.hud.editor.EditorMenu;
import me.blutkrone.rpgcore.hud.editor.root.EditorEffect;

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
