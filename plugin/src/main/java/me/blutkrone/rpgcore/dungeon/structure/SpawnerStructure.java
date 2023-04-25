package me.blutkrone.rpgcore.dungeon.structure;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.dungeon.IDungeonInstance;
import me.blutkrone.rpgcore.dungeon.instance.ActiveDungeonInstance;
import me.blutkrone.rpgcore.dungeon.instance.EditorDungeonInstance;
import me.blutkrone.rpgcore.entity.entities.CoreMob;
import me.blutkrone.rpgcore.hud.editor.bundle.dungeon.EditorDungeonSpawner;
import me.blutkrone.rpgcore.hud.editor.bundle.selector.AbstractEditorSelector;
import me.blutkrone.rpgcore.node.impl.CoreNodeSpawner;
import me.blutkrone.rpgcore.skill.selector.AbstractCoreSelector;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Spawn mobs at the location, if count is greater 1 the mobs
 * are spawned with a bit of distance.
 */
public class SpawnerStructure extends AbstractDungeonStructure<SpawnerStructure.ISpawnerData> {

    private final List<AbstractCoreSelector> activation;
    private final double range;

    private List<String> mobs;
    private int level;
    private int leash;
    private int count;
    private boolean important;

    public SpawnerStructure(EditorDungeonSpawner editor) {
        super(editor);
        this.activation = AbstractEditorSelector.unwrap(editor.activation);
        this.range = editor.range * editor.range;
        this.mobs = new ArrayList<>(editor.mobs);
        this.level = (int) editor.level;
        this.leash = (int) editor.leash;
        this.count = (int) editor.count;
        this.important = editor.important;

        if (this.important) {
            this.count = 1;
        }
    }

    @Override
    public void clean(IDungeonInstance instance) {

    }

    @Override
    public void update(ActiveDungeonInstance instance, List<StructureData<ISpawnerData>> where) {
        if (RPGCore.inst().getTimestamp() % 60 != 0 || this.mobs.isEmpty()) {
            return;
        }

        if (this.important) {
            where.removeIf(datum -> {
                ImportantData data = (ImportantData) datum.data;
                if (data != null) {
                    // ensure leash is being respected
                    CoreNodeSpawner.leash(data.uuid, datum.where, this.leash);
                    // complete, reset or continue
                    Entity entity = Bukkit.getEntity(data.uuid);
                    if (entity == null) {
                        if (data.slain) {
                            return true;
                        } else {
                            datum.data = null;
                            return false;
                        }
                    } else {
                        return false;
                    }
                } else {
                    // spawn important creature if condition is met
                    List<Player> watching = RPGCore.inst().getEntityManager().getObserving(datum.where);
                    watching.removeIf(player -> player.getLocation().distanceSquared(datum.where) > this.range);
                    if (!watching.isEmpty()) {
                        // ensure condition has been archived
                        if (!AbstractCoreSelector.doSelect(activation, datum.context, Collections.singletonList(new IOrigin.SnapshotOrigin(datum.where))).isEmpty()) {
                            List<UUID> spawn = CoreNodeSpawner.spawn(1, mobs, datum.where, level, leash);
                            if (!spawn.isEmpty()) {
                                datum.data = (data = new ImportantData());
                                data.uuid = spawn.iterator().next();
                                CoreMob mob = RPGCore.inst().getEntityManager().getMob(data.uuid);
                                instance.getTemplate().getSpawnsAttributes().forEach((attribute, factor) -> {
                                    mob.getAttribute(attribute).create(factor);
                                });
                                mob.setAsImportantDungeonSpawn(data);
                            }
                        }
                    }

                    return false;
                }
            });
        } else {
            where.removeIf(datum -> {
                // ensure there are players within range
                List<Player> watching = RPGCore.inst().getEntityManager().getObserving(datum.where);
                watching.removeIf(player -> player.getLocation().distanceSquared(datum.where) > this.range);

                // spawn mobs if they haven't been spawned
                NormalData data;
                if (datum.data == null && !watching.isEmpty()) {
                    if (AbstractCoreSelector.doSelect(activation, datum.context, Collections.singletonList(new IOrigin.SnapshotOrigin(datum.where))).isEmpty()) {
                        return false;
                    }
                    data = new NormalData();
                    List<UUID> spawn = CoreNodeSpawner.spawn(count, mobs, datum.where, level, leash);
                    for (UUID uuid : spawn) {
                        CoreMob mob = RPGCore.inst().getEntityManager().getMob(uuid);
                        instance.getTemplate().getSpawnsAttributes().forEach((attribute, factor) -> {
                            mob.getAttribute(attribute).create(factor);
                        });
                    }
                    data.population.addAll(spawn);
                } else {
                    data = (NormalData) datum.data;
                }

                // ensure there is data to be loaded
                if (data != null) {
                    // purge inactive references
                    data.population.removeIf(uuid -> Bukkit.getEntity(uuid) == null);
                    // process the leash
                    for (UUID uuid : data.population) {
                        CoreNodeSpawner.leash(uuid, datum.where, this.leash);
                    }
                    // abandon if population is wiped
                    return true;
                }

                return false;
            });
        }
    }

    @Override
    public void update(EditorDungeonInstance instance, List<StructureData<ISpawnerData>> where) {
        if (RPGCore.inst().getTimestamp() % 20 == 0) {
            for (StructureData<?> structure : where) {
                if (structure.highlight == null) {
                    int x = structure.where.getBlockX();
                    int y = structure.where.getBlockY();
                    int z = structure.where.getBlockZ();
                    structure.highlight = RPGCore.inst().getVolatileManager().getPackets().highlight(x, y, z);
                }

                List<Player> watching = RPGCore.inst().getEntityManager().getObserving(structure.where);
                watching.removeIf(player -> player.getLocation().distance(structure.where) > 32);
                for (Player player : watching) {
                    structure.highlight.enable(player);
                    Bukkit.getScheduler().runTaskLater(RPGCore.inst(), () -> {
                        structure.highlight.disable(player);
                    }, 10);
                }
            }
        }
    }

    interface ISpawnerData {

    }

    public class ImportantData implements ISpawnerData {
        public UUID uuid;
        public boolean slain;
    }

    public class NormalData implements ISpawnerData {
        public List<UUID> population = new ArrayList<>();
    }
}
