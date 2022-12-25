package me.blutkrone.rpgcore.level;

import com.ezylang.evalex.Expression;
import me.blutkrone.rpgcore.util.io.ConfigWrapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LevelSegment {

    // passive points gained per level (rounded off)
    public Map<String, Double> point_per_level = new HashMap<>();
    // attributes gained per level (not rounded)
    public Map<String, Double> attribute_per_level = new HashMap<>();
    // minimum level to activate segment (inclusive)
    public int minimum_level;
    // maximum level to activate segment (inclusive)
    public int maximum_level;
    // experience gained per level
    public Expression exp_formula;
    // tags required to gain exp in segment
    public List<String> requirement;

    public LevelSegment(ConfigWrapper config) {
        config.forEachUnder("points", (path, root) -> {
            point_per_level.put(path.toLowerCase(), root.getDouble(path));
        });
        config.forEachUnder("attributes", (path, root) -> {
            attribute_per_level.put(path.toLowerCase(), root.getDouble(path));
        });
        minimum_level = config.getInt("minimum-level");
        maximum_level = config.getInt("maximum-level");
        exp_formula = new Expression(config.getString("formula"));
        requirement = config.getStringList("requirement");
    }
}
