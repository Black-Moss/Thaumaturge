package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.content.essentia.bellows.BlockBellows;
import com.leclowndu93150.thaumaturge.content.essentia.bellows.BlockEntityBellows;
import com.leclowndu93150.thaumaturge.content.essentia.tube.BlockEntityTubeBuffer;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;
import org.jspecify.annotations.Nullable;

public class BellowsRenderer implements BlockEntityRenderer<BlockEntityBellows, BellowsRenderState> {
    public static final String[] parts = {"bottom_plank", "top_plank", "bag"};
    @SuppressWarnings("unchecked")
    public static final StandaloneModelKey<BlockStateModel>[] MODEL_KEYS = (StandaloneModelKey<BlockStateModel>[]) new StandaloneModelKey<?>[3];

    private static final int BOTTOM_PLANK = 0;
    private static final int TOP_PLANK = 1;
    private static final int BAG = 2;
    private static final float CENTER = 0.5F;
    private static final float PLANK_TRAVEL = 0.25F;
    private static final float BAG_MIN_HEIGHT = 0.1F;
    private static final int[] NO_TINTS = new int[0];
    private static final int OUTLINE_NONE = 0;

    public BellowsRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public BellowsRenderState createRenderState() {
        return new BellowsRenderState();
    }

    @Override
    public void extractRenderState(BlockEntityBellows bellows, BellowsRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(bellows, state, partialTicks, cameraPosition, breakProgress);
        BlockState blockState = bellows.getBlockState();
        BlockPos pos = bellows.getBlockPos();
        Level level = bellows.getLevel();
        state.facing = blockState.getValue(BlockBellows.FACING);
        state.scale = bellows.inflation(partialTicks);
        state.seed = blockState.getSeed(pos);
        if (level != null && level.getBlockEntity(pos.relative(state.facing)) instanceof BlockEntityTubeBuffer) {
            state.extension = true;
        }
        for (int index = 0; index < parts.length; index++) {
            state.parts[index] = new ArrayList<>();
            StandaloneModelKey<BlockStateModel> key = MODEL_KEYS[index];
            state.models[index] = key == null ? null : Minecraft.getInstance().getModelManager().getStandaloneModel(key);
            if (state.models[index] != null && level instanceof ClientLevel clientLevel) {
                state.models[index].collectParts(clientLevel, pos, blockState, clientLevel.getRandom(), state.parts[index]);
            }
        }
    }

    @Override
    public void submit(BellowsRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        for (int index = 0; index < parts.length; index++) {
            if (state.models[index] == null || state.parts[index] == null) {
                return;
            }
        }
        for (int index = 0; index < parts.length; index++) {
            poseStack.pushPose();
            place(poseStack, state.facing, index, state.scale);
            collector.submitMultiLayerBlockModel(poseStack, state.parts[index], true, NO_TINTS, state.lightCoords, OverlayTexture.NO_OVERLAY, OUTLINE_NONE);
            if (state.breakProgress != null) {
                collector.submitBreakingBlockModel(poseStack, state.models[index], state.seed, state.breakProgress.progress());
            }
            poseStack.popPose();
        }
    }

    private static void place(PoseStack poseStack, Direction facing, int part, float inflation) {
        poseStack.translate(CENTER, CENTER, CENTER);
        BlockFacingPose.northBased(poseStack, facing);
        poseStack.translate(-CENTER, -CENTER, -CENTER);
        float travel = PLANK_TRAVEL * (1.0F - inflation);
        switch (part) {
            case TOP_PLANK -> poseStack.translate(0.0F, -travel, 0.0F);
            case BOTTOM_PLANK -> poseStack.translate(0.0F, travel, 0.0F);
            case BAG -> {
                poseStack.translate(0.0F, CENTER, 0.0F);
                poseStack.scale(1.0F, inflation + BAG_MIN_HEIGHT, 1.0F);
                poseStack.translate(0.0F, -CENTER, 0.0F);
            }
            default -> {
            }
        }
    }
}
