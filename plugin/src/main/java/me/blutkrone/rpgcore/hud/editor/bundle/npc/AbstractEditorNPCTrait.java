package me.blutkrone.rpgcore.hud.editor.bundle.npc;

import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.npc.trait.AbstractCoreTrait;

/**
 * A common interface shared by all NPC traits for workflow.
 */
public abstract class AbstractEditorNPCTrait implements IEditorBundle {

    public AbstractEditorNPCTrait() {
    }

    /**
     * Transform into run-time instance.
     *
     * @return runtime instance.
     */
    public abstract AbstractCoreTrait build();

    /**
     * Retrieve symbol to use in cortex.
     *
     * @return cortex symbol
     */
    public abstract String getCortexSymbol();

    /**
     * Language code describing the itemized trait.
     *
     * @return language code
     */
    public abstract String getIconLC();

    /**
     * A flag which can be acquired as a quest reward, should
     * this be "none" we're always unlocked.
     *
     * @return flag needed to unlock.
     */
    public abstract String getUnlockFlag();
}
