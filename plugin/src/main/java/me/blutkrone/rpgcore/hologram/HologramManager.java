package me.blutkrone.rpgcore.hologram;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import java.util.UUID;

/**
 * A manager responsible for creating holograms in the
 * world, either entity holograms (which are locked on
 * the target entity) or world holograms (locked on the
 * given location)
 */
public class HologramManager {

    private Scoreboard scoreboard;

    public HologramManager() {
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
    }

    /**
     * The scoreboard to be used by the hologram management.
     *
     * @return the scoreboard we are using.
     */
    public Scoreboard getScoreboard() {
        if (this.scoreboard == null) {
            ScoreboardManager scoreboard_manager = Bukkit.getScoreboardManager();
            if (scoreboard_manager != null) {
                this.scoreboard = scoreboard_manager.getNewScoreboard();
            } else {
                throw new NullPointerException("Scoreboard Manager not initialized!");
            }
        }

        return this.scoreboard;
    }

    /**
     * Assign a glow color to the entity.
     *
     * @param uuid what entity to update
     * @param color what color to use, RESET removes color.
     */
    public void setGlowColor(UUID uuid, ChatColor color) {
        Team team = getScoreboard().getTeam(uuid.toString());
        if (team == null) {
            team = getScoreboard().registerNewTeam(uuid.toString());
            team.addEntry(uuid.toString());
        }
        team.setColor(color);
    }

    /**
     * Show or hide basic name tag of an entity.
     *
     * @param uuid what entity to update
     * @param flag hide (true) or show (false)
     */
    public void setHideName(UUID uuid, boolean flag) {
        Team team = getScoreboard().getTeam(uuid.toString());
        if (team == null) {
            team = getScoreboard().registerNewTeam(uuid.toString());
            team.addEntry(uuid.toString());
        }
        team.setOption(Team.Option.NAME_TAG_VISIBILITY, flag
                ? Team.OptionStatus.NEVER : Team.OptionStatus.ALWAYS);
    }
}
