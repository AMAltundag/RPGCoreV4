package me.blutkrone.rpgcore.hud.menu;

public class SettingsMenu {

    public SettingsMenu() {
    }

    public static class Settings {
        // screen width used by UX manager
        public int screen_width = 960;
        // density of particles to render
        public double particle_density = 1.0d;
        // activity range for holograms
        public double hologram_distance = 32d;

    }
}
