package me.blutkrone.rpgcore.skill.modifier;

import me.blutkrone.rpgcore.api.IContext;
import org.bukkit.Bukkit;

public class CoreModifierNumber {

    private final double number;

    public CoreModifierNumber(double number) {
        this.number = number;
    }

    public int evalAsInt(IContext context) {
        return (int) evalAsDouble(context);
    }

    public double evalAsDouble(IContext context) {
        Bukkit.getLogger().severe("not implemented (core modifier number, double)");
        return this.number;
    }
}
