package com.leclowndu93150.thaumaturge.client.model;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.wands.WandCap;
import com.leclowndu93150.thaumaturge.api.wands.WandRod;
import com.leclowndu93150.thaumaturge.api.wands.render.WandRenderContext;
import com.leclowndu93150.thaumaturge.api.wands.render.WandRenderers;
import com.leclowndu93150.thaumaturge.client.casters.FocusColors;
import com.leclowndu93150.thaumaturge.client.effect.rendertype.TTFXRenderTypes;
import com.leclowndu93150.thaumaturge.client.render.BoxGeometry;
import com.leclowndu93150.thaumaturge.client.render.TTFlatRenderTypes;
import com.leclowndu93150.thaumaturge.content.spell.item.FocusItems;
import com.leclowndu93150.thaumaturge.content.wands.WandParts;
import com.leclowndu93150.thaumaturge.content.wands.WandVisHelper;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

public final class WandItemSpecialRenderer implements SpecialModelRenderer<WandItemSpecialRenderer.WandArg> {
    private static final Identifier FOCUS_TEXTURE = TTIds.rl("textures/models/wand.png");
    private static final Identifier RUNE_TEXTURE = TTIds.rl("textures/misc/script.png");
    private static final RenderType RUNE_TYPE = TTFXRenderTypes.entityAdditive(RUNE_TEXTURE);

    private static final int WHITE_TINT = 0xFFFFFFFF;
    private static final int WHITE_RGB = 0xFFFFFF;
    private static final int TEXTURE_SIZE = 32;
    private static final float PIXEL = 1.0F / 16.0F;
    private static final float ROOT_CENTER = 0.5F;
    private static final float FLIP_DEGREES = 180.0F;

    private static final int ORDER_SOLID = 0;
    private static final int ORDER_FOCUS = 1;
    private static final int ORDER_RUNES = 2;

    private static final float STAFF_GROUP_Y = 0.2F;
    private static final float STAFF_ROD_Y = -0.1F;
    private static final float STAFF_ROD_WIDTH = 1.2F;
    private static final float STAFF_ROD_LENGTH = 2.0F;
    private static final float STAFF_CAP_OUTER_XZ = 1.3F;
    private static final float STAFF_CAP_OUTER_Y = 1.1F;
    private static final float WAND_CAP_OUTER_XZ = 1.2F;
    private static final float WAND_CAP_OUTER_Y = 1.0F;

    private static final float STAFF_FOCUS_Y = -0.0475F;
    private static final float STAFF_FOCUS_XZ = 0.525F;
    private static final float STAFF_FOCUS_Y_SCALE = 0.5525F;
    private static final float WAND_FOCUS_SCALE = 0.5F;
    private static final int FOCUS_ALPHA = (int) (0.95F * 255.0F);
    private static final int FOCUS_LIGHT_BASE = 195;
    private static final int FOCUS_LIGHT_AMPLITUDE = 10;
    private static final float FOCUS_LIGHT_PERIOD = 3.0F;
    private static final int GLOW_LIGHT_BASE = 200;
    private static final int GLOW_LIGHT_AMPLITUDE = 5;

    private static final Fit GROUP_FIT = new Fit(STAFF_GROUP_Y, 1.0F, 1.0F);
    private static final Fit ROD_FIT = new Fit(STAFF_ROD_Y, STAFF_ROD_WIDTH, STAFF_ROD_LENGTH);
    private static final Fit FOCUS_STAFF_FIT = new Fit(STAFF_FOCUS_Y, STAFF_FOCUS_XZ, STAFF_FOCUS_Y_SCALE);
    private static final Fit FOCUS_PLAIN_FIT = new Fit(0.0F, WAND_FOCUS_SCALE, WAND_FOCUS_SCALE);
    private static final BoxSpec ROD_BOX = BoxSpec.sized(2, 18, 2).at(-1, 1, -1).uv(0, 8);
    private static final BoxSpec CAP_BOX = BoxSpec.sized(2, 2, 2).at(-1, -1, -1).uv(0, 0);
    private static final BoxSpec FOCUS_BOX = BoxSpec.sized(6, 6, 6).at(-3, -6, -3).uv(0, 0);
    private static final List<CapSlot> CAP_SLOTS = List.of(new CapSlot(Presence.SCEPTRE, 0.0F, 1.3F, 1.3F, 0.0F), new CapSlot(Presence.SCEPTRE, 0.3F, 1.0F, 0.66F, 0.0F),
            new CapSlot(Presence.NOT_SCEPTRE, 0.0F, 1.0F, 1.0F, 0.0F), new CapSlot(Presence.STAFF, 0.225F, 1.0F, 0.66F, 0.0F), new CapSlot(Presence.STAFF, 0.875F, 1.0F, 1.0F, 20.0F),
            new CapSlot(Presence.NOT_STAFF, 0.0F, 1.0F, 1.0F, 20.0F));

    private static final int RING_RUNES = 10;
    private static final float RING_YAW_STEP = 36.0F;
    private static final float RING_DISTANCE = 0.16F;
    private static final float RING_DEPTH = -0.125F;
    private static final int SIDE_COUNT = 4;
    private static final int SIDE_RUNES = 14;
    private static final float SIDE_YAW_STEP = 90.0F;
    private static final float SIDE_DISTANCE_BASE = 0.36F;
    private static final float SIDE_DISTANCE_STEP = 0.14F;
    private static final float SIDE_DEPTH = -0.08F;
    private static final int SIDE_GLYPH_STEP = 3;
    private static final int GLYPH_COUNT = 16;
    private static final float RUNE_SIDEWAYS = 0.01F;
    private static final int RUNE_LIGHT = 200;
    private static final float RUNE_PHASE_PER_GLYPH = 5.0F;
    private static final float RUNE_RED_BASE = 0.88F;
    private static final float RUNE_RED_AMPLITUDE = 0.1F;
    private static final float RUNE_RED_PERIOD = 5.0F;
    private static final float RUNE_GREEN_BASE = 0.63F;
    private static final float RUNE_GREEN_AMPLITUDE = 0.1F;
    private static final float RUNE_GREEN_PERIOD = 7.0F;
    private static final float RUNE_BLUE = 0.2F;
    private static final float RUNE_WOBBLE_AMPLITUDE = 0.2F;
    private static final float RUNE_WOBBLE_PERIOD = 10.0F;
    private static final float RUNE_ALPHA_BASE = 0.6F;
    private static final float RUNE_HALF_SIZE_BASE = 0.06F;
    private static final float RUNE_HALF_SIZE_WOBBLE = 40.0F;
    private static final float[] RUNE_CORNER_X = {-1.0F, 1.0F, 1.0F, -1.0F};
    private static final float[] RUNE_CORNER_Y = {-1.0F, -1.0F, 1.0F, 1.0F};

    private static final float TIP_PLAIN = -PIXEL;
    private static final float TIP_SCEPTRE = -PIXEL * 1.3F;
    private static final float TIP_STAFF = STAFF_GROUP_Y - PIXEL * 1.1F;
    private static final float TIP_FOCUS_PLAIN = -(6.0F / 16.0F) * WAND_FOCUS_SCALE;
    private static final float TIP_FOCUS_STAFF = STAFF_GROUP_Y + STAFF_FOCUS_Y - (6.0F / 16.0F) * STAFF_FOCUS_Y_SCALE;

    private static final float EXTENT_HALF_WIDTH = 0.15F;
    private static final float PLAIN_MIN_Y = -0.8125F;
    private static final float PLAIN_MAX_Y = 0.6875F;
    private static final float STAFF_MIN_Y = -2.1063F;
    private static final float STAFF_MAX_Y = 0.5547F;

    private final boolean staff;

    public WandItemSpecialRenderer(boolean staff) {
        this.staff = staff;
    }

    @Override
    public void submit(@Nullable WandArg arg, PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        if (arg == null) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(ROOT_CENTER, ROOT_CENTER, ROOT_CENTER);
        poseStack.mulPose(Axis.XP.rotationDegrees(FLIP_DEGREES));
        WandRenderers.render(context(arg.stack(), arg, poseStack, collector, lightCoords, overlayCoords, null, false));
        poseStack.popPose();
    }

    public static void submitParts(WandArg arg, PoseStack poseStack, SubmitNodeCollector collector, int light) {
        submitParts(arg, poseStack, collector, light, WHITE_TINT, WHITE_TINT);
    }

    public static void submitParts(WandArg arg, PoseStack poseStack, SubmitNodeCollector collector, int light, int rodTint, int capTint) {
        float ticks = clientTicks();
        boolean isStaff = arg.rod().staff();
        poseStack.pushPose();
        Fit.of(isStaff, GROUP_FIT, Fit.NONE).apply(poseStack);
        OrderedSubmitNodeCollector solid = collector.order(ORDER_SOLID);
        submitRod(arg.rod(), isStaff, poseStack, solid, light, rodTint, ticks);
        submitCaps(arg, isStaff, poseStack, solid, light, capTint);
        if (arg.hasFocus()) {
            submitFocus(arg.focusColor(), isStaff, poseStack, collector.order(ORDER_FOCUS), ticks);
        }
        submitRunes(arg.sceptre(), arg.rod().runes(), ticks, poseStack, collector.order(ORDER_RUNES));
        poseStack.popPose();
    }

    private static void submitRod(WandRod rod, boolean isStaff, PoseStack poseStack, OrderedSubmitNodeCollector collector, int light, int tint, float ticks) {
        poseStack.pushPose();
        Fit.of(isStaff, ROD_FIT, Fit.NONE).apply(poseStack);
        int rodLight = rod.glow() ? glowLight(ticks) : light;
        submitBox(collector, poseStack, TTFlatRenderTypes.entityCutoutFlat(rod.texture()), ROD_BOX, tint, rodLight);
        poseStack.popPose();
    }

    private static void submitCaps(WandArg arg, boolean isStaff, PoseStack poseStack, OrderedSubmitNodeCollector collector, int light, int tint) {
        RenderType type = TTFlatRenderTypes.entityCutoutFlat(arg.cap().texture());
        float outerXz = isStaff ? STAFF_CAP_OUTER_XZ : WAND_CAP_OUTER_XZ;
        float outerY = isStaff ? STAFF_CAP_OUTER_Y : WAND_CAP_OUTER_Y;
        for (CapSlot slot : CAP_SLOTS) {
            if (!slot.presence().test(arg.sceptre(), isStaff)) {
                continue;
            }
            poseStack.pushPose();
            slot.place(poseStack, outerXz, outerY);
            submitBox(collector, poseStack, type, CAP_BOX, tint, light);
            poseStack.popPose();
        }
    }

    private static void submitFocus(int focusColor, boolean isStaff, PoseStack poseStack, OrderedSubmitNodeCollector collector, float ticks) {
        poseStack.pushPose();
        Fit.of(isStaff, FOCUS_STAFF_FIT, FOCUS_PLAIN_FIT).apply(poseStack);
        int light = (int) (FOCUS_LIGHT_BASE + FOCUS_LIGHT_AMPLITUDE * Math.sin(ticks / FOCUS_LIGHT_PERIOD) + FOCUS_LIGHT_AMPLITUDE);
        submitBox(collector, poseStack, TTFlatRenderTypes.entityTranslucentFlat(FOCUS_TEXTURE), FOCUS_BOX, ARGB.color(FOCUS_ALPHA, focusColor), light);
        poseStack.popPose();
    }

    private static void submitRunes(boolean sceptre, boolean sideRunes, float ticks, PoseStack poseStack, OrderedSubmitNodeCollector collector) {
        if (!sceptre && !sideRunes) {
            return;
        }
        collector.submitCustomGeometry(poseStack, RUNE_TYPE, (pose, buffer) -> {
            if (sceptre) {
                for (int i = 0; i < RING_RUNES; i++) {
                    emitRune(pose, buffer, RING_YAW_STEP * i + ticks, RING_DISTANCE, RING_DEPTH, i, ticks);
                }
            }
            if (sideRunes) {
                for (int side = 0; side < SIDE_COUNT; side++) {
                    for (int step = 0; step < SIDE_RUNES; step++) {
                        emitRune(pose, buffer, SIDE_YAW_STEP * (side + 1), SIDE_DISTANCE_BASE + SIDE_DISTANCE_STEP * step, SIDE_DEPTH, (step + SIDE_GLYPH_STEP * side) % GLYPH_COUNT, ticks);
                    }
                }
            }
        });
    }

    private static void emitRune(PoseStack.Pose pose, VertexConsumer buffer, float yawDegrees, float distance, float depth, int glyph, float ticks) {
        float phase = ticks + RUNE_PHASE_PER_GLYPH * glyph;
        float red = Math.min(1.0F, RUNE_RED_BASE + RUNE_RED_AMPLITUDE * (float) Math.sin(phase / RUNE_RED_PERIOD));
        float green = Math.min(1.0F, RUNE_GREEN_BASE + RUNE_GREEN_AMPLITUDE * (float) Math.sin(phase / RUNE_GREEN_PERIOD));
        float wobble = RUNE_WOBBLE_AMPLITUDE * (float) Math.sin(phase / RUNE_WOBBLE_PERIOD);
        int tint = ARGB.colorFromFloat(Math.min(1.0F, RUNE_ALPHA_BASE + wobble), red, green, RUNE_BLUE);
        float half = RUNE_HALF_SIZE_BASE + wobble / RUNE_HALF_SIZE_WOBBLE;
        double radians = Math.toRadians(yawDegrees);
        float sin = (float) Math.sin(radians);
        float cos = (float) Math.cos(radians);
        float uLeft = (float) glyph / GLYPH_COUNT;
        float uRight = (float) (glyph + 1) / GLYPH_COUNT;
        for (int corner = 0; corner < RUNE_CORNER_X.length; corner++) {
            float cornerX = RUNE_CORNER_X[corner];
            float cornerY = RUNE_CORNER_Y[corner];
            float localX = RUNE_SIDEWAYS + cornerX * half;
            float localY = distance + cornerY * half;
            float x = localX * cos + depth * sin;
            float z = -localX * sin + depth * cos;
            float u = cornerX < 0.0F ? uRight : uLeft;
            float v = cornerY > 0.0F ? 0.0F : 1.0F;
            buffer.addVertex(pose, x, localY, z).setColor(tint).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(RUNE_LIGHT).setNormal(pose, sin, 0.0F, cos);
        }
    }

    private static void submitBox(OrderedSubmitNodeCollector collector, PoseStack poseStack, RenderType type, BoxSpec box, int tint, int light) {
        collector.submitCustomGeometry(poseStack, type, (pose, buffer) -> BoxGeometry.box(pose, buffer, box.minX() * PIXEL, box.minY() * PIXEL, box.minZ() * PIXEL, (box.minX() + box.width()) * PIXEL,
                (box.minY() + box.height()) * PIXEL, (box.minZ() + box.depth()) * PIXEL, box.u(), box.v(), box.width(), box.height(), box.depth(), TEXTURE_SIZE, TEXTURE_SIZE, tint, light, true));
    }

    private static int glowLight(float ticks) {
        return (int) (GLOW_LIGHT_BASE + GLOW_LIGHT_AMPLITUDE * Math.sin(Mth.floor(ticks)) + GLOW_LIGHT_AMPLITUDE);
    }

    private static float clientTicks() {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null) {
            return 0.0F;
        }
        return player.tickCount + minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false);
    }

    public static float tipModelY(WandArg arg) {
        boolean isStaff = arg.rod().staff();
        if (arg.hasFocus()) {
            return isStaff ? TIP_FOCUS_STAFF : TIP_FOCUS_PLAIN;
        }
        if (isStaff) {
            return TIP_STAFF;
        }
        return arg.sceptre() ? TIP_SCEPTRE : TIP_PLAIN;
    }

    @Override
    public void getExtents(Consumer<Vector3fc> consumer) {
        float minY = staff ? STAFF_MIN_Y : PLAIN_MIN_Y;
        float maxY = staff ? STAFF_MAX_Y : PLAIN_MAX_Y;
        consumer.accept(new Vector3f(ROOT_CENTER - EXTENT_HALF_WIDTH, minY, ROOT_CENTER - EXTENT_HALF_WIDTH));
        consumer.accept(new Vector3f(ROOT_CENTER + EXTENT_HALF_WIDTH, maxY, ROOT_CENTER + EXTENT_HALF_WIDTH));
    }

    @Override
    public WandArg extractArgument(ItemStack stack) {
        return extract(stack);
    }

    public static WandArg extract(ItemStack stack) {
        WandParts parts = WandVisHelper.partsOf(stack);
        ItemStackTemplate socketed = stack.get(TTDataComponents.SOCKETED_FOCUS.get());
        ItemStack focus = socketed == null ? ItemStack.EMPTY : socketed.create();
        boolean hasFocus = FocusItems.isFocus(focus);
        int focusColor = hasFocus ? FocusColors.of(focus) : WHITE_RGB;
        return new WandArg(stack.copy(), parts.cap(), parts.rod(), parts.sceptre(), hasFocus, focusColor);
    }

    public static WandRenderContext context(ItemStack stack, WandArg arg, PoseStack poseStack, SubmitNodeCollector collector, int light, int overlay, @Nullable ItemDisplayContext displayContext, boolean firstPersonHand) {
        return new WandRenderContext(stack, arg.cap(), arg.rod(), arg.sceptre(), arg.hasFocus(), arg.focusColor(), poseStack, collector, light, overlay, displayContext, firstPersonHand,
                (rodTint, capTint) -> submitParts(arg, poseStack, collector, light, rodTint, capTint));
    }

    public record WandArg(ItemStack stack, WandCap cap, WandRod rod, boolean sceptre, boolean hasFocus, int focusColor) {
    }

    public record Unbaked(boolean staff) implements SpecialModelRenderer.Unbaked<WandArg> {
        public static final MapCodec<Unbaked> MAP_CODEC = Codec.BOOL.optionalFieldOf("staff", false).xmap(Unbaked::new, Unbaked::staff);

        @Override
        public SpecialModelRenderer<WandArg> bake(SpecialModelRenderer.BakingContext context) {
            return new WandItemSpecialRenderer(staff);
        }

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }
    }

    private record BoxSpec(int minX, int minY, int minZ, int width, int height, int depth, int u, int v) {
        static BoxSpec sized(int width, int height, int depth) {
            return new BoxSpec(0, 0, 0, width, height, depth, 0, 0);
        }

        BoxSpec at(int x, int y, int z) {
            return new BoxSpec(x, y, z, width, height, depth, u, v);
        }

        BoxSpec uv(int newU, int newV) {
            return new BoxSpec(minX, minY, minZ, width, height, depth, newU, newV);
        }
    }

    private enum Presence {
        SCEPTRE, NOT_SCEPTRE, STAFF, NOT_STAFF;

        boolean test(boolean sceptre, boolean staff) {
            return switch (this) {
                case SCEPTRE -> sceptre;
                case NOT_SCEPTRE -> !sceptre;
                case STAFF -> staff;
                case NOT_STAFF -> !staff;
            };
        }
    }

    private record CapSlot(Presence presence, float offsetY, float scaleXz, float scaleY, float pivotPixels) {
        void place(PoseStack poseStack, float outerXz, float outerY) {
            poseStack.scale(outerXz, outerY, outerXz);
            poseStack.translate(0.0F, offsetY, 0.0F);
            poseStack.scale(scaleXz, scaleY, scaleXz);
            poseStack.translate(0.0F, pivotPixels * PIXEL, 0.0F);
        }
    }

    private record Fit(float offsetY, float scaleXz, float scaleY) {
        static final Fit NONE = new Fit(0.0F, 1.0F, 1.0F);

        static Fit of(boolean staff, Fit staffFit, Fit plainFit) {
            return staff ? staffFit : plainFit;
        }

        void apply(PoseStack poseStack) {
            poseStack.translate(0.0F, offsetY, 0.0F);
            poseStack.scale(scaleXz, scaleY, scaleXz);
        }
    }
}
