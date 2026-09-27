package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.client.golem.GolemMeshes;
import com.leclowndu93150.thaumaturge.client.model.mesh.TCMeshPart;
import com.leclowndu93150.thaumaturge.content.essentia.crystalizer.BlockEntityEssentiaCrystalizer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class EssentiaCrystalizerRenderer implements BlockEntityRenderer<BlockEntityEssentiaCrystalizer, EssentiaCrystalizerRenderState> {
    private static final Identifier MODEL = TCIds.rl("models/mesh/crystalizer_crystal.tcmesh");
    private static final RenderType CRYSTAL = RenderTypes.entityCutout(TCIds.rl("textures/block/crystalizer_crystal.png"));
    private static final String PART_CRYSTAL = "Crystal";
    private static final int CRYSTALS = 4;
    private static final float CRYSTAL_ANGLE = 90.0F;
    private static final float CRYSTAL_SCALE = 0.75F;
    private static final float CRYSTAL_OFFSET_X = 0.34F;
    private static final float CRYSTAL_OFFSET_Z = 1.2125F;
    private static final int MIN_BLOCK_LIGHT = 12;

    public EssentiaCrystalizerRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public EssentiaCrystalizerRenderState createRenderState() {
        return new EssentiaCrystalizerRenderState();
    }

    @Override
    public void extractRenderState(BlockEntityEssentiaCrystalizer crystalizer, EssentiaCrystalizerRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(crystalizer, state, partialTicks, cameraPosition, breakProgress);
        state.facing = crystalizer.getBlockState().getValue(BlockStateProperties.FACING);
        state.active = crystalizer.aspectKey() != null;
        state.spin = crystalizer.rotation() + crystalizer.rotationSpeed() * partialTicks;
        state.color = ARGB.colorFromFloat(1.0F, crystalizer.crystalRed(), crystalizer.crystalGreen(), crystalizer.crystalBlue());
    }

    @Override
    public void submit(EssentiaCrystalizerRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (!state.active) {
            return;
        }
        int light = LightCoordsUtil.pack(Math.max(LightCoordsUtil.block(state.lightCoords), MIN_BLOCK_LIGHT), LightCoordsUtil.sky(state.lightCoords));
        int color = state.color;
        poseStack.pushPose();
        LegacyFacingPose.apply(poseStack, state.facing);
        for (TCMeshPart part : GolemMeshes.get(MODEL).parts()) {
            if (!PART_CRYSTAL.equals(part.name())) {
                continue;
            }
            for (int crystal = 0; crystal < CRYSTALS; crystal++) {
                poseStack.pushPose();
                poseStack.scale(CRYSTAL_SCALE, CRYSTAL_SCALE, CRYSTAL_SCALE);
                poseStack.mulPose(Axis.ZP.rotationDegrees(CRYSTAL_ANGLE * crystal));
                poseStack.translate(CRYSTAL_OFFSET_X, 0.0F, CRYSTAL_OFFSET_Z);
                poseStack.mulPose(Axis.ZP.rotationDegrees(state.spin));
                collector.submitCustomGeometry(poseStack, CRYSTAL, (pose, buffer) -> GolemMeshes.renderPart(part, pose, buffer, light, color));
                poseStack.popPose();
            }
        }
        poseStack.popPose();
    }
}
