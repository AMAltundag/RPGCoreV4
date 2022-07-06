package me.blutkrone.rpgcore.command;

import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class AbstractCommand {

    /**
     * Create a sub-list from the given arguments, which start with
     * the given keyword.
     *
     * @param keyword the keyword to start with
     * @param args    all valid candidates
     * @return arguments which start with the keyword
     */
    public static List<String> suggestLimitBy(String keyword, Collection<String> args) {
        if (keyword.isEmpty()) return new ArrayList<>(args);
        List<String> output = new ArrayList<>();
        for (String arg : args) {
            if (arg.startsWith(keyword)) {
                output.add(arg);
            }
        }
        return output;
    }

    /**
     * Validate if the argument is of a certain type, fail otherwise.
     */
    public static Player validatePlayer(String[] args, int pos, String error) throws CommandArgumentException {
        if (args.length <= pos)
            throw new CommandArgumentException(error + String.format(" (expect: %s args, have %s)", pos, args.length));
        Player player = Bukkit.getPlayer(args[pos]);
        if (player == null)
            throw new CommandArgumentException(error + String.format(" (value: %s is not a player)", args[pos]));
        return player;
    }

    /**
     * Validate if the argument is of a certain type, fail otherwise.
     */
    public static int validateInteger(String[] args, int pos, String error) throws CommandArgumentException {
        if (args.length <= pos)
            throw new CommandArgumentException(error + String.format(" (expect: %s args, have %s)", pos, args.length));
        try {
            return Integer.parseInt(args[pos]);
        } catch (Exception e) {
            throw new CommandArgumentException(error + String.format(" (value: %s is not an integer)", args[pos]));
        }
    }

    /**
     * Validate if the argument is of a certain type, fail otherwise.
     */
    public static double validateDecimal(String[] args, int pos, String error) throws CommandArgumentException {
        if (args.length <= pos)
            throw new CommandArgumentException(error + String.format(" (expect: %s args, have %s)", pos, args.length));
        try {
            return Double.parseDouble(args[pos]);
        } catch (Exception e) {
            throw new CommandArgumentException(error + String.format(" (value: %s is not a decimal)", args[pos]));
        }
    }

    /**
     * Validate if the argument is of a certain type, fail otherwise.
     */
    public static boolean validateBoolean(String[] args, int pos, String error) throws CommandArgumentException {
        if (args.length <= pos)
            throw new CommandArgumentException(error + String.format(" (expect: %s args, have %s)", pos, args.length));
        try {
            return Boolean.parseBoolean(args[pos]);
        } catch (Exception e) {
            throw new CommandArgumentException(error + String.format(" (value: %s is not a boolean)", args[pos]));
        }
    }

    /**
     * Check if the sender has access to this command.
     *
     * @param sender who is using the command.
     * @return true if we have access to it.
     */
    public abstract boolean canUseCommand(CommandSender sender);

    /**
     * Fetch the help text for this specific command.
     *
     * @return the help text to be sent.
     */
    public abstract BaseComponent[] getHelpText();

    /**
     * Invoke the execution of our given command.
     *
     * @param sender who invoked the command.
     * @param args   arguments of the command.
     */
    public abstract void invoke(CommandSender sender, String... args);

    /**
     * Identify the suggestion options for this command.
     *
     * @param sender who are we suggesting to.
     * @param args   arguments we have right now.
     * @return list of suggestions.
     */
    public abstract List<String> suggest(CommandSender sender, String... args);
}
