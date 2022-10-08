package me.blutkrone.rpgcore.hud.editor.instruction;

import me.blutkrone.rpgcore.util.fontmagic.MagicStringBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * A utility to add an instruction text to
 */
public class InstructionBuilder {

    // the instruction lines
    private List<String> lines = new ArrayList<>();

    /**
     * Add a line to serve as an instruction.
     *
     * @param line the line to add.
     * @return this instance for chaining
     */
    public InstructionBuilder add(String line) {
        this.lines.add(line);
        return this;
    }

    /**
     * Add a line to serve as an instruction.
     *
     * @param line the line to add.
     * @return this instance for chaining
     */
    public InstructionBuilder add(List<String> line) {
        this.lines.addAll(line);
        return this;
    }

    /**
     * Add the instructions to the given builder.
     *
     * @param builder the builder to attach to.
     */
    public void apply(MagicStringBuilder builder) {
        // todo build a backdrop on instructions

        // append the lines we got
        for (int i = 0; i < this.lines.size() && i < 24; i++) {
            String line = "§f" + this.lines.get(i);
            builder.shiftToExact(250).append(line, "instruction_text_" + (i + 1));
        }
    }
}
