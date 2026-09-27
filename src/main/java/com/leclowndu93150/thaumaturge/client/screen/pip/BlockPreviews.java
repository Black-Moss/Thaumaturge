package com.leclowndu93150.thaumaturge.client.screen.pip;

import com.leclowndu93150.thaumaturge.mixin.client.gui.GuiGraphicsExtractorAccessor;
import java.util.Map;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix3x2f;

public final class BlockPreviews {
    private static final float TILT = 25;
    private BlockPreviews() {}

    public static void render(GuiGraphicsExtractor graphics, int centerX, int centerY, Map<BlockPos, BlockState> blocks, int width, int height, float maxScale, float rotation, int layer) {
        if (blocks.isEmpty())
            return;
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        for (BlockPos pos : blocks.keySet()) {
            minX = Math.min(minX, pos.getX());
            minY = Math.min(minY, pos.getY());
            minZ = Math.min(minZ, pos.getZ());
            maxX = Math.max(maxX, pos.getX());
            maxY = Math.max(maxY, pos.getY());
            maxZ = Math.max(maxZ, pos.getZ());
        }
        double diameter = Math.hypot(maxX - minX + 1, maxZ - minZ + 1);
        double projectedHeight = (maxY - minY + 1) * Math.cos(Math.toRadians(TILT)) + diameter * Math.sin(Math.toRadians(TILT));
        float scale = (float) Math.min(maxScale, Math.min((width - 2) / diameter, (height - 2) / projectedHeight));
        int left = centerX - width / 2;
        int top = centerY - height / 2;
        ((GuiGraphicsExtractorAccessor) graphics).thaumaturge$getGuiRenderState().addPicturesInPictureState(
                new BlockPreviewRenderState(blocks, TILT, rotation, 1, scale, 0, 0, left, top, left + width, top + height, graphics.peekScissorStack(), layer, new Matrix3x2f(graphics.pose())));
    }
}
