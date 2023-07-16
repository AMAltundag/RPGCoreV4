package me.blutkrone.rpgcore.skill.selector;

import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.editor.bundle.selector.EditorNoneSelector;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NoneSelector extends AbstractCoreSelector {

    public NoneSelector(EditorNoneSelector editor) {
    }

    @Override
    public List<IOrigin> doSelect(IContext context, List<IOrigin> previous) {
        if (previous.isEmpty()) {
            return Collections.singletonList(new IOrigin.SnapshotOrigin(new Location(Bukkit.getWorlds().get(0), 0d, 0d, 0d)));
        } else {
            return new ArrayList<>();
        }
    }
}
