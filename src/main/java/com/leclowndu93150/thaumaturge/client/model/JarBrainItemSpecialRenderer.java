package com.leclowndu93150.thaumaturge.client.model;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.entity.TTModelLayers;
import com.leclowndu93150.thaumaturge.client.model.entity.BrainModel;
import com.leclowndu93150.thaumaturge.client.model.entity.JarBrineModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import java.util.function.Consumer;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public final class JarBrainItemSpecialRenderer implements NoDataSpecialModelRenderer {
    private static final Identifier BRAIN_TEXTURE = TTIds.rl("textures/entity/brain2.png");
    private static final Identifier BRINE_TEXTURE = TTIds.rl("textures/entity/jarbrine.png");
    private static final float CENTER = 0.5F;
    private static final float FRAME_Y = 0.01F;
    private static final float FLIP_DEGREES = 180.0F;
    private static final float BRAIN_Y = -0.77F;
    private static final float BRAIN_YAW_DEGREES = -90.0F;
    private static final float BRAIN_SCALE = 0.4F;
    private static final int BRINE_ORDER = 1;
    private static final float[][] EXTENT_CORNERS = {{0.1875F, 0.0625F, 0.1875F}, {0.8125F, 0.6875F, 0.8125F}};

    private final BrainModel brain;
    private final JarBrineModel brine;

    public JarBrainItemSpecialRenderer(BrainModel brain, JarBrineModel brine) {
        this.brain = brain;
        this.brine = brine;
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        poseStack.pushPose();
        poseStack.translate(CENTER, FRAME_Y, CENTER);
        poseStack.mulPose(Axis.XP.rotationDegrees(FLIP_DEGREES));
        submitBrain(poseStack, collector, lightCoords);
        submitBrine(poseStack, collector, lightCoords);
        poseStack.popPose();
    }

    private void submitBrain(PoseStack poseStack, SubmitNodeCollector collector, int light) {
        poseStack.pushPose();
        poseStack.translate(0.0F, BRAIN_Y, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(BRAIN_YAW_DEGREES));
        poseStack.scale(BRAIN_SCALE, BRAIN_SCALE, BRAIN_SCALE);
        RenderType type = RenderTypes.entityCutout(BRAIN_TEXTURE);
        collector.submitModelPart(brain.root(), poseStack, type, light, OverlayTexture.NO_OVERLAY, null);
        poseStack.popPose();
    }

    private void submitBrine(PoseStack poseStack, SubmitNodeCollector collector, int light) {
        RenderType type = RenderTypes.entityTranslucent(BRINE_TEXTURE);
        OrderedSubmitNodeCollector ordered = collector.order(BRINE_ORDER);
        ordered.submitModelPart(brine.root, poseStack, type, light, OverlayTexture.NO_OVERLAY, null);
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
            EntityModelSet models = context.entityModelSet();
            BrainModel brainModel = new BrainModel(models.bakeLayer(TTModelLayers.BRAIN));
            JarBrineModel brineModel = new JarBrineModel(models.bakeLayer(TTModelLayers.JAR_BRINE));
            return new JarBrainItemSpecialRenderer(brainModel, brineModel);
        }

        @Override
        public MapCodec<? extends NoDataSpecialModelRenderer.Unbaked> type() {
            return MAP_CODEC;
        }
    }
}
