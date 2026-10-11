package com.leclowndu93150.thaumaturge.client.effect.manager;

import com.leclowndu93150.thaumaturge.client.effect.geometry.PolyCone;
import com.leclowndu93150.thaumaturge.client.effect.instance.BoreStreamInstance;
import com.leclowndu93150.thaumaturge.client.effect.instance.StreamInstance;
import com.leclowndu93150.thaumaturge.client.effect.instance.VoidStreamInstance;
import com.leclowndu93150.thaumaturge.client.effect.rendertype.EssentiaStreamRenderType;
import com.leclowndu93150.thaumaturge.client.effect.rendertype.VoidStreamRenderType;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.util.Mth;

public final class BoreVoidStreamManager extends AbstractFXManager<IFXInstance> {
    public static final BoreVoidStreamManager INSTANCE = new BoreVoidStreamManager();

    private static final float TWO_PI = Mth.TWO_PI;
    private static final float HALF_PI = Mth.HALF_PI;
    private static final float GLOW_RADIUS_MULTIPLIER = 1.5F;
    private static final float CORE_RADIUS_MULTIPLIER = 0.5F;
    private static final int UNUSED_LIGHT = 0;
    private static final int RED = 0;
    private static final int GREEN = 1;
    private static final int BLUE = 2;

    private static final List<BoreStreamInstance> BORES = new ArrayList<>();
    private static final List<VoidStreamInstance> VOIDS = new ArrayList<>();

    private BoreVoidStreamManager() {}

    public static void addBore(BoreStreamInstance instance) {
        BORES.add(instance);
    }

    public static void addVoid(VoidStreamInstance instance) {
        VOIDS.add(instance);
    }

    @Override
    protected Collection<IFXInstance> activeInstances() {
        throw new UnsupportedOperationException("BoreVoidStreamManager overrides tick handling directly");
    }

    @Override
    public void clear() {
        BORES.clear();
        VOIDS.clear();
    }

    @Override
    public void tickAll(ClientLevel level) {
        advance(BORES);
        advance(VOIDS);
    }

    private static void advance(List<? extends IFXInstance> instances) {
        instances.removeIf(BoreVoidStreamManager::step);
    }

    private static boolean step(IFXInstance instance) {
        instance.tick();
        return instance.isExpired();
    }

    @Override
    public void renderAll(PoseStack poseStack, Camera camera, float partialTick) {
        if (BORES.isEmpty() && VOIDS.isEmpty()) {
            return;
        }
        double camX = camera.position().x;
        double camY = camera.position().y;
        double camZ = camera.position().z;
        MultiBufferSource.BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();
        drawBores(poseStack, buffers, camX, camY, camZ, partialTick);
        if (!VOIDS.isEmpty()) {
            float yaw = Mth.positiveModulo((float) Math.toRadians(camera.yRot()), TWO_PI) / TWO_PI;
            float pitch = ((float) Math.toRadians(camera.xRot()) + HALF_PI) / Mth.PI;
            drawVoids(poseStack, buffers, VoidStreamRenderType.ADDITIVE, GLOW_RADIUS_MULTIPLIER, yaw, pitch, camX, camY, camZ, partialTick);
            drawVoids(poseStack, buffers, VoidStreamRenderType.TRANSLUCENT, CORE_RADIUS_MULTIPLIER, yaw, pitch, camX, camY, camZ, partialTick);
        }
    }

    private static void drawBores(PoseStack poseStack, MultiBufferSource.BufferSource buffers, double camX, double camY, double camZ, float partialTick) {
        if (BORES.isEmpty()) {
            return;
        }
        VertexConsumer consumer = buffers.getBuffer(EssentiaStreamRenderType.RENDER_TYPE);
        for (BoreStreamInstance bore : BORES) {
            emit(poseStack, consumer, bore.snapshot(partialTick), camX, camY, camZ);
        }
        buffers.endBatch(EssentiaStreamRenderType.RENDER_TYPE);
    }

    private static void drawVoids(PoseStack poseStack, MultiBufferSource.BufferSource buffers, RenderType type, float radiusMultiplier, float yaw, float pitch, double camX, double camY, double camZ, float partialTick) {
        VertexConsumer consumer = buffers.getBuffer(type);
        for (VoidStreamInstance stream : VOIDS) {
            StreamInstance.Snapshot snapshot = stream.snapshotWithRadiusMul(partialTick, radiusMultiplier);
            if (snapshot != null) {
                packAngles(snapshot.colours(), yaw, pitch);
            }
            emit(poseStack, consumer, snapshot, camX, camY, camZ);
        }
        buffers.endBatch(type);
    }

    private static void emit(PoseStack poseStack, VertexConsumer consumer, StreamInstance.Snapshot snapshot, double camX, double camY, double camZ) {
        if (snapshot == null) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(snapshot.originX() - camX, snapshot.originY() - camY, snapshot.originZ() - camZ);
        PolyCone.render(poseStack, consumer, snapshot.points(), snapshot.colours(), snapshot.radii(), UNUSED_LIGHT, snapshot.texSlice(), snapshot.start());
        poseStack.popPose();
    }

    private static void packAngles(float[][] colours, float yaw, float pitch) {
        for (float[] colour : colours) {
            colour[RED] = yaw;
            colour[GREEN] = pitch;
            colour[BLUE] = 0.0F;
        }
    }
}
