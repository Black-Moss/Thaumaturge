package com.leclowndu93150.thaumcraft.client.render.blockentity;

import com.leclowndu93150.thaumcraft.api.aspect.IAspect;
import com.leclowndu93150.thaumcraft.content.essentia.EssentiaTransportHelper;
import com.leclowndu93150.thaumcraft.content.essentia.flow.EssentiaFlowHandler;
import com.leclowndu93150.thaumcraft.content.essentia.jar.BlockEntityJar;
import com.leclowndu93150.thaumcraft.content.essentia.jar.BlockJar;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jspecify.annotations.Nullable;

@OnlyIn(Dist.CLIENT)
public final class JarRenderer implements BlockEntityRenderer<BlockEntityJar, JarRenderState> {
    private static final Identifier BRINE_TEXTURE = Identifier.fromNamespaceAndPath("thaumcraft", "textures/entity/jarbrine.png");
    private static final Identifier LABEL_TEXTURE = Identifier.fromNamespaceAndPath("thaumcraft", "textures/entity/label.png");
    private static final Identifier ANIMATED_GLOW_LOCATION = Identifier.fromNamespaceAndPath("thaumcraft", "block/animatedglow");
    private static final SpriteId ANIMATED_GLOW_SPRITE = new SpriteId(TextureAtlas.LOCATION_BLOCKS, ANIMATED_GLOW_LOCATION);

    private static final float FLUID_MIN = 4.0F / 16.0F;
    private static final float FLUID_MAX = 12.0F / 16.0F;
    private static final float FLUID_BASE_Y = 1.0F / 16.0F;
    private static final float FLUID_MAX_HEIGHT = 10.0F / 16.0F;

    private static final float BRACE_MIN_XZ = 4.5F / 16.0F;
    private static final float BRACE_MAX_XZ = 11.5F / 16.0F;
    private static final float BRACE_BOTTOM_Y = 11.5F / 16.0F;
    private static final float BRACE_TOP_Y = 14.5F / 16.0F;
    private static final float BRACE_STRETCH = 1.001F;

    private static final float LID_EXT_MIN_XZ = (8.0F - 2.0F * 0.9F) / 16.0F;
    private static final float LID_EXT_MAX_XZ = (8.0F + 2.0F * 0.9F) / 16.0F;
    private static final float LID_EXT_BOTTOM_Y = 14.0F / 16.0F;
    private static final float LID_EXT_TOP_Y = 16.0F / 16.0F;

    public JarRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public JarRenderState createRenderState() {
        return new JarRenderState();
    }

    @Override
    public void extractRenderState(
            BlockEntityJar blockEntity,
            JarRenderState state,
            float partialTicks,
            Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress
    ) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        state.amount = blockEntity.amount();
        state.braced = blockEntity.isBraced();
        state.hasFilter = blockEntity.aspectFilterKey() != null;
        var blockState = blockEntity.getBlockState();
        if (blockState.hasProperty(BlockJar.FACING)) {
            state.facing = blockState.getValue(BlockJar.FACING);
        } else {
            state.facing = Direction.NORTH;
        }
        Level level = blockEntity.getLevel();
        state.aspectColor = -1;
        state.filterTexture = null;
        state.filterColor = -1;
        state.filterAspect = null;
        state.connectedAbove = false;
        if (level == null) return;
        var registry = level.registryAccess();
        if (blockEntity.aspectKey() != null) {
            Holder<IAspect> aspect = EssentiaTransportHelper.resolve(registry, blockEntity.aspectKey());
            if (aspect != null) {
                state.aspectColor = 0xFF000000 | (aspect.value().color() & 0x00FFFFFF);
            }
        }
        if (blockEntity.aspectFilterKey() != null) {
            Holder<IAspect> filter = EssentiaTransportHelper.resolve(registry, blockEntity.aspectFilterKey());
            if (filter != null) {
                state.filterAspect = filter;
                state.filterTexture = filter.value().texture();
                state.filterColor = 0xFF000000 | (filter.value().color() & 0x00FFFFFF);
            }
        }
        BlockPos above = blockEntity.getBlockPos().above();
        state.connectedAbove = EssentiaFlowHandler.transport(level, above, Direction.DOWN) != null;
    }

    @Override
    public void submit(JarRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        if (state.amount > 0 && state.aspectColor != -1) {
            submitFluid(state, poseStack, submitNodeCollector);
        }
        if (state.hasFilter && state.filterTexture != null) {
            submitFilterLabel(state, poseStack, submitNodeCollector);
        }
        if (state.braced) {
            submitBrace(state, poseStack, submitNodeCollector);
        }
        if (state.connectedAbove) {
            submitLidExtension(state, poseStack, submitNodeCollector);
        }
    }

    private void submitFluid(JarRenderState state, PoseStack poseStack, SubmitNodeCollector collector) {
        float ratio = Math.min(1.0F, state.amount / (float) BlockEntityJar.CAPACITY);
        float height = FLUID_BASE_Y + ratio * FLUID_MAX_HEIGHT;
        int color = state.aspectColor;
        int light = state.lightCoords;
        TextureAtlasSprite sprite = Minecraft.getInstance().getAtlasManager().get(ANIMATED_GLOW_SPRITE);
        RenderType type = Sheets.translucentBlockItemSheet();
        collector.submitCustomGeometry(poseStack, type, (pose, buffer) -> {
            VertexConsumer wrapped = sprite.wrap(buffer);
            fluidQuadTop(wrapped, pose, height, color, light);
            fluidQuadBottom(wrapped, pose, color, light);
            fluidQuadSide(wrapped, pose, FLUID_MIN, FLUID_MAX, FLUID_MIN, FLUID_MIN, height, color, light, 0.0F, 0.0F, -1.0F);
            fluidQuadSide(wrapped, pose, FLUID_MAX, FLUID_MIN, FLUID_MAX, FLUID_MAX, height, color, light, 0.0F, 0.0F, 1.0F);
            fluidQuadSide(wrapped, pose, FLUID_MIN, FLUID_MIN, FLUID_MIN, FLUID_MAX, height, color, light, -1.0F, 0.0F, 0.0F);
            fluidQuadSide(wrapped, pose, FLUID_MAX, FLUID_MAX, FLUID_MAX, FLUID_MIN, height, color, light, 1.0F, 0.0F, 0.0F);
        });
    }

    private static void fluidQuadTop(VertexConsumer buffer, PoseStack.Pose pose, float y, int color, int light) {
        addVertex(buffer, pose, FLUID_MIN, y, FLUID_MIN, 0.0F, 0.0F, color, light, 0.0F, 1.0F, 0.0F);
        addVertex(buffer, pose, FLUID_MIN, y, FLUID_MAX, 0.0F, 1.0F, color, light, 0.0F, 1.0F, 0.0F);
        addVertex(buffer, pose, FLUID_MAX, y, FLUID_MAX, 1.0F, 1.0F, color, light, 0.0F, 1.0F, 0.0F);
        addVertex(buffer, pose, FLUID_MAX, y, FLUID_MIN, 1.0F, 0.0F, color, light, 0.0F, 1.0F, 0.0F);
    }

    private static void fluidQuadBottom(VertexConsumer buffer, PoseStack.Pose pose, int color, int light) {
        addVertex(buffer, pose, FLUID_MIN, FLUID_BASE_Y, FLUID_MIN, 0.0F, 0.0F, color, light, 0.0F, -1.0F, 0.0F);
        addVertex(buffer, pose, FLUID_MAX, FLUID_BASE_Y, FLUID_MIN, 1.0F, 0.0F, color, light, 0.0F, -1.0F, 0.0F);
        addVertex(buffer, pose, FLUID_MAX, FLUID_BASE_Y, FLUID_MAX, 1.0F, 1.0F, color, light, 0.0F, -1.0F, 0.0F);
        addVertex(buffer, pose, FLUID_MIN, FLUID_BASE_Y, FLUID_MAX, 0.0F, 1.0F, color, light, 0.0F, -1.0F, 0.0F);
    }

    private static void fluidQuadSide(
            VertexConsumer buffer,
            PoseStack.Pose pose,
            float x0,
            float z0,
            float x1,
            float z1,
            float yTop,
            int color,
            int light,
            float nx,
            float ny,
            float nz
    ) {
        addVertex(buffer, pose, x0, FLUID_BASE_Y, z0, 0.0F, 1.0F, color, light, nx, ny, nz);
        addVertex(buffer, pose, x1, FLUID_BASE_Y, z1, 1.0F, 1.0F, color, light, nx, ny, nz);
        addVertex(buffer, pose, x1, yTop, z1, 1.0F, 0.0F, color, light, nx, ny, nz);
        addVertex(buffer, pose, x0, yTop, z0, 0.0F, 0.0F, color, light, nx, ny, nz);
    }

    private static void addVertex(
            VertexConsumer buffer,
            PoseStack.Pose pose,
            float x,
            float y,
            float z,
            float u,
            float v,
            int color,
            int light,
            float nx,
            float ny,
            float nz
    ) {
        buffer.addVertex(pose, x, y, z)
                .setColor(color)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, nx, ny, nz);
    }

    private void submitFilterLabel(JarRenderState state, PoseStack poseStack, SubmitNodeCollector collector) {
        Direction facing = state.facing;
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
        poseStack.translate(0.0, 0.0, -0.5 + 0.001);
        int light = state.lightCoords;
        Identifier labelTex = LABEL_TEXTURE;
        RenderType labelType = RenderTypes.entityCutout(labelTex);
        collector.submitCustomGeometry(poseStack, labelType, (pose, buffer) -> labelQuad(buffer, pose, light));
        if (state.filterTexture != null) {
            poseStack.pushPose();
            poseStack.translate(0.0, 0.0, -0.001);
            int filterColor = state.filterColor;
            Identifier aspectTex = state.filterTexture;
            RenderType aspectType = RenderTypes.entityTranslucent(aspectTex);
            collector.submitCustomGeometry(poseStack, aspectType, (pose, buffer) -> aspectIconQuad(buffer, pose, filterColor, light));
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    private static void labelQuad(VertexConsumer buffer, PoseStack.Pose pose, int light) {
        float s = 0.18F;
        int white = 0xFFFFFFFF;
        addVertex(buffer, pose, -s, -s, 0.0F, 0.0F, 1.0F, white, light, 0.0F, 0.0F, -1.0F);
        addVertex(buffer, pose, s, -s, 0.0F, 1.0F, 1.0F, white, light, 0.0F, 0.0F, -1.0F);
        addVertex(buffer, pose, s, s, 0.0F, 1.0F, 0.0F, white, light, 0.0F, 0.0F, -1.0F);
        addVertex(buffer, pose, -s, s, 0.0F, 0.0F, 0.0F, white, light, 0.0F, 0.0F, -1.0F);
    }

    private static void aspectIconQuad(VertexConsumer buffer, PoseStack.Pose pose, int color, int light) {
        float s = 0.12F;
        addVertex(buffer, pose, -s, -s, 0.0F, 0.0F, 1.0F, color, light, 0.0F, 0.0F, -1.0F);
        addVertex(buffer, pose, s, -s, 0.0F, 1.0F, 1.0F, color, light, 0.0F, 0.0F, -1.0F);
        addVertex(buffer, pose, s, s, 0.0F, 1.0F, 0.0F, color, light, 0.0F, 0.0F, -1.0F);
        addVertex(buffer, pose, -s, s, 0.0F, 0.0F, 0.0F, color, light, 0.0F, 0.0F, -1.0F);
    }

    private void submitBrace(JarRenderState state, PoseStack poseStack, SubmitNodeCollector collector) {
        int light = state.lightCoords;
        int color = 0xFFFFFFFF;
        RenderType type = RenderTypes.entityCutout(BRINE_TEXTURE);
        collector.submitCustomGeometry(poseStack, type, (pose, buffer) -> {
            float minX = 0.5F - (BRACE_MAX_XZ - BRACE_MIN_XZ) * 0.5F * BRACE_STRETCH;
            float maxX = 0.5F + (BRACE_MAX_XZ - BRACE_MIN_XZ) * 0.5F * BRACE_STRETCH;
            float minZ = minX;
            float maxZ = maxX;
            float bottom = BRACE_BOTTOM_Y;
            float top = BRACE_TOP_Y;
            cuboid(buffer, pose, minX, bottom, minZ, maxX, top, maxZ, color, light);
        });
    }

    private void submitLidExtension(JarRenderState state, PoseStack poseStack, SubmitNodeCollector collector) {
        int light = state.lightCoords;
        int color = 0xFFFFFFFF;
        RenderType type = RenderTypes.entityCutout(BRINE_TEXTURE);
        collector.submitCustomGeometry(poseStack, type, (pose, buffer) -> {
            cuboid(buffer, pose,
                    LID_EXT_MIN_XZ, LID_EXT_BOTTOM_Y, LID_EXT_MIN_XZ,
                    LID_EXT_MAX_XZ, LID_EXT_TOP_Y, LID_EXT_MAX_XZ,
                    color, light);
        });
    }

    private static void cuboid(
            VertexConsumer buffer,
            PoseStack.Pose pose,
            float minX,
            float minY,
            float minZ,
            float maxX,
            float maxY,
            float maxZ,
            int color,
            int light
    ) {
        addVertex(buffer, pose, minX, maxY, minZ, 0.0F, 0.0F, color, light, 0.0F, 1.0F, 0.0F);
        addVertex(buffer, pose, minX, maxY, maxZ, 0.0F, 1.0F, color, light, 0.0F, 1.0F, 0.0F);
        addVertex(buffer, pose, maxX, maxY, maxZ, 1.0F, 1.0F, color, light, 0.0F, 1.0F, 0.0F);
        addVertex(buffer, pose, maxX, maxY, minZ, 1.0F, 0.0F, color, light, 0.0F, 1.0F, 0.0F);

        addVertex(buffer, pose, minX, minY, minZ, 0.0F, 0.0F, color, light, 0.0F, -1.0F, 0.0F);
        addVertex(buffer, pose, maxX, minY, minZ, 1.0F, 0.0F, color, light, 0.0F, -1.0F, 0.0F);
        addVertex(buffer, pose, maxX, minY, maxZ, 1.0F, 1.0F, color, light, 0.0F, -1.0F, 0.0F);
        addVertex(buffer, pose, minX, minY, maxZ, 0.0F, 1.0F, color, light, 0.0F, -1.0F, 0.0F);

        addVertex(buffer, pose, minX, minY, minZ, 0.0F, 1.0F, color, light, 0.0F, 0.0F, -1.0F);
        addVertex(buffer, pose, minX, maxY, minZ, 0.0F, 0.0F, color, light, 0.0F, 0.0F, -1.0F);
        addVertex(buffer, pose, maxX, maxY, minZ, 1.0F, 0.0F, color, light, 0.0F, 0.0F, -1.0F);
        addVertex(buffer, pose, maxX, minY, minZ, 1.0F, 1.0F, color, light, 0.0F, 0.0F, -1.0F);

        addVertex(buffer, pose, maxX, minY, maxZ, 0.0F, 1.0F, color, light, 0.0F, 0.0F, 1.0F);
        addVertex(buffer, pose, maxX, maxY, maxZ, 0.0F, 0.0F, color, light, 0.0F, 0.0F, 1.0F);
        addVertex(buffer, pose, minX, maxY, maxZ, 1.0F, 0.0F, color, light, 0.0F, 0.0F, 1.0F);
        addVertex(buffer, pose, minX, minY, maxZ, 1.0F, 1.0F, color, light, 0.0F, 0.0F, 1.0F);

        addVertex(buffer, pose, minX, minY, maxZ, 0.0F, 1.0F, color, light, -1.0F, 0.0F, 0.0F);
        addVertex(buffer, pose, minX, maxY, maxZ, 0.0F, 0.0F, color, light, -1.0F, 0.0F, 0.0F);
        addVertex(buffer, pose, minX, maxY, minZ, 1.0F, 0.0F, color, light, -1.0F, 0.0F, 0.0F);
        addVertex(buffer, pose, minX, minY, minZ, 1.0F, 1.0F, color, light, -1.0F, 0.0F, 0.0F);

        addVertex(buffer, pose, maxX, minY, minZ, 0.0F, 1.0F, color, light, 1.0F, 0.0F, 0.0F);
        addVertex(buffer, pose, maxX, maxY, minZ, 0.0F, 0.0F, color, light, 1.0F, 0.0F, 0.0F);
        addVertex(buffer, pose, maxX, maxY, maxZ, 1.0F, 0.0F, color, light, 1.0F, 0.0F, 0.0F);
        addVertex(buffer, pose, maxX, minY, maxZ, 1.0F, 1.0F, color, light, 1.0F, 0.0F, 0.0F);
    }
}
