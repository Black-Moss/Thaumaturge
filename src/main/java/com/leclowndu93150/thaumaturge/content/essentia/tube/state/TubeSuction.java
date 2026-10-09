package com.leclowndu93150.thaumaturge.content.essentia.tube.state;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import org.jspecify.annotations.Nullable;

public record TubeSuction(@Nullable ResourceKey<IAspect> aspect, int strength) {
    public static final TubeSuction NONE = new TubeSuction(null, 0);

    public static TubeSuction of(@Nullable Holder<IAspect> aspect, int strength) {
        return new TubeSuction(aspect == null ? null : aspect.unwrapKey().orElse(null), Math.max(strength, 0));
    }

    public boolean isPulling() {
        return strength > 0;
    }
}
