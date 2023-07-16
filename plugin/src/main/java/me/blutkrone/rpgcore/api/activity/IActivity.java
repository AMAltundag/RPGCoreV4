package me.blutkrone.rpgcore.api.activity;

/**
 * An activity refers to a process tying to an action, only
 * one activity may be run at a given time.
 */
public interface IActivity {

    /**
     * Update the activity, called once per tick. Once we
     * return true the activity is abandoned.
     *
     * @return true the activity finished.
     */
    boolean update();

    /**
     * A progress ratio for the progress indication.
     *
     * @return the progress ratio.
     */
    double getProgress();

    /**
     * A single line which provides us with information
     * about the activity.
     *
     * @return description of the current activity state.
     */
    String getInfoText();

    /**
     * Interrupt the activity, should this method be called
     * the activity must be removed from the entity.
     */
    void interrupt();
}