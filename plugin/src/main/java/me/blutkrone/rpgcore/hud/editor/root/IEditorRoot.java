package me.blutkrone.rpgcore.hud.editor.root;

import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;

import java.io.File;
import java.io.IOException;

public interface IEditorRoot<K> extends IEditorBundle {

    /**
     * Retrieve the file we are backed up by.
     *
     * @return the file we are backed by.
     */
    File getFile();

    /**
     * Update the file which handles de/serialization of this.
     *
     * @param file which file to de/serialize from
     */
    void setFile(File file);

    /**
     * Dump the current state into the file we are linked to.
     */
    void save() throws IOException;

    /**
     * Transform this configuration into a runtime instance, do note
     * that this should be a one-directional process.
     *
     * @param id the ID of the runtime instance.
     * @return the baked runtime instance
     */
    K build(String id);
}