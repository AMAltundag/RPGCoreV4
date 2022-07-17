package me.blutkrone.rpgcore.item.data;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.item.CoreItem;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * A base class shared by all item data which will be
 * written into items by RPGCore.
 */
public abstract class AbstractItemData {

    // the version which this data is written
    protected int version;
    // the item which we are associated with
    protected CoreItem item;

    /**
     * Constructor which is used when reading existing data.
     *
     * @param buffer data buffer for the item.
     */
    AbstractItemData(ObjectInputStream buffer) throws IOException {
        this.version = buffer.readInt();
        this.item = RPGCore.inst().getItemManager().getItemIndex().get(buffer.readUTF());

        if (this.version == 0) {
            throw new UnsupportedOperationException("Data version '0' may not exist!");
        }
    }

    /**
     * A base constructor which identifies which version the
     * data was created for.
     *
     * @param item    which item creates the data.
     * @param quality the quality the item was instanced with.
     * @param version
     */
    AbstractItemData(CoreItem item, double quality, int version) throws IOException {
        this.version = version;
        this.item = item;
    }

    /**
     * Save the data of the item.
     *
     * @param buffer where to dump the data into.
     */
    public abstract void save(ObjectOutputStream buffer) throws IOException;

    /**
     * Write the current state of this item.
     *
     * @param item which item we want to write into.
     */
    public void save(ItemStack item) {
        // air cannot have any core data
        if (item == null || item.getType().isAir()) {
            return;
        }
        // meta is required for core data
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }
        // override the data on the item
        PersistentDataContainer container = meta.getPersistentDataContainer();
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            try {
                oos.writeInt(this.getVersion());
                oos.writeUTF(this.getItem().getId());
                this.save(oos);
            } catch (IOException e) {
                e.printStackTrace();
            }
            oos.close();
            container.set(new NamespacedKey(RPGCore.inst(), name()), PersistentDataType.BYTE_ARRAY, baos.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
        // save the changes we've done
        item.setItemMeta(meta);
    }

    /**
     * The version of this data base, should the version be
     * zero it means that it was freshly built.
     *
     * @return the version of this data base.
     */
    public int getVersion() {
        return version;
    }

    /**
     * Which item template this data instance was created for.
     *
     * @return the item which backs this up.
     */
    public CoreItem getItem() {
        return item;
    }

    /**
     * A unique identifier for this type of data.
     *
     * @return a unique identifier for this data.
     */
    public final String name() {
        return "rpgcore_" + getClass().getSimpleName().toLowerCase();
    }
}
