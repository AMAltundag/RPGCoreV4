package me.blutkrone.rpgcore.util;

public interface ThrowingRunnable {
    /**
     * Should an exception be raised, it should be handled
     * by the parent container.
     *
     * @throws Throwable exception to be handled by parent.
     */
    void run() throws Throwable;
}
