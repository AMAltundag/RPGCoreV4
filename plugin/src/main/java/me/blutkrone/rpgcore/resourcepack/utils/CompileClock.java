package me.blutkrone.rpgcore.resourcepack.utils;

public class CompileClock {
    private long last = System.nanoTime();

    public int loop() {
        double diff = System.nanoTime() - last;
        last = System.nanoTime();
        return (int) (diff / 1_000_000d);
    }
}
