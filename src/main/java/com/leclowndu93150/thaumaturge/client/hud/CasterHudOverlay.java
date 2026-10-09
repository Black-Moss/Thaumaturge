package com.leclowndu93150.thaumaturge.client.hud;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.casters.ICaster;
import com.leclowndu93150.thaumaturge.api.spell.SpellSummary;
import com.leclowndu93150.thaumaturge.client.screen.TTScreenTextures;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeClientConfig;
import com.leclowndu93150.thaumaturge.content.spell.item.FocusItems;
import com.leclowndu93150.thaumaturge.content.wands.WandEconomy;
import com.leclowndu93150.thaumaturge.content.wands.WandVisHelper;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.gui.GuiLayer;
import org.jspecify.annotations.Nullable;

public final class CasterHudOverlay implements GuiLayer {
    public static final int STACK_HEIGHT = 60;

    private static final Identifier WAND_SHEET = HudTextures.CASTER_DIAL;
    private static final int TEX = TTScreenTextures.TEX_SIZE;
    private static final int PRIMALS = WandEconomy.PRIMAL_COUNT;

    private static final int DIAL_SIZE = 32;
    private static final int DIAL_ART_SIZE = 64;
    private static final int DIAL_CENTER = 16;
    private static final int DIAL_BOTTOM_OFFSET = 32;
    private static final float BAR_RADIUS = 32.0F;
    private static final float BAR_SCALE = 0.5F;
    private static final float BOTTOM_START_DEGREES = -15.0F;
    private static final float STACK_START_DEGREES = 75.0F;
    private static final float BAR_STEP_DEGREES = 24.0F;

    private static final int BAR_BOTTOM = 35;
    private static final int BAR_MAX_FILL = 30;
    private static final int BAR_FILL_U = 104;
    private static final int BAR_FILL_W = 8;
    private static final int BAR_FILL_X = -4;
    private static final int BAR_FILL_ALPHA = 204;
    private static final int DEFAULT_ENERGY_COLOR = 0xC0FFFF;
    private static final int BAR_FRAME_U = 72;
    private static final int BAR_FRAME_X = -8;
    private static final int BAR_FRAME_Y = -3;
    private static final int BAR_FRAME_W = 16;
    private static final int BAR_FRAME_H = 42;
    private static final int MARKER_U_COST = 136;
    private static final int MARKER_U_RISING = 120;
    private static final int MARKER_U_FALLING = 128;
    private static final int MARKER_SIZE = 8;
    private static final int MARKER_X = -4;
    private static final int MARKER_Y = -8;
    private static final int MARKER_Y_STACKED = -16;

    private static final int VIS_TEXT_X = -32;
    private static final int COST_TEXT_X = 8;
    private static final int BAR_TEXT_Y = -4;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int BLACK = 0xFF000000;

    private static final int ITEM_OFFSET = 8;
    private static final int TRADE_ANCHOR_RISE = 9;
    private static final float TRADE_SCALE = 0.5F;
    private static final int TRADE_RIGHT = 16;
    private static final int TRADE_DOWN = 24;
    private static final int[] OUTLINE_STEPS = {-1, 0, 1, 0, -1};

    private static final long SNAPSHOT_INTERVAL_MS = 1000L;
    private static final int[] PREVIOUS_VIS = new int[PRIMALS];
    private static long lastSnapshotMillis;

    private static final int AMOUNT_MAX_INTEGER_DIGITS = 7;
    private static final int AMOUNT_MAX_FRACTION_DIGITS = 1;
    private static final RoundingMode AMOUNT_ROUNDING = RoundingMode.HALF_UP;

    private final NumberFormat amountFormat = createAmountFormat();

    public CasterHudOverlay() {}

    public static boolean isVisible(Minecraft mc) {
        LocalPlayer player = mc.player;
        return player != null && !mc.options.hideGui && mc.mouseHandler.isMouseGrabbed()
                && (player.getMainHandItem().getItem() instanceof ICaster || player.getOffhandItem().getItem() instanceof ICaster);
    }

    public static LeftHudStack.Gauge dialGauge() {
        return new DialGauge(new CasterHudOverlay());
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        if (!ThaumaturgeClientConfig.dialBottom()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (!isVisible(mc)) {
            return;
        }
        drawDial(graphics, mc, mc.player, graphics.guiHeight() - DIAL_BOTTOM_OFFSET, true);
    }

    private void drawDial(GuiGraphicsExtractor graphics, Minecraft mc, LocalPlayer player, int dialY, boolean bottomMode) {
        ItemStack mainStack = player.getMainHandItem();
        ItemStack casterStack = mainStack.getItem() instanceof ICaster ? mainStack : player.getOffhandItem();
        if (!(casterStack.getItem() instanceof ICaster caster)) {
            return;
        }
        ItemStack focus = caster.getFocusStack(casterStack);
        boolean hasFocus = FocusItems.isFocus(focus);
        float[] costs = hasFocus ? primalCosts(mc, player, caster, casterStack, focus) : new float[PRIMALS];
        int maxVis = WandVisHelper.capacityOf(casterStack);
        int[] current = new int[PRIMALS];
        graphics.blit(RenderPipelines.GUI_TEXTURED, WAND_SHEET, 0, dialY, 0, 0, DIAL_SIZE, DIAL_SIZE, DIAL_ART_SIZE, DIAL_ART_SIZE, TEX, TEX);
        float startDegrees = bottomMode ? BOTTOM_START_DEGREES : STACK_START_DEGREES;
        for (int i = 0; i < PRIMALS; i++) {
            ResourceKey<IAspect> primal = TTAspects.PRIMALS.get(i);
            current[i] = WandVisHelper.storedIn(casterStack, primal);
            float radians = (startDegrees + i * BAR_STEP_DEGREES) * Mth.DEG_TO_RAD;
            graphics.pose().pushMatrix();
            graphics.pose().translate(DIAL_CENTER + BAR_RADIUS * Mth.sin(radians), dialY + DIAL_CENTER - BAR_RADIUS * Mth.cos(radians));
            graphics.pose().rotate(radians);
            graphics.pose().scale(BAR_SCALE, BAR_SCALE);
            drawBar(graphics, mc, player, current[i], maxVis, PREVIOUS_VIS[i], costs[i], aspectColor(mc, primal));
            graphics.pose().popMatrix();
        }
        if (hasFocus) {
            drawCentre(graphics, mc, player, focus, dialY);
        }
        long now = Util.getMillis();
        if (now - lastSnapshotMillis >= SNAPSHOT_INTERVAL_MS) {
            System.arraycopy(current, 0, PREVIOUS_VIS, 0, PRIMALS);
            lastSnapshotMillis = now;
        }
    }

    private void drawBar(GuiGraphicsExtractor graphics, Minecraft mc, LocalPlayer player, int stored, int maxVis, int previous, float cost, int color) {
        int fill = maxVis > 0 ? (int) ((float) stored / maxVis * BAR_MAX_FILL) : 0;
        if (fill > 0) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, WAND_SHEET, BAR_FILL_X, BAR_BOTTOM - fill, BAR_FILL_U, 0, BAR_FILL_W, fill, BAR_FILL_W, fill, TEX, TEX, ARGB.color(BAR_FILL_ALPHA, color));
        }
        graphics.blit(RenderPipelines.GUI_TEXTURED, WAND_SHEET, BAR_FRAME_X, BAR_FRAME_Y, BAR_FRAME_U, 0, BAR_FRAME_W, BAR_FRAME_H, BAR_FRAME_W, BAR_FRAME_H, TEX, TEX);
        boolean hasCost = cost > 0.0F;
        if (hasCost) {
            drawMarker(graphics, MARKER_U_COST, MARKER_Y);
        }
        int trendY = hasCost ? MARKER_Y_STACKED : MARKER_Y;
        if (stored < previous) {
            drawMarker(graphics, MARKER_U_FALLING, trendY);
        } else if (stored > previous) {
            drawMarker(graphics, MARKER_U_RISING, trendY);
        }
        if (player.isShiftKeyDown()) {
            drawRotatedText(graphics, mc, amountFormat.format(stored / (float) WandEconomy.CENTIVIS_PER_VIS), VIS_TEXT_X);
            if (hasCost) {
                drawRotatedText(graphics, mc, amountFormat.format(cost), COST_TEXT_X);
            }
        }
    }

    private static void drawMarker(GuiGraphicsExtractor graphics, int u, int y) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, WAND_SHEET, MARKER_X, y, u, 0, MARKER_SIZE, MARKER_SIZE, MARKER_SIZE, MARKER_SIZE, TEX, TEX);
    }

    private static void drawRotatedText(GuiGraphicsExtractor graphics, Minecraft mc, String text, int x) {
        graphics.pose().pushMatrix();
        graphics.pose().rotate(-Mth.HALF_PI);
        graphics.text(mc.font, text, x, BAR_TEXT_Y, WHITE, true);
        graphics.pose().popMatrix();
    }

    private static void drawCentre(GuiGraphicsExtractor graphics, Minecraft mc, LocalPlayer player, ItemStack focus, int dialY) {
        int itemY = dialY + ITEM_OFFSET;
        ItemStack picked = pickedStack(player.getMainHandItem());
        if (picked.isEmpty()) {
            graphics.item(focus, ITEM_OFFSET, itemY);
            return;
        }
        graphics.item(picked, ITEM_OFFSET, itemY);
        String count = String.valueOf(countOf(player, picked.getItem()));
        int x = TRADE_RIGHT - mc.font.width(count);
        graphics.pose().pushMatrix();
        graphics.pose().translate(DIAL_CENTER, dialY + DIAL_CENTER - TRADE_ANCHOR_RISE);
        graphics.pose().scale(TRADE_SCALE, TRADE_SCALE);
        for (int i = 0; i + 1 < OUTLINE_STEPS.length; i++) {
            graphics.text(mc.font, count, x + OUTLINE_STEPS[i], TRADE_DOWN + OUTLINE_STEPS[i + 1], BLACK, false);
        }
        graphics.text(mc.font, count, x, TRADE_DOWN, WHITE, false);
        graphics.pose().popMatrix();
    }

    private static ItemStack pickedStack(ItemStack mainStack) {
        if (!(mainStack.getItem() instanceof ICaster caster)) {
            return ItemStack.EMPTY;
        }
        BlockState state = caster.getPickedBlock(mainStack);
        return state == null ? ItemStack.EMPTY : new ItemStack(state.getBlock());
    }

    private static int countOf(LocalPlayer player, Item item) {
        int total = 0;
        for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
            if (stack.is(item)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    private static float[] primalCosts(Minecraft mc, LocalPlayer player, ICaster caster, ItemStack casterStack, ItemStack focus) {
        float[] costs = new float[PRIMALS];
        if (mc.level == null) {
            return costs;
        }
        HolderLookup.Provider registries = mc.level.registryAccess();
        Optional<SpellSummary> summary = FocusItems.summary(focus, registries, player);
        if (summary.isEmpty() || summary.get().vis() <= 0.0F) {
            return costs;
        }
        Map<ResourceKey<IAspect>, Integer> split = FocusItems.visSplit(summary.get(), 1.0F, registries);
        float modifier = caster.getConsumptionModifier(casterStack, player, false);
        for (int i = 0; i < PRIMALS; i++) {
            int amount = split.getOrDefault(TTAspects.PRIMALS.get(i), 0);
            if (amount > 0) {
                costs[i] = amount * modifier / WandEconomy.CENTIVIS_PER_VIS;
            }
        }
        return costs;
    }

    private static int aspectColor(Minecraft mc, ResourceKey<IAspect> key) {
        if (mc.level == null) {
            return DEFAULT_ENERGY_COLOR;
        }
        return mc.level.registryAccess().lookupOrThrow(IAspect.REGISTRY_KEY).get(key).map(holder -> holder.value().color()).orElse(DEFAULT_ENERGY_COLOR);
    }

    private static NumberFormat createAmountFormat() {
        NumberFormat format = NumberFormat.getNumberInstance(Locale.ROOT);
        format.setMaximumIntegerDigits(AMOUNT_MAX_INTEGER_DIGITS);
        format.setMaximumFractionDigits(AMOUNT_MAX_FRACTION_DIGITS);
        format.setGroupingUsed(false);
        format.setRoundingMode(AMOUNT_ROUNDING);
        return format;
    }

    private static final class DialGauge implements LeftHudStack.Gauge {
        private final CasterHudOverlay renderer;

        private DialGauge(CasterHudOverlay renderer) {
            this.renderer = renderer;
        }

        @Override
        public boolean visible(Minecraft mc, LocalPlayer player) {
            return !ThaumaturgeClientConfig.dialBottom() && isVisible(mc);
        }

        @Override
        public int height() {
            return STACK_HEIGHT;
        }

        @Override
        public @Nullable String exclusiveGroup() {
            return null;
        }

        @Override
        public void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                renderer.drawDial(graphics, mc, mc.player, 0, false);
            }
        }
    }
}
