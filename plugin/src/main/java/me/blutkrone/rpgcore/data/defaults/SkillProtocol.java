package me.blutkrone.rpgcore.data.defaults;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.data.DataBundle;
import me.blutkrone.rpgcore.data.structure.DataProtocol;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.skill.CoreSkill;

public class SkillProtocol implements DataProtocol {

    @Override
    public boolean isRosterData() {
        return false;
    }

    @Override
    public void save(CorePlayer player, DataBundle bundle) {
        for (int i = 0; i < 6; i++) {
            CoreSkill skill = player.getSkillbar().getSkill(i);
            if (skill == null) {
                bundle.addString("nothing");
            } else {
                bundle.addString(skill.getId());
            }
        }
    }

    @Override
    public void load(CorePlayer player, DataBundle bundle, int version) {
        if (!bundle.isEmpty()) {
            int header = 0;

            // recover the skill on the skillbar
            for (; header < 6; header++) {
                String skillId = bundle.getString(header);
                if (!"nothing".equals(skillId)) {
                    player.getSkillbar().setSkill(header, RPGCore.inst().getSkillManager().getIndex().get(skillId));
                }
            }
        }
    }
}
