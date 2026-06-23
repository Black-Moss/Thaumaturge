package com.leclowndu93150.thaumcraft.client.render.stream;

import com.leclowndu93150.thaumcraft.TCIds;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@EventBusSubscriber(modid = TCIds.MODID, value = Dist.CLIENT)
public final class BoreVoidStreamManager {
    private static final List<BoreStreamInstance> BORES = new ArrayList<>();
    private static final List<VoidStreamInstance> VOIDS = new ArrayList<>();

    private BoreVoidStreamManager() {}

    public static void addBore(BoreStreamInstance instance) { BORES.add(instance); }
    public static void addVoid(VoidStreamInstance instance) { VOIDS.add(instance); }

    @SubscribeEvent
    public static void onTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ClientLevel)) return;
        Iterator<BoreStreamInstance> bit = BORES.iterator();
        while (bit.hasNext()) {
            BoreStreamInstance b = bit.next();
            b.tick();
            if (b.isExpired()) bit.remove();
        }
        Iterator<VoidStreamInstance> vit = VOIDS.iterator();
        while (vit.hasNext()) {
            VoidStreamInstance v = vit.next();
            v.tick();
            if (v.isExpired()) vit.remove();
        }
    }

    @SubscribeEvent
    public static void onRender(RenderLevelStageEvent.AfterTranslucentParticles event) {
        if (BORES.isEmpty() && VOIDS.isEmpty()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        Camera camera = mc.gameRenderer.getMainCamera();
        PoseStack poseStack = event.getPoseStack();
        float partialTick = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        double cx = camera.position().x;
        double cy = camera.position().y;
        double cz = camera.position().z;

        if (!BORES.isEmpty()) {
            MultiBufferSource.BufferSource buf = MultiBufferSource.immediate(new ByteBufferBuilder(2048));
            VertexConsumer consumer = buf.getBuffer(EssentiaStreamRenderType.RENDER_TYPE);
            for (BoreStreamInstance inst : BORES) {
                EssentiaStreamInstance.Snapshot snap = inst.snapshot(partialTick);
                if (snap == null) continue;
                poseStack.pushPose();
                poseStack.translate(snap.originX() - cx, snap.originY() - cy, snap.originZ() - cz);
                PolyCone.render(poseStack, consumer, snap.points(), snap.colours(), snap.radii(), 0, snap.texSlice(), snap.start());
                poseStack.popPose();
            }
            buf.endBatch(EssentiaStreamRenderType.RENDER_TYPE);
        }

        if (!VOIDS.isEmpty()) {
            float yawDeg = camera.yRot();
            float pitchDeg = camera.xRot();
            float yawRad = (float)Math.toRadians(yawDeg);
            float pitchRad = (float)Math.toRadians(pitchDeg);
            float yawNorm = ((yawRad % (float)(2.0 * Math.PI)) + (float)(2.0 * Math.PI)) % (float)(2.0 * Math.PI) / (float)(2.0 * Math.PI);
            float pitchNorm = (pitchRad + (float)(Math.PI * 0.5)) / (float)Math.PI;

            MultiBufferSource.BufferSource bufA = MultiBufferSource.immediate(new ByteBufferBuilder(2048));
            VertexConsumer addConsumer = bufA.getBuffer(VoidStreamRenderType.ADDITIVE);
            for (VoidStreamInstance inst : VOIDS) {
                EssentiaStreamInstance.Snapshot snap = inst.snapshotWithRadiusMul(partialTick, 1.5F);
                if (snap == null) continue;
                packYawPitch(snap.colours(), yawNorm, pitchNorm);
                poseStack.pushPose();
                poseStack.translate(snap.originX() - cx, snap.originY() - cy, snap.originZ() - cz);
                PolyCone.render(poseStack, addConsumer, snap.points(), snap.colours(), snap.radii(), 0, snap.texSlice(), snap.start());
                poseStack.popPose();
            }
            bufA.endBatch(VoidStreamRenderType.ADDITIVE);

            MultiBufferSource.BufferSource bufT = MultiBufferSource.immediate(new ByteBufferBuilder(2048));
            VertexConsumer trConsumer = bufT.getBuffer(VoidStreamRenderType.TRANSLUCENT);
            for (VoidStreamInstance inst : VOIDS) {
                EssentiaStreamInstance.Snapshot snap = inst.snapshotWithRadiusMul(partialTick, 0.5F);
                if (snap == null) continue;
                packYawPitch(snap.colours(), yawNorm, pitchNorm);
                poseStack.pushPose();
                poseStack.translate(snap.originX() - cx, snap.originY() - cy, snap.originZ() - cz);
                PolyCone.render(poseStack, trConsumer, snap.points(), snap.colours(), snap.radii(), 0, snap.texSlice(), snap.start());
                poseStack.popPose();
            }
            bufT.endBatch(VoidStreamRenderType.TRANSLUCENT);
        }
    }

    private static void packYawPitch(float[][] colours, float yawNorm, float pitchNorm) {
        for (int i = 0; i < colours.length; i++) {
            float alpha = colours[i][3];
            colours[i][0] = yawNorm;
            colours[i][1] = pitchNorm;
            colours[i][2] = 0.0F;
            colours[i][3] = alpha;
        }
    }
}
