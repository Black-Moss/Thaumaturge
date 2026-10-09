package com.leclowndu93150.thaumaturge.content.essentia.tube.behaviour;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import net.minecraft.resources.ResourceKey;
import org.jspecify.annotations.Nullable;

public final class FilterTubeBehaviour extends DefaultTubeBehaviour {
    private @Nullable ResourceKey<IAspect> aspectFilter;

    @Override
    public @Nullable ResourceKey<IAspect> suctionFilter() {
        return aspectFilter;
    }

    public void setAspectFilter(@Nullable ResourceKey<IAspect> filter) {
        this.aspectFilter = filter;
    }
}
