package me.blutkrone.rpgcore.dungeon.structure;

import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.dungeon.IDungeonInstance;
import me.blutkrone.rpgcore.dungeon.instance.ActiveDungeonInstance;
import me.blutkrone.rpgcore.dungeon.instance.EditorDungeonInstance;
import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import me.blutkrone.rpgcore.hud.editor.bundle.dungeon.AbstractEditorDungeonStructure;
import me.blutkrone.rpgcore.nms.api.packet.handle.IHighlight;
import me.blutkrone.rpgcore.skill.behaviour.CoreAction;
import me.blutkrone.rpgcore.skill.proxy.AbstractSkillProxy;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockVector;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractDungeonStructure<K> {

    private final List<BlockVector> where;
    private final String sync_id;
    private final boolean hidden;
    private final ItemStack icon;

    public AbstractDungeonStructure(AbstractEditorDungeonStructure editor) {
        this.where = new ArrayList<>(editor.where);
        this.sync_id = editor.sync_id;
        this.hidden = editor.hidden;
        this.icon = editor.getPreview();
    }

    /**
     * Preview icon to use when placing.
     *
     * @return Icon during placement menu
     */
    public ItemStack getIcon() {
        return icon;
    }

    /**
     * Upon loading chunk, hide the block.
     *
     * @return Hide block on chunk load.
     */
    public boolean isHidden() {
        return hidden;
    }

    /**
     * An identifier used during the synchronization process of
     * dungeon world saving.
     *
     * @return Sync ID
     */
    public String getSyncId() {
        return sync_id;
    }

    /**
     * Locations where this structure is active.
     *
     * @return Locations that structure is active in.
     */
    public List<BlockVector> getWhere() {
        return where;
    }

    /**
     * This is invoked when a dungeon instance which holds
     * the structure is to be abandoned.
     *
     * @param instance The instance that will be removed
     */
    public abstract void clean(IDungeonInstance instance);

    /**
     * Update this dungeon structure for the given active
     * dungeon instance. Currently in play mode.
     *
     * @param instance The dungeon instance
     * @param where    Locations of structure
     */
    public abstract void update(ActiveDungeonInstance instance, List<StructureData<K>> where);

    /**
     * Update this dungeon structure for the given active
     * dungeon instance. Currently in edit mode.
     *
     * @param instance The dungeon instance
     * @param where    Locations of structure
     */
    public abstract void update(EditorDungeonInstance instance, List<StructureData<K>> where);

    public static class StructureData<T> {
        public final Location where;
        public final IContext context;
        public T data;

        public IHighlight highlight;

        public StructureData(Location where) {
            this.where = where;
            this.context = new StructureContext(where);
        }
    }

    public static class StructureContext implements IContext {

        private final Location where;
        private List<AbstractSkillProxy> proxies = new ArrayList<>();
        private List<CoreAction.ActionPipeline> pipelines = new ArrayList<>();

        public StructureContext(Location where) {
            this.where = where;
        }

        @Override
        public double evaluateAttribute(String attribute) {
            return 0;
        }

        @Override
        public boolean checkForTag(String tag) {
            return false;
        }

        @Override
        public CoreEntity getCoreEntity() {
            return null;
        }

        @Override
        public IOrigin getOrigin() {
            return new IOrigin.SnapshotOrigin(this.where);
        }

        @Override
        public void addProxy(AbstractSkillProxy proxy) {
            this.proxies.add(proxy);
        }

        @Override
        public List<AbstractSkillProxy> getProxies() {
            return this.proxies;
        }

        @Override
        public void addPipeline(CoreAction.ActionPipeline pipeline) {
            this.pipelines.add(pipeline);
        }

        @Override
        public List<CoreAction.ActionPipeline> getPipelines() {
            return this.pipelines;
        }
    }
}
