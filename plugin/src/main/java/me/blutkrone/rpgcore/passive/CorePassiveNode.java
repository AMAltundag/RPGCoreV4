package me.blutkrone.rpgcore.passive;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.hud.editor.bundle.passive.AbstractEditorPassive;
import me.blutkrone.rpgcore.hud.editor.root.passive.EditorPassiveNode;
import me.blutkrone.rpgcore.item.CoreItem;
import me.blutkrone.rpgcore.passive.node.AbstractCorePassive;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class CorePassiveNode {

    private String id;
    private String lc_allocated;
    private String lc_unallocated;
    private AbstractCorePassive effect;

    public CorePassiveNode(String id, EditorPassiveNode passive) {
        this.id = id;
        this.lc_allocated = passive.lc_allocated;
        this.lc_unallocated = passive.lc_unallocated;
        for (IEditorBundle effect : passive.effects) {
            this.effect = ((AbstractEditorPassive) effect).build();
        }
    }

    private CorePassiveNode() {

    }

    /**
     * How the node is expected to affect an entity.
     *
     * @return the effect of the node, may be null.
     */
    public Optional<AbstractCorePassive> getEffect() {
        return Optional.ofNullable(this.effect);
    }

    /**
     * Unique identifier of the node.
     *
     * @return unique identifier.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Icon shown if the node is allocated.
     *
     * @return icon while allocated.
     */
    public ItemStack getAllocatedIcon() {
        return RPGCore.inst().getLanguageManager().getAsItem(this.lc_allocated).build();
    }

    /**
     * Icon shown if the node is not allocated.
     *
     * @return icon while un-allocated.
     */
    public ItemStack getUnallocated() {
        return RPGCore.inst().getLanguageManager().getAsItem(this.lc_unallocated).build();
    }

    public boolean isSocket() {
        return this.effect != null && this.effect.isSocket();
    }

    public boolean canSocket(CoreItem item) {
        return this.effect != null && this.effect.canSocket(item);
    }

    /**
     * Special case, intended to connect nodes.
     */
    public static class Path extends CorePassiveNode {

        Path() {
        }

        @Override
        public String getId() {
            throw new UnsupportedOperationException("Cannot do this with a path node!");
        }

        @Override
        public ItemStack getAllocatedIcon() {
            throw new UnsupportedOperationException("Cannot do this with a path node!");
        }

        @Override
        public ItemStack getUnallocated() {
            throw new UnsupportedOperationException("Cannot do this with a path node!");
        }
    }
}
