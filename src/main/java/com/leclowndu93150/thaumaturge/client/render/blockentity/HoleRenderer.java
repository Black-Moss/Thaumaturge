package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.client.effect.pipeline.TCRenderPipelines;
import com.leclowndu93150.thaumaturge.content.focus.BlockEntityHole;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.AbstractEndPortalRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public final class HoleRenderer implements BlockEntityRenderer<BlockEntityHole, HoleRenderState> {
    private static final double FACE_THICKNESS = 1.0 / 1024.0;
    private static final double SURFACE_INSET = 0.01;
    private static final RenderType SURFACE = RenderType.create("tc_hole_surface",
            RenderSetup.builder(TCRenderPipelines.HOLE_SURFACE).withTexture("Sampler0", AbstractEndPortalRenderer.END_PORTAL_LOCATION).createRenderSetup());

    public HoleRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public HoleRenderState createRenderState() {
        return new HoleRenderState();
    }

    @Override
    public void extractRenderState(BlockEntityHole hole, HoleRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(hole, state, partialTicks, cameraPosition, breakProgress);
        Level level = hole.getLevel();
        state.shape = level == null ? Shapes.empty() : hole.originalBlockState().getShape(level, hole.getBlockPos());
        if (level == null || state.shape.isEmpty()) {
            return;
        }
        BlockPos.MutableBlockPos neighborPos = new BlockPos.MutableBlockPos();
        for (Direction direction : Direction.values()) {
            neighborPos.setWithOffset(hole.getBlockPos(), direction);
            BlockState neighbor = level.getBlockState(neighborPos);
            state.hideBoundary[direction.ordinal()] = neighbor.is(TCBlocks.HOLE.get()) || neighbor.isSolidRender();
        }
    }

    @Override
    public void submit(HoleRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.shape.isEmpty()) {
            return;
        }
        collector.submitCustomGeometry(poseStack, SURFACE, (pose, buffer) -> {
            for (Direction direction : Direction.values()) {
                boolean hideBoundary = state.hideBoundary[direction.ordinal()];
                state.shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
                    if (!hideBoundary || !onBoundary(direction, minX, minY, minZ, maxX, maxY, maxZ)) {
                        renderFace(state.shape, pose, buffer, direction, minX, minY, minZ, maxX, maxY, maxZ);
                    }
                });
            }
        });
    }

    private static boolean onBoundary(Direction direction, double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        return switch (direction) {
            case DOWN -> minY <= 0.0;
            case UP -> maxY >= 1.0;
            case NORTH -> minZ <= 0.0;
            case SOUTH -> maxZ >= 1.0;
            case WEST -> minX <= 0.0;
            case EAST -> maxX >= 1.0;
        };
    }

    private static void renderFace(VoxelShape shape, PoseStack.Pose pose, VertexConsumer buffer, Direction direction, double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        VoxelShape face = switch (direction) {
            case DOWN -> Shapes.box(minX, minY - FACE_THICKNESS, minZ, maxX, minY, maxZ);
            case UP -> Shapes.box(minX, maxY, minZ, maxX, maxY + FACE_THICKNESS, maxZ);
            case NORTH -> Shapes.box(minX, minY, minZ - FACE_THICKNESS, maxX, maxY, minZ);
            case SOUTH -> Shapes.box(minX, minY, maxZ, maxX, maxY, maxZ + FACE_THICKNESS);
            case WEST -> Shapes.box(minX - FACE_THICKNESS, minY, minZ, minX, maxY, maxZ);
            case EAST -> Shapes.box(maxX, minY, minZ, maxX + FACE_THICKNESS, maxY, maxZ);
        };
        Shapes.join(face, shape, BooleanOp.ONLY_FIRST).forAllBoxes((x1, y1, z1, x2, y2, z2) -> {
            float a = (float) x1;
            float b = (float) y1;
            float c = (float) z1;
            float d = (float) x2;
            float e = (float) y2;
            float f = (float) z2;
            switch (direction) {
                case DOWN, UP -> {
                    float y = (float) (direction == Direction.UP ? maxY - SURFACE_INSET : minY + SURFACE_INSET);
                    quad(pose, buffer, a, y, c, d, y, c, d, y, f, a, y, f);
                }
                case NORTH, SOUTH -> {
                    float z = (float) (direction == Direction.SOUTH ? maxZ - SURFACE_INSET : minZ + SURFACE_INSET);
                    quad(pose, buffer, a, b, z, a, e, z, d, e, z, d, b, z);
                }
                case WEST, EAST -> {
                    float x = (float) (direction == Direction.EAST ? maxX - SURFACE_INSET : minX + SURFACE_INSET);
                    quad(pose, buffer, x, b, c, x, e, c, x, e, f, x, b, f);
                }
            }
        });
    }

    private static void quad(PoseStack.Pose pose, VertexConsumer buffer, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4) {
        buffer.addVertex(pose.pose(), x1, y1, z1);
        buffer.addVertex(pose.pose(), x2, y2, z2);
        buffer.addVertex(pose.pose(), x3, y3, z3);
        buffer.addVertex(pose.pose(), x4, y4, z4);
    }
}
