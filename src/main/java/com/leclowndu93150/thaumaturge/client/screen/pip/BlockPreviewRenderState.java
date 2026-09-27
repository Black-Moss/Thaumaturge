package com.leclowndu93150.thaumaturge.client.screen.pip;

import java.util.Map;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix3x2f;
import org.jspecify.annotations.Nullable;

public record BlockPreviewRenderState(Map<BlockPos, BlockState> blocks, float rotX, float rotY, float zoom, float previewScale, float centerX, float centerY, int x0, int y0, int x1, int y1,
        @Nullable ScreenRectangle scissorArea, int visibleLayer, Matrix3x2f pose) implements PictureInPictureRenderState {

    @Override
    public float scale() {
        return 1.0f;
    }

    public BlockPreviewRenderState {
        blocks = Map.copyOf(blocks);
        pose = new Matrix3x2f(pose);
    }

    public BlockPreviewRenderState(Map<BlockPos, BlockState> blocks, float rotX, float rotY, float zoom, float previewScale, float centerX, float centerY, int x0, int y0, int x1, int y1, @Nullable ScreenRectangle scissorArea) {
        this(blocks, rotX, rotY, zoom, previewScale, centerX, centerY, x0, y0, x1, y1, scissorArea, -1, new Matrix3x2f());
    }

    @Override
    public @Nullable ScreenRectangle bounds() {
        ScreenRectangle bounds = new ScreenRectangle(x0, y0, x1 - x0, y1 - y0).transformMaxBounds(pose);
        return scissorArea == null ? bounds : scissorArea.intersection(bounds);
    }
}
