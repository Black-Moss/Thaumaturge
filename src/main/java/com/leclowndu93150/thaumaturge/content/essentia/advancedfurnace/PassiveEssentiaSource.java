package com.leclowndu93150.thaumaturge.content.essentia.advancedfurnace;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaPull;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaConduit;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import org.jspecify.annotations.Nullable;

abstract class PassiveEssentiaSource implements IEssentiaConduit {
    private static final int ZERO = 0;

    @Override
    public final boolean receivesOn(Direction face) {
        return false;
    }

    @Override
    public final int insert(Holder<IAspect> aspect, int amount, Direction face, boolean simulate) {
        return ZERO;
    }

    @Override
    public final int roomFor(Holder<IAspect> aspect, Direction face) {
        return ZERO;
    }

    @Override
    public final int pullThreshold() {
        return ZERO;
    }

    @Override
    public final EssentiaPull pull(@Nullable Direction face) {
        return EssentiaPull.NONE;
    }
}
