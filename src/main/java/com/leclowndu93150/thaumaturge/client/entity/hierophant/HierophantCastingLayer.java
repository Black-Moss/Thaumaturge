package com.leclowndu93150.thaumaturge.client.entity.hierophant;

import com.leclowndu93150.thaumaturge.content.entity.boss.hierophant.HierophantAction;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.util.Mth;
import org.joml.Matrix4fc;
import org.joml.Vector3f;

public final class HierophantCastingLayer extends RenderLayer<HierophantRenderState, HierophantModel> {
    private static final int ARC_SEGMENTS = 12;
    private static final float ARC_WIDTH = 0.045F;
    private static final float ARC_JITTER = 0.11F;
    private static final int ARC_COLOR = 0xFFE2CAF5;
    private static final int FULL_LIGHT = 0xF000F0;
    private static final float GLOW_RADIUS = 0.3F;
    private static final float GLOW_HEIGHT = -0.7F;
    private static final float FADE_IN_TICKS = 5;
    private static final float RELEASE_LINGER_TICKS = 3;

    public HierophantCastingLayer(RenderLayerParent<HierophantRenderState, HierophantModel> parent) {
        super(parent);
    }

    @Override
    public void submit(PoseStack pose, SubmitNodeCollector collector, int light, HierophantRenderState state, float yaw, float pitch) {
        if (state.isInvisible || state.actionTicks > state.action.release() + RELEASE_LINGER_TICKS) {
            return;
        }
        final boolean both = state.action == HierophantAction.CAST;
        final boolean right = both || state.action == HierophantAction.SWIPE_RIGHT;
        final boolean left = both || state.action == HierophantAction.SWIPE_LEFT;
        if (!right && !left) {
            return;
        }
        final float alpha = Mth.clamp(state.actionTicks / FADE_IN_TICKS, 0, 1);
        final Vector3f rightHand = getParentModel().palm(false);
        final Vector3f leftHand = getParentModel().palm(true);
        if (right) {
            glow(pose, collector, rightHand, alpha);
        }
        if (left) {
            glow(pose, collector, leftHand, alpha);
        }
        if (both) {
            final int frame = (int) state.actionTicks;
            collector.submitCustomGeometry(pose, HierophantRenderTypes.ARC, (transform, buffer) -> arc(buffer, transform.pose(), rightHand, leftHand, frame));
        }
    }

    private static void glow(PoseStack pose, SubmitNodeCollector collector, Vector3f point, float alpha) {
        pose.pushPose();
        pose.translate(point.x, point.y, point.z);
        collector.submitCustomGeometry(pose, HierophantRenderTypes.SPELL, (transform, buffer) -> HierophantSprites.flame(buffer, transform.pose(), GLOW_RADIUS, GLOW_HEIGHT, alpha));
        pose.popPose();
    }

    private static void arc(VertexConsumer buffer, Matrix4fc matrix, Vector3f from, Vector3f to, int frame) {
        final Vector3f line = new Vector3f(to).sub(from);
        final Vector3f side = new Vector3f(line).cross(0, 1, 0);
        if (side.lengthSquared() < 0.001F) {
            side.set(1, 0, 0);
        }
        side.normalize(ARC_WIDTH);
        Vector3f previous = new Vector3f(from);
        for (int i = 1; i <= ARC_SEGMENTS; i++) {
            final float progress = (float) i / ARC_SEGMENTS;
            final Vector3f next = new Vector3f(from).lerp(to, progress);
            if (i < ARC_SEGMENTS) {
                next.y += Mth.sin(frame * 1.7F + i * 2.3F) * ARC_JITTER;
                next.z += Mth.sin(frame * 2.1F + i * 3.7F) * ARC_JITTER;
            }
            vertex(buffer, matrix, new Vector3f(previous).sub(side), 0, 1);
            vertex(buffer, matrix, new Vector3f(previous).add(side), 0, 0);
            vertex(buffer, matrix, new Vector3f(next).add(side), 1, 0);
            vertex(buffer, matrix, new Vector3f(next).sub(side), 1, 1);
            previous = next;
        }
    }

    private static void vertex(VertexConsumer buffer, Matrix4fc matrix, Vector3f point, float u, float v) {
        buffer.addVertex(matrix, point.x, point.y, point.z).setUv(u, v).setColor(ARC_COLOR).setLight(FULL_LIGHT);
    }
}
