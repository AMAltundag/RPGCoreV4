package me.blutkrone.rpgcore.item.styling;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.api.item.IItemDescriber;
import me.blutkrone.rpgcore.item.CoreItem;
import me.blutkrone.rpgcore.item.ItemManager;
import me.blutkrone.rpgcore.item.data.ItemDataDurability;
import me.blutkrone.rpgcore.item.data.ItemDataGeneric;
import me.blutkrone.rpgcore.item.data.ItemDataJewel;
import me.blutkrone.rpgcore.item.data.ItemDataModifier;
import me.blutkrone.rpgcore.item.modifier.CoreModifier;
import me.blutkrone.rpgcore.item.modifier.ModifierStyle;
import me.blutkrone.rpgcore.language.LanguageManager;
import me.blutkrone.rpgcore.resourcepack.ResourcePackManager;
import me.blutkrone.rpgcore.resourcepack.utils.IndexedTexture;
import me.blutkrone.rpgcore.util.Utility;
import me.blutkrone.rpgcore.util.fontmagic.MagicStringBuilder;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class DefaultItemDescriptor implements IItemDescriber {

    private int padding = 15;
    private int width_base = 250;

    private List<BaseComponent[]> getHeader(ItemStylingRule styling, int item_level, String name, String sub_name) {
        // output collection
        List<BaseComponent[]> compiled = new ArrayList<>();
        MagicStringBuilder msb = new MagicStringBuilder();
        // managers that we may need
        LanguageManager language_manager = RPGCore.inst().getLanguageManager();
        ItemManager item_manager = RPGCore.inst().getItemManager();
        ResourcePackManager resource_manager = RPGCore.inst().getResourcePackManager();
        // additionally processed data
        String item_level_text = language_manager.formatShortNumber(item_level);

        // [1] header segment to hide the vanilla lore
        msb.shiftToExact(-20);
        msb.append(styling.texture("header", 0), ChatColor.WHITE);
        msb.shiftToExact(-20);
        msb.append(styling.texture("header", 1), ChatColor.WHITE);
        // [2] score of the item
        msb.shiftCentered((this.width_base/2)-20, Utility.measureWidthExact(item_level_text));
        msb.append(styling.color("score", item_level_text), "lore_score");
        // [3] title texture background
        msb.shiftToExact(-20);
        msb.append(styling.texture("title", 0), ChatColor.WHITE);
        msb.shiftToExact(0);
        compiled.add(msb.compileAndClean());
        // [4] slice of title segment, item name
        msb.shiftToExact(-20);
        msb.append(styling.texture("title", 1), ChatColor.WHITE);
        msb.shiftCentered((this.width_base/2)-20, Utility.measureWidthExact(name));
        msb.append(styling.color("upper-name", name), "lore_upper_name");
        msb.shiftToExact(0);
        compiled.add(msb.compileAndClean());
        // [5] slice of title segment, sub name
        msb.shiftToExact(-20);
        msb.append(styling.texture("title", 2), ChatColor.WHITE);
        msb.shiftCentered((this.width_base/2)-20, Utility.measureWidthExact(sub_name));
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

    private List<BaseComponent[]> getHighlight(ItemStylingRule styling, List<CoreModifier> modifiers) {
        // output collection
        List<BaseComponent[]> compiled = new ArrayList<>();
        MagicStringBuilder msb = new MagicStringBuilder();
        // managers that we may need
        LanguageManager language_manager = RPGCore.inst().getLanguageManager();
        ItemManager item_manager = RPGCore.inst().getItemManager();
        ResourcePackManager resource_manager = RPGCore.inst().getResourcePackManager();

        // [7] generate data within the header
        int previous = compiled.size();
        modifiers.removeIf(modifier -> {
            ModifierStyle style = modifier.getStyle();
            if (style == ModifierStyle.HEADER) {
                List<String> readables = language_manager.getTranslationList(modifier.getLCReadable());
                for (String readable : readables) {
                    // generate the backdrop texture
                    String[] split = readable.split("\\#");
                    msb.shiftToExact(-20);
                    msb.append(styling.texture("highlight_top", 0), ChatColor.WHITE);
                    // write the primary info of the modifier
                    msb.shiftToExact(-20 + this.padding - 5);
                    msb.append(styling.color("highlight-left", split[0]));
                    // write the secondary info of the modifier
                    if (split.length == 2) {
                        msb.shiftToExact(-20 + this.width_base - this.padding + 5 - Utility.measureWidthExact(split[1]));
                        msb.append(styling.color("highlight-right", split[1]));
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
            compiled.remove(compiled.size()-1);
        }

        // [8] close up the header, and open the generic lore segment
        msb.shiftToExact(-20);
        msb.append(styling.texture("highlight_bottom", 0), ChatColor.WHITE);
        msb.shiftToExact(0);
        compiled.add(msb.compileAndClean());

        return compiled;
    }

    private List<BaseComponent[]> getInfo(ItemStylingRule styling, List<String> info) {
        // output collection
        List<BaseComponent[]> compiled = new ArrayList<>();
        MagicStringBuilder msb = new MagicStringBuilder();
        // managers that we may need
        LanguageManager language_manager = RPGCore.inst().getLanguageManager();
        ItemManager item_manager = RPGCore.inst().getItemManager();
        ResourcePackManager resource_manager = RPGCore.inst().getResourcePackManager();

        // [11] lore is available as the last entry.
        if (!info.isEmpty()) {
            for (String line : info) {
                // generate the backdrop texture
                String[] split = line.split("\\#");
                msb.shiftToExact(-20);
                msb.append(styling.texture("body_top", 0), ChatColor.WHITE);
                // write the primary info of the line
                msb.shiftToExact(-20 + this.padding);
                msb.append(styling.color("info-left", split[0]));
                // write the secondary info of the line
                if (split.length == 2) {
                    msb.shiftToExact(-20 + this.width_base - this.padding - Utility.measureWidthExact(split[1]));
                    msb.append(styling.color("info-right", split[1]));
                }
                // append the line to our listing
                msb.shiftToExact(0);
                compiled.add(msb.compileAndClean());
            }
        }

        return compiled;
    }

    private List<BaseComponent[]> getAttributes(ItemStylingRule styling, List<CoreModifier> modifiers) {
        // output collection
        List<BaseComponent[]> compiled = new ArrayList<>();
        MagicStringBuilder msb = new MagicStringBuilder();
        // managers that we may need
        LanguageManager language_manager = RPGCore.inst().getLanguageManager();
        ItemManager item_manager = RPGCore.inst().getItemManager();
        ResourcePackManager resource_manager = RPGCore.inst().getResourcePackManager();
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
            msb.shiftCentered((this.width_base/2)-20, Utility.measureWidthExact(translation));
            msb.append(styling.color("category", translation));
            msb.shiftToExact(0);
            compiled.add(msb.compileAndClean());
            // separator below category name
            msb.shiftToExact(-20);
            msb.append(styling.texture("body_separator", 0), ChatColor.WHITE);
            msb.shiftToExact(0);
            compiled.add(msb.compileAndClean());

            for (CoreModifier modifier : mods) {
                List<String> readables = language_manager.getTranslationList(modifier.getLCReadable());
                for (String readable : readables) {
                    // generate the backdrop texture
                    String[] split = readable.split("\\#");
                    msb.shiftToExact(-20);
                    msb.append(styling.texture("body_top", 0), ChatColor.WHITE);
                    // write the primary info of the modifier
                    msb.shiftToExact(-20 + this.padding);
                    msb.append(styling.color("attribute-left", split[0]));
                    // write the secondary info of the modifier
                    if (split.length == 2) {
                        msb.shiftToExact(-20 + this.width_base - this.padding - Utility.measureWidthExact(split[1]));
                        msb.append(styling.color("attribute-right", split[1]));
                    }
                    // append the modifier to our listing
                    msb.shiftToExact(0);
                    compiled.add(msb.compileAndClean());
                }
            }
        });

        return compiled;
    }

    private List<BaseComponent[]> getAbilities(ItemStylingRule styling, List<CoreModifier> modifiers) {
        // output collection
        List<BaseComponent[]> compiled = new ArrayList<>();
        MagicStringBuilder msb = new MagicStringBuilder();
        // managers that we may need
        LanguageManager language_manager = RPGCore.inst().getLanguageManager();
        ItemManager item_manager = RPGCore.inst().getItemManager();
        ResourcePackManager resource_manager = RPGCore.inst().getResourcePackManager();
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
            msb.shiftCentered((this.width_base/2)-20, Utility.measureWidthExact(translation));
            msb.append(styling.color("category", translation));
            msb.shiftToExact(0);
            compiled.add(msb.compileAndClean());
            // separator below category name
            msb.shiftToExact(-20);
            msb.append(styling.texture("body_separator", 0), ChatColor.WHITE);
            msb.shiftToExact(0);
            compiled.add(msb.compileAndClean());

            for (CoreModifier modifier : mods) {
                List<String> readables = language_manager.getTranslationList(modifier.getLCReadable());
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
                    msb.append(styling.color("ability-upper", upper));
                    msb.shiftToExact(0);
                    compiled.add(msb.compileAndClean());
                    // create lower text
                    msb.shiftToExact(-20);
                    msb.append(styling.texture("body_top", 0), ChatColor.WHITE);
                    msb.shiftToExact(-20 + this.padding);
                    msb.append(resource_manager.texture("lore_icon_" + icon), ChatColor.WHITE);
                    msb.shiftToExact(-20 + this.padding + 24);
                    msb.append(styling.color("ability-left", left));
                    if (right != null) {
                        msb.shiftToExact(this.width_base - this.padding - 20 - Utility.measureWidthExact(right));
                        msb.append(styling.color("ability-right", right));
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
            compiled.remove(compiled.size()-1);
        }

        return compiled;
    }

    private List<BaseComponent[]> getDescription(ItemStylingRule styling, List<String> lore) {
        // output collection
        List<BaseComponent[]> compiled = new ArrayList<>();
        MagicStringBuilder msb = new MagicStringBuilder();
        // managers that we may need
        LanguageManager language_manager = RPGCore.inst().getLanguageManager();
        ItemManager item_manager = RPGCore.inst().getItemManager();
        ResourcePackManager resource_manager = RPGCore.inst().getResourcePackManager();

        // [11] lore is available as the last entry.
        if (!lore.isEmpty()) {
            // re-arrange lore to respect text length
            boolean valid = true;
            for (String line : lore) {
                if (Utility.measureWidthExact(line) > 230) {
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
                    if (Utility.measureWidthExact(preview) <= 230) {
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
            msb.shiftCentered((this.width_base/2)-20, Utility.measureWidthExact(translation));
            msb.append(styling.color("category", translation));
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
                msb.shiftCentered((this.width_base/2)-20, Utility.measureWidthExact(line));
                msb.append(styling.color("description", line));
                msb.shiftToExact(0);
                compiled.add(msb.compileAndClean());
            }
        }

        return compiled;
    }

    private List<BaseComponent[]> getJewels(ItemStylingRule styling, int unlocked, int maximum, List<ItemStack> socketed) {
        if (maximum <= 0) {
            return new ArrayList<>();
        }
        // output collection
        List<BaseComponent[]> compiled = new ArrayList<>();
        MagicStringBuilder msb = new MagicStringBuilder();
        // managers that we may need
        LanguageManager language_manager = RPGCore.inst().getLanguageManager();
        ItemManager item_manager = RPGCore.inst().getItemManager();
        ResourcePackManager resource_manager = RPGCore.inst().getResourcePackManager();

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
        int width = symbols.size()*28 + (symbols.size()-1)*2;
        msb.shiftCentered((this.width_base/2)-20, width);
        // render the relevant jewel sockets
        for (String symbol : symbols) {
            IndexedTexture texture = resource_manager.texture("lore_jewel_" + symbol);
            msb.append(texture, ChatColor.WHITE);
            msb.advance(2);
        }
        // offer up the rendering
        msb.shiftToExact(0);
        compiled.add(msb.compileAndClean());

        // commit our changes
        return compiled;
    }

    private List<BaseComponent[]> getFooter(ItemStylingRule styling, double durability) {
        // output collection
        List<BaseComponent[]> compiled = new ArrayList<>();
        MagicStringBuilder msb = new MagicStringBuilder();
        // managers that we may need
        LanguageManager language_manager = RPGCore.inst().getLanguageManager();
        ItemManager item_manager = RPGCore.inst().getItemManager();
        ResourcePackManager resource_manager = RPGCore.inst().getResourcePackManager();

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
        msb.append(styling.color("durability", durability_text + String.format("%.1f%%", durability*100d)), "lore_durability");
        msb.shiftToExact(0);
        compiled.add(msb.compileAndClean());

        return compiled;
    }

    @Override
    public void describe(ItemStack item) {
        LanguageManager language_manager = RPGCore.inst().getLanguageManager();
        ItemManager item_manager = RPGCore.inst().getItemManager();
        ResourcePackManager resource_manager = RPGCore.inst().getResourcePackManager();

        // extract the data we got on this item
        ItemDataGeneric generic_data = item_manager.getItemData(item, ItemDataGeneric.class);
        ItemDataJewel jewel_data = item_manager.getItemData(item, ItemDataJewel.class);
        ItemDataModifier modifier_data = item_manager.getItemData(item, ItemDataModifier.class);
        ItemDataDurability durability_data = item_manager.getItemData(item, ItemDataDurability.class);

        // generic data is present on all rpgcore items
        if (generic_data == null) {
            return;
        }

        // extract the basic information of the item
        CoreItem item_base = generic_data.getItem();
        ItemStylingRule styling = item_manager.getStylingRule(item_base.getStyling());
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
    }
}
