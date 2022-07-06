package me.blutkrone.rpgcore.resourcepack.bbmodel;

public class BBException extends RuntimeException {
    public BBException(String message) {
        super(message);
    }

    public BBException(String message, Throwable cause) {
        super(message, cause);
    }
}
