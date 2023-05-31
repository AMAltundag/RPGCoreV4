package me.blutkrone.rpgcore.item.styling.descriptor;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.api.item.IItemDescriber;
import me.blutkrone.rpgcore.damage.DamageMetric;
import me.blutkrone.rpgcore.item.CoreItem;
import me.blutkrone.rpgcore.item.ItemManager;
import me.blutkrone.rpgcore.item.data.ItemDataGeneric;
import me.blutkrone.rpgcore.item.modifier.ModifierStyle;
import me.blutkrone.rpgcore.item.styling.IDescriptionRequester;
import me.blutkrone.rpgcore.item.styling.StylingRule;
import me.blutkrone.rpgcore.language.LanguageManager;
import me.blutkrone.rpgcore.resourcepack.ResourcePackManager;
import me.blutkrone.rpgcore.resourcepack.utils.IndexedTexture;
import me.blutkrone.rpgcore.skill.CoreSkill;
import me.blutkrone.rpgcore.skill.SkillManager;
import me.blutkrone.rpgcore.skill.info.CoreSkillInfo;
import me.blutkrone.rpgcore.util.Utility;
import me.blutkrone.rpgcore.util.fontmagic.MagicStringBuilder;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * Descriptor meant for itemized skills specifically.
 */
public class SkillDescriptor implements IItemDescriber {

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

    private List<BaseComponent[]> getHighlight(IDescriptionRequester viewer, StylingRule styling, List<CoreSkillInfo> modifiers) {
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
                List<String> readables = modifier.getLCReadable(viewer);
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

    private List<BaseComponent[]> getTags(IDescriptionRequester viewer, StylingRule styling, List<String> tags) {
        // output collection
        List<BaseComponent[]> compiled = new ArrayList<>();
        MagicStringBuilder msb = new MagicStringBuilder();
        // managers that we may need
        LanguageManager language_manager = RPGCore.inst().getLanguageManager();
        ItemManager item_manager = RPGCore.inst().getItemManager();
        ResourcePackManager resource_manager = RPGCore.inst().getResourcePackManager();

        // [11] lore is available as the last entry.
        List<String> collapsed = new ArrayList<>();
        int length = 0;
        String line = "";
        for (String tag : tags) {
            tag = RPGCore.inst().getLanguageManager().getTranslation("skill_tag_" + tag.toLowerCase());
            int len = Utility.measure(tag + " ");
            if ((length + len) > 240) {
                length = 0;
                collapsed.add(line); //
            }
            if (!line.isEmpty()) {
                line += ", ";
            }
            line += tag;
            length += len;
        }
        if (length > 0) {
            collapsed.add(line);
        }

        for (String s : collapsed) {
            // generate the backdrop texture
            msb.shiftToExact(-20);
            msb.append(styling.texture("body_top", 0), ChatColor.WHITE);
            // write the primary info of the line
            msb.shiftToExact(-20 + this.padding);
            msb.append(styling.color("info-left", s), "default_fixed");
            // append the line to our listing
            msb.shiftToExact(0);
            compiled.add(msb.compileAndClean());
        }

        return compiled;
    }

    private List<BaseComponent[]> getAttributes(IDescriptionRequester viewer, StylingRule styling, List<CoreSkillInfo> modifiers) {
        // output collection
        List<BaseComponent[]> compiled = new ArrayList<>();
        MagicStringBuilder msb = new MagicStringBuilder();
        // managers that we may need
        LanguageManager language_manager = RPGCore.inst().getLanguageManager();
        ItemManager item_manager = RPGCore.inst().getItemManager();
        ResourcePackManager resource_manager = RPGCore.inst().getResourcePackManager();
        // additionally processed data
        Map<String, List<CoreSkillInfo>> categorized = new HashMap<>();
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

            for (CoreSkillInfo modifier : mods) {
                List<String> readables = modifier.getLCReadable(viewer);
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

    private List<BaseComponent[]> getAbilities(IDescriptionRequester viewer, StylingRule styling, List<CoreSkillInfo> modifiers) {
        // output collection
        List<BaseComponent[]> compiled = new ArrayList<>();
        MagicStringBuilder msb = new MagicStringBuilder();
        // managers that we may need
        LanguageManager language_manager = RPGCore.inst().getLanguageManager();
        ItemManager item_manager = RPGCore.inst().getItemManager();
        ResourcePackManager resource_manager = RPGCore.inst().getResourcePackManager();
        // additionally processed data
        Map<String, List<CoreSkillInfo>> categorized = new HashMap<>();
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

            for (CoreSkillInfo modifier : mods) {
                List<String> readables = modifier.getLCReadable(viewer);
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

    private List<BaseComponent[]> getLinked(IDescriptionRequester viewer, StylingRule styling, List<String> skills) {
        if (skills.size() <= 0) {
            return new ArrayList<>();
        }
        // output collection
        List<BaseComponent[]> compiled = new ArrayList<>();
        MagicStringBuilder msb = new MagicStringBuilder();
        // managers that we may need
        LanguageManager language_manager = RPGCore.inst().getLanguageManager();
        ItemManager item_manager = RPGCore.inst().getItemManager();
        ResourcePackManager resource_manager = RPGCore.inst().getResourcePackManager();

        // separator above category name
        msb.shiftToExact(-20);
        msb.append(styling.texture("body_separator", 0), ChatColor.WHITE);
        msb.shiftToExact(0);
        compiled.add(msb.compileAndClean());
        // backdrop + name of category
        msb.shiftToExact(-20);
        msb.append(styling.texture("body_top", 0), ChatColor.WHITE);
        String translation = language_manager.getTranslation("lc_category_skill_linked");
        msb.shiftCentered((this.width_base / 2) - 20, Utility.measure(translation));
        msb.append(styling.color("category", translation), "default_fixed");
        msb.shiftToExact(0);
        compiled.add(msb.compileAndClean());
        // separator below category name
        msb.shiftToExact(-20);
        msb.append(styling.texture("body_separator", 0), ChatColor.WHITE);
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

        // compute relevant centering
        int width = skills.size() * 24 + (skills.size() - 1) * 2;
        msb.shiftCentered((this.width_base / 2) - 20, width);
        // render the relevant jewel sockets
        for (String symbol : skills) {
            IndexedTexture texture = resource_manager.texture("lore_skill_" + symbol);
            msb.append(texture, ChatColor.WHITE);
            msb.advance(2);
        }
        // offer up the rendering
        msb.shiftToExact(0);
        compiled.add(msb.compileAndClean());

        // commit our changes
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
        if (viewer != null) {
            String player_info_text = language_manager.getTranslation("lc_skill_item_attuned_yes");
            msb.append(styling.color("durability", player_info_text), "lore_durability");
        } else {
            String player_info_text = language_manager.getTranslation("lc_skill_item_attuned_no");
            msb.append(styling.color("durability", player_info_text), "lore_durability");
        }
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

        // extract the data we got on this item
        ItemDataGeneric generic_data = item_manager.getItemData(item, ItemDataGeneric.class);

        // do not generate data with players
        if (player == null) {
            return;
        }

        // generic data is present on all rpgcore items
        if (generic_data == null) {
            return;
        }

        // extract the basic information of the item
        CoreItem item_base = generic_data.getItem();
        StylingRule styling = item_manager.getStylingRule(item_base.getStyling());

        // extract the skill backed by the item
        CoreSkill skill = item_base.getHidden("skill").map(id -> skill_manager.getIndex().get(id)).orElse(null);
        if (skill == null) {
            return;
        }

        // unfold information we do care about
        List<CoreSkillInfo> skill_info = skill.getInfo();
        String name = skill.getName();
        String sub_name = skill.getSubName();
        List<String> description = skill.getDescription();
        List<String> tags = skill.getTags();
        List<String> linked = new ArrayList<>();
        Map<Long, ItemStack> socketed = player.getPassiveSocketed().get("skill_" + skill.getId().toLowerCase());
        if (socketed != null) {
            for (ItemStack stack : socketed.values()) {
                ItemDataGeneric data = RPGCore.inst().getItemManager().getItemData(stack, ItemDataGeneric.class);
                if (data != null) {
                    data.getItem().getHidden("skill").ifPresent(linked::add);
                }
            }
        }
        // track damage metrics
        for (String element : RPGCore.inst().getDamageManager().getElementIds()) {
            DamageMetric metric = player.getMetric("skill_" + skill.getId() + "_" + element);
            if (metric != null && !metric.isEmpty()) {
                double[] range = metric.getAsRange();
                skill_info.add(new CoreSkillInfo.CoreDamageInfo("lc_category_skill_damage", ModifierStyle.GENERIC,
                        "lc_skill_info_damage_" + element, range[0], range[1]));
            }
        }

        // generate the appropriate lore for the item
        List<BaseComponent[]> compiled = new ArrayList<>();
        if (styling != null) {
            // populate with the relevant parts
            compiled.addAll(this.getHeader(player, styling, name, sub_name));
            compiled.addAll(this.getHighlight(player, styling, skill_info));
            compiled.addAll(this.getTags(player, styling, tags));
            compiled.addAll(this.getAttributes(player, styling, skill_info));
            compiled.addAll(this.getAbilities(player, styling, skill_info));
            compiled.addAll(this.getDescription(player, styling, description));
            compiled.addAll(this.getLinked(player, styling, linked));
            compiled.addAll(this.getFooter(player, styling));
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
