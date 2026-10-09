package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.api.items.GogglesAccess;
import com.leclowndu93150.thaumaturge.client.effect.LateWorldRenderQueue;
import com.leclowndu93150.thaumaturge.client.effect.rendertype.VisRelayBeamRenderTypes;
import com.leclowndu93150.thaumaturge.client.render.aspect.ParticleTextures;
import com.leclowndu93150.thaumaturge.client.render.aspect.StripUv;
import com.leclowndu93150.thaumaturge.content.aura.relay.BlockEntityVisRelay;
import com.leclowndu93150.thaumaturge.content.aura.relay.BlockVisRelay;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

public final class VisRelayRenderer implements BlockEntityRenderer<BlockEntityVisRelay, VisRelayRenderState> {
    private static final float COLOR_CHANNEL_MAX = 255.0F;
    private static final int RED_SHIFT = 16;
    private static final int GREEN_SHIFT = 8;
    private static final int CHANNEL_MASK = 0xFF;
    private static final float WASH_PER_TICK = 0.025F;
    private static final float PULSE_OPACITY = 0.8F;
    private static final float OPACITY_FALL_PER_TICK = 0.025F;
    private static final float OPACITY_FLOOR = 0.3F;
    private static final float SCROLL_SPEED = 0.2F;
    private static final float SCROLL_WRAP_SPEED = 0.1F;
    private static final float BEAM_ALPHA_SCALE = 0.1F;
    private static final float FLARE_ALPHA_SCALE = 0.2F;
    private static final float FLARE_HALF_SIZE_SCALE = 0.66F;
    private static final float FLARE_INSET = 0.0001F;
    private static final float BEAM_HALF_WIDTH = 0.105F;
    private static final float SECOND_RIBBON_OFFSET = 1.0F / 3.0F;
    private static final double VERTICAL_AXIS_LIMIT = 0.99;
    private static final int BEAM_LIGHT = 200;

    public VisRelayRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(BlockEntityVisRelay relay) {
        AABB own = new AABB(relay.getBlockPos());
        BlockPos parent = relay.parentPos();
        return parent == null ? own : own.minmax(new AABB(parent));
    }

    @Override
    public VisRelayRenderState createRenderState() {
        return new VisRelayRenderState();
    }

    @Override
    public void extractRenderState(BlockEntityVisRelay relay, VisRelayRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(relay, state, partialTicks, cameraPosition, breakProgress);
        state.beamTarget = null;
        LocalPlayer player = Minecraft.getInstance().player;
        Level level = relay.getLevel();
        BlockPos parent = relay.parentPos();
        if (player == null || level == null || parent == null || !relay.isLinked()) {
            return;
        }
        Vec3 origin = BlockVisRelay.crystalPosition(relay.getBlockPos(), relay.getBlockState());
        BlockState parentState = level.getBlockState(parent);
        Vec3 target = parentState.getBlock() instanceof BlockVisRelay ? BlockVisRelay.crystalPosition(parent, parentState) : Vec3.atCenterOf(parent);
        state.beamOrigin = origin;
        state.beamTarget = target.subtract(origin);
        long gameTime = level.getGameTime();
        float elapsed = gameTime - relay.pulseStart() + partialTicks;
        float wash = WASH_PER_TICK * Math.max(0.0F, elapsed);
        int color = relay.pulseColor();
        state.red = Math.min(1.0F, (color >> RED_SHIFT & CHANNEL_MASK) / COLOR_CHANNEL_MAX + wash);
        state.green = Math.min(1.0F, (color >> GREEN_SHIFT & CHANNEL_MASK) / COLOR_CHANNEL_MAX + wash);
        state.blue = Math.min(1.0F, (color & CHANNEL_MASK) / COLOR_CHANNEL_MAX + wash);
        state.opacity = elapsed < BlockEntityVisRelay.PULSE_TICKS ? PULSE_OPACITY : Math.max(OPACITY_FLOOR, PULSE_OPACITY - OPACITY_FALL_PER_TICK * (elapsed - BlockEntityVisRelay.PULSE_TICKS));
        float s = player.tickCount + partialTicks;
        state.scroll = -SCROLL_SPEED * s - Mth.floor(-SCROLL_WRAP_SPEED * s);
        state.revealing = GogglesAccess.wearsRevealingGear(player);
        state.flareFrame = (int) Math.floorMod(gameTime, (long) ParticleTextures.STAR_GLINT_FRAMES);
    }

    @Override
    public void submit(VisRelayRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        Vec3 target = state.beamTarget;
        if (target == null) {
            return;
        }
        Vec3 origin = state.beamOrigin;
        float scroll = state.scroll;
        float red = state.red;
        float green = state.green;
        float blue = state.blue;
        float opacity = state.opacity;
        float beamAlpha = opacity * (state.revealing ? 1.0F : BEAM_ALPHA_SCALE);
        float flareAlpha = opacity * (state.revealing ? 1.0F : FLARE_ALPHA_SCALE);
        float flareHalf = FLARE_HALF_SIZE_SCALE * opacity;
        int frame = state.flareFrame;
        LateWorldRenderQueue.enqueue(origin, (stack, buffers) -> {
            Matrix4f pose = stack.last().pose();
            drawBeam(pose, buffers, target, scroll, red, green, blue, beamAlpha);
            drawFlare(pose, buffers, frame, flareHalf, red, green, blue, flareAlpha);
        });
    }

    private static void drawBeam(Matrix4f pose, MultiBufferSource buffers, Vec3 target, float scroll, float red, float green, float blue, float alpha) {
        double length = target.length();
        if (length == 0.0) {
            return;
        }
        Vec3 axis = target.scale(1.0 / length);
        Vec3 reference = Math.abs(axis.y) >= VERTICAL_AXIS_LIMIT ? new Vec3(1.0, 0.0, 0.0) : new Vec3(0.0, 1.0, 0.0);
        Vec3 first = axis.cross(reference).normalize();
        Vec3 second = axis.cross(first).normalize();
        VertexConsumer consumer = buffers.getBuffer(VisRelayBeamRenderTypes.BEAM);
        float span = (float) length;
        ribbon(consumer, pose, first, target, scroll, span, red, green, blue, alpha);
        ribbon(consumer, pose, second, target, scroll + SECOND_RIBBON_OFFSET, span, red, green, blue, alpha);
    }

    private static void ribbon(VertexConsumer consumer, Matrix4f pose, Vec3 side, Vec3 target, float vStart, float span, float red, float green, float blue, float alpha) {
        float sx = (float) side.x * BEAM_HALF_WIDTH;
        float sy = (float) side.y * BEAM_HALF_WIDTH;
        float sz = (float) side.z * BEAM_HALF_WIDTH;
        float tx = (float) target.x;
        float ty = (float) target.y;
        float tz = (float) target.z;
        float vOrigin = vStart + span;
        consumer.addVertex(pose, -sx, -sy, -sz).setUv(1.0F, vOrigin).setColor(red, green, blue, alpha).setLight(BEAM_LIGHT);
        consumer.addVertex(pose, sx, sy, sz).setUv(0.0F, vOrigin).setColor(red, green, blue, alpha).setLight(BEAM_LIGHT);
        consumer.addVertex(pose, tx + sx, ty + sy, tz + sz).setUv(0.0F, vStart).setColor(red, green, blue, alpha).setLight(BEAM_LIGHT);
        consumer.addVertex(pose, tx - sx, ty - sy, tz - sz).setUv(1.0F, vStart).setColor(red, green, blue, alpha).setLight(BEAM_LIGHT);
    }

    private static void drawFlare(Matrix4f pose, MultiBufferSource buffers, int frame, float half, float red, float green, float blue, float alpha) {
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        Vector3fc left = camera.leftVector();
        Vector3fc up = camera.upVector();
        float lx = left.x() * half;
        float ly = left.y() * half;
        float lz = left.z() * half;
        float ux = up.x() * half;
        float uy = up.y() * half;
        float uz = up.z() * half;
        float uLeft = StripUv.u1(frame, ParticleTextures.STAR_GLINT_FRAMES) - FLARE_INSET;
        float uRight = StripUv.u0(frame, ParticleTextures.STAR_GLINT_FRAMES) + FLARE_INSET;
        VertexConsumer consumer = buffers.getBuffer(VisRelayBeamRenderTypes.FLARE);
        consumer.addVertex(pose, lx - ux, ly - uy, lz - uz).setUv(uLeft, StripUv.V1).setColor(red, green, blue, alpha).setLight(BEAM_LIGHT);
        consumer.addVertex(pose, -lx - ux, -ly - uy, -lz - uz).setUv(uRight, StripUv.V1).setColor(red, green, blue, alpha).setLight(BEAM_LIGHT);
        consumer.addVertex(pose, -lx + ux, -ly + uy, -lz + uz).setUv(uRight, StripUv.V0).setColor(red, green, blue, alpha).setLight(BEAM_LIGHT);
        consumer.addVertex(pose, lx + ux, ly + uy, lz + uz).setUv(uLeft, StripUv.V0).setColor(red, green, blue, alpha).setLight(BEAM_LIGHT);
    }
}
