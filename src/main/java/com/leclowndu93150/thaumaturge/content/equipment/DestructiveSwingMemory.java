package com.leclowndu93150.thaumaturge.content.equipment;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public final class DestructiveSwingMemory {
    private @Nullable Swing last;

    public void remember(ResourceKey<Level> dimension, BlockPos pos, Direction face) {
        last = new Swing(dimension, pos.immutable(), face);
    }

    public void forget() {
        last = null;
    }

    public @Nullable Direction takeFace(ResourceKey<Level> dimension, BlockPos pos) {
        Swing swing = last;
        last = null;
        return swing != null && swing.dimension().equals(dimension) && swing.pos().equals(pos) ? swing.face() : null;
    }

    private record Swing(ResourceKey<Level> dimension, BlockPos pos, Direction face) {
    }
}
