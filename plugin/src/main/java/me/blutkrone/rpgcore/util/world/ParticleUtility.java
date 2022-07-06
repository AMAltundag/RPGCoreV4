package me.blutkrone.rpgcore.util.world;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.util.io.ConfigWrapper;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.noise.SimplexNoiseGenerator;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ParticleUtility {

    private static List<Particle> COLOR_PARTICLE = Arrays.asList(Particle.REDSTONE, Particle.SPELL_MOB, Particle.SPELL_MOB_AMBIENT, Particle.NOTE);
    private static List<Particle> ITEM_PARTICLE = Arrays.asList(Particle.ITEM_CRACK, Particle.BLOCK_CRACK, Particle.BLOCK_DUST, Particle.FALLING_DUST);
    private final Particle particle;
    private final float speed;
    private final int amount;
    private final IColorSampler color;
    private final Material material;
    private final BlockData data;
    private final double weighting;

    public ParticleUtility(ConfigWrapper config) {
        // basic parameters for the particle utility
        this.weighting = config.getDouble("weighting", 1d);
        this.particle = Particle.valueOf(config.getString("particle", "REDSTONE"));
        this.speed = (float) config.getDouble("speed", 0.01d);
        this.amount = config.getInt("amount", 1);

        if (ParticleUtility.COLOR_PARTICLE.contains(this.particle)) {
            // which colors to interpolate between
            Color[] colors = config.getStringList("colors").stream()
                    .map(color -> Color.fromRGB(Integer.parseInt(color, 16)))
                    .toArray(Color[]::new);

            // decide how the color will be sampled
            String gradient = config.getString("gradient", "none");
            if (gradient.equalsIgnoreCase("spatial")) {
                double offset = config.getDouble("offset", 0d);
                this.color = new SpatialGradientSampler(colors, offset);
            } else if (gradient.equalsIgnoreCase("temporal")) {
                double offset = config.getDouble("offset", 0d);
                this.color = new TemporalGradientSampler(colors, offset);
            } else {
                this.color = new ExactColorSampler(colors);
            }

            // reset fields not used by this particle
            this.material = null;
            this.data = null;
        } else if (ParticleUtility.ITEM_PARTICLE.contains(this.particle)) {
            // particle based on an item
            this.color = new ExactColorSampler(new Color[]{Color.GRAY});
            this.material = Material.valueOf(config.getString("material", "STONE"));
            this.data = this.material.createBlockData();
        } else {
            // generic particles
            this.color = new ExactColorSampler(new Color[]{Color.GRAY});
            this.material = Material.STONE;
            this.data = this.material.createBlockData();
        }
    }

    /**
     * Assuming multiple particle utilities, allows us to spawn a
     *
     * @return
     */
    public double getWeighting() {
        return weighting;
    }

    /**
     * Show the given player a particle at the defined location
     *
     * @param where   location to show particle at
     * @param viewers player to show particle to
     */
    public void showAt(Location where, Collection<Player> viewers) {
        for (Player viewer : viewers) {
            if (particle == Particle.REDSTONE) {
                // Property: Color
                Color color = this.color.of(where);
                viewer.spawnParticle(Particle.REDSTONE, where, amount, new Particle.DustOptions(color, 1));
            } else if (particle == Particle.SPELL_MOB) {
                // Property: Color
                Color color = this.color.of(where);
                viewer.spawnParticle(Particle.SPELL_MOB, where, 0, (double) color.getRed() / 255.0D, (double) color.getGreen() / 255.0D, (double) color.getBlue() / 255.0D, 1.0D);
            } else if (particle == Particle.SPELL_MOB_AMBIENT) {
                // Property: Color
                Color color = this.color.of(where);
                viewer.spawnParticle(Particle.SPELL_MOB_AMBIENT, where, 0, (double) color.getRed() / 255.0D, (double) color.getGreen() / 255.0D, (double) color.getBlue() / 255.0D, 1.0D);
            } else if (particle == Particle.NOTE) {
                // Property: Color
                Color color = this.color.of(where);
                viewer.spawnParticle(Particle.NOTE, where, 0, (double) color.getBlue() / 24.0D, 0.0D, 0.0D, 1.0D);
            } else if (particle == Particle.ITEM_CRACK) {
                // Property: Direction, Material
                if (amount <= 0) {
                    double dX = where.getDirection().getX();
                    double dY = where.getDirection().getY();
                    double dZ = where.getDirection().getZ();
                    viewer.spawnParticle(Particle.ITEM_CRACK, where, 0, dX, dY, dZ, speed, new ItemStack(material));
                } else {
                    viewer.spawnParticle(Particle.ITEM_CRACK, where, amount, new ItemStack(material));
                }
            } else if (particle == Particle.BLOCK_CRACK) {
                // Property: Material
                viewer.spawnParticle(Particle.BLOCK_CRACK, where, amount, data);
            } else if (particle == Particle.BLOCK_DUST) {
                // Property: Direction, Material
                if (amount <= 0) {
                    double dX = where.getDirection().getX();
                    double dY = where.getDirection().getY();
                    double dZ = where.getDirection().getZ();
                    viewer.spawnParticle(Particle.BLOCK_DUST, where, 0, dX, dY, dZ, speed, data);
                } else {
                    viewer.spawnParticle(Particle.BLOCK_DUST, where, amount, data);
                }
            } else if (particle == Particle.FALLING_DUST) {
                // Property: Material
                viewer.spawnParticle(Particle.FALLING_DUST, where, amount, data);
            } else if (amount == 0) {
                // Property: Direction
                double dX = where.getDirection().getX();
                double dY = where.getDirection().getY();
                double dZ = where.getDirection().getZ();
                viewer.spawnParticle(particle, where, 0, dX, dY, dZ, (double) speed);
            } else {
                viewer.spawnParticle(particle, where, amount, 0.0D, 0.0D, 0.0D, (double) speed);
            }
        }
    }

    private interface IColorSampler {
        Color of(Location where);
    }

    private class ExactColorSampler implements IColorSampler {

        final Color[] color;

        private ExactColorSampler(Color[] color) {
            this.color = color;
        }

        @Override
        public Color of(Location where) {
            return color[ThreadLocalRandom.current().nextInt(color.length)];
        }
    }

    private class SpatialGradientSampler implements IColorSampler {

        private final Color[] colors;
        private final double offset;

        public SpatialGradientSampler(Color[] colors, double offset) {
            this.colors = colors;
            this.offset = offset;
        }

        @Override
        public Color of(Location where) {
            // identify the seed which we are operating with
            double seed = ((1d + SimplexNoiseGenerator.getNoise(where.getX(), where.getY(), where.getZ(), offset)) / 2d) * (colors.length - 1);
            int exact = ((int) seed);
            double b = Math.min(1d, Math.max(0d, seed - exact));
            // identify the two colors we wish to blend together
            Color c1 = colors[exact];
            Color c2 = (exact + 1 == colors.length) ? colors[0] : colors[exact + 1];
            // construct a mix of the two colors, relative to our gradient
            int red = (int) Math.min(255, Math.max(0, (c1.getRed() * b) + (c2.getRed() * (1d - b))));
            int green = (int) Math.min(255, Math.max(0, (c1.getGreen() * b) + (c2.getGreen() * (1d - b))));
            int blue = (int) Math.min(255, Math.max(0, (c1.getBlue() * b) + (c2.getBlue() * (1d - b))));
            // output the resulting gradient we acquired
            return Color.fromRGB(red, green, blue);
        }
    }

    private class TemporalGradientSampler implements IColorSampler {

        private final Color[] colors;
        private final double offset;

        public TemporalGradientSampler(Color[] colors, double offset) {
            this.colors = colors;
            this.offset = offset;
        }

        @Override
        public Color of(Location where) {
            // identify the seed which we are operating with
            double seed = ((1d + SimplexNoiseGenerator.getNoise(offset + (RPGCore.inst().getTimestamp() / 20d))) / 2d) * (colors.length - 1);
            int exact = ((int) seed);
            double b = Math.min(1d, Math.max(0d, seed - exact));
            // identify the two colors we wish to blend together
            Color c1 = colors[exact];
            Color c2 = (exact + 1 == colors.length) ? colors[0] : colors[exact + 1];
            // construct a mix of the two colors, relative to our gradient
            int red = (int) Math.min(255, Math.max(0, (c1.getRed() * b) + (c2.getRed() * (1d - b))));
            int green = (int) Math.min(255, Math.max(0, (c1.getGreen() * b) + (c2.getGreen() * (1d - b))));
            int blue = (int) Math.min(255, Math.max(0, (c1.getBlue() * b) + (c2.getBlue() * (1d - b))));
            // output the resulting gradient we acquired
            return Color.fromRGB(red, green, blue);
        }
    }
}