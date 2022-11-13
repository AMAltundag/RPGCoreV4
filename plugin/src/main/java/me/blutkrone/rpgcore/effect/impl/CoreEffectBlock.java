package me.blutkrone.rpgcore.effect.impl;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.effect.CoreEffect;
import me.blutkrone.rpgcore.effect.EffectObservation;
import me.blutkrone.rpgcore.hud.editor.bundle.effect.EditorEffectBlock;
import me.blutkrone.rpgcore.hud.editor.bundle.other.EditorTransmutation;
import me.blutkrone.rpgcore.util.collection.WeightedRandomMap;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.*;

public class CoreEffectBlock implements CoreEffect.IEffectPart {
    // transmutation choices
    private List<Transmutation> transmutations;
    // ticks to stay disguised
    private int duration;
    // range we are allowed to sample
    private double spread;
    // number of samples we can do
    private double samples;
    // shows particle upon transformation
    private boolean particle;

    public CoreEffectBlock(EditorEffectBlock editor) {
        // transmutations we can do
        this.transmutations = new ArrayList<>();
        for (EditorTransmutation bundle : editor.transmutations) {
            this.transmutations.add(new Transmutation(bundle));
        }
        // how long to transmute
        this.duration = (int) editor.duration;
        // sampling parameters
        this.spread = editor.spread;
        this.samples = (int) editor.samples;
        particle = editor.particle;

        Bukkit.getLogger().severe("not implemented (block worker)");
    }

    @Override
    public List<CoreEffect.ISubPart> process(Location where, WeightedRandomMap<CoreEffectBrush> brush, double scale, EffectObservation viewing) {
        Location anchor = where.clone();
        double distance = this.spread * scale;
        int samples = (int) (this.samples * scale);

        Bukkit.getScheduler().runTask(RPGCore.inst(), () -> {
            // grab world instance
            World world = anchor.getWorld();
            if (world == null) {
                return;
            }

            // collection to hold sampled blocks
            List<Block> sampled = new ArrayList<>();

            // one pass to sample random locations and go down
            for (int i = 0; i < samples; i++) {
                // random offset
                int diffX = (int) ((Math.random()*2-1) * distance);
                int diffY = (int) ((Math.random()*2-1) * distance);
                int diffZ = (int) ((Math.random()*2-1) * distance);
                // sample the block
                Block block = anchor.getBlock().getRelative(diffX, diffY, diffZ);
                // check if we can do down
                for (int j = 0; j < 5; j++) {
                    Block relative = block.getRelative(BlockFace.DOWN);
                    if (relative.getType().isSolid()) {
                        block = relative;
                    }
                }
                // ensure block can be transmuted
                if (canTransmute(block)) {
                    sampled.add(block);
                }
            }

            // one pass to sample random locations and go up
            for (int i = 0; i < samples; i++) {
                // random offset
                int diffX = (int) ((Math.random()*2-1) * distance);
                int diffY = (int) ((Math.random()*2-1) * distance);
                int diffZ = (int) ((Math.random()*2-1) * distance);
                // sample the block
                Block block = anchor.getBlock().getRelative(diffX, diffY, diffZ);
                // check if we can do down
                for (int j = 0; j < 5; j++) {
                    Block relative = block.getRelative(BlockFace.UP);
                    if (relative.getType().isSolid()) {
                        block = relative;
                    }
                }
                // ensure block can be transmuted
                if (canTransmute(block)) {
                    sampled.add(block);
                }
            }

            // one pass to throw random ray-traces (with random offset)
            for (int i = 0; i < samples; i++) {
                // random offset
                int diffX = (int) ((Math.random()*2-1) * distance);
                int diffY = (int) ((Math.random()*2-1) * distance);
                int diffZ = (int) ((Math.random()*2-1) * distance);
                // create random origin
                Location origin = anchor.clone().add(diffX, diffY, diffZ);
                // throw ray-trace to random direction
                Vector vector = new Vector(Math.random()*2-1, Math.random()*2-1, Math.random()*2-1);
                RayTraceResult traced = world.rayTraceBlocks(origin, vector, spread, FluidCollisionMode.NEVER, true);
                if (traced != null && traced.getHitBlock() != null && traced.getHitBlock().getType().isSolid()) {
                    sampled.add(traced.getHitBlock());
                }
            }

            // trim down our sample range
            Collections.shuffle(sampled);
            if (sampled.size() > samples) {
                sampled.subList(samples, sampled.size()).clear();
            }

            // file a request for the transmutation
            Random r = new Random();
            for (Block block : sampled) {
                Material material = null;
                for (Transmutation transmutation : this.transmutations) {
                    if (material == null && transmutation.source.contains(block.getType())) {
                        int i = r.nextInt(transmutation.target.size());
                        material = transmutation.target.get(i);
                    }
                }

                Material final_material = material;
                viewing.forEach((players, chance) -> {
                    for (Player player : players) {
                        if (Math.random() <= chance) {
                            RPGCore.inst().getEffectManager().disguise(player, block, final_material, duration, particle);
                        }
                    }
                });
            }
        });

        return new ArrayList<>();


        // return Collections.singletonList(new BlockTransmutePart(where.clone(), this.duration, this.distance * scale, (int) (this.samples * scale), viewing));
    }

    /*
     * Check if the given block can be transmuted
     *
     * @return true if we are a transmutation candidate
     */
    private boolean canTransmute(Block block) {
        // check for any valid transmutation
        boolean any_transmutable = false;
        for (Transmutation transmutation : this.transmutations) {
            any_transmutable |= transmutation.source.contains(block.getType());
        }
        // we cannot transmute if none of the transmutations match
        if (!any_transmutable) {
            return false;
        }
        // any adjacent block is air
        return block.getRelative(BlockFace.UP).isEmpty()
                || block.getRelative(BlockFace.DOWN).isEmpty()
                || block.getRelative(BlockFace.NORTH).isEmpty()
                || block.getRelative(BlockFace.EAST).isEmpty()
                || block.getRelative(BlockFace.SOUTH).isEmpty()
                || block.getRelative(BlockFace.WEST).isEmpty();
    }

    /**
     * Internal groups that identify materials
     */
    public enum BlockMask {
        NONE("NONE"),
        FENCE("_FENCE"),
        GATE("_FENCE_GATE"),
        GLASS("_STAINED_GLASS"),
        LEAVES("_LEAVES"),
        SLAB("_SLAB"),
        STAIR("_STAIRS"),
        WALL("_WALL"),
        LOG("_LOG"),
        WOOD("_WOOD");

        private List<Material> materials;

        BlockMask(String suffix) {
            materials = new ArrayList<>();
            for (Material material : Material.values()) {
                if (material.isLegacy() || !material.name().endsWith(suffix)) {
                    continue;
                }

                materials.add(material);
            }
        }

        BlockMask(Material... manual) {
            materials = Arrays.asList(manual);
        }

        /**
         * Retrieve blocks which are a member of this
         * mask.
         *
         * @return materials that are our members
         */
        public List<Material> getMembers() {
            return this.materials;
        }
    }

    /*
     * A wrapper containing information on how to transform
     */
    private class Transmutation {
        // materials to transform from
        private Set<Material> source;
        // materials to transform towards
        private List<Material> target;

        public Transmutation(EditorTransmutation editor) {
            // identify the transmutation candidates
            this.target = new ArrayList<>(editor.target);
            if (editor.mask == BlockMask.NONE) {
                this.source = new HashSet<>(editor.source);
            } else {
                this.source = new HashSet<>(editor.mask.getMembers());
            }
        }
    }
}