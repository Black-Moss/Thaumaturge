package com.leclowndu93150.thaumaturge.content.essentia.thaumatorium;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaConduit;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import org.jspecify.annotations.Nullable;

public interface SinkOnlyEssentiaFace extends IEssentiaConduit {
    int NOTHING = 0;

    @Override
    default boolean releasesOn(Direction face) {
        return false;
    }

    @Override
    default @Nullable Holder<IAspect> heldAspect(@Nullable Direction face) {
        return null;
    }

    @Override
    default int heldAmount(@Nullable Direction face) {
        return NOTHING;
    }

    @Override
    default int extract(Holder<IAspect> aspect, int amount, Direction face, boolean simulate) {
        return NOTHING;
    }

    @Override
    default int pullThreshold() {
        return NOTHING;
    }
}
