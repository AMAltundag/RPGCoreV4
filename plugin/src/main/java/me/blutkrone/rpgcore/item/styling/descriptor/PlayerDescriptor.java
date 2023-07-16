package me.blutkrone.rpgcore.item.styling.descriptor;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.chat.PlayerSnapshot;
import me.blutkrone.rpgcore.hud.menu.EquipMenu;
import me.blutkrone.rpgcore.item.CoreItem;
import me.blutkrone.rpgcore.item.ItemManager;
import me.blutkrone.rpgcore.item.modifier.ModifierStyle;
import me.blutkrone.rpgcore.item.styling.StylingRule;
import me.blutkrone.rpgcore.language.LanguageManager;
import me.blutkrone.rpgcore.resourcepack.ResourcePackManager;
import me.blutkrone.rpgcore.resourcepack.utils.IndexedTexture;
import me.blutkrone.rpgcore.util.ItemBuilder;
import me.blutkrone.rpgcore.util.Utility;
import me.blutkrone.rpgcore.util.fontmagic.MagicStringBuilder;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class PlayerDescriptor {

    private static class PseudoModifier {
        private final ModifierStyle style;
        private final String lc_readable;
        private final String lc_category;
        private final Map<String, Double> value;
        private final String string;

        public PseudoModifier(ModifierStyle style, String lc_readable, String lc_category, double value) {
            this.style = style;
            this.lc_readable = lc_readable;
            this.lc_category = lc_category;
            this.value = Collections.singletonMap("AUTO", value);
            this.string = null;
        }

        public PseudoModifier(ModifierStyle style, String lc_readable, String lc_category, String string) {
            this.style = style;
            this.lc_readable = lc_readable;
            this.lc_category = lc_category;
            this.value = null;
            this.string = string;
        }

        public ModifierStyle getStyle() {
            return style;
        }

        public String getLCCategory() {
            return lc_category;
        }

        /**
         * Translate all placeholders and offers the modifier.
         *
         * @return modifiers that were translated.
         */
        public List<String> getReadable() {
            if (this.lc_readable == null) {
                return Collections.singletonList(this.string);
            }

            List<String> template = RPGCore.inst().getLanguageManager().getTranslationList(this.lc_readable);
            template.replaceAll(string -> {
                if (this.value != null) {
                    string = RPGCore.inst().getLanguageManager().formatAsVersatile(string, this.value);
                } else {
                    string = string.replace("{TEXT}", this.string);
                }

                return string;
            });
            return template;
        }
    }

    private static int PADDING = 15;
    private static int WIDTH_BASE = 250;

    private static List<BaseComponent[]> getHeader(StylingRule styling, PlayerSnapshot player) {
        // output collection
        List<BaseComponent[]> compiled = new ArrayList<>();
        MagicStringBuilder msb = new MagicStringBuilder();
        // managers that we may need
        LanguageManager language_manager = RPGCore.inst().getLanguageManager();
        ItemManager item_manager = RPGCore.inst().getItemManager();
        ResourcePackManager resource_manager = RPGCore.inst().getResourcePackManager();
        // additionally processed data
        String item_level_text = "Lv" + language_manager.formatShortNumber(player.level);

        // [1] header segment to hide the vanilla lore
        msb.shiftToExact(-20);
        msb.append(styling.texture("header", 0), ChatColor.WHITE);
        msb.shiftToExact(-20);
        msb.append(styling.texture("header", 1), ChatColor.WHITE);
        // [2] score of the item
        msb.shiftCentered((PlayerDescriptor.WIDTH_BASE / 2) - 20, Utility.measure(item_level_text));
        msb.append(styling.color("score", item_level_text), "lore_score");
        // [3] title texture background
        msb.shiftToExact(-20);
        msb.append(styling.texture("title", 0), ChatColor.WHITE);
        msb.shiftToExact(0);
        compiled.add(msb.compileAndClean());
        // [4] slice of title segment, item name
        msb.shiftToExact(-20);
        msb.append(styling.texture("title", 1), ChatColor.WHITE);
        msb.shiftCentered((PlayerDescriptor.WIDTH_BASE / 2) - 20, Utility.measure(player.alias));
        msb.append(styling.color("upper-name", player.alias), "lore_upper_name");
        msb.shiftToExact(0);
        compiled.add(msb.compileAndClean());
        // [5] slice of title segment, sub name
        msb.shiftToExact(-20);
        msb.append(styling.texture("title", 2), ChatColor.WHITE);
        msb.shiftCentered((PlayerDescriptor.WIDTH_BASE / 2) - 20, Utility.measure(player.user));
        msb.append(styling.color("lower-name", player.user), "lore_lower_name");
        msb.shiftToExact(0);
        compiled.add(msb.compileAndClean());
        // [6] slice of title segment
        msb.shiftToExact(-20);
        msb.append(styling.texture("title", 3), ChatColor.WHITE);
        msb.shiftToExact(0);
        compiled.add(msb.compileAndClean());

        return compiled;
    }

    private static List<BaseComponent[]> getHighlight(StylingRule styling, List<PseudoModifier> modifiers) {
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
                List<String> readables = modifier.getReadable();
                for (String readable : readables) {
                    // generate the backdrop texture
                    String[] split = readable.split("\\#");
                    msb.shiftToExact(-20);
                    msb.append(styling.texture("highlight_top", 0), ChatColor.WHITE);
                    // write the primary info of the modifier
                    msb.shiftToExact(-20 + PlayerDescriptor.PADDING - 5);
                    msb.append(styling.color("highlight-left", split[0]), "default_fixed");
                    // write the secondary info of the modifier
                    if (split.length == 2) {
                        msb.shiftToExact(-20 + PlayerDescriptor.WIDTH_BASE - PlayerDescriptor.PADDING + 5 - Utility.measure(split[1]));
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

    private static List<BaseComponent[]> getInfo(StylingRule styling, List<String> info) {
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
                msb.shiftToExact(-20 + PlayerDescriptor.PADDING);
                msb.append(styling.color("info-left", split[0]), "default_fixed");
                // write the secondary info of the line
                if (split.length == 2) {
                    msb.shiftToExact(-20 + PlayerDescriptor.WIDTH_BASE - PlayerDescriptor.PADDING - Utility.measure(split[1]));
                    msb.append(styling.color("info-right", split[1]), "default_fixed");
                }
                // append the line to our listing
                msb.shiftToExact(0);
                compiled.add(msb.compileAndClean());
            }
        }

        return compiled;
    }

    private static List<BaseComponent[]> getAttributes(StylingRule styling, List<PseudoModifier> modifiers) {
        // output collection
        List<BaseComponent[]> compiled = new ArrayList<>();
        MagicStringBuilder msb = new MagicStringBuilder();
        // managers that we may need
        LanguageManager language_manager = RPGCore.inst().getLanguageManager();
        ItemManager item_manager = RPGCore.inst().getItemManager();
        ResourcePackManager resource_manager = RPGCore.inst().getResourcePackManager();
        // additionally processed data
        Map<String, List<PseudoModifier>> categorized = new HashMap<>();
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
            msb.shiftCentered((PlayerDescriptor.WIDTH_BASE / 2) - 20, Utility.measure(translation));
            msb.append(styling.color("category", translation), "default_fixed");
            msb.shiftToExact(0);
            compiled.add(msb.compileAndClean());
            // separator below category name
            msb.shiftToExact(-20);
            msb.append(styling.texture("body_separator", 0), ChatColor.WHITE);
            msb.shiftToExact(0);
            compiled.add(msb.compileAndClean());

            for (PseudoModifier modifier : mods) {
                List<String> readables = modifier.getReadable();
                for (String readable : readables) {
                    // generate the backdrop texture
                    String[] split = readable.split("\\#");
                    msb.shiftToExact(-20);
                    msb.append(styling.texture("body_top", 0), ChatColor.WHITE);
                    // write the primary info of the modifier
                    msb.shiftToExact(-20 + PlayerDescriptor.PADDING);
                    msb.append(styling.color("attribute-left", split[0]), "default_fixed");
                    // write the secondary info of the modifier
                    if (split.length == 2) {
                        msb.shiftToExact(-20 + PlayerDescriptor.WIDTH_BASE - PlayerDescriptor.PADDING - Utility.measure(split[1]));
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

    private static List<BaseComponent[]> getSkillbar(StylingRule styling, int maximum, List<String> skills) {
        if (maximum <= 0 || skills.isEmpty()) {
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
        List<String> symbols = new ArrayList<>(skills);

        // compute relevant centering
        int width = symbols.size() * 24 + (symbols.size() - 1) * 2;
        msb.shiftCentered((PlayerDescriptor.WIDTH_BASE / 2) - 20, width);
        // render the relevant jewel sockets
        for (String symbol : symbols) {
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

    private static List<BaseComponent[]> getSkills(StylingRule styling, List<PseudoModifier> modifiers) {
        // output collection
        List<BaseComponent[]> compiled = new ArrayList<>();
        MagicStringBuilder msb = new MagicStringBuilder();
        // managers that we may need
        LanguageManager language_manager = RPGCore.inst().getLanguageManager();
        ItemManager item_manager = RPGCore.inst().getItemManager();
        ResourcePackManager resource_manager = RPGCore.inst().getResourcePackManager();
        // additionally processed data
        Map<String, List<PseudoModifier>> categorized = new HashMap<>();
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
            msb.shiftCentered((PlayerDescriptor.WIDTH_BASE / 2) - 20, Utility.measure(translation));
            msb.append(styling.color("category", translation), "default_fixed");
            msb.shiftToExact(0);
            compiled.add(msb.compileAndClean());
            // separator below category name
            msb.shiftToExact(-20);
            msb.append(styling.texture("body_separator", 0), ChatColor.WHITE);
            msb.shiftToExact(0);
            compiled.add(msb.compileAndClean());

            for (PseudoModifier modifier : mods) {
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
                    msb.shiftToExact(-20 + PlayerDescriptor.PADDING + 24);
                    msb.append(styling.color("ability-upper", upper), "default_fixed");
                    msb.shiftToExact(0);
                    compiled.add(msb.compileAndClean());
                    // create lower text
                    msb.shiftToExact(-20);
                    msb.append(styling.texture("body_top", 0), ChatColor.WHITE);
                    msb.shiftToExact(-20 + PlayerDescriptor.PADDING);
                    msb.append(resource_manager.texture("lore_" + icon), ChatColor.WHITE);
                    msb.shiftToExact(-20 + PlayerDescriptor.PADDING + 24);
                    msb.append(styling.color("ability-left", left), "default_fixed");
                    if (right != null) {
                        msb.shiftToExact(PlayerDescriptor.WIDTH_BASE - PlayerDescriptor.PADDING - 20 - Utility.measure(right));
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

    private static List<BaseComponent[]> getDescription(StylingRule styling, List<String> lore) {
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
            msb.shiftCentered((PlayerDescriptor.WIDTH_BASE / 2) - 20, Utility.measure(translation));
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
                msb.shiftCentered((PlayerDescriptor.WIDTH_BASE / 2) - 20, Utility.measure(line));
                msb.append(styling.color("description", line), "default_fixed");
                msb.shiftToExact(0);
                compiled.add(msb.compileAndClean());
            }
        }

        return compiled;
    }

    private static List<BaseComponent[]> getFooter(StylingRule styling, PlayerSnapshot player) {
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
        msb.shiftToExact(-20 + PlayerDescriptor.PADDING);
        //msb.append(styling.color("durability", durability_text + String.format("%.1f%%", durability * 100d)), "lore_durability");
        msb.shiftToExact(0);
        compiled.add(msb.compileAndClean());

        return compiled;
    }

    /**
     * Create a description item for the given player.
     *
     * @param player  Player to describe
     * @param styling Style to utilise
     * @return Description item
     */
    public static ItemStack describe(PlayerSnapshot player, StylingRule styling) {
        LanguageManager language_manager = RPGCore.inst().getLanguageManager();
        ItemManager item_manager = RPGCore.inst().getItemManager();
        ResourcePackManager resource_manager = RPGCore.inst().getResourcePackManager();

        // pseudo modifiers holding player information
        List<PseudoModifier> modifiers = new ArrayList<>();
        modifiers.add(new PseudoModifier(ModifierStyle.HEADER, "chat_tooltip_level", "category", player.level));
        if (player.job.equals("nothing")) {
            String name = RPGCore.inst().getJobManager().getIndexJob().get(player.job).getEmblemIcon().getItemMeta().getDisplayName();
            modifiers.add(new PseudoModifier(ModifierStyle.HEADER, "chat_tooltip_job", "category", name));
        }
        double total_ilvl = 0;
        double count_ilvl = 0;
        for (EquipMenu.Slot slot : RPGCore.inst().getHUDManager().getEquipMenu().slots) {
            count_ilvl += 1;
            ItemStack stack = player.equipment.get(slot.id);
            if (stack != null) {
                CoreItem core_item = RPGCore.inst().getItemManager().getItemFrom(stack).orElse(null);
                if (core_item != null) {
                    total_ilvl += core_item.getItemLevel();
                }
            }
        }
        modifiers.add(new PseudoModifier(ModifierStyle.HEADER, "chat_tooltip_equip", "category", total_ilvl / count_ilvl));
        modifiers.add(new PseudoModifier(ModifierStyle.HEADER, "chat_tooltip_title", "category", "???"));
        modifiers.add(new PseudoModifier(ModifierStyle.HEADER, "chat_tooltip_guild", "category", "???"));

        // todo: not implemented (title in chat)
        // todo: not implemented (guild of player)

        List<String> info = new ArrayList<>();
        List<String> description = new ArrayList<>();

        // generate the appropriate lore for the item
        List<BaseComponent[]> compiled = new ArrayList<>();
        compiled.addAll(PlayerDescriptor.getHeader(styling, player));
        compiled.addAll(PlayerDescriptor.getHighlight(styling, modifiers));
        compiled.addAll(PlayerDescriptor.getInfo(styling, info));
        compiled.addAll(PlayerDescriptor.getAttributes(styling, modifiers));
        compiled.addAll(PlayerDescriptor.getSkills(styling, modifiers));
        compiled.addAll(PlayerDescriptor.getDescription(styling, description));
        compiled.addAll(PlayerDescriptor.getSkillbar(styling, 6, Arrays.asList(player.skillbar)));
        compiled.addAll(PlayerDescriptor.getFooter(styling, player));

        // strip the italics away
        for (BaseComponent[] components : compiled) {
            for (BaseComponent component : components) {
                component.setItalic(false);
            }
        }

        // apply the lore to the item
        ItemStack item = ItemBuilder.of(Material.IRON_AXE).flag(ItemFlag.values()).build();
        RPGCore.inst().getVolatileManager().setItemLore(item, compiled);
        RPGCore.inst().getVolatileManager().setItemName(item, new MagicStringBuilder().compile());
        return item;
    }
}
