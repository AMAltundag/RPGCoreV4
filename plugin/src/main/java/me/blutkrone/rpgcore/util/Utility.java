package me.blutkrone.rpgcore.util;

import me.blutkrone.rpgcore.RPGCore;
import org.bukkit.Location;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Entity;

import java.text.SimpleDateFormat;

public final class Utility {

    private static SimpleDateFormat DATE_TIME = new SimpleDateFormat("dd HH-mm-ss");

    private Utility() {

    }

    public static int measureWidthExact(String message) {
        return measureWidth(message) - 1;
    }

    public static int measureWidth(String message) {
        return RPGCore.inst().getResourcePackManager().measure(message);
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
