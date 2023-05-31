package me.blutkrone.rpgcore.dungeon;

import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.dungeon.structure.AbstractDungeonStructure;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.hud.editor.bundle.dungeon.AbstractEditorDungeonStructure;
import me.blutkrone.rpgcore.hud.editor.bundle.other.EditorAttributeAndFactor;
import me.blutkrone.rpgcore.hud.editor.bundle.selector.AbstractEditorSelector;
import me.blutkrone.rpgcore.hud.editor.root.dungeon.EditorDungeon;
import me.blutkrone.rpgcore.skill.selector.AbstractCoreSelector;
import org.bukkit.Bukkit;

import java.util.*;

/**
 * This a template for a dungeon.
 */
public class CoreDungeon {

    private String id;

    private int player_limit;
    private int minimum_level;
    private int maximum_level;
    private List<AbstractCoreSelector> blacklist;
    private List<AbstractCoreSelector> whitelist;
    private String lc_icon;
    private Map<String, Double> player_attributes;
    private Map<String, Double> spawns_attributes;
    private Map<String, AbstractDungeonStructure> structures;
    private boolean hardcore;

    public CoreDungeon(String id, EditorDungeon editor) {
        this.id = id;

        this.lc_icon = editor.lc_icon;
        this.player_limit = ((int) editor.player_limit);
        this.minimum_level = ((int) editor.minimum_level);
        this.maximum_level = ((int) editor.maximum_level);
        this.blacklist = AbstractEditorSelector.unwrap(editor.blacklist);
        this.whitelist = AbstractEditorSelector.unwrap(editor.whitelist);
        this.player_attributes = EditorAttributeAndFactor.unwrap(editor.player_attributes);
        this.spawns_attributes = EditorAttributeAndFactor.unwrap(editor.spawns_attributes);
        this.hardcore = editor.hardcore;
        this.structures = new HashMap<>();
        for (IEditorBundle structure : editor.structures) {
            AbstractEditorDungeonStructure dungeon_structure = (AbstractEditorDungeonStructure) structure;
            AbstractDungeonStructure built = dungeon_structure.build();
            AbstractDungeonStructure override = this.structures.put(built.getSyncId(), built);
            if (override != null) {
                Bukkit.getLogger().warning(String.format("Dungeon '%s' structure '%s' overlaps!", id, built.getSyncId()));
            }
        }
    }

    /**
     * The LC icon for this specific dungeon.
     *
     * @return LC Identifier for dungeon item.
     */
    public String getLcIcon() {
        return lc_icon;
    }

    /**
     * If not resurrected in time will be kicked out of the
     * dungeon instance.
     *
     * @return Hardcore dungeon
     */
    public boolean isHardcore() {
        return hardcore;
    }

    public Map<String, Double> getPlayerAttributes() {
        return player_attributes;
    }

    public Map<String, Double> getSpawnsAttributes() {
        return spawns_attributes;
    }

    public Map<String, AbstractDungeonStructure> getStructures() {
        return structures;
    }

    public String getId() {
        return id;
    }

    /**
     * Check if the given player is able to access the dungeon
     * we want to play.
     *
     * @param player Who to check
     * @return Whether content is accessible
     */
    public boolean canAccess(CorePlayer player) {
        // ensure we are within level range
        if (player.getCurrentLevel() < this.minimum_level) {
            return false;
        }
        if (player.getCurrentLevel() > this.maximum_level) {
            return false;
        }
        // ensure we are not blacklisted
        if (!this.blacklist.isEmpty()) {
            List<IOrigin> targets = Collections.singletonList(player);
            if (AbstractCoreSelector.doSelect(this.blacklist, player, targets).isEmpty()) {
                return false;
            }
        }
        // make sure we meet whitelist conditions
        if (!this.whitelist.isEmpty()) {
            List<IOrigin> targets = Collections.singletonList(player);
            if (!AbstractCoreSelector.doSelect(this.whitelist, player, targets).isEmpty()) {
                return false;
            }
        }
        // we can participate in this dungeon
        return true;
    }

    /**
     * How many players the dungeon is meant for, the bungeecord
     * matchmaker will bypass this.
     *
     * @return How many players we are for
     */
    public int getPlayerLimit() {
        return player_limit;
    }
}
