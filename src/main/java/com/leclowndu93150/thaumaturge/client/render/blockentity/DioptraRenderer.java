package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.effect.rendertype.TTFXRenderTypes;
import com.leclowndu93150.thaumaturge.content.device.BlockEntityDioptra;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4fc;
import org.jspecify.annotations.Nullable;

public final class DioptraRenderer implements BlockEntityRenderer<BlockEntityDioptra, DioptraRenderState> {
    private static final Identifier SURFACE_TEXTURE = TTIds.rl("textures/misc/gridblock.png");
    private static final Identifier WALL_TEXTURE = TTIds.rl("textures/entity/dioptra_side.png");
    private static final int LAST = BlockEntityDioptra.GRID_SIZE - 1;
    private static final float CELL = 1.0F / LAST;
    private static final float CENTER_CELL = LAST / 2.0F;
    private static final float SURFACE_INSET = 0.005F;
    private static final float SURFACE_INSET_SCALE = 0.99F;
    private static final float SURFACE_BASE_Y = 1.001F;
    private static final float PEAK_HEIGHT = 2.0F / 3.0F;
    private static final float WALL_BOTTOM_Y = 1.0F;
    private static final float WALL_TOP_Y = 2.0F;
    private static final float SURFACE_ALPHA = 0.9F;
    private static final float SKIRT_TOP_ALPHA = 0.9F;
    private static final float WALL_ALPHA = 0.8F;
    private static final float RED_BASE = 0.85F;
    private static final float GREEN_ENABLED = 0.9F;
    private static final float GREEN_DISABLED = 0.45F;
    private static final float BLUE_BASE = 0.95F;
    private static final float COLOR_DRIFT = 0.05F;
    private static final float RED_DRIFT_TICKS = 12.0F;
    private static final float GREEN_DRIFT_TICKS = 11.0F;
    private static final float BLUE_DRIFT_TICKS = 10.0F;
    private static final float CELL_RED_FACTOR = 0.8F;
    private static final float LUMINANCE_BASE = 200.0F;
    private static final float LUMINANCE_RIPPLE = 15.0F;
    private static final float LUMINANCE_SCALE = 255.0F;
    private static final float RIPPLE_TICKS_PER_CELL = 10.0F;
    private static final float RIPPLE_DIVISOR_TICKS = 8.0F;
    private static final double BOUNDS_HEIGHT = WALL_TOP_Y;

    public DioptraRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public DioptraRenderState createRenderState() {
        return new DioptraRenderState();
    }

    @Override
    public void extractRenderState(BlockEntityDioptra dioptra, DioptraRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(dioptra, state, partialTicks, cameraPosition, breakProgress);
        dioptra.copyGrid(state.grid);
        state.enabled = dioptra.getBlockState().getValue(BlockStateProperties.ENABLED);
        Level level = dioptra.getLevel();
        state.time = (level == null ? 0L : level.getGameTime()) + partialTicks;
    }

    @Override
    public void submit(DioptraRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        Palette palette = Palette.of(state.enabled, state.time);
        float[] fills = fills(state.grid);
        poseStack.pushPose();
        poseStack.translate(SURFACE_INSET, SURFACE_BASE_Y, SURFACE_INSET);
        poseStack.scale(SURFACE_INSET_SCALE, 1.0F, SURFACE_INSET_SCALE);
        collector.submitCustomGeometry(poseStack, TTFXRenderTypes.dioptra(SURFACE_TEXTURE), (pose, buffer) -> {
            Matrix4fc matrix = pose.pose();
            writeSurface(buffer, matrix, fills, palette);
            writeSkirts(buffer, matrix, fills, palette);
        });
        poseStack.popPose();
        collector.submitCustomGeometry(poseStack, TTFXRenderTypes.dioptra(WALL_TEXTURE), (pose, buffer) -> writeWalls(buffer, pose.pose(), palette));
    }

    private static void writeSurface(VertexConsumer buffer, Matrix4fc matrix, float[] fills, Palette palette) {
        for (int row = 0; row < LAST; row++) {
            for (int column = 0; column < LAST; column++) {
                float red = palette.cellRed(column, row);
                float green = palette.cellGreen(column, row);
                float blue = palette.cellBlue(column, row);
                surfacePoint(buffer, matrix, fills, red, green, blue, column, row + 1, 0.0F, 0.0F);
                surfacePoint(buffer, matrix, fills, red, green, blue, column + 1, row + 1, 1.0F, 0.0F);
                surfacePoint(buffer, matrix, fills, red, green, blue, column + 1, row, 1.0F, 1.0F);
                surfacePoint(buffer, matrix, fills, red, green, blue, column, row, 0.0F, 1.0F);
            }
        }
    }

    private static void surfacePoint(VertexConsumer buffer, Matrix4fc matrix, float[] fills, float red, float green, float blue, int column, int row, float u, float v) {
        buffer.addVertex(matrix, column * CELL, fill(fills, column, row) * PEAK_HEIGHT, row * CELL).setUv(u, v).setColor(red, green, blue, SURFACE_ALPHA);
    }

    private static void writeSkirts(VertexConsumer buffer, Matrix4fc matrix, float[] fills, Palette palette) {
        for (int step = 0; step < LAST; step++) {
            skirtAlongZ(buffer, matrix, fills, palette, 0, 0, step);
            skirtAlongZ(buffer, matrix, fills, palette, LAST, LAST - 1, step);
            skirtAlongX(buffer, matrix, fills, palette, 0, 0, step);
            skirtAlongX(buffer, matrix, fills, palette, LAST, LAST - 1, step);
        }
    }

    private static void skirtAlongZ(VertexConsumer buffer, Matrix4fc matrix, float[] fills, Palette palette, int column, int cellColumn, int step) {
        float red = palette.cellRed(cellColumn, step);
        float green = palette.cellGreen(cellColumn, step);
        float blue = palette.cellBlue(cellColumn, step);
        float x = column * CELL;
        float nearZ = step * CELL;
        float farZ = (step + 1) * CELL;
        float nearTop = fill(fills, column, step) * PEAK_HEIGHT;
        float farTop = fill(fills, column, step + 1) * PEAK_HEIGHT;
        buffer.addVertex(matrix, x, 0.0F, nearZ).setUv(0.0F, 1.0F).setColor(red, green, blue, 0.0F);
        buffer.addVertex(matrix, x, nearTop, nearZ).setUv(1.0F, 1.0F).setColor(red, green, blue, SKIRT_TOP_ALPHA);
        buffer.addVertex(matrix, x, farTop, farZ).setUv(1.0F, 0.0F).setColor(red, green, blue, SKIRT_TOP_ALPHA);
        buffer.addVertex(matrix, x, 0.0F, farZ).setUv(0.0F, 0.0F).setColor(red, green, blue, 0.0F);
    }

    private static void skirtAlongX(VertexConsumer buffer, Matrix4fc matrix, float[] fills, Palette palette, int row, int cellRow, int step) {
        float red = palette.cellRed(step, cellRow);
        float green = palette.cellGreen(step, cellRow);
        float blue = palette.cellBlue(step, cellRow);
        float z = row * CELL;
        float nearX = step * CELL;
        float farX = (step + 1) * CELL;
        float nearTop = fill(fills, step, row) * PEAK_HEIGHT;
        float farTop = fill(fills, step + 1, row) * PEAK_HEIGHT;
        buffer.addVertex(matrix, nearX, 0.0F, z).setUv(0.0F, 1.0F).setColor(red, green, blue, 0.0F);
        buffer.addVertex(matrix, nearX, nearTop, z).setUv(0.0F, 0.0F).setColor(red, green, blue, SKIRT_TOP_ALPHA);
        buffer.addVertex(matrix, farX, farTop, z).setUv(1.0F, 0.0F).setColor(red, green, blue, SKIRT_TOP_ALPHA);
        buffer.addVertex(matrix, farX, 0.0F, z).setUv(1.0F, 1.0F).setColor(red, green, blue, 0.0F);
    }

    private static void writeWalls(VertexConsumer buffer, Matrix4fc matrix, Palette palette) {
        wall(buffer, matrix, palette, 0.0F, 0.0F, 1.0F, 0.0F);
        wall(buffer, matrix, palette, 1.0F, 1.0F, 0.0F, 1.0F);
        wall(buffer, matrix, palette, 0.0F, 1.0F, 0.0F, 0.0F);
        wall(buffer, matrix, palette, 1.0F, 0.0F, 1.0F, 1.0F);
    }

    private static void wall(VertexConsumer buffer, Matrix4fc matrix, Palette palette, float x0, float z0, float x1, float z1) {
        float red = Math.min(1.0F, palette.red);
        float green = Math.min(1.0F, palette.green);
        float blue = Math.min(1.0F, palette.blue);
        buffer.addVertex(matrix, x0, WALL_BOTTOM_Y, z0).setUv(0.0F, 0.0F).setColor(red, green, blue, WALL_ALPHA);
        buffer.addVertex(matrix, x0, WALL_TOP_Y, z0).setUv(1.0F, 0.0F).setColor(red, green, blue, WALL_ALPHA);
        buffer.addVertex(matrix, x1, WALL_TOP_Y, z1).setUv(1.0F, 1.0F).setColor(red, green, blue, WALL_ALPHA);
        buffer.addVertex(matrix, x1, WALL_BOTTOM_Y, z1).setUv(0.0F, 1.0F).setColor(red, green, blue, WALL_ALPHA);
    }

    private static float[] fills(byte[] grid) {
        float[] fills = new float[grid.length];
        for (int index = 0; index < grid.length; index++) {
            fills[index] = grid[index] / (float) BlockEntityDioptra.STEPS;
        }
        return fills;
    }

    private static float fill(float[] fills, int column, int row) {
        return fills[row * BlockEntityDioptra.GRID_SIZE + column];
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(BlockEntityDioptra dioptra) {
        BlockPos pos = dioptra.getBlockPos();
        return new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1.0, pos.getY() + BOUNDS_HEIGHT, pos.getZ() + 1.0);
    }

    private record Palette(float red, float green, float blue, float time) {
        static Palette of(boolean enabled, float time) {
            float red = RED_BASE + COLOR_DRIFT * Mth.sin(time / RED_DRIFT_TICKS);
            float green = (enabled ? GREEN_ENABLED : GREEN_DISABLED) + COLOR_DRIFT * Mth.sin(time / GREEN_DRIFT_TICKS);
            float blue = BLUE_BASE + COLOR_DRIFT * Mth.sin(time / BLUE_DRIFT_TICKS);
            return new Palette(red, green, blue, time);
        }

        float luminance(int column, int row) {
            float distance = Mth.sqrt(Mth.square(column - CENTER_CELL) + Mth.square(row - CENTER_CELL));
            float ripple = Mth.sin((time - RIPPLE_TICKS_PER_CELL * distance) / RIPPLE_DIVISOR_TICKS);
            return (LUMINANCE_BASE + LUMINANCE_RIPPLE * ripple) / LUMINANCE_SCALE;
        }

        float cellRed(int column, int row) {
            return Math.min(1.0F, red * CELL_RED_FACTOR * luminance(column, row));
        }

        float cellGreen(int column, int row) {
            return Math.min(1.0F, green * luminance(column, row));
        }

        float cellBlue(int column, int row) {
            return Math.min(1.0F, blue * luminance(column, row));
        }
    }
}
