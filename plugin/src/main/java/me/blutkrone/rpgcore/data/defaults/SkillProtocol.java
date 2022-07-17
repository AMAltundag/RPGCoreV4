package me.blutkrone.rpgcore.data.defaults;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.data.DataBundle;
import me.blutkrone.rpgcore.data.structure.DataProtocol;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.skill.CoreSkill;

import java.util.HashMap;
import java.util.HashSet;

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

        bundle.addNumber(player.getEvolutionUnlock().size());
        player.getEvolutionUnlock().forEach((skill, unlocks) -> {
            bundle.addString(skill);
            bundle.addNumber(unlocks.size());
            unlocks.forEach(bundle::addNumber);
        });

        bundle.addNumber(player.getEvolutionFitted().size());
        player.getEvolutionFitted().forEach((skill, fitted) -> {
            bundle.addString(skill);
            bundle.addNumber(fitted.size());
            fitted.forEach((slot, evo) -> {
                bundle.addNumber(slot);
                bundle.addString(evo);
            });
        });
    }

    @Override
    public void load(CorePlayer player, DataBundle bundle) {
        if (!bundle.isEmpty()) {
            int header = 0;

            // recover the skill on the skillbar
            for (; header < 6; header++) {
                String skillId = bundle.getString(header);
                if (!"nothing".equals(skillId)) {
                    player.getSkillbar().setSkill(header, RPGCore.inst().getSkillManager().getIndex().get(skillId));
                }
            }

            // recover the unlocked evolution slots
            int remaining = bundle.getNumber(header++).intValue();
            for (int i = 0; i < remaining; i++) {
                String skillId = bundle.getString(header++);
                int remaining2 = bundle.getNumber(header++).intValue();
                for (int j = 0; j < remaining2; j++) {
                    int slot = bundle.getNumber(header++).intValue();
                    player.getEvolutionUnlock().computeIfAbsent(skillId, (k -> new HashSet<>())).add(slot);
                }
            }

            // recover the fitted evolution choices
            remaining = bundle.getNumber(header++).intValue();
            for (int i = 0; i < remaining; i++) {
                String skillId = bundle.getString(header++);
                int remaining2 = bundle.getNumber(header++).intValue();
                for (int j = 0; j < remaining2; j++) {
                    int slot = bundle.getNumber(header++).intValue();
                    String evolution = bundle.getString(header++);
                    player.getEvolutionFitted().computeIfAbsent(skillId, (k -> new HashMap<>())).put(slot, evolution);
                }
            }
        }
    }
}
