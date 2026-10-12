package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.items.GogglesAccess;
import com.leclowndu93150.thaumaturge.api.nodes.NodeModifier;
import com.leclowndu93150.thaumaturge.api.nodes.NodeType;
import com.leclowndu93150.thaumaturge.client.casters.WandTipTracker;
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
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
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

    private static final float JARRED_SIZE = 0.7F;

    private static final Identifier SHEET = TTIds.rl("textures/effect/auranodes.png");
    private static final int SHEET_GRID = 32;
    private static final float FRAMES_PER_TICK = 1.25F;
    private static final int WHITE_RGB = 0xFFFFFF;

    private static final float CENTER = 0.5F;
    private static final float JAR_CENTER_Y = 0.4F;
    private static final double REVEAL_RANGE = 64.0;
    private static final double THAUMOMETER_RANGE = 48.0;
    private static final double BOUNDS_REACH = 2.0;

    private static final float BRIGHTNESS_HIDDEN = 0.1F;
    private static final float BRIGHTNESS_REVEALED = 0.5F;
    private static final float BRIGHTNESS_FULL = 1.0F;

    private static final int LAYER_ROW = 0;
    private static final float LAYER_AMOUNT_CAP = 50.0F;
    private static final float LAYER_BASE_HALF = 0.2F;
    private static final float LAYER_BREATH_BASE = 0.5F;
    private static final float LAYER_BREATH_AMPLITUDE = 0.25F;
    private static final int LAYER_BREATH_DIVISOR = 14;
    private static final float ALPHA_SHARE_DIVISOR = 2.0F;
    private static final float CONTRAST_BOOST = 1.5F;
    private static final double CLOCK_UNITS_PER_TICK = 10.0;
    private static final double LAYER_TURN_BASE = 5000.0;
    private static final double LAYER_TURN_STEP = 500.0;
    private static final float NORMAL_SPIN = 1.0F;
    private static final float ENERGIZED_SPIN = 2.0F;

    private static final float CORE_BASE_HALF = 0.1F;
    private static final float CORE_AVERAGE_DIVISOR = 150.0F;
    private static final int CORE_ROW_NORMAL = 7;
    private static final int CORE_ROW_PURE = 9;
    private static final int CORE_ROW_DARK = 2;
    private static final int CORE_ROW_TAINTED = 5;
    private static final int CORE_ROW_UNSTABLE = 6;
    private static final int CORE_ROW_HUNGRY = 8;
    private static final float CORE_SCALE_PLAIN = 1.0F;
    private static final float CORE_SCALE_HUNGRY = 0.75F;
    private static final Map<NodeType, CoreStyle> CORE_STYLES = coreStyles();

    private static final float ENERGIZED_CONTRACTION = 0.6F;
    private static final int RING_ROW = 6;
    private static final int RING_RGB = 0x99CCFF;
    private static final float RING_SCALE = 1.35F;
    private static final float RING_BREATH = 0.25F;
    private static final float RING_BREATH_DIVISOR = 4.0F;

    private static final int FAINT_ROW = 7;
    private static final float FAINT_HALF = 0.5F;
    private static final float FAINT_ALPHA = 0.0066F;
    private static final int FAINT_SHADER_RGB = 0x666666;
    private static final float FAINT_SHADER_ALPHA = 1.0F;

    private static final float BRIGHT_FACTOR = 1.5F;
    private static final float PALE_FACTOR = 0.66F;
    private static final float FADING_AMPLITUDE = 0.25F;
    private static final float FADING_BASE = 0.33F;
    private static final float FADING_DIVISOR = 3.0F;

    private static final int ORDER_DIRECT = 0;

    private static final float DRAIN_GROW_TICKS = 10.0F;
    private static final float DRAIN_COLOR_BLEND = 0.25F;
    private static final float DRAIN_LINE_SPEED = -0.02F;
    private static final float DRAIN_LINE_HALF_WIDTH = 0.15F;
    private static final double HAND_SIDE = 0.1;
    private static final double HAND_DROP = 0.1;
    private static final double HAND_REACH = 0.5;
    private static final float WOBBLE_SCALE = 10.0F;
    private static final float WOBBLE_DIVISOR = 10.0F;
    private static final float WOBBLE_YAW = -0.01F;
    private static final float WOBBLE_PITCH = -0.015F;

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
            state.visible = false;
            state.depthIgnore = false;
        }
        float falloff = (float) Math.max(0.0, (range - distance) / range);
        state.alpha = Math.min(1.0F, falloff * modifierFactor(state.modifier, state.ticks));
    }

    private static boolean holdsThaumometer(@Nullable LocalPlayer viewer) {
        return viewer != null && (viewer.getMainHandItem().is(TTItems.THAUMOMETER) || viewer.getOffhandItem().is(TTItems.THAUMOMETER));
    }

    private static void extractDrain(BlockEntityNode node, NodeRenderState state, float partialTicks) {
        Player drainer = drainer(node);
        state.draining = drainer != null && drainer.isUsingItem();
        if (!state.draining) {
            return;
        }
        int target = node.getDrainColor();
        node.clientDrainRed = blendChannel(node.clientDrainRed, ARGB.red(target));
        node.clientDrainGreen = blendChannel(node.clientDrainGreen, ARGB.green(target));
        node.clientDrainBlue = blendChannel(node.clientDrainBlue, ARGB.blue(target));
        state.drainColor = ARGB.color(node.clientDrainRed, node.clientDrainGreen, node.clientDrainBlue);
        state.drainTime = drainer.getTicksUsingItem() + partialTicks;
        Vec3 from = handPoint(drainer, partialTicks, state.drainTime);
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

    private static Vec3 handPoint(Player drainer, float partialTicks, float useTicks) {
        Minecraft mc = Minecraft.getInstance();
        if (drainer == mc.player && mc.options.getCameraType().isFirstPerson()) {
            Vec3 tip = WandTipTracker.firstPersonTip();
            if (tip != null) {
                return tip;
            }
        }
        Vec3 eye = drainer.getPosition(partialTicks).add(0.0, drainer.getEyeHeight(), 0.0);
        Vec3 look = drainer.getViewVector(partialTicks);
        Vec3 up = drainer.getUpVector(partialTicks);
        Vec3 right = look.cross(up);
        float wave = WOBBLE_SCALE * Mth.sin(useTicks / WOBBLE_DIVISOR);
        Vec3 offset = right.scale(HAND_SIDE).add(up.scale(-HAND_DROP)).add(look.scale(HAND_REACH)).yRot(WOBBLE_YAW * wave).xRot(WOBBLE_PITCH * wave);
        return eye.add(offset);
    }

    @Override
    public void submit(NodeRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.jarred) {
            poseStack.pushPose();
            poseStack.translate(CENTER, JAR_CENTER_Y, CENTER);
            poseStack.mulPose(camera.orientation);
            submitLayers(state, poseStack, collector, ORDER_DIRECT);
            poseStack.popPose();
        } else {
            queueGlow(state);
        }
        if (state.draining) {
            queueDrainBeam(state);
        }
    }

    private static void queueGlow(NodeRenderState state) {
        NodeRenderState snapshot = snapshot(state);
        Vec3 center = Vec3.atCenterOf(state.blockPos);
        LateWorldRenderQueue.enqueue(center, (late, buffers) -> drawGlow(snapshot, late, buffers));
    }

    private static NodeRenderState snapshot(NodeRenderState state) {
        NodeRenderState copy = new NodeRenderState();
        copy.type = state.type;
        copy.modifier = state.modifier;
        copy.visible = state.visible;
        copy.depthIgnore = state.depthIgnore;
        copy.shaderPack = state.shaderPack;
        copy.alpha = state.alpha;
        copy.size = state.size;
        copy.ticks = state.ticks;
        copy.frameSeed = state.frameSeed;
        copy.energized = state.energized;
        for (NodeRenderState.AspectLayer source : state.layers) {
            NodeRenderState.AspectLayer layer = new NodeRenderState.AspectLayer();
            layer.color = source.color;
            layer.amount = source.amount;
            layer.blend = source.blend;
            copy.layers.add(layer);
        }
        return copy;
    }

    private static void drawGlow(NodeRenderState snapshot, PoseStack poseStack, MultiBufferSource buffers) {
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        poseStack.pushPose();
        poseStack.mulPose(camera.rotation());
        forEachLayer(snapshot, (index, type, angle, halfSize, alpha, rgb, row, column) -> {
            int tint = ARGB.color(Math.clamp(alpha, 0.0F, 1.0F), rgb);
            poseStack.pushPose();
            poseStack.mulPose(Axis.ZP.rotation(angle));
            emitQuad(poseStack.last().pose(), buffers.getBuffer(type), halfSize, tint, row, column);
            poseStack.popPose();
        });
        poseStack.popPose();
    }

    private static void queueDrainBeam(NodeRenderState state) {
        BlockPos pos = state.blockPos;
        Vec3 origin = new Vec3(pos.getX() + CENTER, pos.getY() + (state.jarred ? JAR_CENTER_Y : CENTER), pos.getZ() + CENTER);
        Vec3 offset = new Vec3(state.drainFromX, state.drainFromY, state.drainFromZ).subtract(origin);
        float fraction = Math.min(state.drainTime, DRAIN_GROW_TICKS) / DRAIN_GROW_TICKS;
        int color = state.drainColor;
        float time = state.ticks;
        LateWorldRenderQueue.enqueue(origin, (late, buffers) -> FloatyLineRenderer.draw(late, buffers, offset, time, color, DRAIN_LINE_SPEED, fraction, DRAIN_LINE_HALF_WIDTH));
    }

    public static void submitLayers(NodeRenderState state, PoseStack poseStack, SubmitNodeCollector collector, int orderBase) {
        forEachLayer(state, (index, type, angle, halfSize, alpha, rgb, row, column) -> {
            int tint = ARGB.color(Math.clamp(alpha, 0.0F, 1.0F), rgb);
            poseStack.pushPose();
            poseStack.mulPose(Axis.ZP.rotation(angle));
            collector.order(orderBase + index).submitCustomGeometry(poseStack, type, (pose, buffer) -> emitQuad(pose.pose(), buffer, halfSize, tint, row, column));
            poseStack.popPose();
        });
    }

    public static void forEachLayer(NodeRenderState state, LayerSink sink) {
        float brightness = brightness(state);
        int column = Math.floorMod(state.frameSeed + (int) (state.ticks * FRAMES_PER_TICK), SHEET_GRID);
        int count = state.layers.size();
        if (!state.visible || count == 0) {
            sinkFaint(state, brightness, column, sink);
            return;
        }
        if (state.alpha <= 0.0F) {
            return;
        }
        boolean throughWalls = state.depthIgnore;
        RenderType glow = throughWalls ? TTFXRenderTypes.additiveNoDepth(SHEET) : TTFXRenderTypes.additive(SHEET);
        RenderType shade = throughWalls ? TTFXRenderTypes.translucentNoDepth(SHEET) : TTFXRenderTypes.translucent(SHEET);
        float spin = state.energized ? ENERGIZED_SPIN : NORMAL_SPIN;
        float contraction = state.energized ? ENERGIZED_CONTRACTION : 1.0F;
        float share = state.alpha / Math.max(1.0F, count / ALPHA_SHARE_DIVISOR);
        float total = 0.0F;
        float angle = 0.0F;
        for (int index = 0; index < count; index++) {
            NodeRenderState.AspectLayer layer = state.layers.get(index);
            float amount = Math.min(layer.amount, LAYER_AMOUNT_CAP);
            total += amount;
            boolean darkens = layer.blend == Aspect.CONTRAST_BLEND;
            int divisor = Math.max(LAYER_BREATH_DIVISOR - index, 1);
            float breath = LAYER_BREATH_BASE + LAYER_BREATH_AMPLITUDE * Mth.sin(state.ticks / divisor);
            float halfSize = (LAYER_BASE_HALF + breath * amount / LAYER_AMOUNT_CAP) * state.size * contraction;
            angle = Mth.TWO_PI * (float) Mth.frac(state.ticks * CLOCK_UNITS_PER_TICK * spin / (LAYER_TURN_BASE + LAYER_TURN_STEP * index));
            float layerAlpha = share * (darkens ? CONTRAST_BOOST : 1.0F);
            sink.layer(index, darkens ? shade : glow, angle, halfSize, layerAlpha, scaleRgb(layer.color, brightness), LAYER_ROW, column);
        }
        CoreStyle core = CORE_STYLES.get(state.type);
        float average = total / count;
        float coreHalf = (CORE_BASE_HALF + average / CORE_AVERAGE_DIVISOR) * state.size * core.scale();
        float coreAngle = core.spins() ? angle : 0.0F;
        sink.layer(count, core.darkens() ? shade : glow, coreAngle, coreHalf, state.alpha, scaleRgb(WHITE_RGB, brightness), core.row(), column);
        if (state.energized) {
            float ringHalf = coreHalf * (RING_SCALE + RING_BREATH * Mth.sin(state.ticks / RING_BREATH_DIVISOR));
            sink.layer(count + 1, glow, -coreAngle, ringHalf, state.alpha, scaleRgb(RING_RGB, brightness), RING_ROW, column);
        }
    }

    private static void sinkFaint(NodeRenderState state, float brightness, int column, LayerSink sink) {
        RenderType faint = TTFXRenderTypes.additive(SHEET);
        int rgb = scaleRgb(state.shaderPack ? FAINT_SHADER_RGB : WHITE_RGB, brightness);
        float alpha = state.shaderPack ? FAINT_SHADER_ALPHA : FAINT_ALPHA;
        sink.layer(0, faint, 0.0F, FAINT_HALF, alpha, rgb, FAINT_ROW, column);
    }

    private static float brightness(NodeRenderState state) {
        if (!state.visible) {
            return BRIGHTNESS_HIDDEN;
        }
        return state.depthIgnore ? BRIGHTNESS_REVEALED : BRIGHTNESS_FULL;
    }

    private static int scaleRgb(int rgb, float factor) {
        return ARGB.color((int) (ARGB.red(rgb) * factor), (int) (ARGB.green(rgb) * factor), (int) (ARGB.blue(rgb) * factor));
    }

    public static void emitQuad(Matrix4fc matrix, VertexConsumer consumer, float halfSize, int tint, int row, int column) {
        float cell = 1.0F / SHEET_GRID;
        float u0 = column * cell;
        float v0 = row * cell;
        float u1 = u0 + cell;
        float v1 = v0 + cell;
        consumer.addVertex(matrix, -halfSize, -halfSize, 0.0F).setUv(u1, v1).setColor(tint).setLight(LightCoordsUtil.FULL_BRIGHT);
        consumer.addVertex(matrix, halfSize, -halfSize, 0.0F).setUv(u0, v1).setColor(tint).setLight(LightCoordsUtil.FULL_BRIGHT);
        consumer.addVertex(matrix, halfSize, halfSize, 0.0F).setUv(u0, v0).setColor(tint).setLight(LightCoordsUtil.FULL_BRIGHT);
        consumer.addVertex(matrix, -halfSize, halfSize, 0.0F).setUv(u1, v0).setColor(tint).setLight(LightCoordsUtil.FULL_BRIGHT);
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
            case FADING -> Mth.sin(ticks / FADING_DIVISOR) * FADING_AMPLITUDE + FADING_BASE;
        };
    }

    private static Map<NodeType, CoreStyle> coreStyles() {
        Map<NodeType, CoreStyle> styles = new EnumMap<>(NodeType.class);
        styles.put(NodeType.NORMAL, new CoreStyle(CORE_ROW_NORMAL, false, true, CORE_SCALE_PLAIN));
        styles.put(NodeType.UNSTABLE, new CoreStyle(CORE_ROW_UNSTABLE, false, false, CORE_SCALE_PLAIN));
        styles.put(NodeType.DARK, new CoreStyle(CORE_ROW_DARK, true, true, CORE_SCALE_PLAIN));
        styles.put(NodeType.TAINTED, new CoreStyle(CORE_ROW_TAINTED, true, true, CORE_SCALE_PLAIN));
        styles.put(NodeType.PURE, new CoreStyle(CORE_ROW_PURE, false, true, CORE_SCALE_PLAIN));
        styles.put(NodeType.HUNGRY, new CoreStyle(CORE_ROW_HUNGRY, false, true, CORE_SCALE_HUNGRY));
        return styles;
    }

    private record CoreStyle(int row, boolean darkens, boolean spins, float scale) {
    }
}
