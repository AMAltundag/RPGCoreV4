package me.blutkrone.rpgcore.hud.editor.constraint.reference.other;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.hud.editor.IEditorConstraint;
import me.blutkrone.rpgcore.util.io.FileUtil;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LanguageConstraint implements IEditorConstraint {

    private static File TEMPORARY_FILE = null;

    @Override
    public List<String> getHint(String value) {
        value = value.toLowerCase();
        List<String> matched = new ArrayList<>();
        for (String translated : RPGCore.inst().getLanguageManager().getTranslated()) {
            if (translated.startsWith(value)) {
                matched.add(translated);
            }
        }
        return matched;
    }

    @Override
    public boolean isDefined(String value) {
        value = value.toLowerCase();

        try {
            return RPGCore.inst().getLanguageManager().getTranslated().contains(value);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void extend(String value) {
        // create a single stub file for the editor
        if (LanguageConstraint.TEMPORARY_FILE == null) {
            File file = FileUtil.file("language", "editor_stub_" + System.currentTimeMillis() + ".yml");
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }

            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            LanguageConstraint.TEMPORARY_FILE = file;
        }

        // dump the value into the stub
        YamlConfiguration cfg = new YamlConfiguration();
        try {
            cfg.load(LanguageConstraint.TEMPORARY_FILE);
            cfg.set(value, "stub=" + value);
            cfg.save(LanguageConstraint.TEMPORARY_FILE);
            RPGCore.inst().getLanguageManager().addStub(value);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean canExtend() {
        return true; // unsupported
    }

    @Override
    public String getConstraintAt(List container, int index) {
        return (String) container.get(index);
    }

    @Override
    public void setElementAt(List container, int index, String value) {
        container.set(index, value);
    }

    @Override
    public void addElement(List container, String value) {
        container.add(value);
    }

    @Override
    public Object asTypeOf(String value) {
        return value;
    }

    @Override
    public String toTypeOf(Object value) {
        return ((String) value);
    }

    @Override
    public List<String> getPreview(List<Object> list) {
        List<String> preview = new ArrayList<>();

        if (list.size() <= 16) {
            for (int i = 0; i < list.size(); i++) {
                preview.add(i + ": " + list.get(i));
            }
        } else {
            for (int i = 0; i < 16; i++) {
                preview.add(i + ": " + list.get(i));
            }

            preview.add("... And " + (list.size() - 16) + " More!");
        }

        return preview;
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("§fLanguage");
        instruction.add("§fDo NOT write the actual translation value here, use");
        instruction.add("§fan ID to configure a language element for it.");
        instruction.add("§cLanguage has to be configured via config files");
        return instruction;
    }

    @Override
    public String getPreview(Object object) {
        return String.valueOf(object);
    }

}
