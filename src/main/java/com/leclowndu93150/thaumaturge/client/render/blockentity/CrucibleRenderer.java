package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.content.crucible.BlockEntityCrucible;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import org.jspecify.annotations.Nullable;

public final class CrucibleRenderer implements BlockEntityRenderer<BlockEntityCrucible, CrucibleRenderState> {
    private static final int TANK_SLOT = 0;
    private static final int OPAQUE_WHITE = 0xFFFFFFFF;
    private static final float TARGET_RED = 0.25F;
    private static final float TARGET_GREEN = 0.0F;
    private static final float TARGET_BLUE = 0.75F;
    private static final float RAMP_OFFSET = 0.5F;
    private static final float RAMP_RISE = 0.5F;
    private static final int RAMP_DIVISOR = BlockEntityCrucible.MAX_ASPECT;
    private static final float RAMP_CEILING = 1.0F;
    private static final float CHANNEL_SCALE = 255.0F;
    private static final float[][] SURFACE_CORNERS = {{0.0F, 0.0F}, {0.0F, 1.0F}, {1.0F, 1.0F}, {1.0F, 0.0F}};

    public CrucibleRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public CrucibleRenderState createRenderState() {
        return new CrucibleRenderState();
    }

    @Override
    public void extractRenderState(BlockEntityCrucible crucible, CrucibleRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(crucible, state, partialTicks, cameraPosition, breakProgress);
        FluidResource resource = crucible.getTank().getResource(TANK_SLOT);
        int amount = crucible.getTank().getAmountAsInt(TANK_SLOT);
        if (resource.isEmpty() || amount <= 0) {
            state.fluid = FluidStack.EMPTY;
            return;
        }
        FluidStack stack = resource.toStack(amount);
        state.fluid = stack;
        Fluid fluid = stack.getFluid();
        FluidModel model = Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(fluid.defaultFluidState());
        state.sprite = model.stillMaterial().sprite();
        state.fluidHeight = crucible.surfaceLevel();
        int base = OPAQUE_WHITE;
        if (model.tintSource() != null) {
            BlockState fluidBlock = fluid.defaultFluidState().createLegacyBlock();
            base = ARGB.opaque(model.tintSource().color(fluidBlock));
        }
        state.color = tinted(base, crucible.getAspects().totalAmount());
    }

    @Override
    public void submit(CrucibleRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.fluid == null || state.fluid.isEmpty() || state.sprite == null) {
            return;
        }
        TextureAtlasSprite sprite = state.sprite;
        float height = state.fluidHeight;
        int color = state.color;
        int light = state.lightCoords;
        collector.submitCustomGeometry(poseStack, Sheets.translucentBlockItemSheet(), (pose, buffer) -> {
            VertexConsumer wrapped = sprite.wrap(buffer);
            for (float[] corner : SURFACE_CORNERS) {
                JarRenderer.addVertex(wrapped, pose, corner[0], height, corner[1], corner[0], corner[1], color, light, 0.0F, 1.0F, 0.0F);
            }
        });
    }

    private static int tinted(int base, int aspectTotal) {
        return darken(base, tintFactor(aspectTotal));
    }

    private static float tintFactor(int aspectTotal) {
        float ramp = Math.min(RAMP_CEILING, RAMP_OFFSET + RAMP_RISE * aspectTotal / RAMP_DIVISOR);
        return ramp * Mth.clamp(aspectTotal, 0, 1);
    }

    private static int darken(int color, float factor) {
        float red = Mth.lerp(factor, ARGB.red(color) / CHANNEL_SCALE, TARGET_RED);
        float green = Mth.lerp(factor, ARGB.green(color) / CHANNEL_SCALE, TARGET_GREEN);
        float blue = Mth.lerp(factor, ARGB.blue(color) / CHANNEL_SCALE, TARGET_BLUE);
        return ARGB.colorFromFloat(1.0F, red, green, blue);
    }
}
