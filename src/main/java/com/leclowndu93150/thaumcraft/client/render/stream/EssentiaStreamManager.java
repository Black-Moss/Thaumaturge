package com.leclowndu93150.thaumcraft.client.render.stream;

import com.leclowndu93150.thaumcraft.TCIds;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
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
public final class EssentiaStreamManager {
    private static final Map<String, EssentiaStreamInstance> ACTIVE = new HashMap<>();

    private EssentiaStreamManager() {}

    public static void spawn(double sx, double sy, double sz, double tx, double ty, double tz, int color, int count, float scale, int extend, double my) {
        String key = key(sx, sy, sz, tx, ty, tz, color);
        EssentiaStreamInstance existing = ACTIVE.get(key);
        if (existing != null && !existing.isExpired()) {
            existing.extend(extend);
            return;
        }
        ACTIVE.put(key, new EssentiaStreamInstance(sx, sy, sz, tx, ty, tz, color, count, scale, extend, my));
    }

    private static String key(double sx, double sy, double sz, double tx, double ty, double tz, int color) {
        return ((int)Math.floor(sx)) + "," + ((int)Math.floor(sy)) + "," + ((int)Math.floor(sz))
                + ":" + ((int)Math.floor(tx)) + "," + ((int)Math.floor(ty)) + "," + ((int)Math.floor(tz))
                + ":" + color;
    }

    @SubscribeEvent
    public static void onTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ClientLevel)) return;
        Iterator<Map.Entry<String, EssentiaStreamInstance>> it = ACTIVE.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, EssentiaStreamInstance> e = it.next();
            e.getValue().tick();
            if (e.getValue().isExpired()) it.remove();
        }
    }

    @SubscribeEvent
    public static void onRender(RenderLevelStageEvent.AfterTranslucentParticles event) {
        if (ACTIVE.isEmpty()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        Camera camera = mc.gameRenderer.getMainCamera();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(new ByteBufferBuilder(2048));
        var consumer = bufferSource.getBuffer(EssentiaStreamRenderType.RENDER_TYPE);
        float partialTick = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        for (EssentiaStreamInstance inst : ACTIVE.values()) {
            EssentiaStreamInstance.Snapshot snap = inst.snapshot(partialTick);
            if (snap == null) continue;
            poseStack.pushPose();
            double cx = camera.position().x;
            double cy = camera.position().y;
            double cz = camera.position().z;
            poseStack.translate(snap.originX() - cx, snap.originY() - cy, snap.originZ() - cz);
            PolyCone.render(poseStack, consumer, snap.points(), snap.colours(), snap.radii(), 0x00F000F0, snap.texSlice(), snap.start());
            poseStack.popPose();
        }
        bufferSource.endBatch(EssentiaStreamRenderType.RENDER_TYPE);
    }
}
