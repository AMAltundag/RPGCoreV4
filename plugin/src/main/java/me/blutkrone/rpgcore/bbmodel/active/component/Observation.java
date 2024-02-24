package me.blutkrone.rpgcore.bbmodel.active.component;

/**
 * Respective states of the observation process
 */
public enum Observation {
    /**
     * Player requested to see a model
     */
    SHOW,
    /**
     * Model is being actively updated
     */
    UPDATE,
    /**
     * Player requested to not see a model
     */
    HIDE
}
