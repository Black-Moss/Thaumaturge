package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.effect.rendertype.TTFXRenderTypes;
import com.leclowndu93150.thaumaturge.content.device.BlockEntityDioptra;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class DioptraRenderer implements BlockEntityRenderer<BlockEntityDioptra, DioptraRenderState> {
    private static final Identifier GRID_TEXTURE = TTIds.rl("textures/misc/gridblock.png");
    private static final Identifier SIDE_TEXTURE = TTIds.rl("textures/entity/dioptra_side.png");
    private static final int CELLS = BlockEntityDioptra.GRID_SIZE - 1;
    private static final int POINTS = BlockEntityDioptra.GRID_SIZE;
    private static final float HEIGHT_DIVISOR = 96.0F;
    private static final float PATCH_INSET = 0.005F;
    private static final float PATCH_SPAN = 0.99F;
    private static final float PATCH_BASE = 1.001F;
    private static final float WALL_BOTTOM = 1.0F;
    private static final float WALL_TOP = 2.0F;
    private static final float TOP_ALPHA = 0.9F;
    private static final float WALL_ALPHA = 0.8F;
    private static final float RED_PERIOD = 12.0F;
    private static final float GREEN_PERIOD = 11.0F;
    private static final float BLUE_PERIOD = 10.0F;
    private static final float COLOR_SWING = 0.05F;
    private static final float RED_BASE = 0.85F;
    private static final float GREEN_BASE_ENABLED = 0.9F;
    private static final float GREEN_BASE_DISABLED = 0.45F;
    private static final float BLUE_BASE = 0.95F;
    private static final float RED_CELL_FACTOR = 0.8F;
    private static final float WAVE_CENTER = CELLS / 2.0F;
    private static final float WAVE_SPEED = 10.0F;
    private static final float WAVE_DIVISOR = 8.0F;
    private static final float LUMINANCE_BASE = 200.0F;
    private static final float LUMINANCE_SWING = 15.0F;
    private static final float LUMINANCE_MAX = 255.0F;
    private static final float BOUNDS_LOW = 0.3F;
    private static final float BOUNDS_HIGH = 1.3F;
    private static final float BOUNDS_TOP = 2.3F;
    private static final int VIEW_DISTANCE = 64;
    private static final float FULL_LIFT = 1.0F;
    private static final float NO_LIFT = 0.0F;
    private static final List<GridCorner> TOP_CORNERS = List.of(new GridCorner(0, 0, 0.0F, 1.0F), new GridCorner(1, 0, 1.0F, 1.0F), new GridCorner(1, 1, 1.0F, 0.0F), new GridCorner(0, 1, 0.0F, 0.0F));
    private static final List<SkirtEdge> SKIRT_EDGES = List.of(
            new SkirtEdge(true, false,
                    List.of(new SkirtCorner(0, 0, 1.0F, 1.0F, FULL_LIFT), new SkirtCorner(0, 1, 1.0F, 0.0F, FULL_LIFT), new SkirtCorner(0, 1, 0.0F, 0.0F, NO_LIFT),
                            new SkirtCorner(0, 0, 0.0F, 1.0F, NO_LIFT))),
            new SkirtEdge(true, true,
                    List.of(new SkirtCorner(1, 0, 1.0F, 1.0F, FULL_LIFT), new SkirtCorner(1, 1, 1.0F, 0.0F, FULL_LIFT), new SkirtCorner(1, 1, 0.0F, 0.0F, NO_LIFT),
                            new SkirtCorner(1, 0, 0.0F, 1.0F, NO_LIFT))),
            new SkirtEdge(false, false,
                    List.of(new SkirtCorner(0, 0, 0.0F, 0.0F, FULL_LIFT), new SkirtCorner(1, 0, 1.0F, 0.0F, FULL_LIFT), new SkirtCorner(1, 0, 1.0F, 1.0F, NO_LIFT),
                            new SkirtCorner(0, 0, 0.0F, 1.0F, NO_LIFT))),
            new SkirtEdge(false, true, List.of(new SkirtCorner(0, 1, 0.0F, 0.0F, FULL_LIFT), new SkirtCorner(1, 1, 1.0F, 0.0F, FULL_LIFT), new SkirtCorner(1, 1, 1.0F, 1.0F, NO_LIFT),
                    new SkirtCorner(0, 1, 0.0F, 1.0F, NO_LIFT))));
    private static final List<WallEdge> WALL_EDGES = List.of(new WallEdge(0.0F, 0.0F, 1.0F, 0.0F), new WallEdge(1.0F, 1.0F, -1.0F, 0.0F), new WallEdge(0.0F, 1.0F, 0.0F, -1.0F),
            new WallEdge(1.0F, 0.0F, 0.0F, 1.0F));

    public DioptraRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public DioptraRenderState createRenderState() {
        return new DioptraRenderState();
    }

    @Override
    public void extractRenderState(BlockEntityDioptra dioptra, DioptraRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(dioptra, state, partialTicks, cameraPosition, breakProgress);
        byte[] grid = dioptra.grid();
        System.arraycopy(grid, 0, state.grid, 0, Math.min(grid.length, state.grid.length));
        state.enabled = dioptra.getBlockState().getValue(BlockStateProperties.ENABLED);
        LocalPlayer player = Minecraft.getInstance().player;
        state.time = (player == null ? 0 : player.tickCount) + partialTicks;
    }

    @Override
    public void submit(DioptraRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        float time = state.time;
        float red = COLOR_SWING * Mth.sin(time / RED_PERIOD) + RED_BASE;
        float green = COLOR_SWING * Mth.sin(time / GREEN_PERIOD) + (state.enabled ? GREEN_BASE_ENABLED : GREEN_BASE_DISABLED);
        float blue = COLOR_SWING * Mth.sin(time / BLUE_PERIOD) + BLUE_BASE;
        collector.submitCustomGeometry(poseStack, TTFXRenderTypes.dioptra(GRID_TEXTURE), (pose, buffer) -> writeGrid(pose, buffer, heightField(state.grid), time, red, green, blue));
        int wallColor = ARGB.colorFromFloat(WALL_ALPHA, Math.min(1.0F, red), Math.min(1.0F, green), Math.min(1.0F, blue));
        collector.submitCustomGeometry(poseStack, TTFXRenderTypes.dioptra(SIDE_TEXTURE), (pose, buffer) -> writeWalls(pose, buffer, wallColor));
    }

    @Override
    public int getViewDistance() {
        return VIEW_DISTANCE;
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(BlockEntityDioptra dioptra) {
        BlockPos pos = dioptra.getBlockPos();
        return new AABB(pos.getX() - BOUNDS_LOW, pos.getY() - BOUNDS_LOW, pos.getZ() - BOUNDS_LOW, pos.getX() + BOUNDS_HIGH, pos.getY() + BOUNDS_TOP, pos.getZ() + BOUNDS_HIGH);
    }

    private static float[][] heightField(byte[] grid) {
        float[][] heights = new float[POINTS][POINTS];
        int source = 0;
        for (int row = 0; row < POINTS; row++) {
            for (int column = 0; column < POINTS; column++) {
                heights[column][row] = PATCH_BASE + grid[source++] / HEIGHT_DIVISOR;
            }
        }
        return heights;
    }

    private static void writeGrid(PoseStack.Pose pose, VertexConsumer buffer, float[][] heights, float time, float red, float green, float blue) {
        for (int a = 0; a < CELLS; a++) {
            for (int b = 0; b < CELLS; b++) {
                CellTint tint = cellTint(a, b, time, red, green, blue);
                emitTop(pose, buffer, heights, a, b, tint);
                for (SkirtEdge edge : SKIRT_EDGES) {
                    if (edge.touches(a, b)) {
                        emitSkirt(pose, buffer, heights, a, b, tint, edge);
                    }
                }
            }
        }
    }

    private static CellTint cellTint(int a, int b, float time, float red, float green, float blue) {
        float distance = Mth.sqrt((a - WAVE_CENTER) * (a - WAVE_CENTER) + (b - WAVE_CENTER) * (b - WAVE_CENTER));
        float ripple = Mth.sin((time - WAVE_SPEED * distance) / WAVE_DIVISOR);
        float luminance = (LUMINANCE_BASE + LUMINANCE_SWING * ripple) / LUMINANCE_MAX;
        return new CellTint(Math.min(1.0F, RED_CELL_FACTOR * red * luminance), Math.min(1.0F, green * luminance), Math.min(1.0F, blue * luminance));
    }

    private static void emitTop(PoseStack.Pose pose, VertexConsumer buffer, float[][] heights, int a, int b, CellTint tint) {
        int color = tint.color(FULL_LIFT);
        for (GridCorner corner : TOP_CORNERS) {
            int column = a + corner.da();
            int row = b + corner.db();
            vertex(pose, buffer, patch((float) column / CELLS), heights[column][row], patch((float) row / CELLS), corner.u(), corner.v(), color);
        }
    }

    private static void emitSkirt(PoseStack.Pose pose, VertexConsumer buffer, float[][] heights, int a, int b, CellTint tint, SkirtEdge edge) {
        for (SkirtCorner corner : edge.corners()) {
            int column = a + corner.da();
            int row = b + corner.db();
            float y = corner.lift() > NO_LIFT ? heights[column][row] : PATCH_BASE;
            vertex(pose, buffer, patch((float) column / CELLS), y, patch((float) row / CELLS), corner.u(), corner.v(), tint.color(corner.lift()));
        }
    }

    private static void writeWalls(PoseStack.Pose pose, VertexConsumer buffer, int color) {
        for (WallEdge edge : WALL_EDGES) {
            wall(pose, buffer, edge, color);
        }
    }

    private static void wall(PoseStack.Pose pose, VertexConsumer buffer, WallEdge edge, int color) {
        buffer.addVertex(pose, edge.startX(), WALL_BOTTOM, edge.startZ()).setUv(0.0F, 0.0F).setColor(color);
        buffer.addVertex(pose, edge.startX(), WALL_TOP, edge.startZ()).setUv(1.0F, 0.0F).setColor(color);
        buffer.addVertex(pose, edge.endX(), WALL_TOP, edge.endZ()).setUv(1.0F, 1.0F).setColor(color);
        buffer.addVertex(pose, edge.endX(), WALL_BOTTOM, edge.endZ()).setUv(0.0F, 1.0F).setColor(color);
    }

    private static float patch(float lattice) {
        return PATCH_INSET + PATCH_SPAN * lattice;
    }

    private static void vertex(PoseStack.Pose pose, VertexConsumer buffer, float x, float y, float z, float u, float v, int color) {
        buffer.addVertex(pose, x, y, z).setUv(u, v).setColor(color);
    }

    private record CellTint(float red, float green, float blue) {
        int color(float lift) {
            return ARGB.colorFromFloat(TOP_ALPHA * lift, red, green, blue);
        }
    }

    private record GridCorner(int da, int db, float u, float v) {
    }

    private record SkirtCorner(int da, int db, float u, float v, float lift) {
    }

    private record SkirtEdge(boolean alongA, boolean high, List<SkirtCorner> corners) {
        boolean touches(int a, int b) {
            int cell = alongA ? a : b;
            return cell == (high ? CELLS - 1 : 0);
        }
    }

    private record WallEdge(float startX, float startZ, float directionX, float directionZ) {
        float endX() {
            return startX + directionX;
        }

        float endZ() {
            return startZ + directionZ;
        }
    }
}
