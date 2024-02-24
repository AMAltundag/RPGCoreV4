package me.blutkrone.rpgcore.bbmodel.util.exception;

public class BBException extends Exception {
    public BBException() {
    }

    public BBException(String message) {
        super(message);
    }

    public BBException(String message, Throwable cause) {
        super(message, cause);
    }

    public BBException(Throwable cause) {
        super(cause);
    }

    public BBException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
