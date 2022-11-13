package me.blutkrone.rpgcore.hud.menu;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.util.io.ConfigWrapper;
import me.blutkrone.rpgcore.util.io.FileUtil;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A menu dedicated to allow players customize their skillbar
 * and the behaviour of their selected skills.
 */
public class SkillMenu {
    // control elements
    public ItemStack unbind_skill;
    public ItemStack viewport_left;
    public ItemStack viewport_right;
    public ItemStack empty_icon;
    // pages for customizing skills
    public List<SkillPage> skill_pages = new ArrayList<>();

    /**
     * A menu dedicated to allow players customize their skillbar
     * and the behaviour of their selected skills.
     */
    public SkillMenu() throws IOException {
        ConfigWrapper config = FileUtil.asConfigYML(FileUtil.file("menu", "skill.yml"));

        config.forEachUnder("skill-pages", (path, root) -> this.skill_pages.add(new SkillPage(root.getSection(path))));

        this.unbind_skill = RPGCore.inst().getLanguageManager().getAsItem("unbind_skill").build();
        this.viewport_left = RPGCore.inst().getLanguageManager().getAsItem("viewport_left").build();
        this.viewport_right = RPGCore.inst().getLanguageManager().getAsItem("viewport_right").build();
        this.empty_icon = RPGCore.inst().getLanguageManager().getAsItem("skillbar_empty").build();
    }

    /**
     * Open the skill menu for the given player.
     *
     * @param player who to present the skill menu to.
     */
    public void open(Player player) {
        new me.blutkrone.rpgcore.menu.SkillMenu(this).finish(player);
    }

    /**
     * A self-contained page which can be configured to
     * present a selection of 35 skills (the 36th would
     * always be 'unbind'
     */
    public class SkillPage {
        public ItemStack icon;
        public List<String> skills;

        SkillPage(ConfigWrapper config) {
            this.icon = RPGCore.inst().getLanguageManager().getAsItem("icon").build();
            this.skills = config.getStringList("skills");
        }
    }
}
