package me.blutkrone.rpgcore.editor.migration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public abstract class AbstractMigration {

    public static Gson GSON = new GsonBuilder()
            .serializeNulls()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    public AbstractMigration() {
    }

    /**
     * Perform the migration, make sure to update the file if
     * requested.
     */
    public abstract void apply();
}
