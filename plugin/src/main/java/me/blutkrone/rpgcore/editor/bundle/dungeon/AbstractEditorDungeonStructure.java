package me.blutkrone.rpgcore.editor.bundle.dungeon;

import me.blutkrone.rpgcore.dungeon.structure.AbstractDungeonStructure;
import me.blutkrone.rpgcore.editor.annotation.EditorCategory;
import me.blutkrone.rpgcore.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.editor.annotation.value.EditorBoolean;
import me.blutkrone.rpgcore.editor.annotation.value.EditorList;
import me.blutkrone.rpgcore.editor.annotation.value.EditorNumber;
import me.blutkrone.rpgcore.editor.annotation.value.EditorWrite;
import me.blutkrone.rpgcore.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.editor.constraint.bundle.multi.SelectorConstraint;
import me.blutkrone.rpgcore.editor.constraint.other.StringConstraint;
import org.bukkit.Material;
import org.bukkit.util.BlockVector;

import java.util.ArrayList;
import java.util.List;

/**
 * Design suggestion:
 * <br>
 * Structures should be contained in a root container, identified
 * by their ID.
 * <br>
 * Locations are an uneditable data dump within the dungeon file,
 * you can sync them when saving a dungeon world.
 * <br>
 * Spawner needs additional option for attributes
 * <br>
 * Mechanic and selector for dungeon score
 * <br>
 * Dungeon Root Class
 * + Dungeon structures by ID
 * + Hidden dungeon structure position dump
 * + Completion reward
 */
public abstract class AbstractEditorDungeonStructure implements IEditorBundle {

    @EditorCategory(info = "Structure", icon = Material.STRUCTURE_BLOCK)
    @EditorList(name = "Activation", constraint = SelectorConstraint.class)
    @EditorTooltip(tooltip = {"Condition to activate, initially the location."})
    public List<IEditorBundle> activation = new ArrayList<>();
    @EditorNumber(name = "Range", minimum = 0d, maximum = 48d)
    @EditorTooltip(tooltip = "Distance to trigger structure at")
    public double range = 48d;
    @EditorBoolean(name = "Hidden")
    @EditorTooltip(tooltip = "Set block to air when chunk is loaded")
    public boolean hidden = true;
    @EditorWrite(name = "ID", constraint = StringConstraint.class)
    @EditorTooltip(tooltip = "Identifier for structure")
    public String sync_id = "structure_" + String.valueOf(System.nanoTime() % 100000);

    // position of structure, written via world sync
    public List<BlockVector> where = new ArrayList<>();

    /**
     * Compile into a live instance.
     *
     * @return Live dungeon instance.
     */
    public abstract AbstractDungeonStructure build();
}
