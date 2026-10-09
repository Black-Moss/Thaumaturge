package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaAccess;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaTransport;
import com.leclowndu93150.thaumaturge.content.essentia.smeltery.BlockEntityAlembic;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class AlembicRenderer implements BlockEntityRenderer<BlockEntityAlembic, AlembicRenderState> {
    private static final Identifier LABEL_TEXTURE = TTIds.rl("textures/entity/label.png");
    private static final float CENTER = 0.5F;
    private static final float LABEL_HALF = 0.18F;
    private static final float ICON_HALF = 0.12F;
    private static final float LABEL_DISTANCE = 0.376F;
    private static final float ICON_LIFT = 0.001F;
    private static final float[] YAW_BY_HORIZONTAL_INDEX = {180.0F, 90.0F, 0.0F, 270.0F};
    private static final float[][] QUAD_CORNERS = {{-1.0F, -1.0F, 0.0F, 1.0F}, {1.0F, -1.0F, 1.0F, 1.0F}, {1.0F, 1.0F, 1.0F, 0.0F}, {-1.0F, 1.0F, 0.0F, 0.0F}};
    private static final int CORNER_X = 0;
    private static final int CORNER_Y = 1;
    private static final int CORNER_U = 2;
    private static final int CORNER_V = 3;
    private static final float MIRRORED = -1.0F;
    private static final float UPRIGHT = 1.0F;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int OPAQUE = 0xFF000000;
    private static final int RGB_MASK = 0xFFFFFF;
    private static final float NORMAL_Z = -1.0F;

    public AlembicRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public AlembicRenderState createRenderState() {
        return new AlembicRenderState();
    }

    @Override
    public void extractRenderState(BlockEntityAlembic alembic, AlembicRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(alembic, state, partialTicks, cameraPosition, breakProgress);
        state.filterAspect = null;
        state.filterTexture = null;
        Level level = alembic.getLevel();
        ResourceKey<IAspect> key = alembic.aspectFilterKey();
        state.hasFilter = level != null && key != null;
        if (level == null) {
            return;
        }
        Holder<IAspect> resolved = resolveAspect(level, key);
        if (resolved != null) {
            IAspect aspect = resolved.value();
            state.filterAspect = resolved;
            state.filterTexture = aspect.texture();
            state.filterColor = OPAQUE | (aspect.color() & RGB_MASK);
            state.facing = alembic.spoutSide();
        }
        state.connectedDirections = connectedNeighbours(level, alembic.getBlockPos());
    }

    @Override
    public void submit(AlembicRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (!state.hasFilter) {
            return;
        }
        int light = state.lightCoords;
        Identifier icon = state.filterTexture;
        int iconColor = state.filterColor;
        poseStack.pushPose();
        poseStack.translate(CENTER, 0.0F, CENTER);
        poseStack.mulPose(Axis.YP.rotationDegrees(yawOf(state.facing)));
        poseStack.translate(0.0F, CENTER, -LABEL_DISTANCE);
        collector.submitCustomGeometry(poseStack, RenderTypes.entityCutout(LABEL_TEXTURE), (pose, buffer) -> writeSquare(pose, buffer, LABEL_HALF, UPRIGHT, WHITE, light));
        if (icon != null) {
            poseStack.translate(0.0F, 0.0F, -ICON_LIFT);
            collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucent(icon), (pose, buffer) -> writeSquare(pose, buffer, ICON_HALF, MIRRORED, iconColor, light));
        }
        poseStack.popPose();
    }

    public static void addVertex(VertexConsumer buffer, PoseStack.Pose pose, float x, float y, float z, float u, float v, int light, int color, float normalX, float normalY, float normalZ) {
        buffer.addVertex(pose, x, y, z).setColor(color).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, normalX, normalY, normalZ);
    }

    private static void writeSquare(PoseStack.Pose pose, VertexConsumer buffer, float half, float handedness, int color, int light) {
        for (float[] corner : QUAD_CORNERS) {
            float x = corner[CORNER_X] * half * handedness;
            float y = corner[CORNER_Y] * half;
            addVertex(buffer, pose, x, y, 0.0F, corner[CORNER_U], corner[CORNER_V], light, color, 0.0F, 0.0F, NORMAL_Z);
        }
    }

    private static @Nullable Holder<IAspect> resolveAspect(Level level, @Nullable ResourceKey<IAspect> key) {
        if (key == null) {
            return null;
        }
        Optional<Holder.Reference<IAspect>> found = level.registryAccess().lookupOrThrow(IAspect.REGISTRY_KEY).get(key);
        return found.orElse(null);
    }

    private static float yawOf(Direction facing) {
        return facing.getAxis().isHorizontal() ? YAW_BY_HORIZONTAL_INDEX[facing.get2DDataValue()] : 0.0F;
    }

    private static Direction[] connectedNeighbours(Level level, BlockPos pos) {
        List<Direction> connected = new ArrayList<>();
        for (Direction side : Direction.Plane.HORIZONTAL) {
            Direction back = side.getOpposite();
            IEssentiaTransport transport = EssentiaAccess.transport(level, pos.relative(side), back);
            if (transport != null && transport.isConnectable(back)) {
                connected.add(side);
            }
        }
        return connected.toArray(new Direction[0]);
    }
}
