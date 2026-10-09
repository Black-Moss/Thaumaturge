package com.leclowndu93150.thaumaturge.client.effect.manager;

import com.leclowndu93150.thaumaturge.client.effect.geometry.PolyCone;
import com.leclowndu93150.thaumaturge.client.effect.instance.ArcInstance;
import com.leclowndu93150.thaumaturge.client.effect.instance.BeamInstance;
import com.leclowndu93150.thaumaturge.client.effect.instance.BoltInstance;
import com.leclowndu93150.thaumaturge.client.effect.rendertype.ArcRenderType;
import com.leclowndu93150.thaumaturge.client.effect.rendertype.BeamRenderType;
import com.leclowndu93150.thaumaturge.client.effect.rendertype.BoltRenderType;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public final class BeamManager extends AbstractFXManager<IFXInstance> {
    public static final BeamManager INSTANCE = new BeamManager();

    private static final List<ArcInstance> ARCS = new ArrayList<>();
    private static final List<BoltInstance> BOLTS = new ArrayList<>();
    private static final List<BeamInstance> BEAMS = new ArrayList<>();

    private static final float ARC_HALF_SIZE = 0.125F;
    private static final float BOLT_RADIUS_DIVISOR = 10.0F;
    private static final float BOLT_CORE_DIVISOR = 3.0F;
    private static final float BOLT_TEX_SLICE = 1.0F;
    private static final int MIN_BOLT_POINTS = 3;
    private static final int COLOUR_CHANNELS = 4;
    private static final int POSITION_AXES = 3;
    private static final int TRUNK_SLOTS = 4;
    private static final int STRIPS = 3;
    private static final float STRIP_SPACING_DEGREES = 60.0F;
    private static final float YAW_FLIP_DEGREES = 180.0F;
    private static final float PITCH_TURN_DEGREES = 90.0F;
    private static final float STRIP_HALF_WIDTH = 0.15F;
    private static final float STRIP_V_BASE = -1.0F;
    private static final int GLOW_GRID = 32;
    private static final int GLOW_ROW = 3;
    private static final float GLOW_HALF_SIZE = 0.33F;
    private static final float GLOW_OPACITY = 0.8F;
    private static final float GLOW_FADE_STEP = 0.2F;
    private static final int FADE_TICKS = 4;

    private BeamManager() {}

    public static void addArc(ArcInstance arc) {
        ARCS.add(arc);
    }

    public static void addBolt(BoltInstance bolt) {
        BOLTS.add(bolt);
    }

    public static void addBeam(BeamInstance beam) {
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
            poseStack.pushPose();
            poseStack.translate(arc.origin.x - cameraPos.x, arc.origin.y - cameraPos.y, arc.origin.z - cameraPos.z);
            drawArc(consumer, poseStack.last().pose(), arc, alpha);
            poseStack.popPose();
        }
        buffers.endBatch(type);
    }

    private static void drawArc(VertexConsumer consumer, Matrix4f matrix, ArcInstance arc, float alpha) {
        float uScale = arc.chordLength > 0.0F ? 1.0F / arc.chordLength : 0.0F;
        for (int i = 0; i < arc.path.size() - 1; i++) {
            Vec3 from = arc.path.get(i);
            Vec3 to = arc.path.get(i + 1);
            float u0 = i * uScale;
            float u1 = (i + 1) * uScale;
            vertex(consumer, matrix, from.x, from.y - ARC_HALF_SIZE, from.z, u0, 1.0F, arc.colorR, arc.colorG, arc.colorB, alpha);
            vertex(consumer, matrix, from.x, from.y + ARC_HALF_SIZE, from.z, u0, 0.0F, arc.colorR, arc.colorG, arc.colorB, alpha);
            vertex(consumer, matrix, to.x, to.y + ARC_HALF_SIZE, to.z, u1, 0.0F, arc.colorR, arc.colorG, arc.colorB, alpha);
            vertex(consumer, matrix, to.x, to.y - ARC_HALF_SIZE, to.z, u1, 1.0F, arc.colorR, arc.colorG, arc.colorB, alpha);
            vertex(consumer, matrix, from.x - ARC_HALF_SIZE, from.y, from.z - ARC_HALF_SIZE, u0, 1.0F, arc.colorR, arc.colorG, arc.colorB, alpha);
            vertex(consumer, matrix, from.x + ARC_HALF_SIZE, from.y, from.z + ARC_HALF_SIZE, u0, 0.0F, arc.colorR, arc.colorG, arc.colorB, alpha);
            vertex(consumer, matrix, to.x + ARC_HALF_SIZE, to.y, to.z + ARC_HALF_SIZE, u1, 0.0F, arc.colorR, arc.colorG, arc.colorB, alpha);
            vertex(consumer, matrix, to.x - ARC_HALF_SIZE, to.y, to.z - ARC_HALF_SIZE, u1, 1.0F, arc.colorR, arc.colorG, arc.colorB, alpha);
        }
    }

    private static void renderBolts(PoseStack poseStack, MultiBufferSource.BufferSource buffers, Vec3 cameraPos, float partialTick) {
        if (BOLTS.isEmpty()) {
            return;
        }
        RenderType type = BoltRenderType.RENDER_TYPE;
        VertexConsumer consumer = buffers.getBuffer(type);
        for (BoltInstance bolt : BOLTS) {
            List<BoltInstance.PathStep> path = bolt.computePath(partialTick);
            if (path.size() < MIN_BOLT_POINTS) {
                continue;
            }
            int count = path.size();
            double[][] points = new double[count][POSITION_AXES];
            float[][] colours = new float[count][COLOUR_CHANNELS];
            double[] radii = new double[count];
            float alpha = bolt.alpha(partialTick);
            for (int i = 0; i < count; i++) {
                BoltInstance.PathStep step = path.get(i);
                points[i][0] = step.x();
                points[i][1] = step.y();
                points[i][2] = step.z();
                colours[i][0] = bolt.colorR;
                colours[i][1] = bolt.colorG;
                colours[i][2] = bolt.colorB;
                colours[i][3] = alpha;
                radii[i] = step.width() / BOLT_RADIUS_DIVISOR;
            }
            poseStack.pushPose();
            poseStack.translate(bolt.startX - cameraPos.x, bolt.startY - cameraPos.y, bolt.startZ - cameraPos.z);
            PolyCone.render(poseStack, consumer, points, colours, radii, 0, BOLT_TEX_SLICE, 0.0F);
            for (int i = 0; i < count; i++) {
                radii[i] /= BOLT_CORE_DIVISOR;
            }
            PolyCone.render(poseStack, consumer, points, colours, radii, 0, BOLT_TEX_SLICE, 0.0F);
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

    private static int trunkSlot(BeamInstance beam) {
        int type = beam.beamType();
        return type >= 0 && type < TRUNK_SLOTS ? type : 0;
    }

    private static void drawTrunk(PoseStack poseStack, VertexConsumer consumer, Vec3 cameraPos, BeamInstance beam, float partialTick) {
        float opacity = beam.computeOpacity(partialTick);
        float size = beam.computeSize(partialTick);
        if (opacity <= 0.0F || size <= 0.0F) {
            return;
        }
        Vec3 source = beam.sourcePos(partialTick);
        float stripLength = (float) source.distanceTo(beam.targetPos(partialTick)) * size;
        float sourceHalf = STRIP_HALF_WIDTH * size;
        float farHalf = sourceHalf * beam.endMod();
        float scroll = beam.texScroll(partialTick);
        float worldRotation = beam.worldRotation(partialTick);
        poseStack.pushPose();
        poseStack.translate(source.x - cameraPos.x, source.y - cameraPos.y, source.z - cameraPos.z);
        poseStack.mulPose(Axis.YP.rotationDegrees(beam.yawAt(partialTick) + YAW_FLIP_DEGREES));
        poseStack.mulPose(Axis.XP.rotationDegrees(beam.pitchAt(partialTick) + PITCH_TURN_DEGREES));
        for (int strip = 0; strip < STRIPS; strip++) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(worldRotation + STRIP_SPACING_DEGREES * (strip + 1)));
            Matrix4f matrix = poseStack.last().pose();
            float sourceV = STRIP_V_BASE + scroll + (float) strip / STRIPS;
            float farV = sourceV + stripLength;
            vertex(consumer, matrix, -farHalf, stripLength, 0.0F, 1.0F, farV, beam.colorR(), beam.colorG(), beam.colorB(), opacity);
            vertex(consumer, matrix, -sourceHalf, 0.0F, 0.0F, 1.0F, sourceV, beam.colorR(), beam.colorG(), beam.colorB(), opacity);
            vertex(consumer, matrix, sourceHalf, 0.0F, 0.0F, 0.0F, sourceV, beam.colorR(), beam.colorG(), beam.colorB(), opacity);
            vertex(consumer, matrix, farHalf, stripLength, 0.0F, 0.0F, farV, beam.colorR(), beam.colorG(), beam.colorB(), opacity);
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    private static void drawGlow(PoseStack poseStack, VertexConsumer consumer, Camera camera, BeamInstance beam, float partialTick) {
        int remaining = beam.maxAge() - beam.age();
        float opacity = remaining > FADE_TICKS ? GLOW_OPACITY : Math.max(0.0F, GLOW_OPACITY - (FADE_TICKS - remaining) * GLOW_FADE_STEP);
        if (opacity <= 0.0F) {
            return;
        }
        Vec3 source = beam.sourcePos(partialTick);
        Vec3 cameraPos = camera.position();
        float u0 = (float) (beam.age() % GLOW_GRID) / GLOW_GRID;
        float u1 = u0 + 1.0F / GLOW_GRID;
        float v0 = (float) GLOW_ROW / GLOW_GRID;
        float v1 = (float) (GLOW_ROW + 1) / GLOW_GRID;
        poseStack.pushPose();
        poseStack.translate(source.x - cameraPos.x, source.y - cameraPos.y, source.z - cameraPos.z);
        poseStack.mulPose(camera.rotation());
        Matrix4f matrix = poseStack.last().pose();
        vertex(consumer, matrix, -GLOW_HALF_SIZE, -GLOW_HALF_SIZE, 0.0F, u1, v1, beam.colorR(), beam.colorG(), beam.colorB(), opacity);
        vertex(consumer, matrix, -GLOW_HALF_SIZE, GLOW_HALF_SIZE, 0.0F, u1, v0, beam.colorR(), beam.colorG(), beam.colorB(), opacity);
        vertex(consumer, matrix, GLOW_HALF_SIZE, GLOW_HALF_SIZE, 0.0F, u0, v0, beam.colorR(), beam.colorG(), beam.colorB(), opacity);
        vertex(consumer, matrix, GLOW_HALF_SIZE, -GLOW_HALF_SIZE, 0.0F, u0, v1, beam.colorR(), beam.colorG(), beam.colorB(), opacity);
        poseStack.popPose();
    }

    private static void vertex(VertexConsumer consumer, Matrix4f matrix, double x, double y, double z, float u, float v, float red, float green, float blue, float alpha) {
        consumer.addVertex(matrix, (float) x, (float) y, (float) z).setUv(u, v).setColor(red, green, blue, alpha);
    }
}
