package com.leclowndu93150.thaumaturge.content.essentia.tube.behaviour;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import net.minecraft.resources.ResourceKey;
import org.jspecify.annotations.Nullable;

public interface TubeBehaviour {
    @Nullable
    ResourceKey<IAspect> suctionFilter();

    boolean restrictiveSuction();

    boolean directionalSuction();

    boolean directionalEqualize();
}
