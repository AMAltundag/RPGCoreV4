package me.blutkrone.rpgcore.command.impl;

import me.blutkrone.rpgcore.command.AbstractCommand;
import me.blutkrone.rpgcore.resourcepack.upload.DBXUploader;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import java.util.List;

public class DropboxCommand extends AbstractCommand {

    public DropboxCommand() {

    }

    @Override
    public boolean canUseCommand(CommandSender sender) {
        return sender instanceof ConsoleCommandSender;
    }

    @Override
    public BaseComponent[] getHelpText() {
        return new BaseComponent[0];
    }

    @Override
    public void invoke(CommandSender sender, String... args) {
        if (args.length == 1) {
            // want to start auth flow
            try {
                DBXUploader.Dropbox.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // want to finish auth flow
            try {
                DBXUploader.Dropbox.finish(args[1]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public List<String> suggest(CommandSender sender, String... args) {
        return null;
    }
}
