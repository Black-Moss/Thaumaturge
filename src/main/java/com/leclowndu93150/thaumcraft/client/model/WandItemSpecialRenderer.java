package com.leclowndu93150.thaumcraft.client.model;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.api.wands.WandCap;
import com.leclowndu93150.thaumcraft.api.wands.WandRod;
import com.leclowndu93150.thaumcraft.client.fx.render.pipeline.TCRenderPipelines;
import com.leclowndu93150.thaumcraft.client.render.TCFlatRenderTypes;
import com.leclowndu93150.thaumcraft.content.casters.ItemFocus;
import com.leclowndu93150.thaumcraft.content.wands.WandParts;
import com.leclowndu93150.thaumcraft.content.wands.WandVisHelper;
import com.leclowndu93150.thaumcraft.registry.TCDataComponents;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

public final class WandItemSpecialRenderer implements SpecialModelRenderer<WandItemSpecialRenderer.WandArg> {
    public record WandArg(WandCap cap, WandRod rod, boolean sceptre, boolean hasFocus, int focusColor) {}

    private static final Identifier WAND_TEXTURE = TCIds.rl("textures/models/wand.png");
    private static final Identifier SCRIPT_TEXTURE = TCIds.rl("textures/misc/script.png");

    private static final RenderType RUNES = RenderType.create("tc_wand_runes",
            RenderSetup.builder(TCRenderPipelines.ENTITY_ADDITIVE_EMISSIVE)
                    .withTexture("Sampler0", SCRIPT_TEXTURE)
                    .useLightmap()
                    .createRenderSetup());

    private static final float PX = 0.0625F;
    private static final int TEX_W = 64;
    private static final int TEX_H = 32;
    private static final int RUNE_LIGHT = 200;
    private static final float MODEL_LIFT = 0.5F;
    private static final float FOCUS_ALPHA = 0.95F;
    private static final int SCEPTRE_RUNE_COUNT = 10;
    private static final int STAFF_RUNE_SIDES = 4;
    private static final int STAFF_RUNE_LENGTH = 14;
    private static final int SCRIPT_GLYPHS = 16;
    private static final float STAFF_MODEL_SHIFT = 0.2F;
    private static final float FOCUS_STAFF_LIFT = -0.0475F;
    private static final float FOCUS_STAFF_SCALE_Y = 0.5525F;
    private static final float FOCUS_SCALE = 0.5F;
    private static final float FOCUS_TOP_PX = 6.0F;
    private static final float CAP_TOP_PX = 1.0F;
    private static final float CAP_STAFF_SCALE_Y = 1.1F;
    private static final float SCEPTRE_CAP_SCALE = 1.3F;

    @Override
    public void submit(@Nullable WandArg arg, PoseStack poseStack, SubmitNodeCollector collector,
                       int light, int overlay, boolean glint, int seed) {
        if (arg == null) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.5F, MODEL_LIFT, 0.5F);
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
        submitParts(arg, poseStack, collector, light);
        poseStack.popPose();
    }

    public static void submitParts(WandArg arg, PoseStack poseStack, SubmitNodeCollector collector, int light) {
        boolean staff = arg.rod().staff();
        boolean runes = arg.rod().runes();
        float ticks = clientTicks();

        poseStack.pushPose();
        if (staff) {
            poseStack.translate(0.0F, 0.2F, 0.0F);
        }

        int rodLight = arg.rod().glow() ? (int) (200.0F + Mth.sin((int) ticks) * 5.0F + 5.0F) : light;
        RenderType rodType = TCFlatRenderTypes.entityCutoutFlat(arg.rod().texture());
        poseStack.pushPose();
        if (staff) {
            poseStack.translate(0.0F, -0.1F, 0.0F);
            poseStack.scale(1.2F, 2.0F, 1.2F);
        }
        PoseStack.Pose rodPose = poseStack.last().copy();
        int rodLightFinal = rodLight;
        collector.submitCustomGeometry(poseStack, rodType, (pose, buffer) ->
                box(rodPose, buffer, -1.0F, 1.0F, -1.0F, 2, 18, 2, 0, 8, 0xFFFFFFFF, rodLightFinal));
        poseStack.popPose();

        RenderType capType = TCFlatRenderTypes.entityCutoutFlat(arg.cap().texture());
        poseStack.pushPose();
        if (staff) {
            poseStack.scale(1.3F, 1.1F, 1.3F);
        } else {
            poseStack.scale(1.2F, 1.0F, 1.2F);
        }
        if (arg.sceptre()) {
            poseStack.pushPose();
            poseStack.scale(1.3F, 1.3F, 1.3F);
            submitCap(poseStack, collector, capType, 0.0F, light);
            poseStack.popPose();
            poseStack.pushPose();
            poseStack.translate(0.0F, 0.3F, 0.0F);
            poseStack.scale(1.0F, 0.66F, 1.0F);
            submitCap(poseStack, collector, capType, 0.0F, light);
            poseStack.popPose();
        } else {
            submitCap(poseStack, collector, capType, 0.0F, light);
        }
        if (staff) {
            poseStack.translate(0.0F, 0.225F, 0.0F);
            poseStack.pushPose();
            poseStack.scale(1.0F, 0.66F, 1.0F);
            submitCap(poseStack, collector, capType, 0.0F, light);
            poseStack.popPose();
            poseStack.translate(0.0F, 0.65F, 0.0F);
        }
        submitCap(poseStack, collector, capType, 20.0F, light);
        poseStack.popPose();

        if (arg.hasFocus()) {
            RenderType focusType = TCFlatRenderTypes.entityTranslucentFlat(WAND_TEXTURE);
            poseStack.pushPose();
            if (staff) {
                poseStack.translate(0.0F, -0.0475F, 0.0F);
                poseStack.scale(0.525F, 0.5525F, 0.525F);
            } else {
                poseStack.scale(0.5F, 0.5F, 0.5F);
            }
            int tint = ARGB.color((int) (FOCUS_ALPHA * 255.0F), arg.focusColor());
            int focusLight = (int) (195.0F + Mth.sin(ticks / 3.0F) * 10.0F + 10.0F);
            PoseStack.Pose focusPose = poseStack.last().copy();
            collector.submitCustomGeometry(poseStack, focusType, (pose, buffer) ->
                    box(focusPose, buffer, -3.0F, -6.0F, -3.0F, 6, 6, 6, 0, 0, tint, focusLight, true));
            poseStack.popPose();
        }

        if (arg.sceptre()) {
            for (int rot = 0; rot < SCEPTRE_RUNE_COUNT; rot++) {
                poseStack.pushPose();
                poseStack.mulPose(Axis.YP.rotationDegrees(36.0F * rot + ticks));
                submitRune(poseStack, collector, 0.16F, -0.01F, -0.125F, rot, ticks);
                poseStack.popPose();
            }
        }

        if (runes) {
            poseStack.pushPose();
            for (int rot = 0; rot < STAFF_RUNE_SIDES; rot++) {
                poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
                for (int a = 0; a < STAFF_RUNE_LENGTH; a++) {
                    int rune = (a + rot * 3) % SCRIPT_GLYPHS;
                    submitRune(poseStack, collector, 0.36F + a * 0.14F, -0.01F, -0.08F, rune, ticks);
                }
            }
            poseStack.popPose();
        }

        poseStack.popPose();
    }

    public static float tipModelY(WandArg arg) {
        boolean staff = arg.rod().staff();
        if (arg.hasFocus()) {
            if (staff) {
                return STAFF_MODEL_SHIFT + FOCUS_STAFF_LIFT - FOCUS_TOP_PX * PX * FOCUS_STAFF_SCALE_Y;
            }
            return -FOCUS_TOP_PX * PX * FOCUS_SCALE;
        }
        if (staff) {
            return STAFF_MODEL_SHIFT - CAP_TOP_PX * PX * CAP_STAFF_SCALE_Y;
        }
        if (arg.sceptre()) {
            return -CAP_TOP_PX * PX * SCEPTRE_CAP_SCALE;
        }
        return -CAP_TOP_PX * PX;
    }

    private static float clientTicks() {
        LocalPlayer player = Minecraft.getInstance().player;
        return player == null ? 0.0F : player.tickCount + Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
    }

    private static void submitCap(PoseStack poseStack, SubmitNodeCollector collector, RenderType type,
                           float rotationPointY, int light) {
        poseStack.pushPose();
        poseStack.translate(0.0F, rotationPointY * PX, 0.0F);
        PoseStack.Pose pose = poseStack.last().copy();
        collector.submitCustomGeometry(poseStack, type, (p, buffer) ->
                box(pose, buffer, -1.0F, -1.0F, -1.0F, 2, 2, 2, 0, 0, 0xFFFFFFFF, light, true));
        poseStack.popPose();
    }

    private static void submitRune(PoseStack poseStack, SubmitNodeCollector collector,
                            float x, float y, float z, int rune, float ticks) {
        float r = Mth.sin((ticks + rune * 5) / 5.0F) * 0.1F + 0.88F;
        float g = Mth.sin((ticks + rune * 5) / 7.0F) * 0.1F + 0.63F;
        float wobble = Mth.sin((ticks + rune * 5) / 10.0F) * 0.2F;
        int tint = ARGB.colorFromFloat(Math.min(1.0F, wobble + 0.6F), Math.min(1.0F, r), Math.min(1.0F, g), 0.2F);
        float u0 = 0.0625F * rune;
        float u1 = u0 + 0.0625F;
        float half = 0.06F + wobble / 40.0F;
        poseStack.pushPose();
        poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
        poseStack.translate(x, y, z);
        PoseStack.Pose pose = poseStack.last().copy();
        collector.submitCustomGeometry(poseStack, RUNES, (p, buffer) -> {
            buffer.addVertex(pose, -half, half, 0.0F).setColor(tint).setUv(u1, 1.0F)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(RUNE_LIGHT).setNormal(pose, 0.0F, 0.0F, 1.0F);
            buffer.addVertex(pose, half, half, 0.0F).setColor(tint).setUv(u1, 0.0F)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(RUNE_LIGHT).setNormal(pose, 0.0F, 0.0F, 1.0F);
            buffer.addVertex(pose, half, -half, 0.0F).setColor(tint).setUv(u0, 0.0F)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(RUNE_LIGHT).setNormal(pose, 0.0F, 0.0F, 1.0F);
            buffer.addVertex(pose, -half, -half, 0.0F).setColor(tint).setUv(u0, 1.0F)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(RUNE_LIGHT).setNormal(pose, 0.0F, 0.0F, 1.0F);
        });
        poseStack.popPose();
    }

    private static void box(PoseStack.Pose pose, VertexConsumer buffer, float x, float y, float z,
                            int dx, int dy, int dz, int u, int v, int tint, int light) {
        box(pose, buffer, x, y, z, dx, dy, dz, u, v, tint, light, false);
    }

    private static void box(PoseStack.Pose pose, VertexConsumer buffer, float x, float y, float z,
                            int dx, int dy, int dz, int u, int v, int tint, int light, boolean opaqueTop) {
        float x0 = x * PX;
        float y0 = y * PX;
        float z0 = z * PX;
        float x1 = (x + dx) * PX;
        float y1 = (y + dy) * PX;
        float z1 = (z + dz) * PX;
        float uPx = u;
        float vPx = v;
        float topU = opaqueTop ? uPx + dz + dx : uPx + dz;
        quad(pose, buffer, tint, light, 0.0F, -1.0F, 0.0F,
                x0, y0, z1, x1, y0, z1, x1, y0, z0, x0, y0, z0,
                topU, vPx, topU + dx, vPx + dz);
        quad(pose, buffer, tint, light, 0.0F, 1.0F, 0.0F,
                x0, y1, z0, x1, y1, z0, x1, y1, z1, x0, y1, z1,
                uPx + dz + dx, vPx, uPx + dz + dx + dx, vPx + dz);
        quad(pose, buffer, tint, light, 0.0F, 0.0F, -1.0F,
                x1, y0, z0, x1, y1, z0, x0, y1, z0, x0, y0, z0,
                uPx + dz, vPx + dz, uPx + dz + dx, vPx + dz + dy);
        quad(pose, buffer, tint, light, 0.0F, 0.0F, 1.0F,
                x0, y0, z1, x0, y1, z1, x1, y1, z1, x1, y0, z1,
                uPx + dz + dx + dz, vPx + dz, uPx + dz + dx + dz + dx, vPx + dz + dy);
        quad(pose, buffer, tint, light, -1.0F, 0.0F, 0.0F,
                x0, y0, z0, x0, y1, z0, x0, y1, z1, x0, y0, z1,
                uPx, vPx + dz, uPx + dz, vPx + dz + dy);
        quad(pose, buffer, tint, light, 1.0F, 0.0F, 0.0F,
                x1, y0, z1, x1, y1, z1, x1, y1, z0, x1, y0, z0,
                uPx + dz + dx, vPx + dz, uPx + dz + dx + dz, vPx + dz + dy);
    }

    private static void quad(PoseStack.Pose pose, VertexConsumer buffer, int tint, int light,
                             float nx, float ny, float nz,
                             float ax, float ay, float az, float bx, float by, float bz,
                             float cx, float cy, float cz, float ex, float ey, float ez,
                             float uMin, float vMin, float uMax, float vMax) {
        float u0 = uMax / TEX_W;
        float u1 = uMin / TEX_W;
        float v0 = vMin / TEX_H;
        float v1 = vMax / TEX_H;
        buffer.addVertex(pose, ax, ay, az).setColor(tint).setUv(u0, v0)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, nx, ny, nz);
        buffer.addVertex(pose, bx, by, bz).setColor(tint).setUv(u1, v0)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, nx, ny, nz);
        buffer.addVertex(pose, cx, cy, cz).setColor(tint).setUv(u1, v1)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, nx, ny, nz);
        buffer.addVertex(pose, ex, ey, ez).setColor(tint).setUv(u0, v1)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, nx, ny, nz);
    }

    @Override
    public void getExtents(Consumer<Vector3fc> consumer) {
        consumer.accept(new Vector3f(-0.3F, -0.2F, -0.3F));
        consumer.accept(new Vector3f(0.3F, 1.6F, 0.3F));
    }

    @Override
    public @Nullable WandArg extractArgument(ItemStack stack) {
        return extract(stack);
    }

    public static WandArg extract(ItemStack stack) {
        WandParts parts = WandVisHelper.getParts(stack);
        ItemStack focusStack = ItemStack.EMPTY;
        var template = stack.get(TCDataComponents.SOCKETED_FOCUS.get());
        if (template != null) {
            focusStack = template.create();
        }
        boolean hasFocus = focusStack.getItem() instanceof ItemFocus;
        int color = hasFocus ? ItemFocus.getFocusColor(focusStack) : 0xFFFFFF;
        return new WandArg(parts.cap(), parts.rod(), parts.sceptre(), hasFocus, color);
    }

    public record Unbaked() implements SpecialModelRenderer.Unbaked<WandArg> {
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(Unbaked::new);

        @Override
        public @Nullable SpecialModelRenderer<WandArg> bake(BakingContext context) {
            return new WandItemSpecialRenderer();
        }

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }
    }
}
