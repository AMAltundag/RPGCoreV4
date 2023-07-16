package me.blutkrone.rpgcore.item.styling.descriptor;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.api.item.IItemDescriber;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.hud.menu.StatusMenu;
import me.blutkrone.rpgcore.item.ItemManager;
import me.blutkrone.rpgcore.item.modifier.ModifierStyle;
import me.blutkrone.rpgcore.item.styling.IDescriptionRequester;
import me.blutkrone.rpgcore.item.styling.StylingRule;
import me.blutkrone.rpgcore.language.LanguageManager;
import me.blutkrone.rpgcore.nms.api.menu.IChestMenu;
import me.blutkrone.rpgcore.resourcepack.ResourcePackManager;
import me.blutkrone.rpgcore.skill.SkillManager;
import me.blutkrone.rpgcore.util.ItemBuilder;
import me.blutkrone.rpgcore.util.Utility;
import me.blutkrone.rpgcore.util.fontmagic.MagicStringBuilder;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * Descriptor meant for status menu to show player attributes
 */
public class StatusDescriptor implements IItemDescriber {

    private int padding = 15;
    private int width_base = 250;

    private List<BaseComponent[]> getHeader(IDescriptionRequester viewer, StylingRule styling, String name, String sub_name) {
        // output collection
        List<BaseComponent[]> compiled = new ArrayList<>();
        MagicStringBuilder msb = new MagicStringBuilder();
        // managers that we may need
        LanguageManager language_manager = RPGCore.inst().getLanguageManager();
        ItemManager item_manager = RPGCore.inst().getItemManager();
        ResourcePackManager resource_manager = RPGCore.inst().getResourcePackManager();

        // [1] header segment to hide the vanilla lore
        msb.shiftToExact(-20);
        msb.append(styling.texture("header", 0), ChatColor.WHITE);
        msb.shiftToExact(-20);
        msb.append(styling.texture("header", 1), ChatColor.WHITE);
        // [2] score of the item

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

    private List<BaseComponent[]> getHighlight(IDescriptionRequester viewer, StylingRule styling, List<StatusMenu.StatusInfo> modifiers) {
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
            ModifierStyle style = modifier.getReadableStyle();
            if (style == ModifierStyle.HEADER) {
                // generate the backdrop texture
                msb.shiftToExact(-20);
                msb.append(styling.texture("highlight_top", 0), ChatColor.WHITE);
                // write the primary info of the modifier
                msb.shiftToExact(-20 + this.padding - 5);
                msb.append(styling.color("highlight-left", modifier.getTextLeft()), "default_fixed");
                // write the secondary info of the modifier
                msb.shiftToExact(-20 + this.width_base - this.padding + 5 - Utility.measure(modifier.getTextRight()));
                msb.append(styling.color("highlight-right", modifier.getTextRight()), "default_fixed");
                // append the modifier to our listing
                msb.shiftToExact(0);
                compiled.add(msb.compileAndClean());
                // add a separator behind the modifier
                msb.shiftToExact(-20);
                msb.append(styling.texture("highlight_separator", 0), ChatColor.WHITE);
                msb.shiftToExact(0);
                compiled.add(msb.compileAndClean());

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

    private List<BaseComponent[]> getAttributes(IDescriptionRequester viewer, StylingRule styling, List<StatusMenu.StatusInfo> modifiers) {
        // output collection
        List<BaseComponent[]> compiled = new ArrayList<>();
        MagicStringBuilder msb = new MagicStringBuilder();
        // managers that we may need
        LanguageManager language_manager = RPGCore.inst().getLanguageManager();
        ItemManager item_manager = RPGCore.inst().getItemManager();
        ResourcePackManager resource_manager = RPGCore.inst().getResourcePackManager();
        // additionally processed data
        Map<String, List<StatusMenu.StatusInfo>> categorized = new LinkedHashMap<>();
        modifiers.removeIf(modifier -> {
            if (modifier.getReadableStyle() == ModifierStyle.GENERIC) {
                categorized.computeIfAbsent(modifier.getCategory(), (k -> new ArrayList<>())).add(modifier);
                return true;
            } else {
                return false;
            }
        });

        // [9] write modifiers to format as an attribute
        categorized.forEach((category, mods) -> {
            if (!category.isEmpty()) {
                // separator above category name
                msb.shiftToExact(-20);
                msb.append(styling.texture("body_separator", 0), ChatColor.WHITE);
                msb.shiftToExact(0);
                compiled.add(msb.compileAndClean());
                // backdrop + name of category
                msb.shiftToExact(-20);
                msb.append(styling.texture("body_top", 0), ChatColor.WHITE);
                msb.shiftCentered((this.width_base / 2) - 20, Utility.measure(category));
                msb.append(styling.color("category", category), "default_fixed");
                msb.shiftToExact(0);
                compiled.add(msb.compileAndClean());
                // separator below category name
                msb.shiftToExact(-20);
                msb.append(styling.texture("body_separator", 0), ChatColor.WHITE);
                msb.shiftToExact(0);
                compiled.add(msb.compileAndClean());
            }

            for (StatusMenu.StatusInfo modifier : mods) {
                // generate the backdrop texture
                msb.shiftToExact(-20);
                msb.append(styling.texture("body_top", 0), ChatColor.WHITE);
                // write the primary info of the modifier
                msb.shiftToExact(-20 + this.padding);
                msb.append(styling.color("attribute-left", modifier.getTextLeft()), "default_fixed");
                // write the secondary info of the modifier
                msb.shiftToExact(-20 + this.width_base - this.padding - Utility.measure(modifier.getTextRight()));
                msb.append(styling.color("attribute-right", modifier.getTextRight()), "default_fixed");
                // append the modifier to our listing
                msb.shiftToExact(0);
                compiled.add(msb.compileAndClean());
            }
        });

        return compiled;
    }

    private List<BaseComponent[]> getDescription(IDescriptionRequester viewer, StylingRule styling, List<String> lore) {
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

    private List<BaseComponent[]> getFooter(IDescriptionRequester viewer, StylingRule styling) {
        // managers that we may need
        LanguageManager language_manager = RPGCore.inst().getLanguageManager();
        ItemManager item_manager = RPGCore.inst().getItemManager();
        ResourcePackManager resource_manager = RPGCore.inst().getResourcePackManager();

        // output collection
        List<BaseComponent[]> compiled = new ArrayList<>();
        MagicStringBuilder msb = new MagicStringBuilder();
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
        msb.shiftToExact(-20 + this.padding);
        msb.shiftToExact(0);
        compiled.add(msb.compileAndClean());
        return compiled;
    }

    @Override
    public void describe(ItemStack item, IDescriptionRequester player) {
        LanguageManager language_manager = RPGCore.inst().getLanguageManager();
        ItemManager item_manager = RPGCore.inst().getItemManager();
        ResourcePackManager resource_manager = RPGCore.inst().getResourcePackManager();
        SkillManager skill_manager = RPGCore.inst().getSkillManager();

        // do not generate data without a player
        if (!(player instanceof CorePlayer)) {
            return;
        }

        // extract the relevant parameters
        String style = IChestMenu.getBrand(item, RPGCore.inst(), "style", null);
        String icon = IChestMenu.getBrand(item, RPGCore.inst(), "icon", null);
        String attributes = IChestMenu.getBrand(item, RPGCore.inst(), "attributes", null);
        if (icon == null || attributes == null || style == null) {
            return;
        }

        // extract the basic information of the item
        StylingRule styling = item_manager.getStylingRule(style);
        List<String> icon_info = language_manager.getTranslationList(icon);

        // apply the icon
        ItemBuilder.of(item).inheritIcon(ItemBuilder.of(icon_info.remove(0)).build()).build();

        // unfold information we do care about
        String name = icon_info.remove(0);
        String sub_name = icon_info.remove(0);
        List<StatusMenu.StatusInfo> status_info = RPGCore.inst().getHUDManager().getStatusMenu().getStatusInfo().computeIfAbsent(attributes, (keying -> {
            List<StatusMenu.StatusInfo> modifiers = new ArrayList<>();
            for (String serialized : language_manager.getTranslationList(keying)) {
                try {
                    modifiers.add(new StatusMenu.StatusInfo(serialized));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return modifiers;
        }));

        // dupe modifiers to write attributes into them
        status_info = new ArrayList<>(status_info);
        status_info.replaceAll((info -> info.with((CorePlayer) player)));
        status_info.removeIf(StatusMenu.StatusInfo::isCompress);

        // generate the appropriate lore for the item
        List<BaseComponent[]> compiled = new ArrayList<>();
        if (styling != null) {
            // populate with the relevant parts
            compiled.addAll(this.getHeader(player, styling, name, sub_name));
            compiled.addAll(this.getHighlight(player, styling, status_info));
            compiled.addAll(this.getAttributes(player, styling, status_info));
            compiled.addAll(this.getDescription(player, styling, icon_info));
            compiled.addAll(this.getFooter(player, styling));
        } else {
            compiled.add(TextComponent.fromLegacyText("§cStyling rule '" + style + "' does not exist!"));
        }

        // fix the unwanted italics
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
