package me.blutkrone.rpgcore.item.data;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.attribute.AttributeModifier;
import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import me.blutkrone.rpgcore.item.CoreItem;
import me.blutkrone.rpgcore.item.modifier.CoreModifier;
import me.blutkrone.rpgcore.item.ItemManager;
import me.blutkrone.rpgcore.util.collection.WeightedRandomMap;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;

/**
 * Tracks modifier specific information.
 */
public class ItemDataModifier extends AbstractItemData {

    // all modifiers present on the item
    private List<CoreModifier> modifiers;

    public ItemDataModifier(ObjectInputStream buffer) throws IOException {
        super(buffer);

        ItemManager manager = RPGCore.inst().getItemManager();

        // initiate the modifier information
        this.modifiers = new ArrayList<>();

        if (this.getVersion() == 1) {
            // load the modifiers present on the item
            int i = buffer.readInt();
            for (int j = 0; j < i; j++) {
                String id = buffer.readUTF();
                if (manager.getModifierIndex().has(id)) {
                    this.modifiers.add(manager.getModifierIndex().get(id));
                }
            }
        } else {
            throw new UnsupportedOperationException("Unknown data version: " + getVersion());
        }
    }

    public ItemDataModifier(CoreItem item, double quality) throws IOException {
        super(item, quality);

        ItemManager manager = RPGCore.inst().getItemManager();

        // initiate the modifier information
        this.modifiers = new ArrayList<>();

        // track implicit modifiers on the item
        for (String id : item.getImplicits()) {
            if (manager.getModifierIndex().has(id)) {
                this.modifiers.add(manager.getModifierIndex().get(id));
            }
        }

        // roll a set of arbitrary affixes
        if (item.getAffixLevel() != 0) {
            double roll_chance = 0.8 + (0.01 * quality);
            for (int i = 0; i < 10; i++) {
                if (Math.random() <= roll_chance && !augmentWithAffix(item, quality)) {
                    break;
                }
            }
        }
    }

    @Override
    public void save(ObjectOutputStream buffer) throws IOException {
        buffer.writeInt(this.modifiers.size());
        for (CoreModifier modifier : this.modifiers) {
            buffer.writeUTF(modifier.getId());
        }
    }

    /**
     * Augment this item with an arbitrary random affix.
     *
     * @return true if we could augment this item.
     */
    public boolean augmentWithAffix(CoreItem item, double quality) {
        ItemManager manager = RPGCore.inst().getItemManager();

        // ensure we do respect the thresholds
        Map<String, Integer> current_limit_usage = new HashMap<>();
        for (CoreModifier modifier : this.modifiers) {
            for (String tag : modifier.getTags()) {
                current_limit_usage.put(tag, current_limit_usage.getOrDefault(tag, 0) + 1);
            }
        }

        // track all affixes which can be rolled
        WeightedRandomMap<CoreModifier> selector = new WeightedRandomMap<>();
        for (CoreModifier modifier : manager.getModifierIndex().getAll()) {
            // do not allow to select implicit modifiers
            if (modifier.isImplicit()) {
                continue;
            }
            // check if we would exceed any limits
            boolean limit_exceed = false;
            for (String tag : modifier.getTags()) {
                int updated = current_limit_usage.getOrDefault(tag, 0) + 1;
                if (updated > item.getAffixLimit().getOrDefault(tag, 999)) {
                    limit_exceed = true;
                }
            }
            // do not roll this affix, if capped out
            if (limit_exceed) {
                continue;
            }
            // acquire the chance to roll this item
            double weight = 0d;
            for (String tag : modifier.getTags()) {
                weight += item.getAffixWeight().getOrDefault(tag, 0d);
            }
            // do not roll this affix if it has no chance
            if (weight <= 0d) {
                continue;
            }
            // acquire affix as an option
            weight = weight * modifier.getWeight(quality);
            selector.add(weight, modifier);
        }

        // if we got no options, do not augment at all
        if (selector.isEmpty()) {
            return false;
        }

        // augment with a random modifier of our choice
        this.modifiers.add(selector.next());

        // notify about successful augmentation
        return true;
    }

    /**
     * Grants all modifiers from this item to the entity, offering up
     * a list of modifiers.
     *
     * @param entity which entity should receive the modifier.
     * @return the effect under which the item effect was received.
     */
    public List<AttributeModifier> apply(CoreEntity entity) {
        List<AttributeModifier> modifiers = new ArrayList<>();

        for (CoreModifier modifier : this.modifiers) {
            modifiers.addAll(modifier.apply(entity));
        }

        return modifiers;
    }

    /**
     * Retrieve all modifiers present on this item.
     *
     * @return the modifiers present on this item.
     */
    public List<CoreModifier> getModifiers() {
        return modifiers;
    }
}