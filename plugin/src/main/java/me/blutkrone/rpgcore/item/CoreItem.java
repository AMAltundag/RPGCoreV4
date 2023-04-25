package me.blutkrone.rpgcore.item;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.hud.editor.bundle.item.EditorAffixChance;
import me.blutkrone.rpgcore.hud.editor.bundle.item.EditorAffixLimit;
import me.blutkrone.rpgcore.hud.editor.root.item.EditorItem;
import me.blutkrone.rpgcore.item.data.ItemDataDurability;
import me.blutkrone.rpgcore.item.data.ItemDataGeneric;
import me.blutkrone.rpgcore.item.data.ItemDataJewel;
import me.blutkrone.rpgcore.item.data.ItemDataModifier;
import me.blutkrone.rpgcore.item.styling.IDescriptorReference;
import me.blutkrone.rpgcore.item.type.ItemType;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class CoreItem {
    private final String id;

    private ItemStack template;
    private String lc_text;
    private String lc_info;

    private ItemType item_type;
    private List<String> tags;

    private int maximum_durability;
    private int repair_grade;

    private List<String> implicits;

    private int affix_level;
    private Map<String, Integer> affix_limit;
    private Map<String, Double> affix_weight;

    private boolean drop_bound;
    private boolean do_not_stack;

    private List<String> equipment_slot;

    private int jewel_maximum;
    private List<String> jewel_accept;

    private int item_level;

    private String styling;

    private String jewel_icon;
    private double jewel_shatter;
    private int jewel_previous;

    private String animation_shatter;

    private String bank_group;
    private int bank_quantity;
    private String bank_symbol;

    private List<String> weapon_scaling_attribute;

    private Map<String, String> hidden_data;

    /**
     * A container for information on how the given item
     * is engaging with the server.
     */
    public CoreItem(String id, EditorItem editor) {
        this.id = id;
        ItemBuilder builder = ItemBuilder.of(editor.material);
        if (editor.model_data != 0) {
            builder.model((int) editor.model_data);
        }
        builder.color(Integer.parseInt(editor.color_data, 16));
        this.template = builder.build();
        this.lc_text = editor.lc_text;
        this.lc_info = editor.lc_info;
        this.item_type = editor.item_type;
        this.tags = new ArrayList<>(editor.tags);
        this.tags.add("direct_" + id);
        this.tags.replaceAll(String::toLowerCase);
        this.weapon_scaling_attribute = editor.weapon_scaling_attribute.stream().distinct().collect(Collectors.toList());
        this.maximum_durability = (int) editor.durability;
        this.repair_grade = (int) editor.repair_grade;
        this.implicits = new ArrayList<>(editor.implicits);
        this.affix_level = (int) editor.affix_level;
        this.affix_limit = new HashMap<>();
        for (EditorAffixLimit limit : editor.affix_limit) {
            this.affix_limit.put(limit.tag, (int) limit.limit);
        }
        this.affix_weight = new HashMap<>();
        for (EditorAffixChance chance : editor.affix_chance) {
            double cumulated = this.affix_weight.getOrDefault(chance.tag, 0d);
            this.affix_weight.put(chance.tag, cumulated + chance.weight);
        }
        this.drop_bound = editor.drop_bound;
        this.do_not_stack = editor.do_not_stack;
        this.equipment_slot = new ArrayList<>(editor.equipment_slot);
        this.jewel_accept = new ArrayList<>(editor.jewel_accept);
        this.jewel_maximum = (int) editor.jewel_maximum;
        this.item_level = (int) editor.item_level;
        this.styling = editor.lore_style.toLowerCase();
        this.jewel_icon = editor.jewel_icon;
        this.jewel_shatter = editor.jewel_shatter;
        this.jewel_previous = editor.jewel_previous;
        this.animation_shatter = editor.animation_shatter;
        this.bank_group = editor.bank_group;
        this.bank_quantity = Math.max(1, (int) editor.bank_quantity);
        this.bank_symbol = editor.bank_symbol;
        this.hidden_data = new HashMap<>(editor.hidden_data);
    }

    /**
     * Attributes that scale damage if we are used as the weapon when
     * dealing weapon damage.
     *
     * @return attributes to scale weapon damage
     */
    public List<String> getWeaponScalingAttribute() {
        return weapon_scaling_attribute;
    }

    /**
     * Check hidden internal data, this may be null.
     *
     * @param id tag to search for.
     * @return value associated with tag.
     */
    public Optional<String> getHidden(String id) {
        return Optional.ofNullable(this.hidden_data.get(id.toLowerCase()));
    }

    /**
     * A distinct identifier of the item.
     *
     * @return identifier of this item.
     */
    public String getId() {
        return id;
    }

    /**
     * Acquire the given item, with the given player being intended
     * as the recipient of it.
     *
     * @param player  who will acquire the item
     * @param quality the quality affects random modifiers
     * @return the item to acquire
     */
    public ItemStack acquire(IDescriptorReference player, double quality) {
        ItemManager manager = RPGCore.inst().getItemManager();

        // create a copy of our base template
        ItemStack item = this.template.clone();

        // initiate the relevant data compounds
        try {
            if (player instanceof CorePlayer) {
                new ItemDataGeneric(this, quality, (CorePlayer) player).save(item);
            } else {
                new ItemDataGeneric(this, quality, null).save(item);
            }
            new ItemDataModifier(this, quality).save(item);
            new ItemDataDurability(this, quality).save(item);
            new ItemDataJewel(this, quality).save(item);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // generate appropriate lore for the item
        manager.describe(item, player);

        // offer up the item that was generated
        return item;
    }

    /**
     * Acquire the given item, with the given player being intended
     * as the recipient of it.
     *
     * @param player  who will acquire the item
     * @param quality the quality affects random modifiers
     * @return the item to acquire
     */
    public ItemStack bounded(CorePlayer player, double quality) {
        ItemManager manager = RPGCore.inst().getItemManager();

        // create a copy of our base template
        ItemStack item = this.template.clone();

        // initiate the relevant data compounds
        try {
            ItemDataGeneric data = new ItemDataGeneric(this, quality, player);
            data.bindTo(player);
            data.save(item);
            new ItemDataModifier(this, quality).save(item);
            new ItemDataDurability(this, quality).save(item);
            new ItemDataJewel(this, quality).save(item);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // generate appropriate lore for the item
        manager.describe(item, player);

        // offer up the item that was generated
        return item;
    }

    /**
     * Retrieve a pseudo-item which is intended to be
     * an "unidentified" item. An unidentified item can
     * be treated as a token.
     *
     * @return the unidentified item.
     */
    public ItemStack unidentified() {
        ItemManager manager = RPGCore.inst().getItemManager();

        // create a copy of our base template
        ItemStack item = this.template.clone();
        // ensure we got a metadata
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            throw new NullPointerException("meta cannot be null!");
        }
        // mark item with the id that it identifies into
        PersistentDataContainer data = meta.getPersistentDataContainer();
        NamespacedKey keying = new NamespacedKey(RPGCore.inst(), "core-unidentified");
        data.set(keying, PersistentDataType.STRING, this.getId());
        item.setItemMeta(meta);
        // describe item to use
        manager.describe(item, null);

        return item;
    }

    /**
     * Template design of the item.
     *
     * @return Template item design.
     */
    public ItemStack getTemplate() {
        return template;
    }

    /**
     * Language config for the item description.
     *
     * @return language identifier
     */
    public String getLCText() {
        return lc_text;
    }

    /**
     * Language config for additional item info.
     *
     * @return language identifier
     */
    public String getLCInfo() {
        return lc_info;
    }

    /**
     * Grants special properties to the item.
     *
     * @return additional logic to attach to item.
     */
    public ItemType getItemType() {
        return item_type;
    }

    /**
     * Grants custom durability, which is consumed thorough usage
     * of abilities, taking damage and dying.
     *
     * @return base durability of item.
     */
    public int getMaximumDurability() {
        return maximum_durability;
    }

    /**
     * Limits what material grade can repair this item.
     *
     * @return minimum grade of repair material needed
     */
    public int getRepairGrade() {
        return repair_grade;
    }

    /**
     * Identifier for modifiers which are offered to any copy
     * of this item.
     *
     * @return implicit modifier list.
     */
    public List<String> getImplicits() {
        return implicits;
    }

    /**
     * The level of affixes which can be rolled.
     *
     * @return the level used to roll affixes.
     */
    public int getAffixLevel() {
        return affix_level;
    }

    /**
     * Limits the concurrent number of affixes, an affix can
     * only roll if none of the tags exceed the limit.
     *
     * @return tags mapped to limits.
     */
    public Map<String, Integer> getAffixLimit() {
        return affix_limit;
    }

    /**
     * Multipliers to the weights of which affixes can roll, if
     * no weight scaling exists for an affix, it cannot roll.
     *
     * @return tags mapped to weights.
     */
    public Map<String, Double> getAffixWeight() {
        return affix_weight;
    }

    /**
     * Binds the item to the player which it is created for.
     *
     * @return true if the item should drop bounded.
     */
    public boolean isDropBound() {
        return drop_bound;
    }

    /**
     * Prevents this item from stacking with any other item.
     *
     * @return true if we cannot stack.
     */
    public boolean isUnstackable() {
        return do_not_stack;
    }

    /**
     * Which equipment slots we can slot the item into.
     *
     * @return which equipment slot to use.
     */
    public List<String> getEquipmentSlot() {
        return equipment_slot;
    }

    /**
     * How many jewels can be socketed on this item, the design that
     * is used to present the jewels also is dependent on this.
     *
     * @return the number of jewel sockets available.
     */
    public int getJewelMaximum() {
        return jewel_maximum;
    }

    /**
     * When a jewel is socketed on an item, it is verified thorough the
     * same system as which equipment is processed.
     *
     * @return which types of equipment slots we accept
     */
    public List<String> getJewelTypes() {
        return jewel_accept;
    }

    /**
     * Item level can be used to gate players out of content and
     * provides a basic estimate of item power. The item level is
     * not granting any power by itself.
     *
     * @return level of this item.
     */
    public int getItemLevel() {
        return item_level;
    }

    /**
     * What icon to present on the lore, if we are socketed upon
     * another item.
     *
     * @return symbol to use while socketed
     */
    public String getJewelIcon() {
        return jewel_icon;
    }

    /**
     * Chance for other jewels to shatter when this one is socketed
     * upon the item.
     *
     * @return chance to shatter other jewels upon socketing.
     */
    public double getJewelShatter() {
        return jewel_shatter;
    }

    /**
     * To socket this jewel, the item must have this given number of
     * jewels already socketed on it.
     *
     * @return number of jewels necessary to socket this jewel.
     */
    public int getJewelPrevious() {
        return jewel_previous;
    }

    /**
     * A distinctive rule to use for styling your items.
     *
     * @return rule for styling an item.
     */
    public String getStyling() {
        return styling;
    }

    /**
     * Animation to play when jewel is shattered, expected to be
     * a slot animation.
     *
     * @return animation to play.
     */
    public String getAnimationShatter() {
        return animation_shatter;
    }

    /**
     * Tags which identify the item, the "DIRECT_"+ID tag is always
     * generated on every item.
     *
     * @return tags on the item.
     */
    public List<String> getTags() {
        return tags;
    }

    /**
     * Tag used to identify bankable items, items cannot be banked
     * if this value is "none"
     *
     * @return group to bank items in.
     */
    public String getBankGroup() {
        return bank_group;
    }

    /**
     * Quantity contributed when banked into the group.
     *
     * @return quantity to contribute.
     */
    public int getBankQuantity() {
        return bank_quantity;
    }

    /**
     * A symbol used when rendered into a text-line.
     *
     * @return currency symbol to render.
     */
    public String getBankSymbol() {
        return bank_symbol;
    }
}
