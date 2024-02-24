package me.blutkrone.rpgcore.bbmodel.active.component;

public class AnimationActive {
    // playback speed of the animation
    public float speed;
    // how far along we are into one loop
    public float progress;
    // whether animation is active or not
    public boolean active;

    // reduce weight and cancel interpolation
    public boolean fade;
    // intensity of the animation
    public float weight;

    // forced looping of animation
    public boolean force_loop;

    public AnimationActive() {
        this.speed = 0.05f;
        this.progress = 0.0f;
        this.active = false;
        this.weight = 1f;
        this.force_loop = false;
    }

    @Override
    public String toString() {
        return "AnimationActive{" +
                "speed=" + speed +
                ", progress=" + progress +
                ", active=" + active +
                ", weight=" + weight +
                '}';
    }
}
