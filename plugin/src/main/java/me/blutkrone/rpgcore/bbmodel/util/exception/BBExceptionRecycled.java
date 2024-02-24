package me.blutkrone.rpgcore.bbmodel.util.exception;

public class BBExceptionRecycled extends BBException {
    public BBExceptionRecycled() {
    }

    public BBExceptionRecycled(String message) {
        super(message);
    }

    public BBExceptionRecycled(String message, Throwable cause) {
        super(message, cause);
    }

    public BBExceptionRecycled(Throwable cause) {
        super(cause);
    }

    public BBExceptionRecycled(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
