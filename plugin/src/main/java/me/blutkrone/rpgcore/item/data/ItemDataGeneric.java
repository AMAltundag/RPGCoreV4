package me.blutkrone.rpgcore.item.data;

import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.item.CoreItem;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.UUID;

/**
 * Tracks miscellaneous information.
 */
public class ItemDataGeneric extends AbstractItemData {

    // the uuid to represent the item is not bound
    private static final UUID EMPTY_UUID = new UUID(0, 0);

    // who the item may be bound toward
    private String bound_name;
    private UUID bound_uuid;
    // identifier to prevent stacking
    private UUID do_not_stack;

    public ItemDataGeneric(ObjectInputStream buffer) throws IOException {
        super(buffer);

        if (getVersion() == 1) {
            this.bound_name = buffer.readUTF();
            this.bound_uuid = new UUID(buffer.readLong(), buffer.readLong());
            this.do_not_stack = new UUID(buffer.readLong(), buffer.readLong());
        } else {
            throw new UnsupportedOperationException("Unknown data version: " + getVersion());
        }
    }

    public ItemDataGeneric(CoreItem item, double quality, CorePlayer player) throws IOException {
        super(item, quality);

        // remove the capacity to stack items.
        this.do_not_stack = item.isUnstackable() ? UUID.randomUUID() : ItemDataGeneric.EMPTY_UUID;
        // apply a binding if the item wants it.
        this.bindTo(item.isDropBound() ? player : null);
    }

    @Override
    public void save(ObjectOutputStream buffer) throws IOException {
        buffer.writeUTF(this.bound_name);
        buffer.writeLong(this.bound_uuid.getMostSignificantBits());
        buffer.writeLong(this.bound_uuid.getLeastSignificantBits());
        buffer.writeLong(this.do_not_stack.getMostSignificantBits());
        buffer.writeLong(this.do_not_stack.getLeastSignificantBits());
    }

    /**
     * Bind this item to the given player.
     *
     * @param player who to bind the item to (null to unbind.)
     */
    public void bindTo(Player player) {
        if (player == null) {
            this.bound_uuid = EMPTY_UUID;
            this.bound_name = "";
        } else {
            this.bound_uuid = player.getUniqueId();
            this.bound_name = player.getName();
        }
    }

    /**
     * Bind this item to the given player.
     *
     * @param player who to bind the item to (null to unbind.)
     */
    public void bindTo(CorePlayer player) {
        bindTo(player == null ? null : player.getEntity());
    }

    /**
     * Check if this item is bound to a user.
     *
     * @return true if the item is bound.
     */
    public boolean isBound() {
        return !bound_uuid.equals(EMPTY_UUID);
    }

    /**
     * Should the item be bound, this method ensures that the
     * player actually meets the criterion to use it.
     *
     * @param who who to check with.
     * @return true if we can use the item.
     */
    public boolean canUseBound(CorePlayer who) {
        return !isBound() || bound_uuid.equals(who.getUniqueId());
    }

    /**
     * Should the item be bound, this method ensures that the
     * player actually meets the criterion to use it.
     *
     * @param who who to check with.
     * @return true if we can use the item.
     */
    public boolean canUseBound(Player who) {
        return !isBound() || bound_uuid.equals(who.getUniqueId());
    }

    /**
     * The name of who the item is bound to, empty if it isn't bound
     * to anyone.
     *
     * @return who the item is bound to.
     */
    public String getBoundTo() {
        return this.bound_name;
    }
}
