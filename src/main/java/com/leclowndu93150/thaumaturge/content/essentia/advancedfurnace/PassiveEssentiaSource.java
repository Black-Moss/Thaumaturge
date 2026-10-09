package com.leclowndu93150.thaumaturge.content.essentia.advancedfurnace;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaTransport;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import org.jspecify.annotations.Nullable;

abstract class PassiveEssentiaSource implements IEssentiaTransport {
    private static final int ZERO = 0;

    @Override
    public final boolean canInputFrom(Direction face) {
        return false;
    }

    @Override
    public final int addEssentia(Holder<IAspect> aspect, int amount, Direction face) {
        return ZERO;
    }

    @Override
    public final int getMinimumSuction() {
        return ZERO;
    }

    @Override
    public final int getSuctionAmount(@Nullable Direction face) {
        return ZERO;
    }

    @Override
    public final @Nullable Holder<IAspect> getSuctionType(@Nullable Direction face) {
        return null;
    }

    @Override
    public final void setSuction(@Nullable Holder<IAspect> aspect, int amount) {}
}
