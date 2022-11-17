package me.blutkrone.rpgcore.util.fontmagic;

import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

public final class FontMagicConstant {
    private static final Object THREAD_SAFETY = new Object();
    private static NavigableMap<Integer, Character> advance;
    private static NavigableMap<Integer, Character> retreat;

    private FontMagicConstant() {
    }

    public static String advance(int depth) {
        if (advance == null) {
            synchronized (THREAD_SAFETY) {
                if (advance == null) init();
            }
        }
        if (depth == 0) return "";
        if (depth < 0) throw new IllegalArgumentException("Bad depth: " + depth);
        StringBuilder output = new StringBuilder();
        while (depth > 0) {
            Map.Entry<Integer, Character> current = advance.floorEntry(depth);
            output.append(current.getValue());
            depth -= current.getKey();
        }


        return output.toString();
    }

    public static String retreat(int depth) {
        if (advance == null) {
            synchronized (THREAD_SAFETY) {
                if (advance == null) init();
            }
        }
        if (depth == 0) return "";
        if (depth < 0) throw new IllegalArgumentException("Bad depth: " + depth);
        StringBuilder output = new StringBuilder();
        while (depth > 0) {
            Map.Entry<Integer, Character> current = retreat.floorEntry(depth);
            output.append(current.getValue());
            depth -= current.getKey();
        }
        return output.toString();
    }

    private static void init() {
        advance = new TreeMap<>();
        advance.put(1024, '\uF82F');
        advance.put(512, '\uF82E');
        advance.put(256, '\uF82D');
        advance.put(128, '\uF82C');
        advance.put(64, '\uF82B');
        advance.put(32, '\uF82A');
        advance.put(16, '\uF829');
        advance.put(8, '\uF828');
        advance.put(7, '\uF827');
        advance.put(6, '\uF826');
        advance.put(5, '\uF825');
        advance.put(4, '\uF824');
        advance.put(3, '\uF823');
        advance.put(2, '\uF822');
        advance.put(1, '\uF821');

        retreat = new TreeMap<>();
        retreat.put(1, '\uF801');
        retreat.put(2, '\uF802');
        retreat.put(3, '\uF803');
        retreat.put(4, '\uF804');
        retreat.put(5, '\uF805');
        retreat.put(6, '\uF806');
        retreat.put(7, '\uF807');
        retreat.put(8, '\uF808');
        retreat.put(16, '\uF809');
        retreat.put(32, '\uF80A');
        retreat.put(64, '\uF80B');
        retreat.put(128, '\uF80C');
        retreat.put(256, '\uF80D');
        retreat.put(512, '\uF80E');
        retreat.put(1024, '\uF80F');
    }
}
