package com.leclowndu93150.thaumaturge.client.golem;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.golems.ISealDisplayer;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealArea;
import com.leclowndu93150.thaumaturge.client.effect.rendertype.SealRenderTypes;
import com.leclowndu93150.thaumaturge.content.golem.seals.ClientSealHolder;
import com.leclowndu93150.thaumaturge.content.golem.seals.SealEntity;
import com.leclowndu93150.thaumaturge.content.particle.SparkParticleOptions;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Collection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class SealWorldRenderer {
    private static final double MAX_DISTANCE_SQR = 256.0;
    private static final double ICON_OUT = 0.55;
    private static final double RING_OUT = 0.51;
    private static final double CORNER_IN = 0.49;
    private static final double SPARK_OUT = 0.66;
    private static final double ICON_HALF = 0.25;
    private static final double RING_HALF = 0.45;
    private static final double FACE_HALF = 0.5;
    private static final float RING_OPACITY = 0.8F;
    private static final float CORNER_OPACITY = 0.7F;
    private static final float STOPPED_SHADE = 0.5F;
    private static final float SPARK_CHANCE = 1.0F / 12.0F;
    private static final float SPARK_RED_MIN = 0.6F;
    private static final float SPARK_RED_SPREAD = 0.2F;
    private static final float SPARK_SCALE = 2.0F;
    private static final float SHIMMER_BASE = 0.7F;
    private static final float SHIMMER_SWING = 0.1F;
    private static final float SHIMMER_RED_PERIOD = 4.0F;
    private static final float SHIMMER_GREEN_PERIOD = 5.0F;
    private static final float SHIMMER_BLUE_PERIOD = 6.0F;
    private static final int FULL_TURN = 360;
    private static final int DYE_COUNT = 16;
    private static final String ICON_PREFIX = "textures/";
    private static final String ICON_SUFFIX = ".png";
    private static final String DEFAULT_ICON = "item/seal_";

    private SealWorldRenderer() {}

    @SubscribeEvent
    public static void onRender(RenderLevelStageEvent.AfterWeather event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        ClientLevel level = minecraft.level;
        if (level == null || player == null || minecraft.options.hideGui || !holdsDisplayer(player)) {
            return;
        }
        Collection<SealEntity> seals = ClientSealHolder.all().values();
        if (seals.isEmpty()) {
            return;
        }
        boolean seeThrough = player.isShiftKeyDown();
        float partial = minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        float time = player.tickCount + partial;
        double spin = Math.toRadians(player.tickCount % FULL_TURN + partial);
        Vec3 camera = minecraft.gameRenderer.getMainCamera().position();
        Matrix4f pose = event.getPoseStack().last().pose();
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        RenderType ringType = seeThrough ? SealRenderTypes.RING_SEE_THROUGH : SealRenderTypes.RING;
        RenderType iconType = seeThrough ? SealRenderTypes.ICON_SEE_THROUGH : SealRenderTypes.ICON;
        RenderType cornerType = seeThrough ? SealRenderTypes.CORNER_SEE_THROUGH : SealRenderTypes.CORNER;
        RandomSource random = level.getRandom();
        for (SealEntity seal : seals) {
            BlockPos block = seal.pos().pos();
            Vec3 centre = Vec3.atCenterOf(block);
            double distanceSqr = player.position().distanceToSqr(centre);
            if (distanceSqr > MAX_DISTANCE_SQR) {
                continue;
            }
            float fade = (float) (1.0 - distanceSqr / MAX_DISTANCE_SQR);
            Direction face = seal.pos().face();
            Vec3 normal = Vec3.atLowerCornerOf(face.getUnitVec3i());
            Vec3 up = iconUp(face);
            Vec3 right = normal.cross(up);
            int tint = tint(seal, block, time);
            Vec3 spunRight = right.scale(Math.cos(spin)).add(normal.cross(right).scale(Math.sin(spin)));
            Vec3 spunUp = up.scale(Math.cos(spin)).add(normal.cross(up).scale(Math.sin(spin)));
            quad(buffers.getBuffer(ringType), pose, centre.add(normal.scale(RING_OUT)).subtract(camera), spunRight.scale(RING_HALF), spunUp.scale(RING_HALF), 0.0F, 1.0F, 0.0F, 1.0F,
                    ARGB.color(ARGB.as8BitChannel(RING_OPACITY * fade), tint));
            float shade = 1.0F;
            if (seal.isStoppedByRedstone(level)) {
                if (random.nextFloat() < SPARK_CHANCE * partial) {
                    Vec3 at = centre.add(normal.scale(SPARK_OUT));
                    level.addParticle(new SparkParticleOptions(ARGB.colorFromFloat(1.0F, SPARK_RED_MIN + random.nextFloat() * SPARK_RED_SPREAD, 0.0F, 0.0F), 1.0F, SPARK_SCALE), at.x, at.y, at.z, 0.0,
                            0.0, 0.0);
                } else {
                    shade = STOPPED_SHADE;
                }
            }
            TextureAtlasSprite sprite = minecraft.getAtlasManager().getAtlasOrThrow(AtlasIds.ITEMS).getSprite(iconSprite(seal));
            quad(buffers.getBuffer(iconType), pose, centre.add(normal.scale(ICON_OUT)).subtract(camera), right.scale(ICON_HALF), up.scale(ICON_HALF), sprite.getU0(), sprite.getU1(), sprite.getV0(),
                    sprite.getV1(), ARGB.colorFromFloat(fade, shade, shade, shade));
            if (seal.type().hasArea()) {
                drawCorners(buffers.getBuffer(cornerType), pose, SealArea.bounds(seal), camera, ARGB.color(ARGB.as8BitChannel(CORNER_OPACITY * fade), tint));
            }
        }
        buffers.endBatch(ringType);
        buffers.endBatch(iconType);
        buffers.endBatch(cornerType);
    }

    private static boolean holdsDisplayer(LocalPlayer player) {
        return player.getMainHandItem().getItem() instanceof ISealDisplayer || player.getOffhandItem().getItem() instanceof ISealDisplayer;
    }

    private static Vec3 iconUp(Direction face) {
        return switch (face) {
            case UP -> Vec3.atLowerCornerOf(Direction.NORTH.getUnitVec3i());
            case DOWN -> Vec3.atLowerCornerOf(Direction.SOUTH.getUnitVec3i());
            default -> Vec3.atLowerCornerOf(Direction.UP.getUnitVec3i());
        };
    }

    private static int tint(SealEntity seal, BlockPos block, float time) {
        if (seal.color() >= 1 && seal.color() <= DYE_COUNT) {
            return DyeColor.byId(seal.color() - 1).getTextureDiffuseColor();
        }
        return ARGB.colorFromFloat(1.0F, SHIMMER_BASE + SHIMMER_SWING * Mth.sin((time + block.getX()) / SHIMMER_RED_PERIOD),
                SHIMMER_BASE + SHIMMER_SWING * Mth.sin((time + block.getY()) / SHIMMER_GREEN_PERIOD), SHIMMER_BASE + SHIMMER_SWING * Mth.sin((time + block.getZ()) / SHIMMER_BLUE_PERIOD));
    }

    private static Identifier iconSprite(SealEntity seal) {
        Identifier type = seal.typeId();
        return seal.type().icon().map(texture -> Identifier.fromNamespaceAndPath(texture.getNamespace(), stripTexturePath(texture.getPath())))
                .orElse(Identifier.fromNamespaceAndPath(type.getNamespace(), DEFAULT_ICON + type.getPath()));
    }

    private static String stripTexturePath(String path) {
        String trimmed = path.startsWith(ICON_PREFIX) ? path.substring(ICON_PREFIX.length()) : path;
        return trimmed.endsWith(ICON_SUFFIX) ? trimmed.substring(0, trimmed.length() - ICON_SUFFIX.length()) : trimmed;
    }

    private static void drawCorners(VertexConsumer buffer, Matrix4f pose, AABB box, Vec3 camera, int color) {
        for (int sx = -1; sx <= 1; sx += 2) {
            for (int sy = -1; sy <= 1; sy += 2) {
                for (int sz = -1; sz <= 1; sz += 2) {
                    Vec3 cornerBlock = new Vec3(sx < 0 ? box.minX + FACE_HALF : box.maxX - FACE_HALF, sy < 0 ? box.minY + FACE_HALF : box.maxY - FACE_HALF,
                            sz < 0 ? box.minZ + FACE_HALF : box.maxZ - FACE_HALF).subtract(camera);
                    Vec3 x = new Vec3(sx, 0.0, 0.0);
                    Vec3 y = new Vec3(0.0, sy, 0.0);
                    Vec3 z = new Vec3(0.0, 0.0, sz);
                    bracket(buffer, pose, cornerBlock, x, y, z, color);
                    bracket(buffer, pose, cornerBlock, y, x, z, color);
                    bracket(buffer, pose, cornerBlock, z, x, y, color);
                }
            }
        }
    }

    private static void bracket(VertexConsumer buffer, Matrix4f pose, Vec3 blockCentre, Vec3 outward, Vec3 firstEdge, Vec3 secondEdge, int color) {
        Vec3 a = firstEdge.scale(FACE_HALF);
        Vec3 b = secondEdge.scale(FACE_HALF);
        Vec3 centre = blockCentre.add(outward.scale(CORNER_IN));
        vertex(buffer, pose, centre.add(a).add(b), 0.0F, 0.0F, color);
        vertex(buffer, pose, centre.subtract(a).add(b), 1.0F, 0.0F, color);
        vertex(buffer, pose, centre.subtract(a).subtract(b), 1.0F, 1.0F, color);
        vertex(buffer, pose, centre.add(a).subtract(b), 0.0F, 1.0F, color);
    }

    private static void quad(VertexConsumer buffer, Matrix4f pose, Vec3 centre, Vec3 right, Vec3 up, float u0, float u1, float v0, float v1, int color) {
        vertex(buffer, pose, centre.subtract(right).add(up), u0, v0, color);
        vertex(buffer, pose, centre.add(right).add(up), u1, v0, color);
        vertex(buffer, pose, centre.add(right).subtract(up), u1, v1, color);
        vertex(buffer, pose, centre.subtract(right).subtract(up), u0, v1, color);
    }

    private static void vertex(VertexConsumer buffer, Matrix4f pose, Vec3 at, float u, float v, int color) {
        buffer.addVertex(pose, (float) at.x, (float) at.y, (float) at.z).setUv(u, v).setColor(color);
    }
}
