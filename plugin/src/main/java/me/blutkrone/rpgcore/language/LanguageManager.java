package me.blutkrone.rpgcore.language;

import com.google.common.collect.Lists;
import me.blutkrone.rpgcore.exception.MalformedPatternException;
import me.blutkrone.rpgcore.language.pattern.AttributePattern;
import me.blutkrone.rpgcore.language.pattern.NumberPattern;
import me.blutkrone.rpgcore.util.ItemBuilder;
import me.blutkrone.rpgcore.util.io.ConfigWrapper;
import me.blutkrone.rpgcore.util.io.FileUtil;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * A manager responsible for everything text specific.
 */
public class LanguageManager {

    // string mapped language constants
    private Map<String, List<String>> translation = new HashMap<>();

    public LanguageManager() {
        try {
            for (File file : FileUtil.buildAllFiles(FileUtil.directory("language"))) {
                ConfigWrapper config = FileUtil.asConfigYML(file);


                // read all language patterns from the files
                config.forEachWithSelf((key, root) -> {
                    if (root.isList(key)) {
                        this.translation.put(key.toLowerCase(), root.getStringList(key));
                    } else {
                        this.translation.put(key.toLowerCase(), Lists.newArrayList(root.getString(key, "undefined")));
                    }
                });
            }

            // resolve color rules on all translations
            for (List<String> strings : this.translation.values()) {
                strings.replaceAll((string -> ChatColor.translateAlternateColorCodes('&', string)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This is NOT going to persist the language entry, this is
     * only added to give the language manager a way to handle
     * the editor creating new language entries.
     *
     * @param translation the translation element
     */
    public void addStub(String translation) {
        translation = translation.toLowerCase();
        this.translation.put(translation, new ArrayList<>(Collections.singletonList("stub=" + translation)));
    }

    /**
     * A collection of all translated keys.
     *
     * @return all translated keys
     */
    public Set<String> getTranslated() {
        return translation.keySet();
    }

    /**
     * Format ticks so they are displayed in 3 letters, up to
     * a maximum of 99 days.
     *
     * @param time ticks to format
     * @return a short format on the cooldown
     */
    public String formatShortTicks(int time) {
        // format by seconds
        int seconds = Math.max(1, time / 20);
        if (seconds <= 60)
            return String.format("%ss", seconds);
        // format by minutes
        int minutes = seconds / 60;
        if (minutes <= 60)
            return String.format("%sm", minutes);
        // format by hours
        int hours = minutes / 60;
        if (hours <= 99)
            return String.format("%sh", Math.min(99, hours));
        // format by days
        return String.format("%sd", Math.min(99, hours / 24));
    }

    /**
     * Format time by the greatest unit, of either
     * seconds, minutes or hours. Capped at 99h at
     * most.
     *
     * @param time ticks to format
     * @return formatted string.
     */
    public String formatShortTime(int time) {
        // format by seconds
        int seconds = Math.max(1, time / 20);
        if (seconds <= 60)
            return String.format("%02d:%02d", 0, seconds);
        // format by minutes
        int minutes = seconds / 60;
        if (minutes <= 60)
            return String.format("%02d:%02d", minutes, seconds % 60);
        // time is hidden if longer then a hour
        return "";
    }

    /**
     * Format time by the greatest unit in milliseconds.
     *
     * @param time milliseconds to format
     * @return formatted string.
     */
    public String formatMillis(long time) {
        time = time / 1000;
        if (time <= 60) {
            return time + "s";
        }
        time = time / 60;
        if (time <= 60) {
            return time + "m";
        }
        time = time / 60;
        if (time <= 24) {
            return time + "h";
        }
        time = time / 24;
        return time + "d";
    }

    /**
     * Format a number so they are displayed in 3 letters, up to
     * a maximum of 99 million.
     *
     * @param number the number we format.
     * @return formatted string.
     */
    public String formatShortNumber(int number) {
        // format by 0-999
        if (number <= 999)
            return String.valueOf(number);
        // format by 1.0k-9.9k
        if (number <= 9999)
            return String.format("%.1fk", number / 1_000d);
        // format by 10k-99k
        if (number <= 99999)
            return (number / 1000) + "k";
        // format by 0.1m-9.9m
        if (number <= 9_999_999)
            return String.format("%.1fm", number / 1_000_000d);
        // format by 10m-99m
        if (number > 99_999_999)
            number = 99_999_999;
        return (number / 1_000_000) + "m";
    }

    /**
     * Retrieve all translation elements.
     *
     * @param key the keying to resolve
     * @return the translation elements retrieved
     */
    public List<String> getTranslationList(String key) {
        return new ArrayList<>(translation.computeIfAbsent(key.toLowerCase(), (k -> new ArrayList<>(Collections.singletonList("missing translation " + key)))));
    }

    /**
     * Retrieve first translation element.
     *
     * @param key the keying to resolve
     * @return the translation element retrieved
     */
    public String getTranslation(String key) {
        List<String> value = translation.computeIfAbsent(key.toLowerCase(), (k -> new ArrayList<>(Collections.singletonList("missing translation " + key))));
        return value.isEmpty() ? "missing translation " + key : value.get(0);
    }

    /**
     * Turn the first translation item into an item, should we fail
     * in doing so we receive a fallback item instead.
     *
     * @param key the keying to resolve.
     * @return an itemized translation element.
     */
    public ItemBuilder getAsItem(String key, Object... args) {
        key = key.toLowerCase();
        // fetch the entire translation
        List<String> translation = new ArrayList<>(getTranslationList(key));
        for (int i = 0; i < args.length; i++) {
            String keying = "{" + i + "}";
            String value = String.valueOf(args[i]);
            translation.replaceAll((line -> line.replace(keying, value)));
        }

        // head with a certain keying
        if (translation.isEmpty()) {
            return ItemBuilder.of(Material.BARRIER).name("§cmissing translation " + key);
        }

        // pop first element as material
        try {
            ItemBuilder builder = ItemBuilder.of(translation.remove(0));
            if (translation.isEmpty()) {
                return builder;
            } else {
                return builder.name(translation.remove(0)).lore(translation);
            }
        } catch (Exception e) {
            return ItemBuilder.of(Material.BARRIER).name("§cbad translation " + key);
        }
    }

    /*
     * Transform the given parameter into a pattern.
     *
     * @param pattern raw form to transform
     * @return the resulting pattern
     */
    private AttributePattern[] parseAttributePattern(String pattern) throws MalformedPatternException {
        // if no pattern is given, note is as a null pattern
        if (pattern == null) {
            return new AttributePattern[]{
                    new AttributePattern.Constant("NULL")
            };
        }
        // extract the pattern from the string
        List<AttributePattern> output = new ArrayList<>();
        boolean open = false;
        StringBuilder current = new StringBuilder();
        for (char c : pattern.toCharArray()) {
            if (c == '{') {
                output.add(new AttributePattern.Constant(current.toString()));
                current = new StringBuilder();
                open = true;
            } else if (c == '}') {
                output.add(new AttributePattern.Variable(current.toString()));
                current = new StringBuilder();
                open = false;
            } else {
                current.append(c);
            }
        }
        // ensure that the pattern was closed properly
        if (open) {
            throw new MalformedPatternException(String.format("Malformed language pattern '%s'", pattern));
        }
        // push the left-over as a constant object
        if (current.length() != 0) {
            output.add(new AttributePattern.Constant(current.toString()));
        }
        // offer up the output as an array
        return output.toArray(new AttributePattern[0]);
    }

    /*
     * Transform the given parameter into a pattern.
     *
     * @param pattern raw form to transform
     * @return the resulting pattern
     */
    private NumberPattern[] parseNumberPattern(String pattern) throws MalformedPatternException {
        // if no pattern is given, note is as a null pattern
        if (pattern == null) {
            return new NumberPattern[]{
                    new NumberPattern.Constant("NULL")
            };
        }
        // extract the pattern from the string
        List<NumberPattern> output = new ArrayList<>();
        boolean open = false;
        StringBuilder current = new StringBuilder();
        for (char c : pattern.toCharArray()) {
            if (c == '{') {
                output.add(new NumberPattern.Constant(current.toString()));
                current = new StringBuilder();
                open = true;
            } else if (c == '}') {
                output.add(new NumberPattern.Variable(current.toString()));
                current = new StringBuilder();
                open = false;
            } else {
                current.append(c);
            }
        }
        // ensure that the pattern was closed properly
        if (open) {
            throw new MalformedPatternException(String.format("Malformed language pattern '%s'", pattern));
        }
        // push the left-over as a constant object
        if (current.length() != 0) {
            output.add(new NumberPattern.Constant(current.toString()));
        }
        // offer up the output as an array
        return output.toArray(new NumberPattern[0]);
    }
}
