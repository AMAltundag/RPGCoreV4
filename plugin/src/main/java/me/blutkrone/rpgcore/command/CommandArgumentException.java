package me.blutkrone.rpgcore.command;

public class CommandArgumentException extends RuntimeException {
    public CommandArgumentException(String message) {
        super("Bad argument " + message);
    }
}
