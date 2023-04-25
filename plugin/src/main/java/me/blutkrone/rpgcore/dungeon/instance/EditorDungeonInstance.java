package me.blutkrone.rpgcore.dungeon.instance;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.dungeon.CoreDungeon;
import me.blutkrone.rpgcore.dungeon.IDungeonInstance;
import me.blutkrone.rpgcore.dungeon.structure.AbstractDungeonStructure;
import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.hud.editor.bundle.dungeon.AbstractEditorDungeonStructure;
import me.blutkrone.rpgcore.hud.editor.index.EditorIndex;
import me.blutkrone.rpgcore.hud.editor.index.IndexAttachment;
import me.blutkrone.rpgcore.hud.editor.root.dungeon.EditorDungeon;
import me.blutkrone.rpgcore.util.io.FileUtil;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockVector;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EditorDungeonInstance implements IDungeonInstance {

    private IndexAttachment<?, CoreDungeon> template;
    private String world;
    private File world_folder;
    private List<ActiveStructure> structures;

    public EditorDungeonInstance(String id, IndexAttachment<?, CoreDungeon> template) {
        // prepare a dungeon world
        this.world = id;

        String template_dir = "dungeon" + File.separator + template.get().getId();
        FileUtil.copyDirectory(FileUtil.directory(template_dir), new File(Bukkit.getWorldContainer() + File.separator + this.world));
        World world = new WorldCreator(this.world).seed(0).environment(World.Environment.NORMAL)
                .type(WorldType.FLAT).generateStructures(false).createWorld();
        if (world == null) {
            throw new NullPointerException("Could not create world!");
        }
        world.setKeepSpawnInMemory(false);
        world.setAutoSave(false);
        world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        world.setGameRule(GameRule.DO_ENTITY_DROPS, false);
        world.setGameRule(GameRule.DO_FIRE_TICK, false);
        world.setGameRule(GameRule.DO_MOB_LOOT, false);
        world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        world.setGameRule(GameRule.DO_TILE_DROPS, false);
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        world.setGameRule(GameRule.KEEP_INVENTORY, true);
        world.setGameRule(GameRule.MOB_GRIEFING, false);
        world.setGameRule(GameRule.SHOW_DEATH_MESSAGES, false);
        world.setGameRule(GameRule.SPECTATORS_GENERATE_CHUNKS, false);
        world.setGameRule(GameRule.DISABLE_RAIDS, true);
        world.setGameRule(GameRule.DO_INSOMNIA, false);
        world.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, false);
        world.setGameRule(GameRule.DO_PATROL_SPAWNING, false);
        world.setGameRule(GameRule.DO_TRADER_SPAWNING, false);

        this.world_folder = world.getWorldFolder();

        // template related information
        this.template = template;
        this.structures = new ArrayList<>();
        for (AbstractDungeonStructure<?> structure : template.get().getStructures().values()) {
            List<AbstractDungeonStructure.StructureData> data = new ArrayList<>();
            for (BlockVector vector : structure.getWhere()) {
                data.add(new AbstractDungeonStructure.StructureData(vector.toLocation(getWorld())));
            }
            this.structures.add(new ActiveStructure(structure, data));
        }
    }

    public List<ActiveStructure> getStructures() {
        return structures;
    }

    @Override
    public CoreDungeon getTemplate() {
        return this.template.get();
    }

    @Override
    public Location getCheckpoint(Player player) {
        return new BlockVector(0, 130, 0).toLocation(getWorld());
    }

    @Override
    public World getWorld() {
        return Bukkit.getWorld(this.world);
    }

    @Override
    public boolean update() {
        // world was abandoned
        if (getWorld() == null) {
            return true;
        }

        // world can be saved
        if (getWorld().getPlayers().isEmpty()) {
            if (Bukkit.unloadWorld(this.world, true)) {
                String editor_id = this.template.get().getId();
                // sync location related information back
                EditorIndex<CoreDungeon, EditorDungeon> index = RPGCore.inst().getDungeonManager().getDungeonIndex();
                EditorDungeon editor = index.edit(editor_id);
                // inherit structure locations from instance
                for (IEditorBundle editor_bundle : editor.structures) {
                    AbstractEditorDungeonStructure editor_structure = (AbstractEditorDungeonStructure) editor_bundle;
                    for (ActiveStructure active : getStructures()) {
                        if (active.structure.getSyncId().equals(editor_structure.sync_id)) {
                            List<BlockVector> vectors = new ArrayList<>();
                            for (AbstractDungeonStructure.StructureData location : active.data) {
                                vectors.add(location.where.toVector().toBlockVector());
                            }
                            editor_structure.where = vectors;
                        }
                    }
                }
                // save the changes
                index.update(editor_id, editor.build(editor_id));
                try {
                    editor.save();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // write back into the template world
                String template_dir = "dungeon" + File.separator + template.get().getId();
                FileUtil.copyDirectory(world_folder, FileUtil.directory(template_dir));
            }

            return true;
        } else {
            // tick our structures
            for (ActiveStructure structure : structures) {
                structure.structure.update(this, structure.data);
            }
        }

        return false;
    }

    @Override
    public void invite(List<String> players) {
        for (String player : players) {
            Player handle = Bukkit.getPlayer(player);
            if (handle != null) {
                handle.teleport(getCheckpoint(handle));
            }
        }
    }

    public class ActiveStructure {
        public final AbstractDungeonStructure structure;
        public final List<AbstractDungeonStructure.StructureData> data;

        ActiveStructure(AbstractDungeonStructure structure, List<AbstractDungeonStructure.StructureData> data) {
            this.structure = structure;
            this.data = data;
        }
    }
}
