package me.blutkrone.rpgcore.effect.impl;

import me.blutkrone.rpgcore.effect.CoreEffect;
import me.blutkrone.rpgcore.hud.editor.bundle.EditorWait;
import me.blutkrone.rpgcore.util.collection.WeightedRandomMap;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.List;

public class CoreWait implements CoreEffect.IEffectPart {

    // how many ticks to wait
    public int time;

    public CoreWait(EditorWait editor) {
        this.time = (int) editor.time;
    }

    @Override
    public void process(Location where, Vector offset, WeightedRandomMap<CoreParticleBrush> brush, double scale, List<Player> viewing) {
        // wait does not do anything
    }
}
