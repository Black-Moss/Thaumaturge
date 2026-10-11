package com.leclowndu93150.thaumaturge.client.effect.manager;

import com.leclowndu93150.thaumaturge.client.effect.geometry.PolyCone;
import com.leclowndu93150.thaumaturge.client.effect.instance.ArcInstance;
import com.leclowndu93150.thaumaturge.client.effect.instance.BeamInstance;
import com.leclowndu93150.thaumaturge.client.effect.instance.BoltInstance;
import com.leclowndu93150.thaumaturge.client.effect.rendertype.ArcRenderType;
import com.leclowndu93150.thaumaturge.client.effect.rendertype.BeamRenderType;
import com.leclowndu93150.thaumaturge.client.effect.rendertype.BoltRenderType;
import com.leclowndu93150.thaumaturge.client.particle.support.ParticleCulling;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public final class BeamManager extends AbstractFXManager<IFXInstance> {
    public static final BeamManager INSTANCE = new BeamManager();

    private static final List<ArcInstance> ARCS = new ArrayList<>();
    private static final List<BoltInstance> BOLTS = new ArrayList<>();
    private static final List<BeamInstance> BEAMS = new ArrayList<>();

    private static final float BOLT_TEX_SLICE = 1.0F;
    private static final float BOLT_TEX_START = 0.0F;
    private static final int TRUNK_SLOTS = 4;
    private static final int STRIPS = 3;
    private static final float STRIP_STEP_DEGREES = 60.0F;
    private static final float STRIP_HALF_WIDTH = 0.14F;
    private static final float STRIP_V_PER_BLOCK = 0.5F;
    private static final double MIN_BEAM_LENGTH = 1.0E-3;
    private static final double PARALLEL_LIMIT = 0.99;
    private static final double BEAM_VIEW_RANGE = 50.0;
    private static final double BEAM_VIEW_RANGE_FAST = 25.0;
    private static final int GLOW_FRAMES = 16;
    private static final int GLOW_ROW = 25;
    private static final float GLOW_SHEET_CELLS = 32.0F;
    private static final float GLOW_HALF_SIZE = 0.4F;
    private static final float GLOW_OPACITY = 0.8F;
    private static final float ARC_HALF_WIDTH = 0.045F;

    private BeamManager() {}

    public static void addArc(ArcInstance arc) {
        if (!arc.isExpired()) {
            ARCS.add(arc);
        }
    }

    public static void addBolt(BoltInstance bolt) {
        if (!bolt.isExpired()) {
            BOLTS.add(bolt);
        }
    }

    public static void addBeam(BeamInstance beam) {
        for (BeamInstance existing : BEAMS) {
            if (existing.sameSourceAs(beam)) {
                existing.refresh(beam);
                return;
            }
        }
        BEAMS.add(beam);
    }

    @Override
    protected Collection<IFXInstance> activeInstances() {
        throw new UnsupportedOperationException("BeamManager overrides tickAll and clear directly");
    }

    @Override
    public void clear() {
        ARCS.clear();
        BOLTS.clear();
        BEAMS.clear();
    }

    @Override
    public void tickAll(ClientLevel level) {
        tickList(ARCS);
        tickList(BOLTS);
        tickList(BEAMS);
    }

    @Override
    public void renderAll(PoseStack poseStack, Camera camera, float partialTick) {
        if (ARCS.isEmpty() && BOLTS.isEmpty() && BEAMS.isEmpty()) {
            return;
        }
        MultiBufferSource.BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();
        Vec3 cameraPos = camera.position();
        renderArcs(poseStack, buffers, cameraPos, partialTick);
        renderBolts(poseStack, buffers, cameraPos, partialTick);
        renderBeams(poseStack, buffers, camera, partialTick);
    }

    private static <I extends IFXInstance> void tickList(List<I> instances) {
        Iterator<I> iterator = instances.iterator();
        while (iterator.hasNext()) {
            I instance = iterator.next();
            instance.tick();
            if (instance.isExpired()) {
                iterator.remove();
            }
        }
    }

    private static void renderArcs(PoseStack poseStack, MultiBufferSource.BufferSource buffers, Vec3 cameraPos, float partialTick) {
        if (ARCS.isEmpty()) {
            return;
        }
        RenderType type = ArcRenderType.RENDER_TYPE;
        VertexConsumer consumer = buffers.getBuffer(type);
        for (ArcInstance arc : ARCS) {
            float alpha = arc.alpha(partialTick);
            if (alpha <= 0.0F) {
                continue;
            }
            Vec3 origin = arc.origin();
            poseStack.pushPose();
            poseStack.translate(origin.x - cameraPos.x, origin.y - cameraPos.y, origin.z - cameraPos.z);
            drawArc(consumer, poseStack.last().pose(), arc, alpha);
            poseStack.popPose();
        }
        buffers.endBatch(type);
    }

    private static void drawArc(VertexConsumer consumer, Matrix4f matrix, ArcInstance arc, float alpha) {
        double[][] points = arc.points();
        int count = points.length;
        if (count < 2) {
            return;
        }
        double[] last = points[count - 1];
        double sideX = -last[2];
        double sideZ = last[0];
        double flat = Math.sqrt(sideX * sideX + sideZ * sideZ);
        if (flat < MIN_BEAM_LENGTH) {
            sideX = 1.0;
            sideZ = 0.0;
        } else {
            sideX /= flat;
            sideZ /= flat;
        }
        float red = Mth.clamp(arc.red(), 0.0F, 1.0F);
        float green = Mth.clamp(arc.green(), 0.0F, 1.0F);
        float blue = Mth.clamp(arc.blue(), 0.0F, 1.0F);
        double sx = sideX * ARC_HALF_WIDTH;
        double sz = sideZ * ARC_HALF_WIDTH;
        for (int i = 0; i < count - 1; i++) {
            double[] a = points[i];
            double[] b = points[i + 1];
            float v0 = (float) i / (count - 1);
            float v1 = (float) (i + 1) / (count - 1);
            vertex(consumer, matrix, a[0], a[1] - ARC_HALF_WIDTH, a[2], 0.0F, v0, red, green, blue, alpha);
            vertex(consumer, matrix, a[0], a[1] + ARC_HALF_WIDTH, a[2], 1.0F, v0, red, green, blue, alpha);
            vertex(consumer, matrix, b[0], b[1] + ARC_HALF_WIDTH, b[2], 1.0F, v1, red, green, blue, alpha);
            vertex(consumer, matrix, b[0], b[1] - ARC_HALF_WIDTH, b[2], 0.0F, v1, red, green, blue, alpha);
            vertex(consumer, matrix, a[0] - sx, a[1], a[2] - sz, 0.0F, v0, red, green, blue, alpha);
            vertex(consumer, matrix, a[0] + sx, a[1], a[2] + sz, 1.0F, v0, red, green, blue, alpha);
            vertex(consumer, matrix, b[0] + sx, b[1], b[2] + sz, 1.0F, v1, red, green, blue, alpha);
            vertex(consumer, matrix, b[0] - sx, b[1], b[2] - sz, 0.0F, v1, red, green, blue, alpha);
        }
    }

    private static void renderBolts(PoseStack poseStack, MultiBufferSource.BufferSource buffers, Vec3 cameraPos, float partialTick) {
        if (BOLTS.isEmpty()) {
            return;
        }
        RenderType type = BoltRenderType.RENDER_TYPE;
        VertexConsumer consumer = buffers.getBuffer(type);
        for (BoltInstance bolt : BOLTS) {
            bolt.shape(partialTick);
            Vec3 origin = bolt.origin();
            poseStack.pushPose();
            poseStack.translate(origin.x - cameraPos.x, origin.y - cameraPos.y, origin.z - cameraPos.z);
            PolyCone.render(poseStack, consumer, bolt.points(), bolt.sheathColours(), bolt.sheathRadii(), LightCoordsUtil.FULL_BRIGHT, BOLT_TEX_SLICE, BOLT_TEX_START);
            PolyCone.render(poseStack, consumer, bolt.points(), bolt.coreColours(), bolt.coreRadii(), LightCoordsUtil.FULL_BRIGHT, BOLT_TEX_SLICE, BOLT_TEX_START);
            poseStack.popPose();
        }
        buffers.endBatch(type);
    }

    private static void renderBeams(PoseStack poseStack, MultiBufferSource.BufferSource buffers, Camera camera, float partialTick) {
        if (BEAMS.isEmpty()) {
            return;
        }
        Vec3 cameraPos = camera.position();
        for (int slot = 0; slot < TRUNK_SLOTS; slot++) {
            RenderType type = BeamRenderType.trunkForType(slot);
            VertexConsumer consumer = buffers.getBuffer(type);
            for (BeamInstance beam : BEAMS) {
                if (trunkSlot(beam) == slot) {
                    drawTrunk(poseStack, consumer, cameraPos, beam, partialTick);
                }
            }
            buffers.endBatch(type);
        }
        RenderType nodeType = BeamRenderType.NODE_TYPE;
        VertexConsumer nodeConsumer = buffers.getBuffer(nodeType);
        for (BeamInstance beam : BEAMS) {
            if (beam.withSource()) {
                drawGlow(poseStack, nodeConsumer, camera, beam, partialTick);
            }
        }
        buffers.endBatch(nodeType);
    }

    private static boolean outOfView(Vec3 source) {
        return ParticleCulling.beyondSpawnRange(source.x, source.y, source.z, BEAM_VIEW_RANGE, BEAM_VIEW_RANGE_FAST);
    }

    private static void drawTrunk(PoseStack poseStack, VertexConsumer consumer, Vec3 cameraPos, BeamInstance beam, float partialTick) {
        Vec3 source = beam.sourcePos(partialTick);
        if (outOfView(source)) {
            return;
        }
        float opacity = beam.computeOpacity(partialTick);
        float size = beam.computeSize(partialTick);
        if (opacity <= 0.0F || size <= 0.0F) {
            return;
        }
        Vec3 target = beam.targetPos(partialTick);
        double dx = target.x - source.x;
        double dy = target.y - source.y;
        double dz = target.z - source.z;
        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (length < MIN_BEAM_LENGTH) {
            return;
        }
        double ax = dx / length;
        double ay = dy / length;
        double az = dz / length;
        double hx = 0.0;
        double hy = 1.0;
        if (Math.abs(ay) > PARALLEL_LIMIT) {
            hx = 1.0;
            hy = 0.0;
        }
        double along = hx * ax + hy * ay;
        double px = hx - along * ax;
        double py = hy - along * ay;
        double pz = -along * az;
        double pLength = Math.sqrt(px * px + py * py + pz * pz);
        px /= pLength;
        py /= pLength;
        pz /= pLength;
        double qx = ay * pz - az * py;
        double qy = az * px - ax * pz;
        double qz = ax * py - ay * px;
        double reach = length * size;
        double endX = ax * reach;
        double endY = ay * reach;
        double endZ = az * reach;
        float nearWidth = STRIP_HALF_WIDTH * size;
        float farWidth = nearWidth * beam.endMod();
        float scroll = beam.texScroll(partialTick);
        float spin = beam.worldRotation(partialTick);
        float vLength = (float) reach * STRIP_V_PER_BLOCK;
        float red = Mth.clamp(beam.colorR(), 0.0F, 1.0F);
        float green = Mth.clamp(beam.colorG(), 0.0F, 1.0F);
        float blue = Mth.clamp(beam.colorB(), 0.0F, 1.0F);
        float alpha = Mth.clamp(opacity, 0.0F, 1.0F);
        poseStack.pushPose();
        poseStack.translate(source.x - cameraPos.x, source.y - cameraPos.y, source.z - cameraPos.z);
        Matrix4f matrix = poseStack.last().pose();
        for (int strip = 0; strip < STRIPS; strip++) {
            double angle = (spin + strip * STRIP_STEP_DEGREES) * Mth.DEG_TO_RAD;
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            double ex = cos * px + sin * qx;
            double ey = cos * py + sin * qy;
            double ez = cos * pz + sin * qz;
            float v0 = scroll + (float) strip / STRIPS;
            float v1 = v0 + vLength;
            vertex(consumer, matrix, -ex * nearWidth, -ey * nearWidth, -ez * nearWidth, 0.0F, v0, red, green, blue, alpha);
            vertex(consumer, matrix, ex * nearWidth, ey * nearWidth, ez * nearWidth, 1.0F, v0, red, green, blue, alpha);
            vertex(consumer, matrix, endX + ex * farWidth, endY + ey * farWidth, endZ + ez * farWidth, 1.0F, v1, red, green, blue, alpha);
            vertex(consumer, matrix, endX - ex * farWidth, endY - ey * farWidth, endZ - ez * farWidth, 0.0F, v1, red, green, blue, alpha);
        }
        poseStack.popPose();
    }

    private static void drawGlow(PoseStack poseStack, VertexConsumer consumer, Camera camera, BeamInstance beam, float partialTick) {
        Vec3 source = beam.sourcePos(partialTick);
        if (outOfView(source)) {
            return;
        }
        float alpha = Mth.clamp(GLOW_OPACITY * beam.fadeFactor(partialTick), 0.0F, 1.0F);
        if (alpha <= 0.0F) {
            return;
        }
        Vec3 cameraPos = camera.position();
        int frame = Math.floorMod(beam.age(), GLOW_FRAMES);
        float u0 = frame / GLOW_SHEET_CELLS;
        float u1 = (frame + 1) / GLOW_SHEET_CELLS;
        float v0 = GLOW_ROW / GLOW_SHEET_CELLS;
        float v1 = (GLOW_ROW + 1) / GLOW_SHEET_CELLS;
        float red = Mth.clamp(beam.colorR(), 0.0F, 1.0F);
        float green = Mth.clamp(beam.colorG(), 0.0F, 1.0F);
        float blue = Mth.clamp(beam.colorB(), 0.0F, 1.0F);
        poseStack.pushPose();
        poseStack.translate(source.x - cameraPos.x, source.y - cameraPos.y, source.z - cameraPos.z);
        poseStack.mulPose(camera.rotation());
        Matrix4f matrix = poseStack.last().pose();
        vertex(consumer, matrix, -GLOW_HALF_SIZE, -GLOW_HALF_SIZE, 0.0, u0, v1, red, green, blue, alpha);
        vertex(consumer, matrix, GLOW_HALF_SIZE, -GLOW_HALF_SIZE, 0.0, u1, v1, red, green, blue, alpha);
        vertex(consumer, matrix, GLOW_HALF_SIZE, GLOW_HALF_SIZE, 0.0, u1, v0, red, green, blue, alpha);
        vertex(consumer, matrix, -GLOW_HALF_SIZE, GLOW_HALF_SIZE, 0.0, u0, v0, red, green, blue, alpha);
        poseStack.popPose();
    }

    private static int trunkSlot(BeamInstance beam) {
        int type = beam.beamType();
        return type >= 0 && type < TRUNK_SLOTS ? type : 0;
    }

    private static void vertex(VertexConsumer consumer, Matrix4f matrix, double x, double y, double z, float u, float v, float red, float green, float blue, float alpha) {
        consumer.addVertex(matrix, (float) x, (float) y, (float) z).setUv(u, v).setColor(red, green, blue, alpha);
    }
}
