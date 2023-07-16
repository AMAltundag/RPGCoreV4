package me.blutkrone.rpgcore.editor.bundle.other;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.editor.annotation.EditorName;
import me.blutkrone.rpgcore.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.editor.root.IEditorRoot;
import me.blutkrone.rpgcore.quest.dialogue.CoreDialogue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;

@EditorName(name = "Dialogue")
@EditorTooltip(tooltip = "Dialogue for NPC/Quest")
public class EditorDialogue extends EditorDialogueBundle implements IEditorRoot<CoreDialogue> {

    public transient File file;
    public int migration_version = RPGCore.inst().getMigrationManager().getVersion();

    public EditorDialogue() {
    }

    @Override
    public File getFile() {
        return this.file;
    }

    @Override
    public void setFile(File file) {
        this.file = file;
    }

    @Override
    public void save() throws IOException {
        try (FileWriter fw = new FileWriter(file, Charset.forName("UTF-8"))) {
            RPGCore.inst().getGsonPretty().toJson(this, fw);
        }
    }

    @Override
    public CoreDialogue build(String id) {
        return this.build();
    }
}
