package me.blutkrone.rpgcore.skill.behaviour;

import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.hud.editor.bundle.mechanic.AbstractEditorMechanic;
import me.blutkrone.rpgcore.hud.editor.bundle.other.EditorAction;
import me.blutkrone.rpgcore.hud.editor.bundle.other.EditorMobLogic;
import me.blutkrone.rpgcore.hud.editor.bundle.selector.AbstractEditorSelector;
import me.blutkrone.rpgcore.skill.mechanic.*;
import me.blutkrone.rpgcore.skill.selector.AbstractCoreSelector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * A wrapper for mechanics to run a subset of targets.
 */
public class CoreAction {

    // transmute the original set of targets
    public List<AbstractCoreSelector> selectors = new ArrayList<>();
    // the mechanics to be invoked
    public List<AbstractCoreMechanic> mechanics = new ArrayList<>();

    /**
     * @param editor
     */
    public CoreAction(EditorMobLogic editor) {
        for (IEditorBundle bundle : editor.steps_to_execute) {
            if (bundle instanceof AbstractEditorMechanic) {
                this.mechanics.add(((AbstractEditorMechanic) bundle).build());
            }
        }
        for (IEditorBundle bundle : editor.selector) {
            if (bundle instanceof AbstractEditorSelector) {
                this.selectors.add(((AbstractEditorSelector) bundle).build());
            }
        }
    }

    /**
     * @param editor
     */
    public CoreAction(EditorAction editor) {
        for (IEditorBundle selector : editor.selectors) {
            this.selectors.add(((AbstractEditorSelector) selector).build());
        }
        for (IEditorBundle mechanic : editor.mechanics) {
            this.mechanics.add(((AbstractEditorMechanic) mechanic).build());
        }
    }

    /**
     * Create a pipeline for the execution of an action.
     *
     * @return the pipeline that we created.
     */
    public ActionPipeline pipeline(IContext context) {
        return new ActionPipeline(context, Collections.singletonList(context.getCoreEntity()));
    }

    /**
     * Create a pipeline for the execution of an action.
     *
     * @return the pipeline that we created.
     */
    public ActionPipeline pipeline(IContext context, List<IOrigin> origins) {
        return new ActionPipeline(context, origins);
    }

    /**
     * A pipeline which we are using to execute a set of logic.
     */
    public class ActionPipeline {
        private IContext context;

        private int sleeping;
        private List<AbstractCoreSelector> stalled;
        private BarrierMechanic.ActiveBarrier barrier;

        private List<ActionWorker> working = new ArrayList<>();
        private List<IOrigin> targets;

        /**
         * A pipeline which we are using to execute a set of logic.
         *
         * @param context the context invoked within.
         */
        ActionPipeline(IContext context, List<IOrigin> targets) {
            this.context = context;
            this.targets = targets;
            this.working.add(new ActionWorker(CoreAction.this));
        }

        /**
         * Work off this pipeline, if we return true we are done.
         *
         * @return whether we are finished
         */
        public boolean update() {
            // if nothing remains, we are done
            if (this.working.isEmpty()) {
                return true;
            }

            CoreEntity core_entity = this.context.getCoreEntity();

            // asleep for a fixed duration
            if (sleeping > 0) {
                // work off our sleep timer
                sleeping -= 1;
                return false;
            }

            // asleep until logic is finished
            if (stalled != null) {
                // check if we've archived our target condition
                List<IOrigin> targets = new ArrayList<>();
                targets.add(context.getCoreEntity());
                for (AbstractCoreSelector selector : stalled) {
                    targets = selector.doSelect(core_entity, targets);
                }
                // if we found anyone, we do no longer stall
                if (!targets.isEmpty()) {
                    stalled = null;
                }
                // this ensures at least one tick of delay
                return false;
            }

            // asleep until barrier is broken
            if (barrier != null) {
                // check if barrier ran out of time
                if (barrier.duration > 0) {
                    if (--barrier.duration == 0) {
                        barrier.applyFailure(core_entity);
                        boolean terminate = barrier.doTerminateWhenFailed();
                        barrier = null;
                        return terminate;
                    }
                }
                // check if barrier has been broken
                if (barrier.damage > 0) {
                    return false;
                } else {
                    barrier = null;
                }
            }

            // work off the actions we've queried
            A:
            while (!this.working.isEmpty()) {
                ActionWorker worker = this.working.get(0);

                // grab the target we want to use
                List<IOrigin> targets = new ArrayList<>(this.targets);
                for (AbstractCoreSelector sel : worker.action.selectors) {
                    targets = sel.doSelect(core_entity, targets);
                }

                while (worker.mechanics.hasNext()) {
                    AbstractCoreMechanic next = worker.mechanics.next();
                    // special cases for mob owned mechanics
                    if (next instanceof BranchMechanic) {
                        List<ActionWorker> workers = new ArrayList<>();
                        for (CoreAction action : ((BranchMechanic) next).getMechanic(context).getActions()) {
                            workers.add(new ActionWorker(action));
                        }
                        this.working.addAll(0, workers);
                        break A;
                    } else if (next instanceof MultiMechanic) {
                        List<ActionWorker> workers = new ArrayList<>();
                        for (CoreAction action : ((MultiMechanic) next).getActions()) {
                            workers.add(new ActionWorker(action));
                        }
                        this.working.addAll(0, workers);
                        break A;
                    } else if (next instanceof BarrierMechanic) {
                        // invoke a barrier on the mob
                        barrier = ((BarrierMechanic) next).activate(core_entity);
                        return false;
                    } else if (next instanceof SleepMechanic) {
                        // delay by a number of ticks
                        sleeping = ((SleepMechanic) next).timeToSleep(core_entity);
                        return false;
                    } else if (next instanceof StallMechanic) {
                        // delay until condition is archived
                        stalled = ((StallMechanic) next).getCondition();
                        return false;
                    } else if (next instanceof ExitMechanic) {
                        // early exit requested
                        return true;
                    } else {
                        // generic execution of the mechanic
                        next.doMechanic(context, targets);
                    }
                }

                this.working.remove(0);
            }

            // check if anything is left to work off
            return this.working.isEmpty();
        }

        /**
         * Stall further execution of this action pipeline, until we
         *
         * @return
         */
        public BarrierMechanic.ActiveBarrier getBarrier() {
            return barrier;
        }
    }

    /*
     * A wrapper to assist
     */
    private class ActionWorker {
        CoreAction action;
        Iterator<AbstractCoreMechanic> mechanics;

        public ActionWorker(CoreAction action) {
            this.action = action;
            this.mechanics = action.mechanics.iterator();
        }
    }
}
