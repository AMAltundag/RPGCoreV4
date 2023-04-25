package me.blutkrone.rpgcore.hud.menu;

import me.blutkrone.rpgcore.util.io.ConfigWrapper;
import me.blutkrone.rpgcore.util.io.FileUtil;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SocialMenu {

    private Map<String, Integer> equipment_slots = new HashMap<>();
    private List<Integer> skill_slots;
    private List<Integer> special_slots;
    private int profession_slot;
    private int job_slot;

    public SocialMenu() throws IOException {
        ConfigWrapper config = FileUtil.asConfigYML(FileUtil.file("menu", "social.yml"));
        config.forEachUnder("equipment-slot", (path, root) -> equipment_slots.put(path, root.getInt(path)));
        this.skill_slots = config.getIntegerList("skill-slots");
        if (this.skill_slots.size() > 6) {
            this.skill_slots.subList(6, this.skill_slots.size()).clear();
        }
        this.special_slots = config.getIntegerList("special-slots");
        this.profession_slot = config.getInt("profession-slot");
        this.job_slot = config.getInt("job-slot");
    }

    public void present(Player player, UUID target) {
        new me.blutkrone.rpgcore.menu.SocialMenu(target, this).finish(player);
    }

    public Map<String, Integer> getEquipmentSlots() {
        return equipment_slots;
    }

    public List<Integer> getSkillSlots() {
        return skill_slots;
    }

    public int getProfessionSlot() {
        return profession_slot;
    }

    public int getJobSlot() {
        return job_slot;
    }

    public List<Integer> getSpecialSlots() {
        return special_slots;
    }
}
