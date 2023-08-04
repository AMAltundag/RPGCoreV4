package me.blutkrone.rpgcore.editor.migration;

import me.blutkrone.rpgcore.editor.migration.scripts.Migration_0002_VisitTaskNowNode;
import me.blutkrone.rpgcore.editor.migration.scripts.Migration_CORE_DeleteBadJSON;
import me.blutkrone.rpgcore.editor.migration.scripts.Migration_0000_ClassRefactorings;
import me.blutkrone.rpgcore.editor.migration.scripts.Migration_0001_HologramExtraParameters;

import java.util.ArrayList;
import java.util.List;

/**
 * Migration handler updates all configuration files when
 * the server starts.
 */
public class MigrationHandler {

    private List<AbstractMigration> migrations = new ArrayList<>();

    public MigrationHandler() {
        // register migration scripts
        this.migrations.add(new Migration_CORE_DeleteBadJSON());
        this.migrations.add(new Migration_0000_ClassRefactorings());
        this.migrations.add(new Migration_0001_HologramExtraParameters());
        this.migrations.add(new Migration_0002_VisitTaskNowNode());

        // invoke migration scripts
        for (AbstractMigration migration : this.migrations) {
            migration.apply();
        }
    }

    /**
     * The version is the number of migration scripts in total.
     *
     * @return Version we have.
     */
    public int getVersion() {
        return this.migrations.size();
    }
}
