package me.blutkrone.rpgcore.data.defaults;

import me.blutkrone.rpgcore.data.DataBundle;
import me.blutkrone.rpgcore.data.structure.DataProtocol;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;

import java.util.List;

public class EditorProtocol implements DataProtocol {
    @Override
    public boolean isRosterData() {
        return false;
    }

    @Override
    public void save(CorePlayer player, DataBundle bundle) {
        List<String> history = player.getEditorHistory();
        bundle.addNumber(history.size());
        for (String s : history) {
            bundle.addString(s);
        }
    }

    @Override
    public void load(CorePlayer player, DataBundle bundle) {
        if (!bundle.isEmpty()) {
            int i = bundle.getNumber(0).intValue();
            for (int j = 0; j < i; j++) {
                player.getEditorHistory().add(bundle.getString(1 + j));
            }
        }
    }
}
