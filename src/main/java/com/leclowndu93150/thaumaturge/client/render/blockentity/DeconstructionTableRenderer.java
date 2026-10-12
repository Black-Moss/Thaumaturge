package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.client.effect.rendertype.TTFXRenderTypes;
import com.leclowndu93150.thaumaturge.client.entity.TTModelLayers;
import com.leclowndu93150.thaumaturge.client.model.entity.DeconTableModel;
import com.leclowndu93150.thaumaturge.client.render.TTFlatRenderTypes;
import com.leclowndu93150.thaumaturge.client.render.aspect.AspectTagWorldRenderer;
import com.leclowndu93150.thaumaturge.client.render.aspect.ParticleTextures;
import com.leclowndu93150.thaumaturge.client.render.aspect.StripUv;
import com.leclowndu93150.thaumaturge.content.research.decon.BlockEntityDeconstructionTable;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.joml.Matrix4fc;
import org.jspecify.annotations.Nullable;

public final class DeconstructionTableRenderer implements BlockEntityRenderer<BlockEntityDeconstructionTable, DeconstructionTableRenderState> {
    private static final Identifier TEXTURE = TTIds.rl("textures/entity/decontable.png");
    private static final float CENTER = 0.5F;
    private static final float TABLE_Y = 1.0F;
    private static final float ITEM_SCALE = 0.5128205F;
    private static final float FLIP_DEGREES = 180.0F;
    private static final float LAY_FLAT_DEGREES = -90.0F;
    private static final float TIME_PERIOD = 360.0F;
    private static final int OUTLINE_NONE = 0;
    private static final int DISPLAY_SEED = 0;
    private static final float BOOK_Y = TABLE_Y + 0.002F;
    private static final float BOOK_SCALE = 0.85F;
    private static final float BOOK_YAW = 20.0F;
    private static final float ITEM_Y = 1.42F;
    private static final float ITEM_LIFT = LegacyItemLift.LEGACY_CENTER_Y * ITEM_SCALE;
    private static final float ITEM_SPIN_PER_TICK = 1.0F;
    private static final float ITEM_BOB_HEIGHT = 0.045F;
    private static final float ITEM_BOB_PERIOD = 90.0F;
    private static final float GLOW_SIZE = 0.62F;
    private static final float GLOW_HALF = 0.5F;
    private static final float GLOW_ALPHA = 0.28F;
    private static final float GLOW_ALPHA_SWING = 0.08F;
    private static final float GLOW_PULSE_PERIOD = 60.0F;
    private static final int GLOW_RGB = 0xE4D6FF;
    private static final int GLOW_FRAME = 0;
    private static final float ASPECT_Y = TABLE_Y + 0.03F;
    private static final float ASPECT_SIZE = 0.44F;
    private static final float ASPECT_ALPHA = 0.82F;
    private static final float ASPECT_SPIN_PER_TICK = -1.0F;

    private final DeconTableModel model;
    private final ItemModelResolver itemModelResolver;
    private @Nullable ItemStack bookStack;

    public DeconstructionTableRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new DeconTableModel(context.bakeLayer(TTModelLayers.DECONSTRUCTION_TABLE));
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public DeconstructionTableRenderState createRenderState() {
        return new DeconstructionTableRenderState();
    }

    @Override
    public void extractRenderState(BlockEntityDeconstructionTable table, DeconstructionTableRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(table, state, partialTicks, cameraPosition, breakProgress);
        Level level = table.getLevel();
        if (level == null) {
            return;
        }
        state.ticks = level.getGameTime() % TIME_PERIOD + partialTicks;
        if (bookStack == null) {
            bookStack = new ItemStack(TTItems.THAUMOMETER.get());
        }
        state.book = displayState(bookStack, level);
        ItemResource input = table.items().getResource(BlockEntityDeconstructionTable.SLOT_INPUT);
        state.input = input.isEmpty() ? null : displayState(input.toStack(1), level);
        Identifier resultId = table.resultAspect();
        state.aspect = resultId == null
                ? null
                : level.registryAccess().lookupOrThrow(IAspect.REGISTRY_KEY).get(ResourceKey.create(IAspect.REGISTRY_KEY, resultId)).<Holder<IAspect>>map(reference -> reference).orElse(null);
    }

    private ItemStackRenderState displayState(ItemStack stack, Level level) {
        ItemStackRenderState display = new ItemStackRenderState();
        itemModelResolver.updateForTopItem(display, stack, ItemDisplayContext.FIXED, level, null, DISPLAY_SEED);
        return display;
    }

    @Override
    public void submit(DeconstructionTableRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        int light = state.lightCoords;
        poseStack.pushPose();
        poseStack.translate(CENTER, TABLE_Y, CENTER);
        poseStack.mulPose(Axis.XP.rotationDegrees(FLIP_DEGREES));
        collector.submitModelPart(model.root, poseStack, RenderTypes.entityCutout(TEXTURE), light, OverlayTexture.NO_OVERLAY, null);
        poseStack.popPose();
        if (state.book != null) {
            submitBook(state.book, poseStack, collector, light);
        }
        if (state.aspect != null) {
            submitAspect(state.aspect, state.ticks, poseStack, collector, light);
        }
        if (state.input != null) {
            submitInput(state.input, state.ticks, poseStack, collector, camera);
        }
    }

    private static void submitBook(ItemStackRenderState book, PoseStack poseStack, SubmitNodeCollector collector, int light) {
        poseStack.pushPose();
        poseStack.translate(CENTER, BOOK_Y, CENTER);
        poseStack.mulPose(Axis.YP.rotationDegrees(BOOK_YAW));
        poseStack.mulPose(Axis.XP.rotationDegrees(LAY_FLAT_DEGREES));
        poseStack.scale(BOOK_SCALE, BOOK_SCALE, BOOK_SCALE);
        poseStack.translate(0.0F, 0.0F, (float) -book.getModelBoundingBox().minZ);
        book.submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, OUTLINE_NONE);
        poseStack.popPose();
    }

    private static void submitAspect(Holder<IAspect> aspect, float ticks, PoseStack poseStack, SubmitNodeCollector collector, int light) {
        if (!aspect.isBound()) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(CENTER, ASPECT_Y, CENTER);
        poseStack.mulPose(Axis.YP.rotationDegrees(ticks * ASPECT_SPIN_PER_TICK));
        poseStack.mulPose(Axis.XP.rotationDegrees(LAY_FLAT_DEGREES));
        poseStack.scale(ASPECT_SIZE, ASPECT_SIZE, ASPECT_SIZE);
        collector.submitCustomGeometry(poseStack, TTFlatRenderTypes.entityTranslucentFlat(aspect.value().texture()),
                (pose, buffer) -> AspectTagWorldRenderer.renderQuad(pose, buffer, aspect, ASPECT_ALPHA, false, light));
        poseStack.popPose();
    }

    private static void submitInput(ItemStackRenderState input, float ticks, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        float bob = Mth.sin(ticks * Mth.TWO_PI / ITEM_BOB_PERIOD) * ITEM_BOB_HEIGHT;
        float centreY = ITEM_Y + bob;
        poseStack.pushPose();
        poseStack.translate(CENTER, centreY, CENTER);
        poseStack.mulPose(camera.orientation);
        poseStack.scale(GLOW_SIZE, GLOW_SIZE, GLOW_SIZE);
        float glowAlpha = GLOW_ALPHA + Mth.sin(ticks * Mth.TWO_PI / GLOW_PULSE_PERIOD) * GLOW_ALPHA_SWING;
        int tint = ARGB.color(glowAlpha, GLOW_RGB);
        collector.submitCustomGeometry(poseStack, TTFXRenderTypes.additive(ParticleTextures.ORB_GLOW), (pose, buffer) -> writeGlow(buffer, pose.pose(), tint));
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(CENTER, centreY - ITEM_LIFT, CENTER);
        poseStack.mulPose(Axis.YP.rotationDegrees(ticks * ITEM_SPIN_PER_TICK));
        poseStack.scale(ITEM_SCALE, ITEM_SCALE, ITEM_SCALE);
        poseStack.translate(0.0F, LegacyItemLift.centerLift(input), 0.0F);
        input.submit(poseStack, collector, LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, OUTLINE_NONE);
        poseStack.popPose();
    }

    private static void writeGlow(VertexConsumer buffer, Matrix4fc matrix, int tint) {
        float u0 = StripUv.u0(GLOW_FRAME, ParticleTextures.ORB_GLOW_FRAMES);
        float u1 = StripUv.u1(GLOW_FRAME, ParticleTextures.ORB_GLOW_FRAMES);
        buffer.addVertex(matrix, -GLOW_HALF, -GLOW_HALF, 0.0F).setUv(u0, StripUv.V1).setColor(tint).setLight(LightCoordsUtil.FULL_BRIGHT);
        buffer.addVertex(matrix, GLOW_HALF, -GLOW_HALF, 0.0F).setUv(u1, StripUv.V1).setColor(tint).setLight(LightCoordsUtil.FULL_BRIGHT);
        buffer.addVertex(matrix, GLOW_HALF, GLOW_HALF, 0.0F).setUv(u1, StripUv.V0).setColor(tint).setLight(LightCoordsUtil.FULL_BRIGHT);
        buffer.addVertex(matrix, -GLOW_HALF, GLOW_HALF, 0.0F).setUv(u0, StripUv.V0).setColor(tint).setLight(LightCoordsUtil.FULL_BRIGHT);
    }
}
