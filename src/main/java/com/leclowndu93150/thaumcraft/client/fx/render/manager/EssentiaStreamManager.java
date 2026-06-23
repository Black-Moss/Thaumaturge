package com.leclowndu93150.thaumcraft.client.fx.render.manager;

import com.leclowndu93150.thaumcraft.client.fx.render.geometry.PolyCone;
import com.leclowndu93150.thaumcraft.client.fx.render.instance.EssentiaStreamInstance;
import com.leclowndu93150.thaumcraft.client.fx.render.rendertype.EssentiaStreamRenderType;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;

public final class EssentiaStreamManager extends AbstractFXManager<EssentiaStreamInstance> {
    public static final EssentiaStreamManager INSTANCE = new EssentiaStreamManager();

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

    @Override
    protected Collection<EssentiaStreamInstance> activeInstances() {
        throw new UnsupportedOperationException("EssentiaStreamManager overrides tickAll directly to handle map-keyed dedup");
    }

    @Override
    public void tickAll(ClientLevel level) {
        Iterator<Map.Entry<String, EssentiaStreamInstance>> it = ACTIVE.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, EssentiaStreamInstance> e = it.next();
            e.getValue().tick();
            if (e.getValue().isExpired()) it.remove();
        }
    }

    @Override
    public void renderAll(PoseStack poseStack, Camera camera, float partialTick) {
        if (ACTIVE.isEmpty()) return;
        MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(new ByteBufferBuilder(2048));
        VertexConsumer consumer = bufferSource.getBuffer(EssentiaStreamRenderType.RENDER_TYPE);
        double cx = camera.position().x;
        double cy = camera.position().y;
        double cz = camera.position().z;
        for (EssentiaStreamInstance inst : ACTIVE.values()) {
            EssentiaStreamInstance.Snapshot snap = inst.snapshot(partialTick);
            if (snap == null) continue;
            poseStack.pushPose();
            poseStack.translate(snap.originX() - cx, snap.originY() - cy, snap.originZ() - cz);
            PolyCone.render(poseStack, consumer, snap.points(), snap.colours(), snap.radii(), 0x00F000F0, snap.texSlice(), snap.start());
            poseStack.popPose();
        }
        bufferSource.endBatch(EssentiaStreamRenderType.RENDER_TYPE);
    }
}
