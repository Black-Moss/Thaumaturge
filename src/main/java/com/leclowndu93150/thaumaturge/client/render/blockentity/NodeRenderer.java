package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.items.GogglesAccess;
import com.leclowndu93150.thaumaturge.api.nodes.NodeModifier;
import com.leclowndu93150.thaumaturge.api.nodes.NodeType;
import com.leclowndu93150.thaumaturge.client.effect.FloatyLineRenderer;
import com.leclowndu93150.thaumaturge.client.effect.LateWorldRenderQueue;
import com.leclowndu93150.thaumaturge.client.effect.rendertype.TTFXRenderTypes;
import com.leclowndu93150.thaumaturge.compat.iris.IrisCompat;
import com.leclowndu93150.thaumaturge.content.aspect.Aspect;
import com.leclowndu93150.thaumaturge.content.aura.node.BlockEntityJarNode;
import com.leclowndu93150.thaumaturge.content.aura.node.BlockEntityNode;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.EnumMap;
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
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4fc;
import org.jspecify.annotations.Nullable;

public final class NodeRenderer implements BlockEntityRenderer<BlockEntityNode, NodeRenderState> {
    public interface LayerSink {
        void layer(int index, RenderType type, float angle, float halfSize, float alpha, int rgb, int row, int column);
    }

    public static final float JARRED_SIZE = 0.26F;

    private static final Identifier SHEET = TTIds.rl("textures/misc/auranodes.png");
    private static final int SHEET_GRID = 32;
    private static final float FRAMES_PER_TICK = 1.2F;
    private static final int WHITE_RGB = 0xFFFFFF;

    private static final float CENTER = 0.5F;
    private static final float JAR_CENTER_Y = 0.36F;
    private static final double REVEAL_RANGE = 64.0;
    private static final double THAUMOMETER_RANGE = 48.0;
    private static final double BOUNDS_REACH = 2.0;

    private static final int LAYER_ROW = 0;
    private static final float LAYER_FULL_AMOUNT = 36.0F;
    private static final float LAYER_MIN_HALF = 0.32F;
    private static final float LAYER_MAX_HALF = 1.8F;
    private static final float LAYER_OPACITY = 0.7F;
    private static final float LAYER_SHARE_FALLOFF = 0.55F;
    private static final float CONTRAST_BOOST = 1.5F;
    private static final float BREATH_AMPLITUDE = 0.06F;
    private static final float BREATH_PERIOD = 90.0F;
    private static final float BREATH_PERIOD_STEP = 13.0F;
    private static final float LAYER_TURN_PERIOD = 320.0F;
    private static final float LAYER_TURN_STEP = 75.0F;

    private static final float CORE_MIN_HALF = 0.28F;
    private static final float CORE_MAX_HALF = 0.62F;
    private static final float CORE_FULL_AMOUNT = 60.0F;
    private static final float CORE_OPACITY = 0.85F;
    private static final float CORE_TURN_PERIOD = 240.0F;
    private static final int CORE_ROW_NORMAL = 0;
    private static final int CORE_ROW_PURE = 1;
    private static final int CORE_ROW_DARK = 2;
    private static final int CORE_ROW_TAINTED = 5;
    private static final int CORE_ROW_UNSTABLE = 6;
    private static final int CORE_ROW_HUNGRY = 9;
    private static final float CORE_SCALE_PLAIN = 1.0F;
    private static final float CORE_SCALE_UNSTABLE = 1.15F;
    private static final float CORE_SCALE_PURE = 0.9F;
    private static final float CORE_SCALE_HUNGRY = 0.55F;
    private static final Map<NodeType, CoreStyle> CORE_STYLES = coreStyles();

    private static final float ENERGIZED_SPIN = 3.0F;
    private static final float ENERGIZED_CONTRACTION = 0.7F;
    private static final int RING_ROW = 8;
    private static final int RING_RGB = 0xA6DBFF;
    private static final float RING_SCALE = 1.55F;
    private static final float RING_OPACITY = 0.75F;
    private static final float RING_PULSE = 0.22F;
    private static final float RING_PULSE_PERIOD = 32.0F;
    private static final float RING_TURN_PERIOD = 150.0F;

    private static final int SHIMMER_ROW = 7;
    private static final float SHIMMER_HALF = 0.22F;
    private static final float SHIMMER_OPACITY = 0.1F;

    private static final float BRIGHT_FACTOR = 1.4F;
    private static final float PALE_FACTOR = 0.55F;
    private static final float FADING_HIGH = 0.5F;
    private static final float FADING_LOW = 0.22F;
    private static final float FADING_PERIOD = 9.0F;

    private static final float THROUGH_WALL_SHARE = 0.4F;
    private static final int ORDER_DIRECT = 0;
    private static final int ORDER_THROUGH = 64;

    private static final float DRAIN_FADE_TICKS = 10.0F;
    private static final float DRAIN_COLOR_BLEND = 0.25F;
    private static final float DRAIN_LINE_SPEED = -0.04F;
    private static final float DRAIN_LINE_FRACTION = 1.0F;
    private static final float DRAIN_LINE_HALF_WIDTH = 0.12F;
    private static final double FIRST_PERSON_REACH = 0.6;
    private static final double FIRST_PERSON_SIDE = 0.32;
    private static final double FIRST_PERSON_DROP = 0.28;
    private static final double HAND_BELOW_EYE = 0.62;
    private static final double HAND_SIDE = 0.36;
    private static final double HAND_REACH = 0.48;
    private static final double SWAY = 0.06;
    private static final float SWAY_PERIOD_HORIZONTAL = 23.0F;
    private static final float SWAY_PERIOD_VERTICAL = 31.0F;
    private static final double SWAY_VERTICAL_SHARE = 0.5;

    public NodeRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public NodeRenderState createRenderState() {
        return new NodeRenderState();
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(BlockEntityNode node) {
        AABB bounds = new AABB(node.getBlockPos()).inflate(BOUNDS_REACH);
        Player drainer = drainer(node);
        return drainer == null ? bounds : bounds.minmax(drainer.getBoundingBox());
    }

    @Override
    public void extractRenderState(BlockEntityNode node, NodeRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(node, state, partialTicks, cameraPosition, breakProgress);
        Level level = node.getLevel();
        BlockPos pos = node.getBlockPos();
        state.time = level == null ? 0L : level.getGameTime();
        state.ticks = FloatyLineRenderer.time(state.time, partialTicks);
        state.frameSeed = (int) Math.floorMod(Mth.getSeed(pos), (long) SHEET_GRID);
        state.type = node.kind();
        state.modifier = node.trait();
        state.energized = node.isEnergized();
        state.jarred = node instanceof BlockEntityJarNode;
        state.size = state.jarred ? JARRED_SIZE : 1.0F;
        state.shaderPack = IrisCompat.shaderPackInUse();
        fillLayers(state, node.getAspects());
        applySight(state, Minecraft.getInstance().player, cameraPosition.distanceTo(Vec3.atCenterOf(pos)));
        extractDrain(node, state, partialTicks);
    }

    private static void applySight(NodeRenderState state, @Nullable LocalPlayer viewer, double distance) {
        double range = REVEAL_RANGE;
        if (state.jarred) {
            state.visible = true;
            state.depthIgnore = false;
        } else if (GogglesAccess.wearsRevealingGear(viewer) && distance <= REVEAL_RANGE) {
            state.visible = true;
            state.depthIgnore = true;
        } else if (holdsThaumometer(viewer) && distance <= THAUMOMETER_RANGE) {
            state.visible = true;
            state.depthIgnore = true;
            range = THAUMOMETER_RANGE;
        } else {
            state.visible = state.energized;
            state.depthIgnore = false;
        }
        float falloff = (float) Math.max(0.0, 1.0 - distance / range);
        state.alpha = falloff * (state.visible ? modifierFactor(state.modifier, state.ticks) : SHIMMER_OPACITY);
    }

    private static boolean holdsThaumometer(@Nullable LocalPlayer viewer) {
        return viewer != null && (viewer.getMainHandItem().is(TTItems.THAUMOMETER) || viewer.getOffhandItem().is(TTItems.THAUMOMETER));
    }

    private static void extractDrain(BlockEntityNode node, NodeRenderState state, float partialTicks) {
        Player drainer = drainer(node);
        state.draining = drainer != null;
        if (drainer == null) {
            return;
        }
        int target = node.getDrainColor();
        node.clientDrainRed = blendChannel(node.clientDrainRed, ARGB.red(target));
        node.clientDrainGreen = blendChannel(node.clientDrainGreen, ARGB.green(target));
        node.clientDrainBlue = blendChannel(node.clientDrainBlue, ARGB.blue(target));
        state.drainColor = ARGB.color(node.clientDrainRed, node.clientDrainGreen, node.clientDrainBlue);
        boolean using = drainer.isUsingItem();
        state.drainTime = using ? drainer.getTicksUsingItem() + partialTicks : DRAIN_FADE_TICKS;
        Vec3 from = wandTip(drainer, partialTicks, using ? state.drainTime : 0.0F);
        state.drainFromX = from.x;
        state.drainFromY = from.y;
        state.drainFromZ = from.z;
    }

    private static @Nullable Player drainer(BlockEntityNode node) {
        UUID id = node.getDrainPlayer();
        Level level = node.getLevel();
        return id == null || level == null ? null : level.getPlayerByUUID(id);
    }

    private static int blendChannel(int current, int target) {
        return current + Math.round((target - current) * DRAIN_COLOR_BLEND);
    }

    private static Vec3 wandTip(Player drainer, float partialTicks, float swayTicks) {
        HumanoidArm arm = drainer.getUsedItemHand() == InteractionHand.MAIN_HAND ? drainer.getMainArm() : drainer.getMainArm().getOpposite();
        double side = arm == HumanoidArm.RIGHT ? 1.0 : -1.0;
        Minecraft mc = Minecraft.getInstance();
        if (drainer == mc.getCameraEntity() && mc.options.getCameraType().isFirstPerson()) {
            Vec3 right = rightOf(drainer.getViewYRot(partialTicks));
            return drainer.getEyePosition(partialTicks).add(drainer.getViewVector(partialTicks).scale(FIRST_PERSON_REACH)).add(right.scale(side * FIRST_PERSON_SIDE)).add(0.0, -FIRST_PERSON_DROP, 0.0);
        }
        float bodyYaw = Mth.rotLerp(partialTicks, drainer.yBodyRotO, drainer.yBodyRot);
        Vec3 right = rightOf(bodyYaw);
        Vec3 forward = new Vec3(right.z, 0.0, -right.x);
        float swing = Mth.TWO_PI * swayTicks;
        Vec3 sway = new Vec3(SWAY * Mth.sin(swing / SWAY_PERIOD_HORIZONTAL), SWAY * SWAY_VERTICAL_SHARE * Mth.sin(swing / SWAY_PERIOD_VERTICAL), SWAY * Mth.cos(swing / SWAY_PERIOD_HORIZONTAL));
        return drainer.getPosition(partialTicks).add(0.0, drainer.getEyeHeight() - HAND_BELOW_EYE, 0.0).add(right.scale(side * HAND_SIDE)).add(forward.scale(HAND_REACH)).add(sway);
    }

    private static Vec3 rightOf(float yawDegrees) {
        float yaw = yawDegrees * Mth.DEG_TO_RAD;
        return new Vec3(-Mth.cos(yaw), 0.0, -Mth.sin(yaw));
    }

    @Override
    public void submit(NodeRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(CENTER, state.jarred ? JAR_CENTER_Y : CENTER, CENTER);
        poseStack.mulPose(camera.orientation);
        if (state.depthIgnore && !state.shaderPack) {
            submitPass(state, false, 1.0F - THROUGH_WALL_SHARE, poseStack, collector, ORDER_DIRECT);
            submitPass(state, true, THROUGH_WALL_SHARE, poseStack, collector, ORDER_THROUGH);
        } else {
            submitPass(state, false, 1.0F, poseStack, collector, ORDER_DIRECT);
        }
        poseStack.popPose();
        if (state.draining) {
            queueDrainBeam(state);
        }
    }

    private static void queueDrainBeam(NodeRenderState state) {
        BlockPos pos = state.blockPos;
        Vec3 origin = new Vec3(state.drainFromX, state.drainFromY, state.drainFromZ);
        Vec3 offset = new Vec3(pos.getX() + CENTER, pos.getY() + (state.jarred ? JAR_CENTER_Y : CENTER), pos.getZ() + CENTER).subtract(origin);
        float fade = Math.clamp(state.drainTime / DRAIN_FADE_TICKS, 0.0F, 1.0F);
        int color = ARGB.scaleRGB(state.drainColor, fade);
        float time = state.ticks;
        LateWorldRenderQueue.enqueue(origin, (late, buffers) -> FloatyLineRenderer.draw(late, buffers, offset, time, color, DRAIN_LINE_SPEED, DRAIN_LINE_FRACTION, DRAIN_LINE_HALF_WIDTH));
    }

    public static void submitLayers(NodeRenderState state, PoseStack poseStack, SubmitNodeCollector collector, int orderBase) {
        submitPass(state, state.depthIgnore, 1.0F, poseStack, collector, orderBase);
    }

    private static void submitPass(NodeRenderState state, boolean throughWalls, float alphaScale, PoseStack poseStack, SubmitNodeCollector collector, int orderBase) {
        forEachLayer(state, throughWalls, alphaScale, (index, type, angle, halfSize, alpha, rgb, row, column) -> {
            int tint = ARGB.color(Math.clamp(alpha, 0.0F, 1.0F), rgb);
            poseStack.pushPose();
            poseStack.mulPose(Axis.ZP.rotation(angle));
            collector.order(orderBase + index).submitCustomGeometry(poseStack, type, (pose, buffer) -> emitQuad(pose.pose(), buffer, halfSize, tint, row, column));
            poseStack.popPose();
        });
    }

    public static void forEachLayer(NodeRenderState state, LayerSink sink) {
        forEachLayer(state, state.depthIgnore, 1.0F, sink);
    }

    private static void forEachLayer(NodeRenderState state, boolean throughWalls, float alphaScale, LayerSink sink) {
        float alpha = state.alpha * alphaScale;
        if (alpha <= 0.0F) {
            return;
        }
        int column = Math.floorMod(state.frameSeed + (int) (state.ticks * FRAMES_PER_TICK), SHEET_GRID);
        RenderType glow = throughWalls ? TTFXRenderTypes.additiveNoDepth(SHEET) : TTFXRenderTypes.additive(SHEET);
        if (!state.visible) {
            sink.layer(0, glow, 0.0F, SHIMMER_HALF * state.size, alpha, WHITE_RGB, SHIMMER_ROW, column);
            return;
        }
        RenderType shade = throughWalls ? TTFXRenderTypes.translucentNoDepth(SHEET) : TTFXRenderTypes.translucent(SHEET);
        float spin = state.energized ? ENERGIZED_SPIN : 1.0F;
        float contraction = state.energized ? ENERGIZED_CONTRACTION : 1.0F;
        int count = state.layers.size();
        float share = LAYER_OPACITY / (1.0F + LAYER_SHARE_FALLOFF * Math.max(0, count - 1));
        int total = 0;
        for (int index = 0; index < count; index++) {
            NodeRenderState.AspectLayer layer = state.layers.get(index);
            total += layer.amount;
            boolean darkens = layer.blend == Aspect.CONTRAST_BLEND;
            float fill = Math.min(layer.amount, LAYER_FULL_AMOUNT) / LAYER_FULL_AMOUNT;
            float breath = 1.0F + BREATH_AMPLITUDE * Mth.sin(Mth.TWO_PI * state.ticks / (BREATH_PERIOD + index * BREATH_PERIOD_STEP));
            float halfSize = Mth.lerp(fill, LAYER_MIN_HALF, LAYER_MAX_HALF) * breath * contraction * state.size;
            float angle = Mth.TWO_PI * state.ticks * spin / (LAYER_TURN_PERIOD + index * LAYER_TURN_STEP);
            float layerAlpha = alpha * share * (darkens ? CONTRAST_BOOST : 1.0F);
            sink.layer(index, darkens ? shade : glow, angle, halfSize, layerAlpha, layer.color, LAYER_ROW, column);
        }
        CoreStyle core = CORE_STYLES.get(state.type);
        float average = count == 0 ? 0.0F : (float) total / count;
        float coreHalf = Mth.lerp(Math.min(average, CORE_FULL_AMOUNT) / CORE_FULL_AMOUNT, CORE_MIN_HALF, CORE_MAX_HALF) * core.scale() * state.size;
        float coreAngle = core.spins() ? -Mth.TWO_PI * state.ticks * spin / CORE_TURN_PERIOD : 0.0F;
        sink.layer(count, core.darkens() ? shade : glow, coreAngle, coreHalf, alpha * CORE_OPACITY, WHITE_RGB, core.row(), column);
        if (state.energized) {
            float pulse = 1.0F + RING_PULSE * Mth.sin(Mth.TWO_PI * state.ticks / RING_PULSE_PERIOD);
            float ringAngle = -Mth.TWO_PI * state.ticks / RING_TURN_PERIOD;
            sink.layer(count + 1, glow, ringAngle, coreHalf * RING_SCALE * pulse, alpha * RING_OPACITY * pulse, RING_RGB, RING_ROW, column);
        }
    }

    public static void emitQuad(Matrix4fc matrix, VertexConsumer consumer, float halfSize, int tint, int row, int column) {
        float cell = 1.0F / SHEET_GRID;
        float u0 = column * cell;
        float v0 = row * cell;
        float u1 = u0 + cell;
        float v1 = v0 + cell;
        consumer.addVertex(matrix, -halfSize, -halfSize, 0.0F).setUv(u0, v1).setColor(tint).setLight(LightCoordsUtil.FULL_BRIGHT);
        consumer.addVertex(matrix, halfSize, -halfSize, 0.0F).setUv(u1, v1).setColor(tint).setLight(LightCoordsUtil.FULL_BRIGHT);
        consumer.addVertex(matrix, halfSize, halfSize, 0.0F).setUv(u1, v0).setColor(tint).setLight(LightCoordsUtil.FULL_BRIGHT);
        consumer.addVertex(matrix, -halfSize, halfSize, 0.0F).setUv(u0, v0).setColor(tint).setLight(LightCoordsUtil.FULL_BRIGHT);
    }

    public static void fillLayers(NodeRenderState state, AspectList aspects) {
        int count = 0;
        for (AspectInstance entry : aspects.entries()) {
            if (entry.amount() <= 0) {
                continue;
            }
            if (count == state.layers.size()) {
                state.layers.add(new NodeRenderState.AspectLayer());
            }
            IAspect aspect = entry.aspect().value();
            NodeRenderState.AspectLayer layer = state.layers.get(count++);
            layer.color = aspect.color();
            layer.amount = entry.amount();
            layer.blend = aspect.blend();
        }
        while (state.layers.size() > count) {
            state.layers.removeLast();
        }
    }

    public static float modifierFactor(@Nullable NodeModifier modifier, float ticks) {
        if (modifier == null) {
            return 1.0F;
        }
        return switch (modifier) {
            case BRIGHT -> BRIGHT_FACTOR;
            case PALE -> PALE_FACTOR;
            case FADING -> Mth.lerp(0.5F + 0.5F * Mth.sin(Mth.TWO_PI * ticks / FADING_PERIOD), FADING_LOW, FADING_HIGH);
        };
    }

    private static Map<NodeType, CoreStyle> coreStyles() {
        Map<NodeType, CoreStyle> styles = new EnumMap<>(NodeType.class);
        styles.put(NodeType.NORMAL, new CoreStyle(CORE_ROW_NORMAL, false, true, CORE_SCALE_PLAIN));
        styles.put(NodeType.UNSTABLE, new CoreStyle(CORE_ROW_UNSTABLE, false, false, CORE_SCALE_UNSTABLE));
        styles.put(NodeType.DARK, new CoreStyle(CORE_ROW_DARK, true, true, CORE_SCALE_PLAIN));
        styles.put(NodeType.TAINTED, new CoreStyle(CORE_ROW_TAINTED, true, true, CORE_SCALE_PLAIN));
        styles.put(NodeType.PURE, new CoreStyle(CORE_ROW_PURE, false, true, CORE_SCALE_PURE));
        styles.put(NodeType.HUNGRY, new CoreStyle(CORE_ROW_HUNGRY, false, true, CORE_SCALE_HUNGRY));
        return styles;
    }

    private record CoreStyle(int row, boolean darkens, boolean spins, float scale) {
    }
}
