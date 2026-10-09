package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.client.entity.TTModelLayers;
import com.leclowndu93150.thaumaturge.client.model.entity.TTBannerModel;
import com.leclowndu93150.thaumaturge.content.decor.banner.AbstractBannerBlock;
import com.leclowndu93150.thaumaturge.content.decor.banner.BannerStandingBlock;
import com.leclowndu93150.thaumaturge.content.decor.banner.BannerWallBlock;
import com.leclowndu93150.thaumaturge.content.decor.banner.BlockEntityBanner;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.jspecify.annotations.Nullable;

public final class BannerRenderer implements BlockEntityRenderer<BlockEntityBanner, BannerRenderState> {
    private static final Identifier BLANK_TEXTURE = TTIds.rl("textures/entity/banner_blank.png");
    private static final Identifier CULTIST_TEXTURE = TTIds.rl("textures/entity/banner_cultist.png");
    private static final float STANDING_STEP_DEGREES = 22.5F;
    private static final int UNDYED = -1;
    private static final int OPAQUE = 0xFF000000;
    private static final float PHASE_X = 7.0F;
    private static final float PHASE_Y = 9.0F;
    private static final float PHASE_Z = 13.0F;
    private static final float PHASE_DIVISOR = 11.0F;
    private static final float HALF_TURN = 180.0F;
    private static final float POSE_X = 0.5F;
    private static final float POSE_Y = 1.5F;
    private static final float WALL_OFFSET_Y = 1.0F;
    private static final float WALL_OFFSET_Z = -0.21875F;
    private static final float EMBLEM_DROP = -0.3125F;
    private static final float EMBLEM_Z = -0.052F;
    private static final float EMBLEM_HALF_WIDTH = 0.3F;
    private static final float EMBLEM_BOTTOM = 0.35F;
    private static final float EMBLEM_TOP = 0.95F;
    private static final float EMBLEM_ALPHA = 0.75F;
    private static final int OUTLINE_NONE = 0;
    private static final int UNTINTED = -1;
    private static final int TOP_SEGMENT = 0;

    private final TTBannerModel frame;
    private final TTBannerModel cloth;

    public BannerRenderer(BlockEntityRendererProvider.Context context) {
        this.frame = new TTBannerModel(context.bakeLayer(TTModelLayers.TC_BANNER), false);
        this.cloth = new TTBannerModel(context.bakeLayer(TTModelLayers.TC_BANNER), true);
    }

    @Override
    public BannerRenderState createRenderState() {
        return new BannerRenderState();
    }

    @Override
    public void extractRenderState(BlockEntityBanner banner, BannerRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(banner, state, partialTicks, cameraPosition, breakProgress);
        BlockState blockState = banner.getBlockState();
        Block block = blockState.getBlock();
        boolean wall = block instanceof BannerWallBlock;
        state.onWall = wall;
        if (wall) {
            state.yawDegrees = blockState.getValue(BannerWallBlock.FACING).toYRot();
        } else {
            state.yawDegrees = blockState.getValue(BannerStandingBlock.ROTATION) * STANDING_STEP_DEGREES;
        }
        state.color = block instanceof AbstractBannerBlock bannerBlock ? tintOf(bannerBlock.dye()) : UNDYED;
        state.aspectTexture = aspectTexture(banner);
        state.phase = swayPhase(banner.getBlockPos(), partialTicks);
    }

    @Override
    public void submit(BannerRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        orient(state, poseStack);
        submitBody(state, poseStack, collector);
        Identifier emblem = state.aspectTexture;
        if (emblem != null) {
            submitEmblem(state, poseStack, collector, emblem);
        }
        poseStack.popPose();
    }

    private static void orient(BannerRenderState state, PoseStack poseStack) {
        poseStack.translate(POSE_X, POSE_Y, POSE_X);
        poseStack.mulPose(Axis.XP.rotationDegrees(HALF_TURN));
        poseStack.mulPose(Axis.YP.rotationDegrees(HALF_TURN + state.yawDegrees));
        if (state.onWall) {
            poseStack.translate(0.0F, WALL_OFFSET_Y, WALL_OFFSET_Z);
        }
    }

    private void submitBody(BannerRenderState state, PoseStack poseStack, SubmitNodeCollector collector) {
        RenderType layer = RenderTypes.entityCutout(state.color == UNDYED ? CULTIST_TEXTURE : BLANK_TEXTURE);
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(HALF_TURN));
        collector.submitModel(frame, state, poseStack, layer, state.lightCoords, OverlayTexture.NO_OVERLAY, UNTINTED, null, OUTLINE_NONE, state.breakProgress);
        collector.submitModel(cloth, state, poseStack, layer, state.lightCoords, OverlayTexture.NO_OVERLAY, state.color, null, OUTLINE_NONE, state.breakProgress);
        poseStack.popPose();
    }

    private static int tintOf(@Nullable DyeColor dye) {
        return dye == null ? UNDYED : OPAQUE | dye.getTextureDiffuseColor();
    }

    private static float swayPhase(BlockPos pos, float partialTicks) {
        return (positionOffset(pos) + localPlayerTicks() + partialTicks) / PHASE_DIVISOR;
    }

    private static float positionOffset(BlockPos pos) {
        return pos.getX() * PHASE_X + pos.getY() * PHASE_Y + pos.getZ() * PHASE_Z;
    }

    private static float localPlayerTicks() {
        LocalPlayer player = Minecraft.getInstance().player;
        return player == null ? 0.0F : player.tickCount;
    }

    private static void submitEmblem(BannerRenderState state, PoseStack poseStack, SubmitNodeCollector collector, Identifier texture) {
        int light = state.lightCoords;
        poseStack.pushPose();
        poseStack.translate(0.0F, EMBLEM_DROP, 0.0F);
        poseStack.mulPose(new Quaternionf().rotationX(-TTBannerModel.clothBend(state.phase, TOP_SEGMENT)));
        collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucent(texture), (pose, buffer) -> writeEmblem(pose, buffer, light));
        poseStack.popPose();
    }

    private static void writeEmblem(PoseStack.Pose pose, VertexConsumer buffer, int light) {
        int color = ARGB.colorFromFloat(EMBLEM_ALPHA, 1.0F, 1.0F, 1.0F);
        emblemVertex(pose, buffer, -EMBLEM_HALF_WIDTH, EMBLEM_BOTTOM, 0.0F, 1.0F, color, light);
        emblemVertex(pose, buffer, EMBLEM_HALF_WIDTH, EMBLEM_BOTTOM, 1.0F, 1.0F, color, light);
        emblemVertex(pose, buffer, EMBLEM_HALF_WIDTH, EMBLEM_TOP, 1.0F, 0.0F, color, light);
        emblemVertex(pose, buffer, -EMBLEM_HALF_WIDTH, EMBLEM_TOP, 0.0F, 0.0F, color, light);
    }

    private static void emblemVertex(PoseStack.Pose pose, VertexConsumer buffer, float x, float y, float u, float v, int color, int light) {
        buffer.addVertex(pose, x, y, EMBLEM_Z).setColor(color).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, -1.0F);
    }

    private static @Nullable Identifier aspectTexture(BlockEntityBanner banner) {
        Level level = banner.getLevel();
        ResourceKey<IAspect> key = banner.aspect();
        if (level == null || key == null) {
            return null;
        }
        Optional<Holder.Reference<IAspect>> found = level.registryAccess().lookupOrThrow(IAspect.REGISTRY_KEY).get(key);
        return found.map(holder -> holder.value().texture()).orElse(null);
    }
}
