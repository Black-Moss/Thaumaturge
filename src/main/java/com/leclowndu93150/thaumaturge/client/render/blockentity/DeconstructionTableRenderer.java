package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.client.entity.TTModelLayers;
import com.leclowndu93150.thaumaturge.client.model.entity.DeconTableModel;
import com.leclowndu93150.thaumaturge.client.render.aspect.AspectTagWorldRenderer;
import com.leclowndu93150.thaumaturge.content.research.decon.BlockEntityDeconstructionTable;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.mojang.blaze3d.vertex.PoseStack;
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
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jspecify.annotations.Nullable;

public final class DeconstructionTableRenderer implements BlockEntityRenderer<BlockEntityDeconstructionTable, DeconstructionTableRenderState> {
    private static final Identifier TEXTURE = TTIds.rl("textures/entity/decontable.png");
    private static final float CENTER = 0.5F;
    private static final float TABLE_Y = 1.0F;
    private static final float BOOK_Y = 1.02F;
    private static final float ITEM_SCALE = 0.5128205F;
    private static final float BOOK_SCALE = 0.8F * ITEM_SCALE;
    private static final float ITEM_LIFT = 0.075F * ITEM_SCALE;
    private static final float ITEM_Y = 1.15F + ITEM_LIFT;
    private static final float ASPECT_Y = 1.081F;
    private static final float ASPECT_SIZE = 0.384F;
    private static final float ASPECT_ALPHA = 0.8F;
    private static final float FLIP_DEGREES = 180.0F;
    private static final float LAY_FLAT_DEGREES = -90.0F;
    private static final float TIME_PERIOD = 360.0F;
    private static final int OUTLINE_NONE = 0;
    private static final int DISPLAY_SEED = 0;

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

    @Override
    public void submit(DeconstructionTableRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        int light = state.lightCoords;
        float spin = state.ticks % TIME_PERIOD;
        poseStack.pushPose();
        poseStack.translate(CENTER, TABLE_Y, CENTER);
        poseStack.mulPose(Axis.XP.rotationDegrees(FLIP_DEGREES));
        collector.submitModelPart(model.root, poseStack, RenderTypes.entityCutout(TEXTURE), light, OverlayTexture.NO_OVERLAY, null);
        poseStack.popPose();
        if (state.book != null) {
            poseStack.pushPose();
            poseStack.translate(CENTER, BOOK_Y, CENTER);
            poseStack.mulPose(Axis.XP.rotationDegrees(LAY_FLAT_DEGREES));
            poseStack.mulPose(Axis.YP.rotationDegrees(FLIP_DEGREES));
            poseStack.scale(BOOK_SCALE, BOOK_SCALE, BOOK_SCALE);
            state.book.submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, OUTLINE_NONE);
            poseStack.popPose();
        }
        if (state.input != null) {
            poseStack.pushPose();
            poseStack.translate(CENTER, ITEM_Y, CENTER);
            poseStack.mulPose(Axis.YP.rotationDegrees(spin + FLIP_DEGREES));
            poseStack.scale(ITEM_SCALE, ITEM_SCALE, ITEM_SCALE);
            state.input.submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, OUTLINE_NONE);
            poseStack.popPose();
        }
        if (state.aspect != null && state.aspect.isBound()) {
            poseStack.pushPose();
            poseStack.translate(CENTER, ASPECT_Y, CENTER);
            poseStack.mulPose(Axis.YP.rotationDegrees(-spin));
            poseStack.mulPose(Axis.XP.rotationDegrees(LAY_FLAT_DEGREES));
            poseStack.scale(ASPECT_SIZE, ASPECT_SIZE, ASPECT_SIZE);
            Holder<IAspect> aspect = state.aspect;
            collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucent(aspect.value().texture()),
                    (pose, buffer) -> AspectTagWorldRenderer.renderQuad(pose, buffer, aspect, ASPECT_ALPHA, false, light));
            poseStack.popPose();
        }
    }

    private ItemStackRenderState displayState(ItemStack stack, Level level) {
        ItemStackRenderState display = new ItemStackRenderState();
        itemModelResolver.updateForTopItem(display, stack, ItemDisplayContext.FIXED, level, null, DISPLAY_SEED);
        return display;
    }
}
