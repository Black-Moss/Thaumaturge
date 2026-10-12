package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.eldritch.OuterLands;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.function.ToIntFunction;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class EldritchCapRenderer<T extends BlockEntity> implements BlockEntityRenderer<T, EldritchCapRenderState> {
    public static final Identifier CAP_TEXTURE = TTIds.rl("textures/entity/obelisk_cap.png");
    public static final Identifier CAP_TEXTURE_OUTER = TTIds.rl("textures/entity/obelisk_cap_2.png");
    public static final Identifier ALTAR_TEXTURE = TTIds.rl("textures/entity/obelisk_cap_altar.png");
    public static final Identifier ALTAR_MODEL = TTIds.rl("models/mesh/obelisk_cap_altar.ttmesh");

    private static final float CENTER = 0.5F;
    private static final float UPRIGHT_DEGREES = 90.0F;
    private static final int MAX_EYES = 4;
    private static final float FLIP_DEGREES = 180.0F;
    private static final float EYE_SIDE_DEGREES = 90.0F;
    private static final float EYE_HEIGHT = 0.2F;
    private static final float RIM_HEIGHT = 0.041F;
    private static final float RIM_REACH = 0.497F;
    private static final float SIDE_SLOPE = 0.249F;
    private static final float EYE_TILT_DEGREES = 14.0F;
    private static final float EYE_STANDOFF = 0.02F;
    private static final float EYE_SCALE = 0.25F;
    private static final float EYE_REACH = RIM_REACH - (EYE_HEIGHT - RIM_HEIGHT) * SIDE_SLOPE;
    private static final int OUTLINE_NONE = 0;
    private static final int DISPLAY_SEED = 0;

    private final Identifier meshId;
    private final Identifier texture;
    private final Identifier outerTexture;
    private final ToIntFunction<T> eyeCount;
    private final ItemModelResolver itemModelResolver;
    private @Nullable ItemStack eyeStack;

    public EldritchCapRenderer(BlockEntityRendererProvider.Context context, Identifier meshId, Identifier texture, Identifier outerTexture, ToIntFunction<T> eyeCount) {
        this.meshId = meshId;
        this.texture = texture;
        this.outerTexture = outerTexture;
        this.eyeCount = eyeCount;
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public EldritchCapRenderState createRenderState() {
        return new EldritchCapRenderState();
    }

    @Override
    public void extractRenderState(T blockEntity, EldritchCapRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        Level level = blockEntity.getLevel();
        state.outerLands = level != null && OuterLands.DIMENSION.equals(level.dimension());
        state.eyes = Math.min(MAX_EYES, eyeCount.applyAsInt(blockEntity));
        state.eye = state.eyes > 0 ? resolveEye(level) : null;
    }

    private ItemStackRenderState resolveEye(@Nullable Level level) {
        if (eyeStack == null) {
            eyeStack = new ItemStack(TTItems.ELDRITCH_EYE.get());
        }
        ItemStackRenderState resolved = new ItemStackRenderState();
        itemModelResolver.updateForTopItem(resolved, eyeStack, ItemDisplayContext.FIXED, level, null, DISPLAY_SEED);
        return resolved;
    }

    @Override
    public void submit(EldritchCapRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        submitMesh(state, poseStack, collector);
        ItemStackRenderState eye = state.eye;
        if (eye == null) {
            return;
        }
        for (int side = 0; side < state.eyes; side++) {
            poseStack.pushPose();
            placeEye(poseStack, side);
            eye.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, OUTLINE_NONE);
            poseStack.popPose();
        }
    }

    private void submitMesh(EldritchCapRenderState state, PoseStack poseStack, SubmitNodeCollector collector) {
        RenderType type = RenderTypes.entityCutout(state.outerLands ? outerTexture : texture);
        poseStack.pushPose();
        poseStack.translate(CENTER, 0.0F, CENTER);
        poseStack.mulPose(Axis.XN.rotationDegrees(UPRIGHT_DEGREES));
        EldritchObeliskRenderer.submitCap(meshId, poseStack, collector, type, state.lightCoords);
        poseStack.popPose();
    }

    private static void placeEye(PoseStack poseStack, int side) {
        poseStack.translate(CENTER, EYE_HEIGHT, CENTER);
        poseStack.mulPose(Axis.YP.rotationDegrees(side * EYE_SIDE_DEGREES));
        poseStack.translate(0.0F, 0.0F, EYE_REACH);
        poseStack.mulPose(Axis.XN.rotationDegrees(EYE_TILT_DEGREES));
        poseStack.translate(0.0F, 0.0F, EYE_STANDOFF);
        poseStack.mulPose(Axis.YP.rotationDegrees(FLIP_DEGREES));
        poseStack.scale(EYE_SCALE, EYE_SCALE, EYE_SCALE);
    }

}
