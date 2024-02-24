package me.blutkrone.rpgcore.bbmodel.io.deserialized.animation;

/**
 * Interpolation supported by *.bbmodel files.
 */
public enum Interpolation {
    /**
     *
     */
    LINEAR,
    /**
     *
     */
    CATMULLROM,
    /**
     *
     */
    BEZIER,
    /**
     *
     */
    STEP;

    /**
     * Default interpolation
     *
     * @param first  Start interpolation from
     * @param second Finish interpolation at
     * @param ratio  Progress (0.0 to 1.0)
     * @return Interpolated at 'ratio' step
     */
    public float with(float first, float second, float ratio) {
        return first + ((second - first) * ratio);
    }
}
