package me.blutkrone.rpgcore.hud.editor.constraint.bundle;

import me.blutkrone.rpgcore.hud.editor.IEditorConstraint;

import java.util.List;

public abstract class AbstractMonoConstraint implements IEditorConstraint {

    @Override
    public final boolean isMonoType() {
        return true;
    }

    @Override
    public final List<String> getHint(String value) {
        throw new UnsupportedOperationException("cannot do this with a mono type!");
    }

    @Override
    public final boolean isDefined(String value) {
        throw new UnsupportedOperationException("cannot do this with a mono type!");
    }

    @Override
    public final void extend(String value) {
        throw new UnsupportedOperationException("cannot do this with a mono type!");
    }

    @Override
    public final boolean canExtend() {
        throw new UnsupportedOperationException("cannot do this with a mono type!");
    }

    @Override
    public final String getConstraintAt(List container, int index) {
        return "mono_type";
    }

    @Override
    public final Object asTypeOf(String value) {
        throw new UnsupportedOperationException("cannot do this with a mono type!");
    }

    @Override
    public final String toTypeOf(Object value) {
        throw new UnsupportedOperationException("cannot do this with a mono type!");
    }
}
