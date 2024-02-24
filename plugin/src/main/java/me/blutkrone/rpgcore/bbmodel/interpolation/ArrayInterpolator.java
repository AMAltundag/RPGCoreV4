package me.blutkrone.rpgcore.bbmodel.interpolation;

/**
 * Interpolation of a float array.
 */
public class ArrayInterpolator {

    private float[] have;
    private float[] want;

    private int dirty_since = 0;

    public ArrayInterpolator(float[] start) {
        this.have = start;
        this.want = start;
    }

    /**
     * Update interpolation target
     *
     * @param want
     */
    public void update(float[] want) {
        this.want = want;
    }

    /**
     * Perform the interpolation
     *
     * @param delta
     * @param base Interpolation in base units
     * @param percent Interpolation in percentage
     * @return
     */
    public float[] interpolate(int delta, float base, float percent) {
        boolean any_dirty = false;

        for (int i = 0; i < delta; i++) {
            float[] updated = new float[have.length];

            for (int j = 0; j < updated.length; j++) {
                if (Math.abs(have[j]-want[j]) <= base) {
                    updated[j] = want[j];
                } else if (want[j] < have[j]) {
                    float step = ((have[j]-want[j])*percent + base) * (1+this.dirty_since);
                    updated[j] = Math.min(want[j], have[j]+step);
                    any_dirty = true;
                } else if (want[j] > have[j]) {
                    float step = ((want[j]-have[j])*percent + base) * (1+this.dirty_since);
                    updated[j] = Math.max(want[j], have[j]-step);
                    any_dirty = true;
                }
            }

            have = updated;
        }

        if (any_dirty) {
            dirty_since += 1;
        } else {
            dirty_since = 0;
        }

        return have;
    }
}
