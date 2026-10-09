package com.leclowndu93150.thaumaturge.client.model;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.nodes.NodeModifier;
import com.leclowndu93150.thaumaturge.client.render.blockentity.NodeRenderState;
import com.leclowndu93150.thaumaturge.client.render.blockentity.NodeRenderer;
import com.leclowndu93150.thaumaturge.content.aura.node.NodeData;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import java.util.function.Consumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

public final class JarNodeItemSpecialRenderer implements SpecialModelRenderer<NodeData> {
    private static final float CENTER_X = 0.5F;
    private static final float CENTER_Y = 0.4F;
    private static final float CENTER_Z = 0.5F;
    private static final float PLANE_TURN_DEGREES = 90.0F;
    private static final int ORDER_BASE_A = 0;
    private static final int ORDER_BASE_B = 64;
    private static final int ORDER_BASE_C = 128;
    private static final long CLOCK_MODULUS = 1_000_000L;
    private static final float MILLIS_PER_TICK = 50.0F;
    private static final float BASE_ALPHA = 0.5F;
    private static final float BRIGHT_FACTOR = 1.5F;
    private static final float PALE_FACTOR = 0.66F;
    private static final float FADING_BASE = 0.33F;
    private static final float FADING_AMPLITUDE = 0.25F;
    private static final float FADING_PERIOD = 3.0F;
    private static final int FRAME_SEED = 1;
    private static final float EXTENT_MIN_XZ = 0.1875F;
    private static final float EXTENT_MIN_Y = 0.0625F;
    private static final float EXTENT_MAX_XZ = 0.8125F;
    private static final float EXTENT_MAX_Y = 0.6875F;

    @Override
    public @Nullable NodeData extractArgument(ItemStack stack) {
        return stack.get(TTDataComponents.NODE_DATA.get());
    }

    @Override
    public void submit(@Nullable NodeData data, PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        if (data == null || data.aspects().isEmpty()) {
            return;
        }
        NodeRenderState state = buildState(data);
        poseStack.pushPose();
        poseStack.translate(CENTER_X, CENTER_Y, CENTER_Z);
        NodeRenderer.submitLayers(state, poseStack, collector, ORDER_BASE_A);
        poseStack.mulPose(Axis.YP.rotationDegrees(PLANE_TURN_DEGREES));
        NodeRenderer.submitLayers(state, poseStack, collector, ORDER_BASE_B);
        poseStack.mulPose(Axis.XP.rotationDegrees(PLANE_TURN_DEGREES));
        NodeRenderer.submitLayers(state, poseStack, collector, ORDER_BASE_C);
        poseStack.popPose();
    }

    private static NodeRenderState buildState(NodeData data) {
        NodeRenderState state = new NodeRenderState();
        state.type = data.type();
        state.modifier = data.modifier().orElse(null);
        state.visible = true;
        state.size = 1.0F;
        state.frameSeed = FRAME_SEED;
        state.ticks = (Util.getMillis() % CLOCK_MODULUS) / MILLIS_PER_TICK;
        state.alpha = Math.min(1.0F, BASE_ALPHA * modifierFactor(state.modifier, state.ticks));
        for (AspectInstance entry : data.aspects().entries()) {
            IAspect aspect = entry.aspect().value();
            NodeRenderState.AspectLayer layer = new NodeRenderState.AspectLayer();
            layer.color = aspect.color();
            layer.amount = entry.amount();
            layer.blend = aspect.blend();
            state.layers.add(layer);
        }
        return state;
    }

    private static float modifierFactor(@Nullable NodeModifier modifier, float ticks) {
        if (modifier == null) {
            return 1.0F;
        }
        return switch (modifier) {
            case BRIGHT -> BRIGHT_FACTOR;
            case PALE -> PALE_FACTOR;
            case FADING -> FADING_BASE + FADING_AMPLITUDE * Mth.sin(ticks / FADING_PERIOD);
        };
    }

    @Override
    public void getExtents(Consumer<Vector3fc> consumer) {
        consumer.accept(new Vector3f(EXTENT_MIN_XZ, EXTENT_MIN_Y, EXTENT_MIN_XZ));
        consumer.accept(new Vector3f(EXTENT_MAX_XZ, EXTENT_MAX_Y, EXTENT_MAX_XZ));
    }

    public record Unbaked() implements SpecialModelRenderer.Unbaked<NodeData> {
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(Unbaked::new);

        @Override
        public SpecialModelRenderer<NodeData> bake(SpecialModelRenderer.BakingContext context) {
            return new JarNodeItemSpecialRenderer();
        }

        @Override
        public MapCodec<? extends SpecialModelRenderer.Unbaked<NodeData>> type() {
            return MAP_CODEC;
        }
    }
}
