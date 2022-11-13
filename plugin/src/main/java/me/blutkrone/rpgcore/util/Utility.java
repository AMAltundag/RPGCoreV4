package me.blutkrone.rpgcore.util;

import me.blutkrone.rpgcore.RPGCore;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import java.text.SimpleDateFormat;

public final class Utility {

    private static final SimpleDateFormat DATE_TIME = new SimpleDateFormat("dd HH-mm-ss");
    private static final Vector AXIS = new Vector(0, 1, 0);

    private Utility() {

    }

    public static boolean isChunkLoaded(Location location) {
        World world = location.getWorld();
        if (world == null) {
            return false;
        }
        int x = location.getBlockX() >> 4;
        int z = location.getBlockZ() >> 4;
        return world.isChunkLoaded(x, z);
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

    public static Vector perpendicular(Vector a, Vector b) {
        // perpendicular 'a' upon 'b'
        return b.clone().subtract(project(a, b));
    }

    public static Vector project(Vector a, Vector b) {
        // project 'a' upon 'b
        return a.clone().multiply(a.dot(b) / a.lengthSquared());
    }

    private static double betweenPointAndLine(double x1, double y1, double z1, double x2, double y2, double z2, double x3, double y3, double z3) {
        double b = Math.sqrt(Math.pow((x2 - x3), 2)
                + Math.pow((y2 - y3), 2)
                + Math.pow((z2 - z3), 2));

        double S = Math.sqrt(Math.pow((y2 - y1) * (z3 - z1) - (z2 - z1) * (y3 - y1), 2) +
                Math.pow((z2 - z1) * (x3 - x1) - (x2 - x1) * (z3 - z1), 2) +
                Math.pow((x2 - x1) * (y3 - y1) - (y2 - y1) * (x3 - x1), 2)) / 2;

        return 2 * S / b;
    }

    public static double betweenPointAndLine(Vector p, Vector s, Vector f) {
        return betweenPointAndLine(p.getX(), p.getY(), p.getZ(), s.getX(), s.getY(), s.getZ(), f.getX(), f.getY(), f.getZ());
    }

    public static double betweenPointAndDirection(Vector point, Vector start, Vector direction, double distance) {
        return betweenPointAndLine(point, start, start.clone().add(direction.clone().multiply(distance)));
    }

    public static Vector drawOnPlane(Location anchor, Vector point, double distance) {
        // normal vector for the plane to draw on
        Vector norm = anchor.getDirection().normalize();
        // origin from which we shall draw
        double oX = distance * norm.getX() + anchor.getX();
        double oY = distance * norm.getY() + anchor.getY();
        double oZ = distance * norm.getZ() + anchor.getZ();
        // construct base vectors for the plane
        Vector axisY = perpendicular(norm, AXIS).normalize();
        Vector axisX = axisY.getCrossProduct(norm).normalize();
        // invert the normal vector we got
        norm.multiply(-1);
        // relative points generated via transform matrix
        double rX = axisX.getX() * point.getX() + axisY.getX() * point.getY() + norm.getX() * point.getZ();
        double rY = axisX.getY() * point.getX() + axisY.getY() * point.getY() + norm.getY() * point.getZ();
        double rZ = axisX.getZ() * point.getX() + axisY.getZ() * point.getY() + norm.getZ() * point.getZ();
        // sum up the constants with our anchor vector
        return new Vector(rX + oX, rY + oY, rZ + oZ);
    }
}
