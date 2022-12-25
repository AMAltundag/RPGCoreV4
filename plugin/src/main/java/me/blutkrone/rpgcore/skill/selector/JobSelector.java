package me.blutkrone.rpgcore.skill.selector;

import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.hud.editor.bundle.selector.EditorJobSelector;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class JobSelector extends AbstractCoreSelector {

    private final Set<String> jobs;

    public JobSelector(EditorJobSelector editor) {
        this.jobs = editor.jobs.stream().map(String::toLowerCase).collect(Collectors.toSet());
    }

    @Override
    public List<IOrigin> doSelect(IContext context, List<IOrigin> previous) {
        List<IOrigin> output = new ArrayList<>();
        for (IOrigin target : previous) {
            if (target instanceof CorePlayer) {
                String job = ((CorePlayer) target).getRawJob();
                if (jobs.contains(job.toLowerCase())) {
                    output.add(target);
                }
            }
        }
        return output;
    }
}
