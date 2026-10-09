package com.leclowndu93150.thaumaturge.content.essentia.tube.behaviour;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import net.minecraft.resources.ResourceKey;
import org.jspecify.annotations.Nullable;

public abstract class DefaultTubeBehaviour implements TubeBehaviour {
    @Override
    public @Nullable ResourceKey<IAspect> suctionFilter() {
        return null;
    }

    @Override
    public boolean restrictiveSuction() {
        return false;
    }

    @Override
    public boolean directionalSuction() {
        return false;
    }

    @Override
    public boolean directionalEqualize() {
        return false;
    }
}
