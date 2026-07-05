package com.leclowndu93150.thaumcraft.api.items;

import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

/**
 * Provides floating text lines that render above a block when a player wearing goggles
 * looks directly at it.
 *
 * <p>Implement this on a {@link net.minecraft.world.level.block.entity.BlockEntity} or a {@link
 * net.minecraft.world.level.block.Block}; the client overlay checks the crosshair target each
 * frame, the block entity taking precedence over the block. Text renders as yaw-billboarded,
 * depth-ignoring lines centered above the block.
 *
 * @since 1.0.0
 */
public interface IGogglesDisplayExtended {
    /**
     * Returns the text lines to display, ordered top to bottom.
     *
     * <p>Called on the client render thread every frame while the block is targeted, so the
     * result must be computed from client-synced state only and should allocate sparingly.
     *
     * @return the lines to render; an empty array renders nothing
     */
    Component[] getIGogglesText();

    /**
     * Returns the offset from the block position at which the text column is anchored.
     *
     * @return the anchor offset in block units; {@link Vec3#ZERO} centers on the block
     */
    default Vec3 getIGogglesTextOffset() {
        return Vec3.ZERO;
    }
}
