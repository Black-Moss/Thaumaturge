package com.leclowndu93150.thaumaturge.client.model;

import com.leclowndu93150.thaumaturge.client.render.blockentity.ResearchTableTop;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import java.util.function.Consumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.core.Direction;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public final class ResearchTableItemSpecialRenderer implements NoDataSpecialModelRenderer {
    private static final Direction ITEM_FACING = Direction.SOUTH;
    private static final float[][] EXTENT_CORNERS = {{0.0F, 1.0F, -1.0F}, {1.0F, 1.1F, 1.0F}};

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        poseStack.pushPose();
        ResearchTableTop.pose(poseStack, ITEM_FACING);
        ResearchTableTop.submitSheets(poseStack, collector, lightCoords);
        poseStack.popPose();
    }

    @Override
    public void getExtents(Consumer<Vector3fc> consumer) {
        for (float[] corner : EXTENT_CORNERS) {
            consumer.accept(new Vector3f(corner[0], corner[1], corner[2]));
        }
    }

    public record Unbaked() implements NoDataSpecialModelRenderer.Unbaked {
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(Unbaked::new);

        @Override
        public SpecialModelRenderer<Void> bake(SpecialModelRenderer.BakingContext context) {
            return new ResearchTableItemSpecialRenderer();
        }

        @Override
        public MapCodec<? extends NoDataSpecialModelRenderer.Unbaked> type() {
            return MAP_CODEC;
        }
    }
}
