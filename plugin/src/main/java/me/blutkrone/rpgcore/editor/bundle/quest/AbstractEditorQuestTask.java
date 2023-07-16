package me.blutkrone.rpgcore.editor.bundle.quest;

import me.blutkrone.rpgcore.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.quest.CoreQuest;
import me.blutkrone.rpgcore.quest.task.AbstractQuestTask;

import java.util.UUID;

public abstract class AbstractEditorQuestTask implements IEditorBundle {

    public AbstractEditorQuestTask() {
    }

    /**
     * Transform into run-time instance.
     *
     * @param quest which quest is linked
     * @return runtime instance.
     */
    public abstract AbstractQuestTask<?> build(CoreQuest quest);

    /**
     * Language code describing the task info, placeholders
     * may apply depending on the exact task.
     *
     * @return language code.
     */
    public abstract String getInfoLC();

    /**
     * A unique ID assigned upon the initial instantiation of
     * the instance, this will be empty when first created.
     *
     * @return a unique identifier
     */
    public abstract UUID getUniqueId();
}
