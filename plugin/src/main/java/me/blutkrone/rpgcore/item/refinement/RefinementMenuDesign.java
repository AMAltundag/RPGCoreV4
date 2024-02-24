package me.blutkrone.rpgcore.item.refinement;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.resourcepack.ResourcepackManager;
import me.blutkrone.rpgcore.util.io.ConfigWrapper;

import java.util.List;

/**
 * A design used by refinement menus to establish
 * some identity on the crafting process.
 */
public class RefinementMenuDesign {

    private String id;
    private String menu;
    private List<Integer> inputs;
    private List<Integer> outputs;
    private int animation_size;

    public RefinementMenuDesign(String id, ConfigWrapper config) {
        this.id = id;
        this.menu = config.getString("menu");
        this.inputs = config.getIntegerList("inputs");
        this.outputs = config.getIntegerList("outputs");
        this.animation_size = -1;
    }

    /**
     * Compute the number of animation panels.
     *
     * @return how many panels for animation.
     */
    public int getAnimationSize() {
        if (this.animation_size == -1) {
            ResourcepackManager rpm = RPGCore.inst().getResourcepackManager();
            int counter = 0;
            while (rpm.textures().containsKey("menu_" + this.menu + "_progress_" + counter)) {
                counter += 1;
            }
            this.animation_size = counter;
        }

        return this.animation_size;
    }

    /**
     * Identifier for the refinement.
     *
     * @return identifier for this menu design.
     */
    public String getId() {
        return id;
    }

    /**
     * Menu design to use for refinement.
     *
     * @return refinement menu.
     */
    public String getMenu() {
        return menu;
    }

    /**
     * Slots marked as "input" for ingredients.
     *
     * @return ingredient slots.
     */
    public List<Integer> getInputs() {
        return inputs;
    }

    /**
     * Slots marked as "output" item slots.
     *
     * @return result slots.
     */
    public List<Integer> getOutputs() {
        return outputs;
    }
}
