package me.blutkrone.rpgcore.util;

import me.blutkrone.rpgcore.util.fontmagic.DefaultFontInfo;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public final class Utility {

    private static SimpleDateFormat DATE_TIME = new SimpleDateFormat("dd HH-mm-ss");

    private Utility() {

    }

    public static int measureWidthExact(String message) {
        return measureWidth(message) - 1;
    }

    public static int measureWidth(String message) {
        message = ChatColor.translateAlternateColorCodes('&', message);

        int messagePxSize = 0;
        boolean previousCode = false;
        boolean isBold = false;

        for (char c : message.toCharArray()) {
            if (c == '§') {
                previousCode = true;
            } else if (previousCode) {
                previousCode = false;
                isBold = c == 'l' || c == 'L';
            } else {
                messagePxSize += DefaultFontInfo.measureCharacter(c, isBold);
                messagePxSize++;
            }
        }

        return messagePxSize;
    }

    public static List<String> makeCenteredLine(List<String> message) {
        if (message.isEmpty()) return message;
        int length = 0;
        for (String s : message)
            length = Math.max(length, Utility.measureWidth(s));
        List<String> output = new ArrayList<>();
        for (String s : message) {
            output.add(makeCenteredLine(s, length / 2));
        }
        return output;
    }

    public static String makeCenteredLineNoSuffix(String message, int CENTER_PX) {
        if (message == null || message.equals("")) {
            return "";
        }

        int messagePxSize = measureWidth(message);
        message = ChatColor.translateAlternateColorCodes('&', message);

        int halvedMessageSize = messagePxSize / 2;
        int toCompensate = CENTER_PX - halvedMessageSize;
        int spaceLength = DefaultFontInfo.SPACE.getLength() + 1;
        int compensated = 0;
        StringBuilder prefix = new StringBuilder();
        while (compensated < toCompensate) {
            prefix.append(" ");
            compensated += spaceLength;
        }
        return prefix.toString() + message;
    }

    public static String makeCenteredLine(String message, int CENTER_PX) {
        if (message == null || message.equals("")) {
            return "";
        }

        int messagePxSize = measureWidth(message);
        message = ChatColor.translateAlternateColorCodes('&', message);

        int halvedMessageSize = messagePxSize / 2;
        int toCompensate = CENTER_PX - halvedMessageSize;
        int spaceLength = DefaultFontInfo.SPACE.getLength() + 1;
        int compensated = 0;
        StringBuilder prefix = new StringBuilder();
        while (compensated < toCompensate) {
            prefix.append(" ");
            compensated += spaceLength;
        }

        StringBuilder suffix = new StringBuilder();
        int aftercompensated = 0;
        while ((compensated + messagePxSize + aftercompensated) < CENTER_PX * 2) {
            suffix.append(" ");
            aftercompensated += spaceLength;
        }

        return prefix.toString() + message + suffix.toString();
    }

    public static void sendCenteredMessage(Player player, String message) {
        player.sendMessage(makeCenteredLine(message, 154));
    }

    public static List<String> patternize(List<String> patterns, Object... args) {
        List<String> lines = new ArrayList<>();
        for (String pattern : patterns) {
            String updated = pattern;
            for (int i = 0; i < args.length; i++) {
                updated = updated.replace("{" + i + "}", String.valueOf(args[i]));
            }
            lines.add(updated);
        }
        return lines;
    }

    public static String patternize(String pattern, Object... args) {
        for (int i = 0; i < args.length; i++) {
            pattern = pattern.replace("{" + i + "}", String.valueOf(args[i]));
        }
        return pattern;
    }

    public static <T> List<List<T>> partition(List<T> list, int partition) {
        List<List<T>> partitions = new ArrayList<>();
        List<T> current_partition = new ArrayList<>();
        for (T t : list) {
            current_partition.add(t);
            if (current_partition.size() == partition) {
                partitions.add(current_partition);
                current_partition = new ArrayList<>();
            }
        }
        if (!current_partition.isEmpty())
            partitions.add(current_partition);
        return partitions;
    }

    public static String timestamp() {
        return DATE_TIME.format(new Date(System.currentTimeMillis()));
    }

    public static boolean itemNameSimilar(String wanted, ItemStack item) {
        if (item == null) return false;
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta == null || !itemMeta.hasDisplayName()) return false;
        String have = ChatColor.stripColor(itemMeta.getDisplayName());
        return wanted.equals(have);
    }

    public static String makeMMSS(long seconds) {
        long s = seconds % 60;
        long m = (seconds / 60) % 60;
        if (m > 99) m = 99;
        return String.format("%02d:%02d", m, s);
    }

    public static String makeHHMMSS(long seconds) {
        long s = seconds % 60;
        long m = (seconds / 60) % 60;
        long h = (seconds / (60 * 60)) % 24;
        if (h > 99) h = 99;
        return String.format("%d:%02d:%02d", h, m, s);
    }

    public static double distanceSqOrWorld(Entity start, Location finish) {
        if (start.getWorld() != finish.getWorld())
            return Double.MAX_VALUE;
        return start.getLocation().distanceSquared(finish);
    }

    public static double distanceSqOrWorld(Location start, Location finish) {
        if (start == null || finish == null) return Double.MAX_VALUE;
        if (start.getWorld() != finish.getWorld())
            return Double.MAX_VALUE;
        return start.distanceSquared(finish);
    }

    public static double distanceSqOrWorld(Entity start, Entity finish) {
        if (start.getWorld() != finish.getWorld())
            return Double.MAX_VALUE;
        return start.getLocation().distanceSquared(finish.getLocation());
    }

    public static String escapeString(String in) {
        final StringBuilder out = new StringBuilder();
        for (int i = 0; i < in.length(); i++) {
            final char ch = in.charAt(i);
            out.append("\\u").append(String.format("%04x", (int) ch));
        }
        return out.toString();
    }

    public static boolean instanceOf(Object o1, Object o2) {
        return o1.getClass().isInstance(o2);
    }

    public static void removeModifier(AttributeInstance attribute, String identifier) {
        if (attribute == null) return;
        for (AttributeModifier modifier : attribute.getModifiers()) {
            if (!modifier.getName().equals(identifier))
                continue;
            attribute.removeModifier(modifier);
        }
    }

    public static void swapModifier(AttributeInstance attribute, String identifier, double updated, AttributeModifier.Operation operation) {
        if (attribute == null) return;
        for (AttributeModifier modifier : attribute.getModifiers()) {
            if (!modifier.getName().equals(identifier))
                continue;
            attribute.removeModifier(modifier);
        }
        attribute.addModifier(new AttributeModifier(identifier, updated, operation));
    }
}
