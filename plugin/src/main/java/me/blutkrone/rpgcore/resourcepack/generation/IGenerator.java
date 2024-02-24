package me.blutkrone.rpgcore.resourcepack.generation;

import me.blutkrone.rpgcore.resourcepack.OngoingGeneration;

/**
 * A generator instance which will supply us with additions
 * to the resourcepack.
 */
public interface IGenerator {

    /**
     * Request to generate something.
     *
     * @param generation Object containing all data on our generation.
     */
    void generate(OngoingGeneration generation) throws Exception;
}
