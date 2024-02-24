package me.blutkrone.rpgcore.bbmodel.interpolation;

public class AngleInterpolator {

    private float[] have;
    private float[] want;

    private int dirty_since = 0;

    public AngleInterpolator(float[] start) {
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
            float[] updated = new float[this.have.length];

            for (int j = 0; j < updated.length; j++) {
                float $have = this.have[j];
                float $want = getCloserAngle($have, this.want[j]);

                if (Math.abs($have-$want) <= base) {
                    updated[j] = $want;
                } else if ($want > $have) {
                    float step = (($have-$want)*percent + base) * (1+this.dirty_since);
                    updated[j] = Math.min($want, $have+step);
                    any_dirty = true;
                } else if ($want < $have) {
                    float step = (($want-$have)*percent + base) * (1+this.dirty_since);
                    updated[j] = Math.max($want, $have-step);
                    any_dirty = true;
                }

                updated[j] = updated[j] % 360f;
            }

            this.have = updated;
        }

        if (any_dirty) {
            this.dirty_since += 1;
        } else {
            this.dirty_since = 0;
        }

        return this.have;
    }

    /*
     * Find the closer equivalent angle.
     *
     * @param have Angle we have
     * @param want Angle we want
     * @return Equivalent to 'want' that is closer
     */
    private float getCloserAngle(float have, float want) {
        float dist_a = Math.abs(have-(want+360f));
        float dist_b = Math.abs(have-(want));
        float dist_c = Math.abs(have-(want-360f));

        if (dist_a <= dist_b && dist_a <= dist_c) {
            return want + 360f;
        } else if (dist_b <= dist_a && dist_b <= dist_c) {
            return want;
        } else if (dist_c <= dist_a && dist_c <= dist_b) {
            return want - 360f;
        } else {
            return want;
        }
    }
}
