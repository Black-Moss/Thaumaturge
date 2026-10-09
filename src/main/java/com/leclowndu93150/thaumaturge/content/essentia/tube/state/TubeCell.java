package com.leclowndu93150.thaumaturge.content.essentia.tube.state;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import org.jspecify.annotations.Nullable;

public record TubeCell(@Nullable ResourceKey<IAspect> aspect, int amount) {
    public static final int CAPACITY = 1;
    public static final TubeCell EMPTY = new TubeCell(null, 0);

    public static TubeCell holding(ResourceKey<IAspect> aspect) {
        return new TubeCell(aspect, CAPACITY);
    }

    public boolean hasUnit() {
        return amount >= CAPACITY;
    }

    public boolean isOccupied() {
        return amount > 0;
    }

    public boolean holds(Holder<IAspect> candidate) {
        return aspect != null && candidate.is(aspect);
    }

    public TubeCell dropDanglingAspect() {
        return aspect != null && amount == 0 ? EMPTY : this;
    }
}
