package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.client.effect.rendertype.TTFXRenderTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.GraphicsPreset;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

public final class SpecialItemRenderer extends ItemEntityRenderer {
    private static final int FANCY_CONES = 10;
    private static final int FAST_CONES = 5;
    private static final long CONE_SEED = 187L;
    private static final int VALUES_PER_CONE = 8;
    private static final int ANGLE_VALUES = 6;
    private static final int LENGTH_VALUE = 6;
    private static final int WIDTH_VALUE = 7;
    private static final float[] CONE_RANDOM = buildConeRandom();
    private static final float GROW_TICKS = 10.0F;
    private static final float SPIN_TICKS_PER_TURN = 500.0F;
    private static final float FULL_TURN = 360.0F;
    private static final float CONE_LIFT = 0.25F;
    private static final float ITEM_BOB_RATE_DIVISOR = 10.0F;
    private static final float ITEM_BOB_HEIGHT = 0.1F;
    private static final float CONE_LENGTH_RANGE = 20.0F;
    private static final float CONE_LENGTH_BASE = 5.0F;
    private static final float CONE_LENGTH_DIVISOR = 30.0F;
    private static final float CONE_WIDTH_RANGE = 2.0F;
    private static final float CONE_WIDTH_BASE = 1.0F;
    private static final float CONE_WIDTH_DIVISOR = 30.0F;
    private static final float CORNER_X = 0.866F;
    private static final float CORNER_Z = 0.5F;
    private static final int APEX_COLOR = ARGB.color(255, 255, 255, 255);
    private static final int BASE_COLOR = ARGB.color(0, 255, 0, 255);

    public SpecialItemRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ItemEntityRenderState createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(ItemEntity entity, ItemEntityRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        if (state instanceof State special) {
            special.fast = Minecraft.getInstance().options.graphicsPreset().get() == GraphicsPreset.FAST;
        }
    }

    @Override
    public void submit(ItemEntityRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (!state.item.isEmpty()) {
            float age = state.ageInTicks;
            float bob = state.shouldBob ? Mth.sin(age / ITEM_BOB_RATE_DIVISOR + state.bobOffset) * ITEM_BOB_HEIGHT + ITEM_BOB_HEIGHT : 0.0F;
            int cones = state instanceof State special && special.fast ? FAST_CONES : FANCY_CONES;
            float grow = Math.min(age, GROW_TICKS) / GROW_TICKS;
            poseStack.pushPose();
            poseStack.translate(0.0F, bob + CONE_LIFT, 0.0F);
            collector.submitCustomGeometry(poseStack, TTFXRenderTypes.SPARKLE, (pose, buffer) -> writeCones(buffer, pose.pose(), cones, grow, age));
            poseStack.popPose();
        }
        super.submit(state, poseStack, collector, camera);
    }

    private static void writeCones(VertexConsumer buffer, Matrix4fc base, int cones, float grow, float age) {
        Matrix4f matrix = new Matrix4f(base);
        float spin = age / SPIN_TICKS_PER_TURN * FULL_TURN;
        for (int cone = 0; cone < cones; cone++) {
            int at = cone * VALUES_PER_CONE;
            matrix.rotateX(angle(at))
                .rotateY(angle(at + 1))
                .rotateZ(angle(at + 2))
                .rotateX(angle(at + 3))
                .rotateY(angle(at + 4))
                .rotateZ((CONE_RANDOM[at + ANGLE_VALUES - 1] * FULL_TURN + spin) * Mth.DEG_TO_RAD);
            float height = (CONE_RANDOM[at + LENGTH_VALUE] * CONE_LENGTH_RANGE + CONE_LENGTH_BASE) / CONE_LENGTH_DIVISOR * grow;
            float size = (CONE_RANDOM[at + WIDTH_VALUE] * CONE_WIDTH_RANGE + CONE_WIDTH_BASE) / CONE_WIDTH_DIVISOR * grow;
            float cornerX = CORNER_X * size;
            float cornerZ = CORNER_Z * size;
            writeSide(buffer, matrix, -cornerX, -cornerZ, cornerX, -cornerZ, height);
            writeSide(buffer, matrix, cornerX, -cornerZ, 0.0F, size, height);
            writeSide(buffer, matrix, 0.0F, size, -cornerX, -cornerZ, height);
        }
    }

    private static float angle(int index) {
        return CONE_RANDOM[index] * FULL_TURN * Mth.DEG_TO_RAD;
    }

    private static void writeSide(VertexConsumer buffer, Matrix4fc matrix, float firstX, float firstZ, float secondX, float secondZ, float height) {
        buffer.addVertex(matrix, 0.0F, 0.0F, 0.0F).setColor(APEX_COLOR);
        buffer.addVertex(matrix, firstX, height, firstZ).setColor(BASE_COLOR);
        buffer.addVertex(matrix, secondX, height, secondZ).setColor(BASE_COLOR);
    }

    private static float[] buildConeRandom() {
        RandomSource random = RandomSource.create(CONE_SEED);
        float[] values = new float[FANCY_CONES * VALUES_PER_CONE];
        for (int i = 0; i < values.length; i++) {
            values[i] = random.nextFloat();
        }
        return values;
    }

    public static final class State extends ItemEntityRenderState {
        public boolean fast;
    }
}
