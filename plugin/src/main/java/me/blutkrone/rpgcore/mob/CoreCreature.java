package me.blutkrone.rpgcore.mob;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.api.entity.EntityProvider;
import me.blutkrone.rpgcore.entity.entities.CoreMob;
import me.blutkrone.rpgcore.entity.providers.LivingProvider;
import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.hud.editor.bundle.entity.AbstractEditorEntityProvider;
import me.blutkrone.rpgcore.hud.editor.bundle.loot.AbstractEditorLoot;
import me.blutkrone.rpgcore.hud.editor.bundle.other.EditorAttributeAndFactor;
import me.blutkrone.rpgcore.hud.editor.bundle.other.EditorMobLogic;
import me.blutkrone.rpgcore.hud.editor.root.mob.EditorCreature;
import me.blutkrone.rpgcore.mob.ai.CoreMobLogic;
import me.blutkrone.rpgcore.mob.loot.AbstractCoreLoot;
import me.blutkrone.rpgcore.nms.api.mob.AbstractEntityRoutine;
import me.blutkrone.rpgcore.nms.api.mob.IEntityBase;
import me.blutkrone.rpgcore.util.Utility;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CoreCreature {

    private String id;
    private String name;

    // factory which can supply us with the mob
    public EntityProvider mob_factory;
    // attributes granted upon spawning
    public Map<String, Double> attributes_base;
    // attributes granted per level
    public Map<String, Double> attributes_level;
    // rewards offered upon death
    public List<AbstractCoreLoot> loot = new ArrayList<>();
    // AI invoked once and never again
    public List<CoreMobLogic> ai_mono;
    // AI invoked while alive
    public List<CoreMobLogic> ai_tick;
    // AI invoked before allowing death
    public List<CoreMobLogic> ai_death;
    // relationship parameters we utilize
    public List<String> tags;
    public List<String> tags_hostile;
    public List<String> tags_friendly;
    // symbol used on the creature
    public String focus_sigil;

    public CoreCreature(String id, EditorCreature editor) {
        this.id = id;
        this.name = editor.lc_name;
        if (!this.name.equalsIgnoreCase("nothingness")) {
            this.name = RPGCore.inst().getLanguageManager().getTranslation(this.name);
        } else {
            this.name = id;
        }
        this.mob_factory = new LivingProvider(EntityType.ZOMBIE);
        if (!editor.factory.isEmpty()) {
            this.mob_factory = ((AbstractEditorEntityProvider) editor.factory.get(0)).build();
        }
        for (IEditorBundle bundle : editor.loot) {
            this.loot.add(((AbstractEditorLoot) bundle).build());
        }
        this.attributes_base = EditorAttributeAndFactor.unwrap(editor.attributes_base);
        this.attributes_level = EditorAttributeAndFactor.unwrap(editor.attributes_level);
        this.ai_mono = EditorMobLogic.unwrap(editor.ai_mono);
        this.ai_tick = EditorMobLogic.unwrap(editor.ai_tick);
        this.ai_death = EditorMobLogic.unwrap(editor.ai_death);
        this.tags = new ArrayList<>(editor.tags);
        this.tags_hostile = new ArrayList<>(editor.hostile_tag);
        this.tags_friendly = new ArrayList<>(editor.friendly_tag);
        this.focus_sigil = editor.focus_sigil;
        Bukkit.getLogger().severe("Not implemented (loot from mobs)");
    }

    /**
     * Unique identifier for the monster design.
     *
     * @return unique identifier.
     */
    public String getId() {
        return id;
    }

    /**
     * Name of the creature, mainly for UX purposes.
     *
     * @return name of the creature
     */
    public String getName() {
        return name;
    }

    /**
     * Spawn this entity at a given location, assigned
     * a certain level.
     *
     * @param where the location to spawn at
     * @param level the level to spawn at
     */
    public CoreMob spawn(Location where, int level) {
        if (!Utility.isChunkLoaded(where)) {
            return null;
        }

        // initialization within bukkit space
        LivingEntity bukkit_entity = this.mob_factory.create(where);
        bukkit_entity.setCustomName(this.getName());
        bukkit_entity.setCustomNameVisible(false);
        bukkit_entity.setPersistent(false);

        // initialization within rpgcore space
        CoreMob core_entity = new CoreMob(bukkit_entity, this.mob_factory, this);

        // assign the relevant tags for relationships
        core_entity.getMyTags().addAll(this.tags);
        core_entity.getTagsFriendly().addAll(this.tags_friendly);
        core_entity.getTagsHostile().addAll(this.tags_hostile);

        // allocate relevant attributes
        this.attributes_base.forEach((id, value) -> core_entity.getAttribute(id).create(value));
        this.attributes_level.forEach((id, value) -> core_entity.getAttribute(id).create(value*level));

        // initialize relevant behaviours
        IEntityBase base_entity = RPGCore.inst().getVolatileManager().getEntity(bukkit_entity);

        // AI intended to tick while alive
        for (CoreMobLogic logic : this.ai_tick) {
            AbstractEntityRoutine routine = logic.construct(base_entity, core_entity);
            base_entity.addRoutine("SERVER_" + logic.getGroup(), routine);
        }

        // Attempt a random spawn routine
        for (CoreMobLogic logic : this.ai_mono) {
            AbstractEntityRoutine routine = logic.construct(base_entity, core_entity);
            if (routine.doStart()) {
                routine.setSingleton(true);
                base_entity.addRoutine("RPGCORE_SPAWN_ROUTINE", routine);
                break;
            }
        }

        // AI options to run before dying (only one is picked.)
        for (CoreMobLogic logic : this.ai_death) {
            AbstractEntityRoutine routine = logic.construct(base_entity, core_entity);
            base_entity.addDeathRoutine(routine);
        }

        // register within the core
        RPGCore.inst().getEntityManager().register(core_entity.getUniqueId(), core_entity);

        // offer up for subsequent processing
        return core_entity;
    }
}
