package me.blutkrone.rpgcore.exception;

public class MalformedPatternException extends Exception {
    public MalformedPatternException() {
    }

    public MalformedPatternException(String message) {
        super(message);
    }

    public MalformedPatternException(String message, Throwable cause) {
        super(message, cause);
    }

    public MalformedPatternException(Throwable cause) {
        super(cause);
    }

    public MalformedPatternException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
