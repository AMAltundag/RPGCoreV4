package me.blutkrone.rpgcore.util.io;

import me.blutkrone.rpgcore.RPGCore;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

public class FileUtil {
    private FileUtil() {

    }

    public static File directory(String directory) {
        return new File(RPGCore.inst().getDataFolder() + "/" + directory);
    }

    public static File file(String directory, String file_name) {
        return new File(directory(directory), file_name);
    }

    public static File file(File directory, String file_name) {
        return new File(directory, file_name);
    }

    public static File file(String file_name) {
        return new File(RPGCore.inst().getDataFolder(), file_name);
    }

    public static File[] buildAllDirectories(File directory) throws IOException {
        Queue<File> directories = new LinkedList<>();
        Queue<File> found = new LinkedList<>();
        directories.add(directory);
        found.add(directory);
        while (!directories.isEmpty()) {
            File poll = directories.poll();
            File[] children = poll.listFiles();
            if (children == null) continue;
            for (File child_file : children) {
                if (child_file.isDirectory()) {
                    directories.add(child_file);
                    found.add(child_file);
                }
            }
        }
        return found.toArray(new File[found.size()]);
    }

    public static File[] buildAllFiles(File directory) throws IOException {
        Queue<File> directories = new LinkedList<>();
        Queue<File> files = new LinkedList<>();
        directories.add(directory);
        while (!directories.isEmpty()) {
            File poll = directories.poll();
            File[] children = poll.listFiles();
            if (children == null) continue;
            for (File child_file : children) {
                if (child_file.isDirectory())
                    directories.add(child_file);
                else files.add(child_file);
            }
        }
        return files.toArray(new File[files.size()]);
    }

    public static void saveToDirectory(YamlConfiguration data, File file) throws IOException {
        if (!file.exists()) {
            file.createNewFile();
        }
        data.save(file);
    }

    public static ConfigWrapper asConfigYML(File file) throws IOException {
        YamlConfiguration configuration = new YamlConfiguration();
        if (file.exists()) {
            try {
                configuration.load(file);
            } catch (InvalidConfigurationException e) {
                e.printStackTrace();
            }
        }
        return new ConfigWrapper(configuration);
    }

    public static byte[] readBytes(File file) throws IOException {
        byte[] bytes = new byte[0];
        try (FileInputStream outputStream = new FileInputStream(file)) {
            bytes = outputStream.readAllBytes();
        }
        return bytes;
    }
}