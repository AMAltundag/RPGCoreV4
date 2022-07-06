package me.blutkrone.rpgcore.item.data;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.item.CoreItem;
import me.blutkrone.rpgcore.item.ItemManager;
import me.blutkrone.rpgcore.item.modifier.CoreModifier;
import me.blutkrone.rpgcore.item.modifier.ModifierType;
import me.blutkrone.rpgcore.util.io.BukkitSerialization;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tracks jewel related information
 */
public class ItemDataJewel extends AbstractItemData {

    // base64 encoded jewels that are socketed
    private Map<Integer, String> jewel_items;
    // attributes granted by all jewels together
    private Map<String, Double> jewel_attributes;
    // which sockets are unlocked on the item
    private int unlocked;

    public ItemDataJewel(ObjectInputStream buffer) throws IOException {
        super(buffer);

        if (getVersion() == 1) {
            this.jewel_items = new HashMap<>();
            this.jewel_attributes = new HashMap<>();

            int counter = buffer.readInt();
            for (int i = 0; i < counter; i++) {
                int position = buffer.readInt();
                String itemB64 = buffer.readUTF();
                this.jewel_items.put(position, itemB64);
            }

            counter = buffer.readInt();
            for (int i = 0; i < counter; i++) {
                String attribute = buffer.readUTF();
                double factor = buffer.readDouble();
                this.jewel_attributes.put(attribute, factor);
            }

            this.unlocked = buffer.readInt();
        } else {
            throw new UnsupportedOperationException("Unknown data version: " + getVersion());
        }
    }

    public ItemDataJewel(CoreItem item, double quality) throws IOException {
        super(item, quality);

        this.jewel_attributes = new HashMap<>();
        this.jewel_items = new HashMap<>();
        this.unlocked = 0;
        if (item.getJewelMaximum() != 0) {
            // one jewel socket is always available
            this.unlocked += 1;
            // roll a random number of jewel sockets
            double chance = 0.2d + (1d - (1d/(1d+Math.sqrt(1d+quality))));
            for (int i = 1; i < item.getJewelMaximum(); i++) {
                if (Math.random() <= chance) {
                    this.unlocked += 1;
                } else {
                    break;
                }
            }
        }
    }

    @Override
    public void save(ObjectOutputStream buffer) throws IOException {
        buffer.writeInt(this.jewel_items.size());
        for (Map.Entry<Integer, String> entry : this.jewel_items.entrySet()) {
            buffer.writeInt(entry.getKey());
            buffer.writeUTF(entry.getValue());
        }

        buffer.writeInt(this.jewel_attributes.size());
        for (Map.Entry<String, Double> entry : this.jewel_attributes.entrySet()) {
            buffer.writeUTF(entry.getKey());
            buffer.writeDouble(entry.getValue());
        }

        buffer.writeInt(this.unlocked);
    }

    /**
     * Recompute the attributes present on the respective jewels.
     */
    public void recompute() {
        // recompute the attribute cache.
        ItemManager manager = RPGCore.inst().getItemManager();
        Map<String, Double> snapshot = new HashMap<>();
        for (ItemStack jewel : getItems().values()) {
            ItemDataModifier modifiers = manager.getItemData(jewel, ItemDataModifier.class);
            for (CoreModifier modifier : modifiers.getModifiers()) {
                if (modifier.getType() == ModifierType.ITEM) {
                    modifier.getAttributeEffects().forEach((attribute, factor) -> {
                        snapshot.merge(attribute, factor, (a,b) -> a+b);
                    });
                }
            }
        }
        this.jewel_attributes = snapshot;
    }

    /**
     * Check the maximum number of jewel sockets this item
     * can have.
     *
     * @return maximum number of sockets.
     */
    public int getMaximumSockets() {
        return this.item.getJewelMaximum();
    }

    /**
     * Check how many jewel sockets are occupied.
     *
     * @return sockets which are occupied.
     */
    public int getUsedSockets() {
        return this.jewel_items.size();
    }

    /**
     * Check how many sockets are currently unlocked.
     *
     * @return how many sockets are available.
     */
    public int getAvailableSockets() {
        return this.unlocked;
    }

    /**
     * Randomize the number of unlocked jewel sockets, this
     * will fail silently if anything is socketed on it.
     *
     * @param quality higher quality makes it easier to unlock sockets.
     */
    public void reforge(double quality) {
        if (this.jewel_items.isEmpty() && item.getJewelMaximum() != 0) {
            // one jewel socket is always available
            this.unlocked += 1;
            // roll a random number of jewel sockets
            double chance = 0.2d + (1d - (1d/(1d+Math.sqrt(1d+quality))));
            for (int i = 1; i < this.item.getJewelMaximum(); i++) {
                if (Math.random() <= chance) {
                    this.unlocked += 1;
                } else {
                    break;
                }
            }
        }
    }

    /**
     * Check if the given can be socketed upon this one.
     *
     * @param item the item to be socketed
     * @return true if we can be socketed
     */
    public boolean check(ItemStack item) {
        ItemManager manager = RPGCore.inst().getItemManager();
        // make sure that the item can actually be socketed
        if (item == null || item.getType().isAir()) {
            return false;
        }
        // which equipment slots work with the sockets
        List<String> compatible = this.item.getJewelTypes();
        // grab valid data to compare against
        ItemDataJewel sacrifice_data = manager.getItemData(item, ItemDataJewel.class);
        if (sacrifice_data == null) {
            return false;
        }
        CoreItem sacrifice_item = sacrifice_data.getItem();
        if (sacrifice_item == null) {
            return false;
        }
        // ensure we meet count requirement
        if (getUsedSockets() < sacrifice_data.getNeededJewelCount()) {
            return false;
        }
        // ensure that the item can actually be socketed
        for (String tag : compatible) {
            if (sacrifice_item.getEquipmentSlot().contains(tag)) {
                return true;
            }
        }
        // if nothing matches, we cannot be socketed
        return false;
    }

    /**
     * Add the given item to our sockets.
     *
     * @param slot which slot to socket item on.
     * @param item the item we wish to socket.
     */
    public void socket(int slot, ItemStack item) {
        // acquire the socketed item.
        this.jewel_items.put(slot, BukkitSerialization.toBase64(item));
    }

    /**
     * Attempt to shatter every jewel on the item, multiple jewels
     * shattering becomes less likely to happen.
     *
     * @return the jewel which was shattered, null if no jewel is socketed.
     */
    public Map<Integer, ItemStack> shatter(double chance) {
        // identify what is to shatter
        Map<Integer, ItemStack> shattered = new HashMap<>();
        this.jewel_items.forEach((pos, b64) -> {
            // check if we actually will shatter
            if (Math.random() > (chance / (1d + shattered.size()))) {
                return;
            }
            // track the item which has shattered
            try {
                shattered.put(pos, BukkitSerialization.fromBase64(b64)[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // remove everything that has shattered
        shattered.forEach((pos, item) -> this.jewel_items.remove(pos));

        // if anything shattered recompute things
        if (!shattered.isEmpty()) {
            recompute();
        }

        // offer up every jewel that shattered
        return shattered;
    }

    /**
     * Retrieve the jewels which were socketed, this is an expensive
     * operation since it runs the b64 decoding whenever called.
     *
     * @return a list of socketed items.
     */
    public Map<Integer, ItemStack> getItems() {
        Map<Integer, ItemStack> serialized = new HashMap<>();
        this.jewel_items.forEach((position, b64) -> {
            try {
                serialized.put(position, BukkitSerialization.fromBase64(b64)[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        return serialized;
    }

    /**
     * The attributes which are present on this item.
     *
     * @return the modifiers which are present.
     */
    public Map<String, Double> getAttributes() {
        return new HashMap<>(this.jewel_attributes);
    }

    /**
     * Check how many jewels have to be previously socketed.
     *
     * @return jewel count necessary to socket this item.
     */
    public int getNeededJewelCount() {
        return this.getItem().getJewelPrevious();
    }

    /**
     * Chance to shatter other jewels when we are socketed.
     *
     * @return chance to shatter.
     */
    public double getChanceToShatterOther() {
        return this.getItem().getJewelShatter();
    }
}
