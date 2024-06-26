package me.blutkrone.rpgcore.item.styling.descriptor;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.api.item.IItemDescriber;
import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import me.blutkrone.rpgcore.item.CoreItem;
import me.blutkrone.rpgcore.item.ItemManager;
import me.blutkrone.rpgcore.item.Requirement;
import me.blutkrone.rpgcore.item.data.ItemDataDurability;
import me.blutkrone.rpgcore.item.data.ItemDataGeneric;
import me.blutkrone.rpgcore.item.data.ItemDataJewel;
import me.blutkrone.rpgcore.item.data.ItemDataModifier;
import me.blutkrone.rpgcore.item.modifier.CoreModifier;
import me.blutkrone.rpgcore.item.modifier.ModifierStyle;
import me.blutkrone.rpgcore.item.styling.IDescriptionRequester;
import me.blutkrone.rpgcore.item.styling.StylingRule;
import me.blutkrone.rpgcore.language.LanguageManager;
import me.blutkrone.rpgcore.resourcepack.ResourcepackManager;
import me.blutkrone.rpgcore.resourcepack.generation.component.hud.AbstractTexture;
import me.blutkrone.rpgcore.util.Utility;
import me.blutkrone.rpgcore.util.fontmagic.MagicStringBuilder;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

/**
 * Descriptor meant for a wide variety of generic items.
 */
public class ItemDescriptor implements IItemDescriber {

    private int padding = 15;
    private int width_base = 250;

    private List<BaseComponent[]> getHeader(StylingRule styling, int item_level, String name, String sub_name) {
        // output collection
        List<BaseComponent[]> compiled = new ArrayList<>();
        MagicStringBuilder msb = new MagicStringBuilder();
        // managers that we may need
        LanguageManager language_manager = RPGCore.inst().getLanguageManager();
        ItemManager item_manager = RPGCore.inst().getItemManager();
        ResourcepackManager resource_manager = RPGCore.inst().getResourcepackManager();
        // additionally processed data
        String item_level_text = language_manager.formatShortNumber(item_level);

        // [1] header segment to hide the vanilla lore
        msb.shiftToExact(-20);
        msb.append(styling.texture("header", 0), ChatColor.WHITE);
        msb.shiftToExact(-20);
        msb.append(styling.texture("header", 1), ChatColor.WHITE);
        // [2] score of the item
        msb.shiftCentered((this.width_base / 2) - 20, Utility.measure(item_level_text));
        msb.append(styling.color("score", item_level_text), "lore_score");
        // [3] title texture background
        msb.shiftToExact(-20);
        msb.append(styling.texture("title", 0), ChatColor.WHITE);
        msb.shiftToExact(0);
        compiled.add(msb.compileAndClean());
        // [4] slice of title segment, item name
        msb.shiftToExact(-20);
        msb.append(styling.texture("title", 1), ChatColor.WHITE);
        msb.shiftCentered((this.width_base / 2) - 20, Utility.measure(name));
        msb.append(styling.color("upper-name", name), "lore_upper_name");
        msb.shiftToExact(0);
        compiled.add(msb.compileAndClean());
        // [5] slice of title segment, sub name
        msb.shiftToExact(-20);
        msb.append(styling.texture("title", 2), ChatColor.WHITE);
        msb.shiftCentered((this.width_base / 2) - 20, Utility.measure(sub_name));
        msb.append(styling.color("lower-name", sub_name), "lore_lower_name");
        msb.shiftToExact(0);
        compiled.add(msb.compileAndClean());
        // [6] slice of title segment
        msb.shiftToExact(-20);
        msb.append(styling.texture("title", 3), ChatColor.WHITE);
        msb.shiftToExact(0);
        compiled.add(msb.compileAndClean());

        return compiled;
    }

    private List<BaseComponent[]> getHighlight(StylingRule styling, List<CoreModifier> modifiers) {
        // output collection
        List<BaseComponent[]> compiled = new ArrayList<>();
        MagicStringBuilder msb = new MagicStringBuilder();
        // managers that we may need
        LanguageManager language_manager = RPGCore.inst().getLanguageManager();
        ItemManager item_manager = RPGCore.inst().getItemManager();
        ResourcepackManager resource_manager = RPGCore.inst().getResourcepackManager();

        // [7] generate data within the header
        int previous = compiled.size();
        modifiers.removeIf(modifier -> {
            ModifierStyle style = modifier.getStyle();
            if (style == ModifierStyle.HEADER) {
                List<String> readables = modifier.getReadable();
                for (String readable : readables) {
                    // generate the backdrop texture
                    String[] split = readable.split("\\#");
                    msb.shiftToExact(-20);
                    msb.append(styling.texture("highlight_top", 0), ChatColor.WHITE);
                    // write the primary info of the modifier
                    msb.shiftToExact(-20 + this.padding - 5);
                    msb.append(styling.color("highlight-left", split[0]), "default_fixed");
                    // write the secondary info of the modifier
                    if (split.length == 2) {
                        msb.shiftToExact(-20 + this.width_base - this.padding + 5 - Utility.measure(split[1]));
                        msb.append(styling.color("highlight-right", split[1]), "default_fixed");
                    }
                    // append the modifier to our listing
                    msb.shiftToExact(0);
                    compiled.add(msb.compileAndClean());
                    // add a separator behind the modifier
                    msb.shiftToExact(-20);
                    msb.append(styling.texture("highlight_separator", 0), ChatColor.WHITE);
                    msb.shiftToExact(0);
                    compiled.add(msb.compileAndClean());
                }
                return true;
            }
            return false;
        });

        // drop last element if it is a separator.
        if (previous != compiled.size()) {
            compiled.remove(compiled.size() - 1);
        }

        // [8] close up the header, and open the generic lore segment
        msb.shiftToExact(-20);
        msb.append(styling.texture("highlight_bottom", 0), ChatColor.WHITE);
        msb.shiftToExact(0);
        compiled.add(msb.compileAndClean());

        return compiled;
    }

    private List<BaseComponent[]> getInfo(StylingRule styling, List<String> info) {
        // output collection
        List<BaseComponent[]> compiled = new ArrayList<>();
        MagicStringBuilder msb = new MagicStringBuilder();
        // managers that we may need
        LanguageManager language_manager = RPGCore.inst().getLanguageManager();
        ItemManager item_manager = RPGCore.inst().getItemManager();
        ResourcepackManager resource_manager = RPGCore.inst().getResourcepackManager();

        // [11] lore is available as the last entry.
        if (!info.isEmpty()) {
            for (String line : info) {
                // generate the backdrop texture
                String[] split = line.split("\\#");
                msb.shiftToExact(-20);
                msb.append(styling.texture("body_top", 0), ChatColor.WHITE);
                // write the primary info of the line
                msb.shiftToExact(-20 + this.padding);
                msb.append(styling.color("info-left", split[0]), "default_fixed");
                // write the secondary info of the line
                if (split.length == 2) {
                    msb.shiftToExact(-20 + this.width_base - this.padding - Utility.measure(split[1]));
                    msb.append(styling.color("info-right", split[1]), "default_fixed");
                }
                // append the line to our listing
                msb.shiftToExact(0);
                compiled.add(msb.compileAndClean());
            }
        }

        return compiled;
    }

    private List<BaseComponent[]> getAttributes(StylingRule styling, List<CoreModifier> modifiers) {
        // output collection
        List<BaseComponent[]> compiled = new ArrayList<>();
        MagicStringBuilder msb = new MagicStringBuilder();
        // managers that we may need
        LanguageManager language_manager = RPGCore.inst().getLanguageManager();
        ItemManager item_manager = RPGCore.inst().getItemManager();
        ResourcepackManager resource_manager = RPGCore.inst().getResourcepackManager();
        // additionally processed data
        Map<String, List<CoreModifier>> categorized = new HashMap<>();
        modifiers.removeIf(modifier -> {
            if (modifier.getStyle() == ModifierStyle.GENERIC) {
                categorized.computeIfAbsent(modifier.getLCCategory(), (k -> new ArrayList<>())).add(modifier);
                return true;
            } else {
                return false;
            }
        });

        // [9] write modifiers to format as an attribute
        categorized.forEach((category, mods) -> {
            // separator above category name
            msb.shiftToExact(-20);
            msb.append(styling.texture("body_separator", 0), ChatColor.WHITE);
            msb.shiftToExact(0);
            compiled.add(msb.compileAndClean());
            // backdrop + name of category
            msb.shiftToExact(-20);
            msb.append(styling.texture("body_top", 0), ChatColor.WHITE);
            String translation = language_manager.getTranslation(category);
            msb.shiftCentered((this.width_base / 2) - 20, Utility.measure(translation));
            msb.append(styling.color("category", translation), "default_fixed");
            msb.shiftToExact(0);
            compiled.add(msb.compileAndClean());
            // separator below category name
            msb.shiftToExact(-20);
            msb.append(styling.texture("body_separator", 0), ChatColor.WHITE);
            msb.shiftToExact(0);
            compiled.add(msb.compileAndClean());

            for (CoreModifier modifier : mods) {
                List<String> readables = modifier.getReadable();
                for (String readable : readables) {
                    // generate the backdrop texture
                    String[] split = readable.split("\\#");
                    msb.shiftToExact(-20);
                    msb.append(styling.texture("body_top", 0), ChatColor.WHITE);
                    // write the primary info of the modifier
                    msb.shiftToExact(-20 + this.padding);
                    msb.append(styling.color("attribute-left", split[0]), "default_fixed");
                    // write the secondary info of the modifier
                    if (split.length == 2) {
                        msb.shiftToExact(-20 + this.width_base - this.padding - Utility.measure(split[1]));
                        msb.append(styling.color("attribute-right", split[1]), "default_fixed");
                    }
                    // append the modifier to our listing
                    msb.shiftToExact(0);
                    compiled.add(msb.compileAndClean());
                }
            }
        });

        return compiled;
    }

    private List<BaseComponent[]> getAbilities(StylingRule styling, List<CoreModifier> modifiers) {
        // output collection
        List<BaseComponent[]> compiled = new ArrayList<>();
        MagicStringBuilder msb = new MagicStringBuilder();
        // managers that we may need
        LanguageManager language_manager = RPGCore.inst().getLanguageManager();
        ItemManager item_manager = RPGCore.inst().getItemManager();
        ResourcepackManager resource_manager = RPGCore.inst().getResourcepackManager();
        // additionally processed data
        Map<String, List<CoreModifier>> categorized = new HashMap<>();
        modifiers.removeIf(modifier -> {
            if (modifier.getStyle() == ModifierStyle.ABILITY) {
                categorized.computeIfAbsent(modifier.getLCCategory(), (k -> new ArrayList<>())).add(modifier);
                return true;
            } else {
                return false;
            }
        });

        // [10] write modifiers to format as an ability
        int previous = compiled.size();
        categorized.forEach((category, mods) -> {
            // separator above category name
            msb.shiftToExact(-20);
            msb.append(styling.texture("body_separator", 0), ChatColor.WHITE);
            msb.shiftToExact(0);
            compiled.add(msb.compileAndClean());
            // backdrop + name of category
            msb.shiftToExact(-20);
            msb.append(styling.texture("body_top", 0), ChatColor.WHITE);
            String translation = language_manager.getTranslation(category);
            msb.shiftCentered((this.width_base / 2) - 20, Utility.measure(translation));
            msb.append(styling.color("category", translation), "default_fixed");
            msb.shiftToExact(0);
            compiled.add(msb.compileAndClean());
            // separator below category name
            msb.shiftToExact(-20);
            msb.append(styling.texture("body_separator", 0), ChatColor.WHITE);
            msb.shiftToExact(0);
            compiled.add(msb.compileAndClean());

            for (CoreModifier modifier : mods) {
                List<String> readables = modifier.getReadable();
                for (String readable : readables) {
                    // ensure we got a valid format to work with
                    String[] split = readable.split("\\#");
                    if (split.length < 3) {
                        continue;
                    }
                    // unwrap the description we are using here
                    String icon = split[0];
                    String upper = split[1];
                    String left = split[2];
                    String right = split.length >= 4 ? split[3] : null;
                    // create the icon and the upper text
                    msb.shiftToExact(-20);
                    msb.append(styling.texture("body_top", 0), ChatColor.WHITE);
                    msb.shiftToExact(-20 + this.padding + 24);
                    msb.append(styling.color("ability-upper", upper), "default_fixed");
                    msb.shiftToExact(0);
                    compiled.add(msb.compileAndClean());
                    // create lower text
                    msb.shiftToExact(-20);
                    msb.append(styling.texture("body_top", 0), ChatColor.WHITE);
                    msb.shiftToExact(-20 + this.padding);
                    msb.append(resource_manager.texture("lore_icon_" + icon), ChatColor.WHITE);
                    msb.shiftToExact(-20 + this.padding + 24);
                    msb.append(styling.color("ability-left", left), "default_fixed");
                    if (right != null) {
                        msb.shiftToExact(this.width_base - this.padding - 20 - Utility.measure(right));
                        msb.append(styling.color("ability-right", right), "default_fixed");
                    }
                    msb.shiftToExact(0);
                    compiled.add(msb.compileAndClean());
                    // create separator line
                    msb.shiftToExact(-20);
                    msb.append(styling.texture("body_top", 0), ChatColor.WHITE);
                    msb.shiftToExact(0);
                    compiled.add(msb.compileAndClean());
                }
            }
        });
        if (previous != compiled.size()) {
            compiled.remove(compiled.size() - 1);
        }

        return compiled;
    }

    private List<BaseComponent[]> getRequirement(StylingRule styling, List<Requirement> requirements, IDescriptionRequester entity) {
        // output collection
        List<BaseComponent[]> compiled = new ArrayList<>();
        MagicStringBuilder msb = new MagicStringBuilder();
        // managers that we may need
        LanguageManager language_manager = RPGCore.inst().getLanguageManager();
        ItemManager item_manager = RPGCore.inst().getItemManager();
        ResourcepackManager resource_manager = RPGCore.inst().getResourcepackManager();

        // [12] requirement appears after description.
        if (!requirements.isEmpty()) {
            // separator above category name
            msb.shiftToExact(-20);
            msb.append(styling.texture("body_separator", 0), ChatColor.WHITE);
            msb.shiftToExact(0);
            compiled.add(msb.compileAndClean());
            // backdrop + name of category
            msb.shiftToExact(-20);
            msb.append(styling.texture("body_top", 0), ChatColor.WHITE);
            String translation = language_manager.getTranslation("lore_category_requirement");
            msb.shiftCentered((this.width_base / 2) - 20, Utility.measure(translation));
            msb.append(styling.color("category", translation), "default_fixed");
            msb.shiftToExact(0);
            compiled.add(msb.compileAndClean());
            // separator below category name
            msb.shiftToExact(-20);
            msb.append(styling.texture("body_separator", 0), ChatColor.WHITE);
            msb.shiftToExact(0);
            compiled.add(msb.compileAndClean());

            for (Requirement requirement : requirements) {
                // check if requirement was met
                boolean archived = false;
                if (entity instanceof CoreEntity) {
                    archived = requirement.doesArchive(((CoreEntity) entity));
                }
                // render the text we picked
                if (archived) {
                    for (String text : requirement.getDisplayText()) {
                        // generate the backdrop texture
                        String[] split = text.split("\\#");
                        msb.shiftToExact(-20);
                        msb.append(styling.texture("body_top", 0), ChatColor.WHITE);
                        // write the primary info of the modifier
                        msb.shiftToExact(-20 + this.padding);
                        msb.append(styling.color("alright-left", split[0]), "default_fixed");
                        // write the secondary info of the modifier
                        if (split.length == 2) {
                            msb.shiftToExact(-20 + this.width_base - this.padding - Utility.measure(split[1]));
                            msb.append(styling.color("alright-right", split[1]), "default_fixed");
                        }
                        // append the modifier to our listing
                        msb.shiftToExact(0);
                        compiled.add(msb.compileAndClean());
                    }
                } else {
                    for (String text : requirement.getDisplayText()) {
                        // generate the backdrop texture
                        String[] split = text.split("\\#");
                        msb.shiftToExact(-20);
                        msb.append(styling.texture("body_top", 0), ChatColor.WHITE);
                        // write the primary info of the modifier
                        msb.shiftToExact(-20 + this.padding);
                        msb.append(styling.color("warning-left", split[0]), "default_fixed");
                        // write the secondary info of the modifier
                        if (split.length == 2) {
                            msb.shiftToExact(-20 + this.width_base - this.padding - Utility.measure(split[1]));
                            msb.append(styling.color("warning-right", split[1]), "default_fixed");
                        }
                        // append the modifier to our listing
                        msb.shiftToExact(0);
                        compiled.add(msb.compileAndClean());
                    }
                }
            }
        }

        return compiled;
    }
    private List<BaseComponent[]> getDescription(StylingRule styling, List<String> lore) {
        // output collection
        List<BaseComponent[]> compiled = new ArrayList<>();
        MagicStringBuilder msb = new MagicStringBuilder();
        // managers that we may need
        LanguageManager language_manager = RPGCore.inst().getLanguageManager();
        ItemManager item_manager = RPGCore.inst().getItemManager();
        ResourcepackManager resource_manager = RPGCore.inst().getResourcepackManager();

        // [11] lore is available as the last entry.
        if (!lore.isEmpty()) {
            // re-arrange lore to respect text length
            boolean valid = true;
            for (String line : lore) {
                if (Utility.measure(line) > 230) {
                    valid = false;
                }
            }
            // if invalid, re-do the text
            if (!valid) {
                // reduce to words
                List<String> words = new ArrayList<>();
                for (String line : lore) {
                    words.addAll(Arrays.asList(line.split(" ")));
                }
                // transform words into lines
                lore.clear();
                String sentence = words.remove(0);
                for (String word : words) {
                    String preview = sentence + " " + word;
                    if (Utility.measure(preview) <= 230) {
                        sentence = preview;
                    } else {
                        lore.add(sentence);
                        sentence = word;
                    }
                }
                // if we got a left, over pool it
                if (!sentence.isBlank()) {
                    lore.add(sentence);
                }
            }

            // separator above category name
            msb.shiftToExact(-20);
            msb.append(styling.texture("body_separator", 0), ChatColor.WHITE);
            msb.shiftToExact(0);
            compiled.add(msb.compileAndClean());
            // backdrop + name of category
            msb.shiftToExact(-20);
            msb.append(styling.texture("body_top", 0), ChatColor.WHITE);
            String translation = language_manager.getTranslation("lore_category_description");
            msb.shiftCentered((this.width_base / 2) - 20, Utility.measure(translation));
            msb.append(styling.color("category", translation), "default_fixed");
            msb.shiftToExact(0);
            compiled.add(msb.compileAndClean());
            // separator below category name
            msb.shiftToExact(-20);
            msb.append(styling.texture("body_separator", 0), ChatColor.WHITE);
            msb.shiftToExact(0);
            compiled.add(msb.compileAndClean());

            for (String line : lore) {
                msb.shiftToExact(-20);
                msb.append(styling.texture("body_top", 0), ChatColor.WHITE);
                msb.shiftCentered((this.width_base / 2) - 20, Utility.measure(line));
                msb.append(styling.color("description", line), "default_fixed");
                msb.shiftToExact(0);
                compiled.add(msb.compileAndClean());
            }
        }

        return compiled;
    }

    private List<BaseComponent[]> getJewels(StylingRule styling, int unlocked, int maximum, List<ItemStack> socketed) {
        if (maximum <= 0) {
            return new ArrayList<>();
        }
        // output collection
        List<BaseComponent[]> compiled = new ArrayList<>();
        MagicStringBuilder msb = new MagicStringBuilder();
        // managers that we may need
        LanguageManager language_manager = RPGCore.inst().getLanguageManager();
        ItemManager item_manager = RPGCore.inst().getItemManager();
        ResourcepackManager resource_manager = RPGCore.inst().getResourcepackManager();

        // empty lines create the relevant spacing
        msb.shiftToExact(-20);
        msb.append(styling.texture("body_top", 0), ChatColor.WHITE);
        msb.shiftToExact(0);
        compiled.add(msb.compileAndClean());
        // empty lines create the relevant spacing
        msb.shiftToExact(-20);
        msb.append(styling.texture("body_top", 0), ChatColor.WHITE);
        msb.shiftToExact(0);
        compiled.add(msb.compileAndClean());
        // empty lines create the relevant spacing
        msb.shiftToExact(-20);
        msb.append(styling.texture("body_top", 0), ChatColor.WHITE);
        msb.shiftToExact(0);
        compiled.add(msb.compileAndClean());
        // empty lines create the relevant spacing
        msb.shiftToExact(-20);
        msb.append(styling.texture("body_top", 0), ChatColor.WHITE);
        msb.shiftToExact(0);

        // identify symbols we are going to use
        List<String> symbols = new ArrayList<>();

        // populate with socketed jewels
        for (ItemStack jewel : socketed) {
            ItemDataGeneric generic_data = item_manager.getItemData(jewel, ItemDataGeneric.class);
            if (generic_data == null) {
                symbols.add("default");
            } else {
                symbols.add(generic_data.getItem().getJewelIcon());
            }
        }
        // fill in with unlocked sockets
        while (symbols.size() < unlocked) {
            symbols.add("unlocked");
        }
        // fill in with locked sockets
        while (symbols.size() < maximum) {
            symbols.add("locked");
        }

        // compute relevant centering
        int width = symbols.size() * 28 + (symbols.size() - 1) * 2;
        msb.shiftCentered((this.width_base / 2) - 20, width);
        // render the relevant jewel sockets
        for (String symbol : symbols) {
            AbstractTexture texture = resource_manager.texture("lore_jewel_" + symbol);
            msb.append(texture, ChatColor.WHITE);
            msb.advance(2);
        }
        // offer up the rendering
        msb.shiftToExact(0);
        compiled.add(msb.compileAndClean());

        // commit our changes
        return compiled;
    }

    private List<BaseComponent[]> getFooter(StylingRule styling, double durability) {
        // output collection
        List<BaseComponent[]> compiled = new ArrayList<>();
        MagicStringBuilder msb = new MagicStringBuilder();
        // managers that we may need
        LanguageManager language_manager = RPGCore.inst().getLanguageManager();
        ItemManager item_manager = RPGCore.inst().getItemManager();
        ResourcepackManager resource_manager = RPGCore.inst().getResourcepackManager();

        // close up the body segment, open up footer
        msb.shiftToExact(-20);
        msb.append(styling.texture("body_bottom", 0), ChatColor.WHITE);
        msb.shiftToExact(0);
        compiled.add(msb.compileAndClean());
        // write the durability remaining on the item
        msb.shiftToExact(-20);
        msb.append(styling.texture("body_bottom", 1), ChatColor.WHITE);
        msb.shiftToExact(0);
        compiled.add(msb.compileAndClean());
        // close up the footer
        msb.shiftToExact(-20);
        msb.append(styling.texture("footer", 0), ChatColor.WHITE);
        msb.shiftToExact(-20);
        msb.append(styling.texture("footer", 1), ChatColor.WHITE);
        msb.shiftToExact(0);
        // note down the durability on the footer
        String durability_text = language_manager.getTranslation("lore_durability");
        msb.shiftToExact(-20 + this.padding);
        msb.append(styling.color("durability", durability_text + String.format("%.1f%%", durability * 100d)), "lore_durability");
        msb.shiftToExact(0);
        compiled.add(msb.compileAndClean());

        return compiled;
    }

    /*
     * Attempt to describe like an identified item, this
     * returns false if we are not.
     *
     * @param item what item are we describing
     * @return true if we were described
     */
    private boolean describeIdentified(ItemStack item, IDescriptionRequester player) {
        LanguageManager language_manager = RPGCore.inst().getLanguageManager();
        ItemManager item_manager = RPGCore.inst().getItemManager();
        ResourcepackManager resource_manager = RPGCore.inst().getResourcepackManager();

        // extract the data we got on this item
        ItemDataGeneric generic_data = item_manager.getItemData(item, ItemDataGeneric.class);
        ItemDataJewel jewel_data = item_manager.getItemData(item, ItemDataJewel.class);
        ItemDataModifier modifier_data = item_manager.getItemData(item, ItemDataModifier.class);
        ItemDataDurability durability_data = item_manager.getItemData(item, ItemDataDurability.class);

        // generic data is present on all rpgcore items
        if (generic_data == null) {
            return false;
        }

        // extract the basic information of the item
        CoreItem item_base = generic_data.getItem();
        StylingRule styling = item_manager.getStylingRule(item_base.getStyling());
        List<String> lore = language_manager.getTranslationList(item_base.getLCText());
        String name = lore.isEmpty() ? "name is missing" : lore.remove(0);
        String sub_name = lore.isEmpty() ? "sub-name is missing" : lore.remove(0);
        List<String> info = language_manager.getTranslationList(item_base.getLCInfo());
        int item_level = item_base.getItemLevel();
        double durability = durability_data.getAsPercentage();
        List<ItemStack> socketed = new ArrayList<>(jewel_data.getItems().values());
        int unlocked_socket = jewel_data.getAvailableSockets();
        int maximum_socket = jewel_data.getMaximumSockets();
        List<CoreModifier> modifiers = modifier_data.getModifiers();
        List<Requirement> requirements = item_base.getRequirements();

        modifiers.sort(Comparator.comparing(CoreModifier::getId));
        modifiers.sort(Comparator.comparing(CoreModifier::isImplicit));

        // generate the appropriate lore for the item
        List<BaseComponent[]> compiled = new ArrayList<>();
        if (styling != null) {
            // populate with the relevant parts
            compiled.addAll(this.getHeader(styling, item_level, name, sub_name));
            compiled.addAll(this.getHighlight(styling, modifiers));
            compiled.addAll(this.getInfo(styling, info));
            compiled.addAll(this.getAttributes(styling, modifiers));
            compiled.addAll(this.getAbilities(styling, modifiers));
            compiled.addAll(this.getDescription(styling, lore));
            compiled.addAll(this.getRequirement(styling, requirements, player));
            compiled.addAll(this.getJewels(styling, unlocked_socket, maximum_socket, socketed));
            compiled.addAll(this.getFooter(styling, durability));
        } else {
            compiled.add(TextComponent.fromLegacyText("§cStyling rule '" + item_base.getStyling() + "' does not exist!"));
        }

        // ItemBuilder.of(item).name("EMPTY NAME").lore("EMPTY LORE").build();

        for (BaseComponent[] components : compiled) {
            for (BaseComponent component : components) {
                component.setItalic(false);
            }
        }

        // apply the lore to the item
        RPGCore.inst().getVolatileManager().setItemLore(item, compiled);
        RPGCore.inst().getVolatileManager().setItemName(item, new MagicStringBuilder().compile());

        // write name snapshot to item
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            PersistentDataContainer data = meta.getPersistentDataContainer();
            if (styling != null) {
                data.set(new NamespacedKey(RPGCore.inst(), "rpgcore-name"), PersistentDataType.STRING, styling.color("upper-name", name));
            } else {
                data.set(new NamespacedKey(RPGCore.inst(), "rpgcore-name"), PersistentDataType.STRING, name);
            }
            item.setItemMeta(meta);
        }

        // notify about successful update
        return true;
    }

    /*
     * Attempt to describe like an unidentified item, this
     * returns false if we are not.
     *
     * @param item what item are we describing
     * @return true if we were described
     */
    private boolean describeUnidentified(ItemStack item, IDescriptionRequester player) {
        LanguageManager language_manager = RPGCore.inst().getLanguageManager();
        ItemManager item_manager = RPGCore.inst().getItemManager();
        ResourcepackManager resource_manager = RPGCore.inst().getResourcepackManager();

        // extract id we are wrapping.
        if (!item.hasItemMeta()) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }
        PersistentDataContainer data = meta.getPersistentDataContainer();
        NamespacedKey keying = new NamespacedKey(RPGCore.inst(), "core-unidentified");
        String item_id = data.get(keying, PersistentDataType.STRING);
        if (item_id == null) {
            return false;
        }
        // build description based on it.

        // extract the basic information of the item
        CoreItem item_base = RPGCore.inst().getItemManager().getItemIndex().get(item_id);
        StylingRule styling = item_manager.getStylingRule(item_base.getStyling());
        List<String> lore = language_manager.getTranslationList(item_base.getLCText());
        String name = lore.isEmpty() ? "name is missing" : lore.remove(0);
        String sub_name = lore.isEmpty() ? "sub-name is missing" : lore.remove(0);
        List<String> info = new ArrayList<>(Collections.singletonList(language_manager.getTranslation("item_unidentified")));
        int item_level = item_base.getItemLevel();
        List<Requirement> requirements = item_base.getRequirements();

        // generate the appropriate lore for the item
        List<BaseComponent[]> compiled = new ArrayList<>();
        if (styling != null) {
            // populate with the relevant parts
            compiled.addAll(this.getHeader(styling, item_level, name, sub_name));
            compiled.addAll(this.getHighlight(styling, new ArrayList<>()));
            compiled.addAll(this.getInfo(styling, info));
            compiled.addAll(this.getAttributes(styling, new ArrayList<>()));
            compiled.addAll(this.getAbilities(styling, new ArrayList<>()));
            compiled.addAll(this.getDescription(styling, lore));
            compiled.addAll(this.getJewels(styling, 0, 0, new ArrayList<>()));
            compiled.addAll(this.getFooter(styling, 1.0d));
        } else {
            compiled.add(TextComponent.fromLegacyText("§cStyling rule '" + item_base.getStyling() + "' does not exist!"));
        }

        for (BaseComponent[] components : compiled) {
            for (BaseComponent component : components) {
                component.setItalic(false);
            }
        }

        // apply the lore to the item
        RPGCore.inst().getVolatileManager().setItemLore(item, compiled);
        RPGCore.inst().getVolatileManager().setItemName(item, new MagicStringBuilder().compile());

        // write name snapshot to item
        ItemMeta _meta = item.getItemMeta();
        if (_meta != null) {
            PersistentDataContainer _data = _meta.getPersistentDataContainer();
            _data.set(new NamespacedKey(RPGCore.inst(), "rpgcore-name"), PersistentDataType.STRING, name);
            item.setItemMeta(_meta);
        }

        return true;
    }

    @Override
    public void describe(ItemStack item, IDescriptionRequester player) {
        // try to describe as an identified item
        if (describeIdentified(item, player)) {
            return;
        }
        // try to describe as an unidentified item
        if (describeUnidentified(item, player)) {
            return;
        }
    }
}
