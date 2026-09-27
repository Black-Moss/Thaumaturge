package com.leclowndu93150.thaumaturge.client.model;

import com.leclowndu93150.thaumaturge.client.render.blockentity.AdvancedAlchemicalFurnaceRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import java.util.function.Consumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

public final class AdvancedAlchemicalFurnaceItemSpecialRenderer implements NoDataSpecialModelRenderer {
    private static final float MODEL_WIDTH = 3.0F;
    private static final float MODEL_HEIGHT = 2.0F;
    private static final float SCALE = 1.0F / MODEL_WIDTH;
    private static final float LIFT = (1.0F - MODEL_HEIGHT * SCALE) / 2.0F;
    private static final float UPRIGHT_ANGLE = 90.0F;

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        poseStack.pushPose();
        poseStack.translate(0.5F, LIFT, 0.5F);
        poseStack.scale(SCALE, SCALE, SCALE);
        poseStack.mulPose(Axis.XN.rotationDegrees(UPRIGHT_ANGLE));
        AdvancedAlchemicalFurnaceRenderer.submitMesh(false, false, poseStack, collector, lightCoords);
        poseStack.popPose();
    }

    @Override
    public void getExtents(Consumer<Vector3fc> consumer) {
        consumer.accept(new Vector3f(0.0F, 0.0F, 0.0F));
        consumer.accept(new Vector3f(1.0F, 1.0F, 1.0F));
    }

    public record Unbaked() implements NoDataSpecialModelRenderer.Unbaked {
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(Unbaked::new);

        @Override
        public @Nullable SpecialModelRenderer<Void> bake(SpecialModelRenderer.BakingContext context) {
            return new AdvancedAlchemicalFurnaceItemSpecialRenderer();
        }

        @Override
        public MapCodec<? extends NoDataSpecialModelRenderer.Unbaked> type() {
            return MAP_CODEC;
        }
    }
}
