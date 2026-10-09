package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.client.entity.TTModelLayers;
import com.leclowndu93150.thaumaturge.client.model.entity.ManaPodModel;
import com.leclowndu93150.thaumaturge.content.manabean.BlockEntityManaPod;
import com.leclowndu93150.thaumaturge.content.manabean.BlockManaPod;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class ManaPodRenderer implements BlockEntityRenderer<BlockEntityManaPod, ManaPodRenderState> {
    private static final Identifier CORE_TEXTURE = TTIds.rl("textures/entity/manapod_0.png");
    private static final Identifier SHELL_TEXTURE = TTIds.rl("textures/entity/manapod_2.png");
    private static final int OPAQUE = 0xFF000000;
    private static final float BASE_RED = 37.0F / 255.0F;
    private static final float BASE_GREEN = 157.0F / 255.0F;
    private static final float BASE_BLUE = 117.0F / 255.0F;
    private static final float SHELL_ALPHA = 0.9F;
    private static final float CENTER = 0.5F;
    private static final float ANCHOR_Y = 0.75F;
    private static final float CORE_DROP = 0.1F;
    private static final float HALF_TURN = 180.0F;
    private static final int SHELL_MIN_AGE = 2;
    private static final int CORE_MIN_AGE = 3;
    private static final float GROWTH_SPAN = 5.0F;

    private final ManaPodModel model;

    public ManaPodRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new ManaPodModel(context.bakeLayer(TTModelLayers.MANA_POD));
    }

    @Override
    public ManaPodRenderState createRenderState() {
        return new ManaPodRenderState();
    }

    @Override
    public void extractRenderState(BlockEntityManaPod pod, ManaPodRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(pod, state, partialTicks, cameraPosition, breakProgress);
        state.age = pod.getBlockState().getValue(BlockManaPod.AGE);
        Holder<IAspect> aspect = pod.aspect();
        state.hasAspect = aspect != null;
        state.aspectColor = aspect == null ? -1 : OPAQUE | aspect.value().color();
    }

    @Override
    public void submit(ManaPodRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.age < SHELL_MIN_AGE) {
            return;
        }
        if (state.age >= CORE_MIN_AGE) {
            poseStack.pushPose();
            poseStack.translate(CENTER, ANCHOR_Y - CORE_DROP, CENTER);
            poseStack.mulPose(Axis.XP.rotationDegrees(HALF_TURN));
            collector.submitModelPart(model.core, poseStack, RenderTypes.entityCutout(CORE_TEXTURE), LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, null);
            poseStack.popPose();
        }
        poseStack.pushPose();
        poseStack.translate(CENTER, ANCHOR_Y, CENTER);
        poseStack.mulPose(Axis.XP.rotationDegrees(HALF_TURN));
        collector.submitModelPart(model.shell, poseStack, RenderTypes.entityTranslucent(SHELL_TEXTURE), state.lightCoords, OverlayTexture.NO_OVERLAY, null, tint(state), state.breakProgress);
        poseStack.popPose();
    }

    private static int tint(ManaPodRenderState state) {
        if (!state.hasAspect) {
            return ARGB.colorFromFloat(SHELL_ALPHA, BASE_RED, BASE_GREEN, BASE_BLUE);
        }
        float progress = Mth.clamp((state.age - SHELL_MIN_AGE) / GROWTH_SPAN, 0.0F, 1.0F);
        float red = Mth.lerp(progress, BASE_RED, ARGB.redFloat(state.aspectColor));
        float green = Mth.lerp(progress, BASE_GREEN, ARGB.greenFloat(state.aspectColor));
        float blue = Mth.lerp(progress, BASE_BLUE, ARGB.blueFloat(state.aspectColor));
        return ARGB.colorFromFloat(SHELL_ALPHA, red, green, blue);
    }
}
