package com.leclowndu93150.thaumaturge.api.recipe;

import net.minecraft.core.Direction;
import org.jspecify.annotations.Nullable;

/**
 * Where a multiblock dust trigger matched relative to the clicked block.
 *
 * <p>The offsets are added to the clicked position to obtain the multiblock origin. The facing names the
 * horizontal rotation of the structure that matched and is {@code null} only for {@link #origin()}.
 *
 * @param xOffset the x displacement in blocks from the clicked block to the structure origin
 * @param yOffset the y displacement in blocks from the clicked block to the structure origin
 * @param zOffset the z displacement in blocks from the clicked block to the structure origin
 * @param facing the matched horizontal orientation, or {@code null} for the origin placement
 * @since 1.0.0
 */
public record DustTriggerPlacement(int xOffset, int yOffset, int zOffset, @Nullable Direction facing) {
    private static final int NO_OFFSET = 0;

    /**
     * Creates the stand-in placement for triggers that are not multiblocks.
     *
     * @return a placement with all offsets zero and no facing
     * @since 1.0.0
     */
    public static DustTriggerPlacement origin() {
        return new DustTriggerPlacement(NO_OFFSET, NO_OFFSET, NO_OFFSET, null);
    }
}
