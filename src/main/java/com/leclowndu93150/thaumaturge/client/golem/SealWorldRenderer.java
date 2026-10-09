package com.leclowndu93150.thaumaturge.client.golem;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealArea;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealPos;
import com.leclowndu93150.thaumaturge.client.effect.rendertype.SealRenderTypes;
import com.leclowndu93150.thaumaturge.content.golem.seals.ClientSealHolder;
import com.leclowndu93150.thaumaturge.content.golem.seals.SealEntity;
import com.leclowndu93150.thaumaturge.content.particle.SparkParticleOptions;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class SealWorldRenderer {
    private static final String TEXTURES_PREFIX = "textures/";
    private static final String PNG_SUFFIX = ".png";
    private static final String DEFAULT_ICON_PREFIX = "item/seal_";

    private static final double MAX_DISTANCE_SQ = 256.0;
    private static final float HALF = 0.5F;
    private static final float RING_OFFSET = 0.51F;
    private static final float RING_HALF_SIZE = 0.45F;
    private static final float RING_ALPHA = 0.8F;
    private static final float ICON_OFFSET = 0.55F;
    private static final float ICON_HALF_SIZE = 0.25F;
    private static final float CORNER_ALPHA = 0.7F;
    private static final float BRACKET_OFFSET = 0.49F;
    private static final float SPARK_OFFSET = 0.66F;
    private static final float SPARK_SCALE = 2.0F;
    private static final float SPARK_ALPHA = 1.0F;
    private static final float SPARK_RED_BASE = 0.6F;
    private static final float SPARK_RED_SPREAD = 0.2F;
    private static final float SPARK_CHANCE_DIVISOR = 12.0F;
    private static final float HALTED_SHADE = 0.5F;
    private static final float FULL_SHADE = 1.0F;
    private static final float SHIMMER_BASE = 0.7F;
    private static final float SHIMMER_AMPLITUDE = 0.1F;
    private static final float RED_PERIOD = 4.0F;
    private static final float GREEN_PERIOD = 5.0F;
    private static final float BLUE_PERIOD = 6.0F;
    private static final int SPIN_PERIOD_TICKS = 360;
    private static final int AXIS_COUNT = 3;
    private static final int CORNER_COUNT = 8;
    private static final int DYE_COLOR_COUNT = 16;

    private SealWorldRenderer() {}

    @SubscribeEvent
    public static void onRender(RenderLevelStageEvent.AfterWeather event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        ClientLevel level = mc.level;
        if (player == null || level == null || mc.options.hideGui || !GolemRenderer.isHoldingSealDisplayer(player)) {
            return;
        }
        Map<SealPos, SealEntity> seals = ClientSealHolder.all();
        if (seals.isEmpty()) {
            return;
        }
        boolean seeThrough = player.isShiftKeyDown();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        float partial = mc.getDeltaTracker().getGameTimeDeltaPartialTick(true);
        Overlay overlay = new Overlay(level, player, event.getPoseStack().last().pose(), mc.gameRenderer.getMainCamera().position(), partial, mc.getAtlasManager().getAtlasOrThrow(AtlasIds.ITEMS));
        List<SealFrame> frames = overlay.collect(seals.values());
        flush(buffers, seeThrough ? SealRenderTypes.RING_SEE_THROUGH : SealRenderTypes.RING, frames, overlay::ring);
        flush(buffers, seeThrough ? SealRenderTypes.ICON_SEE_THROUGH : SealRenderTypes.ICON, frames, overlay::icon);
        flush(buffers, seeThrough ? SealRenderTypes.CORNER_SEE_THROUGH : SealRenderTypes.CORNER, frames, overlay::corners);
    }

    private static void flush(MultiBufferSource.BufferSource buffers, RenderType type, List<SealFrame> frames, BiConsumer<SealFrame, VertexConsumer> painter) {
        VertexConsumer consumer = buffers.getBuffer(type);
        frames.forEach(frame -> painter.accept(frame, consumer));
        buffers.endBatch(type);
    }

    private static Identifier iconSprite(SealEntity seal) {
        Identifier typeId = seal.typeId();
        String path = seal.type().icon().map(Identifier::getPath).map(SealWorldRenderer::stripTexturePath).orElse(DEFAULT_ICON_PREFIX + typeId.getPath());
        return typeId.withPath(path);
    }

    private static String stripTexturePath(String path) {
        String withoutDir = path.startsWith(TEXTURES_PREFIX) ? path.substring(TEXTURES_PREFIX.length()) : path;
        int extension = withoutDir.length() - PNG_SUFFIX.length();
        return withoutDir.endsWith(PNG_SUFFIX) ? withoutDir.substring(0, extension) : withoutDir;
    }

    private record SealFrame(SealEntity seal, Vector3f origin, Vector3f normal, Vector3f right, Vector3f up, float[] tint, float fade) {
    }

    private static final class Overlay {
        private final float time;
        private final float spinCos;
        private final float spinSin;
        private final float partial;
        private final Matrix4f pose;
        private final Vec3 camera;
        private final TextureAtlas itemAtlas;
        private final LocalPlayer player;
        private final ClientLevel level;

        Overlay(ClientLevel level, LocalPlayer player, Matrix4f pose, Vec3 camera, float partial, TextureAtlas itemAtlas) {
            float spin = ((player.tickCount % SPIN_PERIOD_TICKS) + partial) * Mth.DEG_TO_RAD;
            this.spinCos = Mth.cos(spin);
            this.spinSin = Mth.sin(spin);
            this.time = player.tickCount + partial;
            this.partial = partial;
            this.itemAtlas = itemAtlas;
            this.camera = camera;
            this.pose = pose;
            this.player = player;
            this.level = level;
        }

        List<SealFrame> collect(Collection<SealEntity> seals) {
            return seals.stream().map(this::frameOf).filter(Objects::nonNull).toList();
        }

        private @Nullable SealFrame frameOf(SealEntity seal) {
            SealPos location = seal.pos();
            BlockPos block = location.pos();
            double distanceSq = player.distanceToSqr(block.getX() + HALF, block.getY() + HALF, block.getZ() + HALF);
            if (distanceSq > MAX_DISTANCE_SQ) {
                return null;
            }
            Direction face = location.face();
            float fade = (float) (1.0 - distanceSq / MAX_DISTANCE_SQ);
            Vector3f normal = new Vector3f(face.getStepX(), face.getStepY(), face.getStepZ());
            Vector3f up = upAxis(face);
            Vector3f right = normal.cross(up, new Vector3f());
            Vector3f origin = new Vector3f((float) (block.getX() + HALF - camera.x), (float) (block.getY() + HALF - camera.y), (float) (block.getZ() + HALF - camera.z));
            return new SealFrame(seal, origin, normal, right, up, tint(seal.color(), block), fade);
        }

        private static Vector3f upAxis(Direction face) {
            return switch (face) {
                case UP -> new Vector3f(0.0F, 0.0F, -1.0F);
                case DOWN -> new Vector3f(0.0F, 0.0F, 1.0F);
                default -> new Vector3f(0.0F, 1.0F, 0.0F);
            };
        }

        private float[] tint(byte colorByte, BlockPos block) {
            if (colorByte >= 1 && colorByte <= DYE_COLOR_COUNT) {
                int rgb = DyeColor.byId(colorByte - 1).getTextureDiffuseColor();
                return new float[]{ARGB.redFloat(rgb), ARGB.greenFloat(rgb), ARGB.blueFloat(rgb)};
            }
            return new float[]{shimmer(block.getX(), RED_PERIOD), shimmer(block.getY(), GREEN_PERIOD), shimmer(block.getZ(), BLUE_PERIOD)};
        }

        private float shimmer(int coordinate, float period) {
            return SHIMMER_BASE + SHIMMER_AMPLITUDE * Mth.sin((time + coordinate) / period);
        }

        private Vector3f liftedCentre(SealFrame frame, float offset) {
            Vector3f lift = frame.normal().mul(offset, new Vector3f());
            return frame.origin().add(lift, new Vector3f());
        }

        void ring(SealFrame frame, VertexConsumer buffer) {
            Vector3f spunUp = spin(frame.normal(), frame.up()).mul(RING_HALF_SIZE);
            Vector3f spunRight = spin(frame.normal(), frame.right()).mul(RING_HALF_SIZE);
            quad(buffer, liftedCentre(frame, RING_OFFSET), spunRight, spunUp, 0.0F, 0.0F, 1.0F, 1.0F, frame.tint(), RING_ALPHA * frame.fade());
        }

        private Vector3f spin(Vector3f normal, Vector3f axis) {
            Vector3f turned = normal.cross(axis, new Vector3f()).mul(spinSin);
            return axis.mul(spinCos, new Vector3f()).add(turned);
        }

        private float iconShade(SealEntity seal, BlockPos block, Vector3f normal) {
            if (!seal.isStoppedByRedstone(level)) {
                return FULL_SHADE;
            }
            if (level.getRandom().nextFloat() >= partial / SPARK_CHANCE_DIVISOR) {
                return HALTED_SHADE;
            }
            float red = SPARK_RED_BASE + level.getRandom().nextFloat() * SPARK_RED_SPREAD;
            int color = ARGB.colorFromFloat(SPARK_ALPHA, red, 0.0F, 0.0F);
            double x = block.getX() + HALF + normal.x * SPARK_OFFSET;
            double y = block.getY() + HALF + normal.y * SPARK_OFFSET;
            double z = block.getZ() + HALF + normal.z * SPARK_OFFSET;
            level.addParticle(new SparkParticleOptions(color, SPARK_ALPHA, SPARK_SCALE), x, y, z, 0.0, 0.0, 0.0);
            return FULL_SHADE;
        }

        void icon(SealFrame frame, VertexConsumer buffer) {
            SealEntity seal = frame.seal();
            float shade = iconShade(seal, seal.pos().pos(), frame.normal());
            TextureAtlasSprite sprite = itemAtlas.getSprite(iconSprite(seal));
            Vector3f halfRight = frame.right().mul(ICON_HALF_SIZE, new Vector3f());
            Vector3f halfUp = frame.up().mul(ICON_HALF_SIZE, new Vector3f());
            quad(buffer, liftedCentre(frame, ICON_OFFSET), halfRight, halfUp, sprite.getU0(), sprite.getV0(), sprite.getU1(), sprite.getV1(), new float[]{shade, shade, shade}, frame.fade());
        }

        void corners(SealFrame frame, VertexConsumer buffer) {
            if (frame.seal().type().hasArea()) {
                drawCorners(buffer, SealArea.bounds(frame.seal()), frame.tint(), CORNER_ALPHA * frame.fade());
            }
        }

        private void drawCorners(VertexConsumer corner, AABB box, float[] tint, float alpha) {
            Direction.Axis[] axes = Direction.Axis.values();
            for (int mask = 0; mask < CORNER_COUNT; mask++) {
                Vector3f blockCentre = new Vector3f();
                float[] outward = new float[AXIS_COUNT];
                for (int axis = 0; axis < AXIS_COUNT; axis++) {
                    boolean high = (mask >> axis & 1) != 0;
                    double centre = high ? box.max(axes[axis]) - HALF : box.min(axes[axis]) + HALF;
                    blockCentre.setComponent(axis, (float) (centre - camera.get(axes[axis])));
                    outward[axis] = high ? 1.0F : -1.0F;
                }
                for (int axis = 0; axis < AXIS_COUNT; axis++) {
                    drawBracket(corner, blockCentre, outward, axis, tint, alpha);
                }
            }
        }

        private void drawBracket(VertexConsumer corner, Vector3f blockCentre, float[] outward, int axis, float[] tint, float alpha) {
            int first = axis == 0 ? 1 : 0;
            int second = axis == 2 ? 1 : 2;
            Vector3f centre = new Vector3f(blockCentre);
            centre.setComponent(axis, centre.get(axis) + outward[axis] * BRACKET_OFFSET);
            Vector3f right = new Vector3f().setComponent(first, -outward[first] * HALF);
            Vector3f up = new Vector3f().setComponent(second, outward[second] * HALF);
            quad(corner, centre, right, up, 0.0F, 0.0F, 1.0F, 1.0F, tint, alpha);
        }

        private void quad(VertexConsumer buffer, Vector3f centre, Vector3f right, Vector3f up, float u0, float v0, float u1, float v1, float[] tint, float alpha) {
            vertex(buffer, centre.x - right.x + up.x, centre.y - right.y + up.y, centre.z - right.z + up.z, u0, v0, tint, alpha);
            vertex(buffer, centre.x + right.x + up.x, centre.y + right.y + up.y, centre.z + right.z + up.z, u1, v0, tint, alpha);
            vertex(buffer, centre.x + right.x - up.x, centre.y + right.y - up.y, centre.z + right.z - up.z, u1, v1, tint, alpha);
            vertex(buffer, centre.x - right.x - up.x, centre.y - right.y - up.y, centre.z - right.z - up.z, u0, v1, tint, alpha);
        }

        private void vertex(VertexConsumer buffer, float x, float y, float z, float u, float v, float[] tint, float alpha) {
            buffer.addVertex(pose, x, y, z).setUv(u, v).setColor(tint[0], tint[1], tint[2], alpha);
        }
    }
}
