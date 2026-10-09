package com.leclowndu93150.thaumaturge.content.essentia.thaumatorium;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaTransport;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import org.jspecify.annotations.Nullable;

public interface SinkOnlyEssentiaFace extends IEssentiaTransport {
    int NOTHING = 0;

    @Override
    default boolean canOutputTo(Direction face) {
        return false;
    }

    @Override
    default @Nullable Holder<IAspect> getEssentiaType(@Nullable Direction face) {
        return null;
    }

    @Override
    default int getEssentiaAmount(@Nullable Direction face) {
        return NOTHING;
    }

    @Override
    default int takeEssentia(Holder<IAspect> aspect, int amount, Direction face) {
        return NOTHING;
    }

    @Override
    default int getMinimumSuction() {
        return NOTHING;
    }
}
