package com.leclowndu93150.thaumaturge.content.essentia.thaumatorium.work;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import org.jspecify.annotations.Nullable;

public final class CraftSelection {
    private @Nullable Identifier recipe;
    private @Nullable ResourceKey<IAspect> requested;

    public @Nullable Identifier recipe() {
        return recipe;
    }

    public @Nullable ResourceKey<IAspect> requested() {
        return requested;
    }

    public void choose(@Nullable Identifier id) {
        recipe = id;
    }

    public void request(@Nullable ResourceKey<IAspect> aspect) {
        requested = aspect;
    }

    public void reset() {
        recipe = null;
        requested = null;
    }
}
