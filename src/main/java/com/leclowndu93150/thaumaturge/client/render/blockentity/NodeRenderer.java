package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.items.GogglesAccess;
import com.leclowndu93150.thaumaturge.api.nodes.NodeModifier;
import com.leclowndu93150.thaumaturge.api.nodes.NodeType;
import com.leclowndu93150.thaumaturge.client.casters.WandTipTracker;
import com.leclowndu93150.thaumaturge.client.effect.FloatyLineRenderer;
import com.leclowndu93150.thaumaturge.client.effect.LateWorldRenderQueue;
import com.leclowndu93150.thaumaturge.client.effect.rendertype.TTFXRenderTypes;
import com.leclowndu93150.thaumaturge.compat.iris.IrisCompat;
import com.leclowndu93150.thaumaturge.content.aura.node.BlockEntityNode;
import com.leclowndu93150.thaumaturge.content.item.ThaumometerItem;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.jspecify.annotations.Nullable;

public final class NodeRenderer implements BlockEntityRenderer<BlockEntityNode, NodeRenderState> {
    @FunctionalInterface
    public interface LayerSink {
        void layer(int index, RenderType type, float angle, float halfSize, float alpha, int rgb, int row, int column);
    }

    private static final Identifier AURA_TEXTURE = TTIds.rl("textures/misc/auranodes.png");
    private static final Map<NodeType, CoreStyle> CORE_STYLES = coreStyles();
    private static final int ATLAS_CELLS = 32;
    private static final int TRANSLUCENT_BLEND = 771;
    private static final int ASPECT_ROW = 0;
    private static final int ENERGIZED_ROW = 6;
    private static final int FALLBACK_ROW = 7;
    private static final int WHITE_RGB = 0xFFFFFF;
    private static final int ENERGIZED_RGB = 0x99CCFF;
    private static final int FALLBACK_SHADER_RGB = 0x666666;
    private static final float FALLBACK_ALPHA = 0.0066F;
    private static final float FALLBACK_HALF = 0.5F;
    private static final float FALLBACK_SHADER_ALPHA = 1.0F;
    private static final float CENTER = 0.5F;
    private static final float JAR_CENTER_Y = 0.4F;
    private static final float JARRED_VIEW = 64.0F;
    private static final float GOGGLES_VIEW = 64.0F;
    private static final float THAUMOMETER_VIEW = 48.0F;
    private static final float JARRED_SIZE = 0.7F;
    private static final float BRIGHT_FACTOR = 1.5F;
    private static final float PALE_FACTOR = 0.66F;
    private static final float FADING_AMPLITUDE = 0.25F;
    private static final float FADING_BASE = 0.33F;
    private static final float FADING_PERIOD = 3.0F;
    private static final float FRAME_RATE = 1.25F;
    private static final float CLOCK_RATE = 10.0F;
    private static final float ENERGIZED_CLOCK_FACTOR = 2.0F;
    private static final float ENERGIZED_CONTRACTION = 0.6F;
    private static final float DISPLAY_CAP = 50.0F;
    private static final float ASPECT_BASE_HALF = 0.2F;
    private static final float ASPECT_PULSE_AMPLITUDE = 0.25F;
    private static final float ASPECT_PULSE_BASE = 0.5F;
    private static final float PULSE_PERIOD = 14.0F;
    private static final float ROTATION_PERIOD_BASE = 5000.0F;
    private static final float ROTATION_PERIOD_STEP = 500.0F;
    private static final float TRANSLUCENT_BOOST = 1.5F;
    private static final float CORE_BASE_HALF = 0.1F;
    private static final float CORE_AMOUNT_DIVISOR = 150.0F;
    private static final float ENERGIZED_PULSE_AMPLITUDE = 0.25F;
    private static final float ENERGIZED_PULSE_PERIOD = 4.0F;
    private static final float ENERGIZED_PULSE_BASE = 1.35F;
    private static final float HIDDEN_BRIGHTNESS = 0.10F;
    private static final float DEPTHLESS_BRIGHTNESS = 0.5F;
    private static final float BYTE_RANGE = 255.0F;
    private static final double BOUNDS_MARGIN = 2.2627;
    private static final float LINE_SPEED = -0.02F;
    private static final float LINE_WIDTH = 0.15F;
    private static final float LINE_RAMP_TICKS = 10.0F;
    private static final float WAVE_AMPLITUDE = 10.0F;
    private static final float WAVE_PERIOD = 10.0F;
    private static final float YAW_WOBBLE = 0.01F;
    private static final float PITCH_WOBBLE = 0.015F;
    private static final float HAND_X = -0.1F;
    private static final float HAND_Y = -0.1F;
    private static final float HAND_Z = 0.5F;
    private static final int SMOOTHING_WEIGHT = 4;
    private static final int SMOOTHING_DIVISOR = 5;
    private static final int CHANNEL_MASK = 0xFF;
    private static final int RED_SHIFT = 16;
    private static final int GREEN_SHIFT = 8;

    public NodeRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(BlockEntityNode node) {
        return new AABB(node.getBlockPos()).inflate(BOUNDS_MARGIN);
    }

    @Override
    public NodeRenderState createRenderState() {
        return new NodeRenderState();
    }

    @Override
    public void extractRenderState(BlockEntityNode node, NodeRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(node, state, partialTicks, cameraPosition, breakProgress);
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        Level level = node.getLevel();
        state.jarred = node.getType() == TTBlockEntities.JAR_NODE.get();
        state.type = node.kind();
        state.modifier = node.trait();
        state.energized = node.isEnergized();
        state.shaderPack = IrisCompat.shaderPackInUse();
        state.frameSeed = node.getBlockPos().getX();
        state.time = level == null ? 0L : level.getGameTime();
        state.visible = false;
        state.depthIgnore = false;
        state.alpha = 0.0F;
        state.size = 1.0F;
        state.draining = false;
        state.ticks = 0.0F;
        syncLayers(node, state);
        if (player == null) {
            return;
        }
        state.ticks = player.tickCount + partialTicks;
        applyVisibility(node, state, player);
        extractDrain(node, state, level, minecraft, partialTicks);
    }

    private static void syncLayers(BlockEntityNode node, NodeRenderState state) {
        List<AspectInstance> entries = node.getAspects().entries();
        while (state.layers.size() > entries.size()) {
            state.layers.remove(state.layers.size() - 1);
        }
        for (int index = 0; index < entries.size(); index++) {
            if (index >= state.layers.size()) {
                state.layers.add(new NodeRenderState.AspectLayer());
            }
            NodeRenderState.AspectLayer layer = state.layers.get(index);
            AspectInstance entry = entries.get(index);
            layer.color = entry.aspect().value().color();
            layer.amount = entry.amount();
            layer.blend = entry.aspect().value().blend();
        }
    }

    private static void applyVisibility(BlockEntityNode node, NodeRenderState state, LocalPlayer player) {
        float view;
        if (state.jarred) {
            state.visible = true;
            state.size = JARRED_SIZE;
            view = JARRED_VIEW;
        } else if (GogglesAccess.wearsRevealingGear(player)) {
            state.visible = true;
            state.depthIgnore = true;
            view = GOGGLES_VIEW;
        } else if (player.getMainHandItem().getItem() instanceof ThaumometerItem || player.getOffhandItem().getItem() instanceof ThaumometerItem) {
            state.visible = true;
            state.depthIgnore = true;
            view = THAUMOMETER_VIEW;
        } else {
            return;
        }
        double distance = player.position().distanceTo(Vec3.atCenterOf(node.getBlockPos()));
        if (distance > view) {
            state.visible = false;
            state.depthIgnore = false;
            return;
        }
        float base = (float) ((view - distance) / view);
        state.alpha = Math.min(base * modifierFactor(state.modifier, state.ticks), 1.0F);
    }

    private static float modifierFactor(@Nullable NodeModifier modifier, float time) {
        if (modifier == null) {
            return 1.0F;
        }
        return switch (modifier) {
            case BRIGHT -> BRIGHT_FACTOR;
            case PALE -> PALE_FACTOR;
            case FADING -> FADING_AMPLITUDE * Mth.sin(time / FADING_PERIOD) + FADING_BASE;
        };
    }

    private static void extractDrain(BlockEntityNode node, NodeRenderState state, @Nullable Level level, Minecraft minecraft, float partialTicks) {
        UUID drainId = node.getDrainPlayer();
        if (drainId == null || level == null) {
            return;
        }
        Player drainer = level.getPlayerByUUID(drainId);
        if (drainer == null || !drainer.isUsingItem()) {
            return;
        }
        float useTime = drainer.getTicksUsingItem() + partialTicks;
        float wave = WAVE_AMPLITUDE * Mth.sin(useTime / WAVE_PERIOD);
        float pitch = Mth.lerp(partialTicks, drainer.xRotO, drainer.getXRot()) * Mth.DEG_TO_RAD;
        float yaw = Mth.lerp(partialTicks, drainer.yRotO, drainer.getYRot()) * Mth.DEG_TO_RAD;
        Vec3 offset = new Vec3(HAND_X, HAND_Y, HAND_Z).xRot(-pitch).yRot(-(yaw + YAW_WOBBLE * wave)).xRot(-PITCH_WOBBLE * wave);
        Vec3 hand = new Vec3(Mth.lerp((double) partialTicks, drainer.xo, drainer.getX()), Mth.lerp((double) partialTicks, drainer.yo, drainer.getY()) + drainer.getEyeHeight(),
                Mth.lerp((double) partialTicks, drainer.zo, drainer.getZ())).add(offset);
        if (drainer == minecraft.player && minecraft.options.getCameraType().isFirstPerson()) {
            Vec3 tip = WandTipTracker.firstPersonTip();
            if (tip != null) {
                hand = tip;
            }
        }
        Vec3 from = hand.subtract(Vec3.atCenterOf(node.getBlockPos()));
        state.draining = true;
        state.drainFromX = from.x;
        state.drainFromY = from.y;
        state.drainFromZ = from.z;
        state.drainTime = useTime;
        state.drainColor = smoothDrainColor(node);
    }

    private static int smoothDrainColor(BlockEntityNode node) {
        int target = node.getDrainColor();
        node.clientDrainRed = (((target >> RED_SHIFT) & CHANNEL_MASK) + SMOOTHING_WEIGHT * node.clientDrainRed) / SMOOTHING_DIVISOR;
        node.clientDrainGreen = (((target >> GREEN_SHIFT) & CHANNEL_MASK) + SMOOTHING_WEIGHT * node.clientDrainGreen) / SMOOTHING_DIVISOR;
        node.clientDrainBlue = ((target & CHANNEL_MASK) + SMOOTHING_WEIGHT * node.clientDrainBlue) / SMOOTHING_DIVISOR;
        return (node.clientDrainRed << RED_SHIFT) | (node.clientDrainGreen << GREEN_SHIFT) | node.clientDrainBlue;
    }

    @Override
    public void submit(NodeRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        Quaternionf orientation = new Quaternionf(camera.orientation);
        Vec3 center = Vec3.atCenterOf(state.blockPos);
        Vec3 from = new Vec3(state.drainFromX, state.drainFromY, state.drainFromZ);
        boolean draining = state.draining;
        float lineTime = FloatyLineRenderer.time(state.time, state.ticks - (float) Math.floor(state.ticks));
        float lineProgress = Math.min(state.drainTime, LINE_RAMP_TICKS) / LINE_RAMP_TICKS;
        int lineColor = state.drainColor;
        if (state.jarred) {
            poseStack.pushPose();
            poseStack.translate(CENTER, JAR_CENTER_Y, CENTER);
            poseStack.mulPose(orientation);
            submitLayers(state, poseStack, collector, 0);
            poseStack.popPose();
            if (draining) {
                LateWorldRenderQueue.enqueue(center, (late, buffers) -> FloatyLineRenderer.draw(late, buffers, from, lineTime, lineColor, LINE_SPEED, lineProgress, LINE_WIDTH));
            }
            return;
        }
        List<LayerDraw> draws = new ArrayList<>();
        float brightness = brightness(state);
        forEachLayer(state, (index, type, angle, halfSize, alpha, rgb, row, column) -> draws.add(new LayerDraw(type, angle, halfSize, tint(alpha, rgb, brightness), row, column)));
        LateWorldRenderQueue.enqueue(center, (late, buffers) -> {
            if (draining) {
                FloatyLineRenderer.draw(late, buffers, from, lineTime, lineColor, LINE_SPEED, lineProgress, LINE_WIDTH);
            }
            late.pushPose();
            late.mulPose(orientation);
            for (LayerDraw draw : draws) {
                late.pushPose();
                late.mulPose(Axis.ZP.rotation(draw.angle()));
                emitQuad(late.last().pose(), buffers.getBuffer(draw.type()), draw.halfSize(), draw.tint(), draw.row(), draw.column());
                late.popPose();
            }
            late.popPose();
        });
    }

    public static void submitLayers(NodeRenderState state, PoseStack poseStack, SubmitNodeCollector collector, int orderBase) {
        float brightness = brightness(state);
        forEachLayer(state, (index, type, angle, halfSize, alpha, rgb, row, column) -> {
            int tint = tint(alpha, rgb, brightness);
            poseStack.pushPose();
            poseStack.mulPose(Axis.ZP.rotation(angle));
            collector.order(orderBase + index).submitCustomGeometry(poseStack, type, (pose, buffer) -> emitQuad(pose.pose(), buffer, halfSize, tint, row, column));
            poseStack.popPose();
        });
    }

    public static void forEachLayer(NodeRenderState state, LayerSink sink) {
        float time = state.ticks;
        int count = state.layers.size();
        int column = Math.floorMod((int) Math.floor(time * FRAME_RATE + state.frameSeed), ATLAS_CELLS);
        if (!state.visible || count == 0) {
            sink.layer(0, additive(state), 0.0F, FALLBACK_HALF, state.shaderPack ? FALLBACK_SHADER_ALPHA : FALLBACK_ALPHA, state.shaderPack ? FALLBACK_SHADER_RGB : WHITE_RGB, FALLBACK_ROW, column);
            return;
        }
        float clock = time * CLOCK_RATE * (state.energized ? ENERGIZED_CLOCK_FACTOR : 1.0F);
        float contraction = state.energized ? ENERGIZED_CONTRACTION : 1.0F;
        float layerAlpha = state.alpha / Math.max(1.0F, count / 2.0F);
        float displaySum = 0.0F;
        float lastAngle = 0.0F;
        for (int index = 0; index < count; index++) {
            NodeRenderState.AspectLayer layer = state.layers.get(index);
            float display = Math.min(layer.amount, DISPLAY_CAP);
            float divisor = PULSE_PERIOD - index;
            float pulse = divisor == 0.0F ? 0.0F : Mth.sin(time / divisor);
            float halfSize = (ASPECT_BASE_HALF + (ASPECT_PULSE_AMPLITUDE * pulse + ASPECT_PULSE_BASE) * display / DISPLAY_CAP) * state.size * contraction;
            float period = ROTATION_PERIOD_BASE + ROTATION_PERIOD_STEP * index;
            lastAngle = clock % period / period * Mth.TWO_PI;
            boolean translucent = layer.blend == TRANSLUCENT_BLEND;
            sink.layer(index, translucent ? translucent(state) : additive(state), lastAngle, halfSize, translucent ? layerAlpha * TRANSLUCENT_BOOST : layerAlpha, layer.color, ASPECT_ROW, column);
            displaySum += display;
        }
        CoreStyle style = CORE_STYLES.get(state.type);
        float coreHalf = (CORE_BASE_HALF + displaySum / count / CORE_AMOUNT_DIVISOR) * state.size * style.scale();
        float coreAngle = style.spins() ? lastAngle : 0.0F;
        sink.layer(count, style.translucent() ? translucent(state) : additive(state), coreAngle, coreHalf, state.alpha, WHITE_RGB, style.row(), column);
        if (state.energized) {
            float energizedHalf = coreHalf * (ENERGIZED_PULSE_AMPLITUDE * Mth.sin(time / ENERGIZED_PULSE_PERIOD) + ENERGIZED_PULSE_BASE);
            sink.layer(count + 1, additive(state), -coreAngle, energizedHalf, state.alpha, ENERGIZED_RGB, ENERGIZED_ROW, column);
        }
    }

    public static void emitQuad(Matrix4fc matrix, VertexConsumer consumer, float halfSize, int tint, int row, int column) {
        float u0 = (float) column / ATLAS_CELLS;
        float u1 = (float) (column + 1) / ATLAS_CELLS;
        float v0 = (float) row / ATLAS_CELLS;
        float v1 = (float) (row + 1) / ATLAS_CELLS;
        vertex(matrix, consumer, -halfSize, -halfSize, u1, v1, tint);
        vertex(matrix, consumer, -halfSize, halfSize, u1, v0, tint);
        vertex(matrix, consumer, halfSize, halfSize, u0, v0, tint);
        vertex(matrix, consumer, halfSize, -halfSize, u0, v1, tint);
    }

    private static void vertex(Matrix4fc matrix, VertexConsumer consumer, float x, float y, float u, float v, int tint) {
        consumer.addVertex(matrix, x, y, 0.0F).setUv(u, v).setColor(tint).setLight(LightCoordsUtil.FULL_BRIGHT);
    }

    private static float brightness(NodeRenderState state) {
        if (!state.visible) {
            return HIDDEN_BRIGHTNESS;
        }
        return state.depthIgnore ? DEPTHLESS_BRIGHTNESS : 1.0F;
    }

    private static int tint(float alpha, int rgb, float brightness) {
        int alphaByte = (int) (Mth.clamp(alpha, 0.0F, 1.0F) * BYTE_RANGE);
        return ARGB.color(alphaByte, (int) (ARGB.red(rgb) * brightness), (int) (ARGB.green(rgb) * brightness), (int) (ARGB.blue(rgb) * brightness));
    }

    private static RenderType additive(NodeRenderState state) {
        return state.depthIgnore ? TTFXRenderTypes.additiveNoDepth(AURA_TEXTURE) : TTFXRenderTypes.additive(AURA_TEXTURE);
    }

    private static RenderType translucent(NodeRenderState state) {
        return state.depthIgnore ? TTFXRenderTypes.translucentNoDepth(AURA_TEXTURE) : TTFXRenderTypes.translucent(AURA_TEXTURE);
    }

    private static Map<NodeType, CoreStyle> coreStyles() {
        Map<NodeType, CoreStyle> styles = new EnumMap<>(NodeType.class);
        styles.put(NodeType.NORMAL, new CoreStyle(7, false, 1.0F, true));
        styles.put(NodeType.UNSTABLE, new CoreStyle(6, false, 1.0F, false));
        styles.put(NodeType.DARK, new CoreStyle(2, true, 1.0F, true));
        styles.put(NodeType.TAINTED, new CoreStyle(5, true, 1.0F, true));
        styles.put(NodeType.PURE, new CoreStyle(9, false, 1.0F, true));
        styles.put(NodeType.HUNGRY, new CoreStyle(8, false, 0.75F, true));
        return styles;
    }

    private record CoreStyle(int row, boolean translucent, float scale, boolean spins) {
    }

    private record LayerDraw(RenderType type, float angle, float halfSize, int tint, int row, int column) {
    }
}
