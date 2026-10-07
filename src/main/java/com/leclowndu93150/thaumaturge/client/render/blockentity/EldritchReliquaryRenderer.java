package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.eldritch.ReliquaryViewHolder;
import com.leclowndu93150.thaumaturge.content.eldritch.reliquary.BlockEldritchReliquary;
import com.leclowndu93150.thaumaturge.content.eldritch.reliquary.BlockEntityEldritchReliquary;
import com.leclowndu93150.thaumaturge.content.eldritch.reliquary.ReliquaryRole;
import com.leclowndu93150.thaumaturge.content.eldritch.reliquary.ReliquaryView;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class EldritchReliquaryRenderer implements BlockEntityRenderer<BlockEntityEldritchReliquary, EldritchReliquaryRenderState> {
    private static final SpriteId GLOW_SPRITE = new SpriteId(TextureAtlas.LOCATION_BLOCKS, TTIds.rl("block/eldritch_crust_glowing"));
    private static final SpriteId VEIL_SPRITE = new SpriteId(TextureAtlas.LOCATION_BLOCKS, TTIds.rl("block/eldritch_door"));
    private static final float PIXEL = 1.0F / 16.0F;
    private static final float ALCOVE_MIN_X = 4.0F * PIXEL;
    private static final float ALCOVE_MAX_X = 12.0F * PIXEL;
    private static final float ALCOVE_MIN_Y = 4.0F * PIXEL;
    private static final float ALCOVE_MAX_Y = 13.0F * PIXEL;
    private static final float SURFACE_OFFSET = 0.002F;
    private static final float BACK_Z = 8.0F * PIXEL - SURFACE_OFFSET;
    private static final float MOUTH_Z = 3.0F * PIXEL - SURFACE_OFFSET;
    private static final float REWARD_X = 0.5F;
    private static final float REWARD_Y = 6.5F * PIXEL;
    private static final float REWARD_Z = 5.5F * PIXEL;
    private static final float REWARD_SCALE = 0.75F;
    private static final float BOB_HEIGHT = 0.03F;
    private static final float BOB_PERIOD = 20.0F;
    private static final float SPIN_PER_TICK = 1.5F;
    private static final float FULL_TURN = 360.0F;
    private static final int WHITE = 0xFFFFFFFF;

    private final ItemModelResolver itemModelResolver;
    private final Map<ReliquaryRole, ItemStack> rewards = new EnumMap<>(ReliquaryRole.class);

    public EldritchReliquaryRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public EldritchReliquaryRenderState createRenderState() {
        return new EldritchReliquaryRenderState();
    }

    @Override
    public void extractRenderState(BlockEntityEldritchReliquary reliquary, EldritchReliquaryRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(reliquary, state, partialTicks, cameraPosition, breakProgress);
        state.facing = reliquary.getBlockState().getValue(BlockEldritchReliquary.FACING);
        state.view = ReliquaryViewHolder.get(reliquary.getBlockPos());
        var viewEntity = Minecraft.getInstance().getCameraEntity();
        state.ticks = viewEntity == null ? partialTicks : viewEntity.tickCount + partialTicks;
        if (!state.view.showsReward()) {
            state.reward = null;
            return;
        }
        ItemStackRenderState itemState = new ItemStackRenderState();
        itemModelResolver.updateForTopItem(itemState, rewards.computeIfAbsent(reliquary.role(), EldritchReliquaryRenderer::rewardFor), ItemDisplayContext.GROUND, reliquary.getLevel(), null, 0);
        state.reward = itemState;
        state.rewardLift = LegacyItemLift.centerLift(itemState);
    }

    private static ItemStack rewardFor(ReliquaryRole role) {
        return switch (role) {
            case KEY_ROOM -> new ItemStack(TTItems.RUNED_TABLET.get());
            case BOSS -> new ItemStack(TTItems.LOOT_BAG_RARE.get());
            case CACHE -> new ItemStack(TTItems.LOOT_BAG_UNCOMMON.get());
        };
    }

    @Override
    public void submit(EldritchReliquaryRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.0F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(-state.facing.getOpposite().toYRot()));
        poseStack.translate(-0.5F, 0.0F, -0.5F);
        if (state.view == ReliquaryView.CLAIMABLE) {
            submitPanel(poseStack, collector, Minecraft.getInstance().getAtlasManager().get(GLOW_SPRITE), BACK_Z);
        }
        if (state.view == ReliquaryView.WARDED) {
            submitPanel(poseStack, collector, Minecraft.getInstance().getAtlasManager().get(VEIL_SPRITE), MOUTH_Z);
        }
        if (state.reward != null) {
            poseStack.pushPose();
            poseStack.translate(REWARD_X, REWARD_Y + Mth.sin(state.ticks / BOB_PERIOD) * BOB_HEIGHT, REWARD_Z);
            poseStack.mulPose(Axis.YP.rotationDegrees(state.ticks * SPIN_PER_TICK % FULL_TURN));
            poseStack.scale(REWARD_SCALE, REWARD_SCALE, REWARD_SCALE);
            poseStack.translate(0.0F, state.rewardLift, 0.0F);
            state.reward.submit(poseStack, collector, LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    private static void submitPanel(PoseStack poseStack, SubmitNodeCollector collector, TextureAtlasSprite sprite, float z) {
        collector.submitCustomGeometry(poseStack, Sheets.cutoutBlockItemSheet(), (pose, buffer) -> {
            VertexConsumer wrapped = sprite.wrap(buffer);
            float u0 = 16.0F * PIXEL - ALCOVE_MAX_X;
            float u1 = 16.0F * PIXEL - ALCOVE_MIN_X;
            float v0 = 16.0F * PIXEL - ALCOVE_MAX_Y;
            float v1 = 16.0F * PIXEL - ALCOVE_MIN_Y;
            vertex(wrapped, pose, ALCOVE_MIN_X, ALCOVE_MIN_Y, z, u1, v1);
            vertex(wrapped, pose, ALCOVE_MIN_X, ALCOVE_MAX_Y, z, u1, v0);
            vertex(wrapped, pose, ALCOVE_MAX_X, ALCOVE_MAX_Y, z, u0, v0);
            vertex(wrapped, pose, ALCOVE_MAX_X, ALCOVE_MIN_Y, z, u0, v1);
        });
    }

    private static void vertex(VertexConsumer buffer, PoseStack.Pose pose, float x, float y, float z, float u, float v) {
        buffer.addVertex(pose, x, y, z).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightCoordsUtil.FULL_BRIGHT).setNormal(pose, 0.0F, 0.0F, -1.0F).setColor(WHITE);
    }
}
