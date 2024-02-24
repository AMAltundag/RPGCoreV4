package me.blutkrone.rpgcore.level;

import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.parser.ParseException;
import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.util.io.ConfigWrapper;
import me.blutkrone.rpgcore.util.io.FileUtil;

import java.io.IOException;
import java.util.*;

/**
 * A manager responsible for levelling related things.
 */
public class LevelManager {

    // the segment of levels we are operating in
    private List<LevelSegment> segments = new ArrayList<>();
    // experience needed to break out of a level
    private Map<Integer, Double> exp_to_level = new HashMap<>();
    // total passive points (from levels) at a level
    private Map<Integer, Map<String, Integer>> points_at_level = new HashMap<>();
    // total attributes (from levels) at a level
    private Map<Integer, Map<String, Double>> attributes_at_level = new HashMap<>();
    // tags required to gain exp in the target level
    private Map<Integer, Set<String>> tags_required_at_level = new HashMap<>();
    // whether a level was segmented for
    private Map<Integer, Boolean> level_is_segmented = new HashMap<>();
    // experience reduction by level difference
    private Map<Integer, Double> exp_multiplier = new HashMap<>();
    private Expression lazy_exp_multiplier;
    // experience required to level professions
    private Map<Integer, Double> profession_exp = new HashMap<>();
    private Expression lazy_profession_exp;

    public LevelManager() {
        // load the segments from the disk
        try {
            ConfigWrapper config = FileUtil.asConfigYML(FileUtil.file("level.yml"));
            this.segments.addAll(config.getObjectList("character-level-segment", LevelSegment::new));
            this.lazy_exp_multiplier = new Expression(config.getString("level-difference-multiplier"));
            this.lazy_profession_exp = new Expression(config.getString("profession-exp-formula"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get an exp multiplier with relative levels.
     *
     * @param self  level of the one gaining the exp
     * @param other level of the one providing the exp
     * @return multiplier to experience
     */
    public double getMultiplier(int self, int other) {
        return this.exp_multiplier.computeIfAbsent((self - other), (diff -> {
            try {
                return this.lazy_exp_multiplier.with("difference", diff).evaluate().getNumberValue().doubleValue();
            } catch (Exception e) {
                RPGCore.inst().getLogger().severe("Failed evaluating " + this.lazy_exp_multiplier.getExpressionString());
                e.printStackTrace();
                return 1.0d;
            }
        }));
    }

    /**
     * Whether there is any configuration present for the given level.
     *
     * @param level whether any configuration is present
     * @return true if we have any configuraiton
     */
    public boolean isSegmentedFor(int level) {
        return this.level_is_segmented.computeIfAbsent(level, (l -> {
            boolean segmented = false;
            for (LevelSegment segment : this.segments) {
                if (l <= segment.maximum_level && l >= segment.minimum_level) {
                    segmented = true;
                }
            }
            return segmented;
        }));
    }

    /**
     * Check if we have the tags required for the next level.
     *
     * @param level which level to check against
     * @return the tags required at the given level
     */
    public Set<String> getRequiredTags(int level) {
        return this.tags_required_at_level.computeIfAbsent(level, (l -> {
            Set<String> tagged = new HashSet<>();
            for (LevelSegment segment : this.segments) {
                if (level >= segment.minimum_level && level <= segment.maximum_level) {
                    tagged.addAll(segment.requirement);
                }
            }
            return tagged;
        }));
    }

    /**
     * Passive points (of a type) gained based on the levels
     * accumulated by the player
     *
     * @param player who we are dealing with
     * @param point  the points we have
     * @return how many passive points of a certain point we get
     */
    public int getPassivesFromLevels(CorePlayer player, String point) {
        return points_at_level.computeIfAbsent(player.getCurrentLevel(), (level -> {
            Map<String, Integer> points = new HashMap<>();

            for (LevelSegment segment : this.segments) {
                if (level >= segment.minimum_level && level <= segment.maximum_level) {
                    int relative_level = 1 + (level - segment.minimum_level);
                    for (Map.Entry<String, Double> entry : segment.point_per_level.entrySet()) {
                        String p = entry.getKey();
                        double multi = entry.getValue();
                        points.merge(p, ((int) multi * relative_level), (a, b) -> a + b);
                    }
                }
            }

            return points;
        })).getOrDefault(point, 0);
    }

    /**
     * Get attributes gained based on level.
     *
     * @param player whose level to base it off.
     * @return the
     */
    public Map<String, Double> getAttributesFromLevels(CorePlayer player) {
        return attributes_at_level.computeIfAbsent(player.getCurrentLevel(), (level -> {
            Map<String, Double> attributes = new HashMap<>();

            for (LevelSegment segment : this.segments) {
                if (level >= segment.minimum_level && level <= segment.maximum_level) {
                    int relative_level = (level - segment.minimum_level);
                    for (Map.Entry<String, Double> entry : segment.attribute_per_level.entrySet()) {
                        String attr = entry.getKey();
                        double multi = entry.getValue();
                        attributes.merge(attr, multi * relative_level, (a, b) -> a + b);
                    }
                }
            }

            return attributes;
        }));
    }

    /**
     * Experience points needed to break out of the current level
     * of a profession. Level cap is defined by profession. This
     * will be MAX_VALUE if a bad formula is used.
     *
     * @param profession_level level of the profession we want to level up
     * @return experience to level it up
     */
    public double getExpToLevelUpProfession(int profession_level) {
        return profession_exp.computeIfAbsent(profession_level, level -> {
            try {
                return this.lazy_profession_exp.with("level", level).evaluate().getNumberValue().doubleValue();
            } catch (EvaluationException | ParseException e) {
                RPGCore.inst().getLogger().severe("Failed evaluating " + this.lazy_profession_exp.getExpressionString());
                e.printStackTrace();
                return Double.MAX_VALUE;
            }
        });
    }

    /**
     * Experience points needed to break out of the current level
     * of a player. If this is less then zero, level is not able
     * to be reached.
     *
     * @param player who wants to level up
     * @return exp needed to level up
     */
    public double getExpToLevelUp(CorePlayer player) {
        return exp_to_level.computeIfAbsent(player.getCurrentLevel(), (level) -> {
            double required = -1d;
            for (LevelSegment segment : this.segments) {
                if (level >= segment.minimum_level && level <= segment.maximum_level) {
                    double wanted = -1d;
                    try {
                        wanted = segment.exp_formula.with("level", level).evaluate().getNumberValue().doubleValue();
                    } catch (Exception e) {
                        RPGCore.inst().getLogger().severe("Failed evaluating " + segment.exp_formula.getExpressionString());
                        e.printStackTrace();
                    }
                    required = Math.max(required, wanted);
                }
            }
            return required;
        });
    }
}
