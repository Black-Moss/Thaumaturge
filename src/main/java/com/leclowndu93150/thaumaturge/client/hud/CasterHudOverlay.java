package com.leclowndu93150.thaumaturge.client.hud;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.casters.FocusCaster;
import com.leclowndu93150.thaumaturge.api.casters.IFocusBlockPicker;
import com.leclowndu93150.thaumaturge.api.spell.Spell;
import com.leclowndu93150.thaumaturge.api.spell.SpellNode;
import com.leclowndu93150.thaumaturge.api.spell.Spells;
import com.leclowndu93150.thaumaturge.api.spell.part.SpellPart;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeClientConfig;
import com.leclowndu93150.thaumaturge.content.spell.item.FocusItems;
import com.leclowndu93150.thaumaturge.content.wands.WandEconomy;
import com.leclowndu93150.thaumaturge.content.wands.WandVisHelper;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.gui.GuiLayer;
import org.joml.Matrix3x2fStack;
import org.jspecify.annotations.Nullable;

public final class CasterHudOverlay implements GuiLayer {
    private static final int TEX = 256;
    private static final int STACK_HEIGHT = 60;

    private static final int DISC_U = 0;
    private static final int DISC_V = 0;
    private static final int DISC_SOURCE = 64;
    private static final int DISC_SIZE = 32;
    private static final int DISC_CENTRE = 16;
    private static final int BOTTOM_DISC_RISE = 32;

    private static final float BOTTOM_FIRST_BAR_DEGREES = -15.0F;
    private static final float TOP_FIRST_BAR_DEGREES = 75.0F;
    private static final float BAR_STEP_DEGREES = 24.0F;
    private static final float BAR_SCALE = 0.5F;
    private static final int BAR_TIP = 64;

    private static final int FRAME_U = 72;
    private static final int FRAME_V = 0;
    private static final int FRAME_W = 16;
    private static final int FRAME_H = 42;
    private static final int FILL_U = 104;
    private static final int FILL_V = 0;
    private static final int FILL_W = 8;
    private static final int FILL_MAX = 30;
    private static final int FILL_INNER_GAP = 4;
    private static final int FRAME_OUTSET = 3;
    private static final float FILL_ALPHA = 0.8F;
    private static final float CHANNEL_MAX = 255.0F;
    private static final int FALLBACK_COLOR = 0xE8E2F0;

    private static final int MARKER_SIZE = 8;
    private static final int MARKER_V = 0;
    private static final int RISE_U = 120;
    private static final int FALL_U = 128;
    private static final int COST_U = 136;

    private static final int TEXT_COLOR = 0xFFFFFFFF;
    private static final int OUTLINE_COLOR = 0xFF000000;
    private static final int TEXT_HALF_HEIGHT = 4;
    private static final int AMOUNT_TEXT_START = 32;
    private static final int COST_TEXT_START = 72;

    private static final int ICON_SIZE = 16;
    private static final int ICON_HALF = 8;
    private static final float COUNT_SCALE = 0.5F;
    private static final int COUNT_DROP = 3;
    private static final int GLYPH_TAIL = 1;
    private static final int OUTLINE_STEP = 1;

    private static final int TREND_REFRESH_TICKS = 20;
    private static final int COST_REFRESH_TICKS = 20;
    private static final int OFFHAND_SLOT = -1;
    private static final int NO_SLOT = Integer.MIN_VALUE;
    private static final String COST_PATTERN = "#######.#";

    private final DecimalFormat costFormat = new DecimalFormat(COST_PATTERN);
    private final int[] trendReference = new int[TTAspects.PRIMALS.size()];
    private int trendSlot = NO_SLOT;
    private int trendTick;
    private ItemStack costFocus = ItemStack.EMPTY;
    private Map<ResourceKey<IAspect>, Integer> baseCosts = Map.of();
    private int costTick;

    public CasterHudOverlay() {}

    public static boolean isVisible(Minecraft mc) {
        LocalPlayer player = mc.player;
        if (player == null || mc.options.hideGui || mc.screen != null || !mc.mouseHandler.isMouseGrabbed()) {
            return false;
        }
        return !heldCaster(player).isEmpty();
    }

    public static LeftHudStack.Gauge dialGauge() {
        return new DialSlot();
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || !isVisible(mc)) {
            return;
        }
        boolean mainHand = player.getMainHandItem().getItem() instanceof FocusCaster;
        ItemStack caster = mainHand ? player.getMainHandItem() : player.getOffhandItem();
        if (!(caster.getItem() instanceof FocusCaster focusCaster)) {
            return;
        }
        boolean bottom = ThaumaturgeClientConfig.dialBottom();
        int originY = bottom ? graphics.guiHeight() - BOTTOM_DISC_RISE : 0;
        int centreX = DISC_CENTRE;
        int centreY = originY + DISC_CENTRE;
        graphics.blit(RenderPipelines.GUI_TEXTURED, HudTextures.CASTER_DIAL, 0, originY, DISC_U, DISC_V, DISC_SIZE, DISC_SIZE, DISC_SOURCE, DISC_SOURCE, TEX, TEX);
        ItemStack focus = focusCaster.socketedFocus(caster);
        Map<ResourceKey<IAspect>, Integer> costs = focus.isEmpty() ? Map.of() : baseCosts(focus, player);
        refreshTrend(player, caster, mainHand ? player.getInventory().getSelectedSlot() : OFFHAND_SLOT);
        drawBars(graphics, mc.font, player, caster, costs, centreX, centreY, bottom ? BOTTOM_FIRST_BAR_DEGREES : TOP_FIRST_BAR_DEGREES);
        if (!focus.isEmpty()) {
            drawCentre(graphics, mc.font, player, mainHand ? focusCaster.rememberedBlock(caster) : null, focus, centreX, centreY);
        }
    }

    private void drawBars(GuiGraphicsExtractor graphics, Font font, LocalPlayer player, ItemStack caster, Map<ResourceKey<IAspect>, Integer> costs, int centreX, int centreY, float firstDegrees) {
        List<ResourceKey<IAspect>> primals = TTAspects.PRIMALS;
        int capacity = WandVisHelper.capacityOf(caster);
        boolean sneaking = player.isShiftKeyDown();
        HolderLookup.RegistryLookup<IAspect> aspects = player.registryAccess().lookupOrThrow(IAspect.REGISTRY_KEY);
        Matrix3x2fStack pose = graphics.pose();
        for (int i = 0; i < primals.size(); i++) {
            ResourceKey<IAspect> primal = primals.get(i);
            int stored = WandVisHelper.storedIn(caster, primal);
            int baseCost = costs.getOrDefault(primal, 0);
            int fill = capacity > 0 ? Mth.clamp((int) ((long) stored * FILL_MAX / capacity), 0, FILL_MAX) : 0;
            int color = aspects.get(primal).map(holder -> holder.value().color()).orElse(FALLBACK_COLOR);
            pose.pushMatrix();
            pose.translate(centreX, centreY);
            pose.rotate((firstDegrees + i * BAR_STEP_DEGREES) * Mth.DEG_TO_RAD);
            pose.scale(BAR_SCALE, BAR_SCALE);
            drawBar(graphics, fill, color);
            drawMarkers(graphics, baseCost > 0, Integer.compare(stored, trendReference[i]));
            if (sneaking) {
                String costText = baseCost > 0 ? costFormat.format(perCast(caster, player, primal, baseCost)) : null;
                drawBarText(graphics, font, costFormat.format(stored / (float) WandEconomy.CENTIVIS_PER_VIS), costText);
            }
            pose.popMatrix();
        }
    }

    private static void drawBar(GuiGraphicsExtractor graphics, int fill, int color) {
        int frameTop = -BAR_TIP - FRAME_OUTSET;
        int fillBottom = frameTop + FRAME_H - FILL_INNER_GAP;
        if (fill > 0) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, HudTextures.CASTER_DIAL, -FILL_W / 2, fillBottom - fill, FILL_U, FILL_V, FILL_W, fill, FILL_W, fill, TEX, TEX,
                    ARGB.color(Math.round(FILL_ALPHA * CHANNEL_MAX), color));
        }
        graphics.blit(RenderPipelines.GUI_TEXTURED, HudTextures.CASTER_DIAL, -FRAME_W / 2, frameTop, FRAME_U, FRAME_V, FRAME_W, FRAME_H, FRAME_W, FRAME_H, TEX, TEX);
    }

    private static void drawMarkers(GuiGraphicsExtractor graphics, boolean costed, int trend) {
        int span = 0;
        if (costed) {
            span += MARKER_SIZE;
            drawMarker(graphics, COST_U, span);
        }
        if (trend != 0) {
            span += MARKER_SIZE;
            drawMarker(graphics, trend > 0 ? RISE_U : FALL_U, span);
        }
    }

    private static void drawMarker(GuiGraphicsExtractor graphics, int u, int reach) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, HudTextures.CASTER_DIAL, -MARKER_SIZE / 2, -BAR_TIP - reach, u, MARKER_V, MARKER_SIZE, MARKER_SIZE, MARKER_SIZE, MARKER_SIZE, TEX, TEX);
    }

    private static void drawBarText(GuiGraphicsExtractor graphics, Font font, String stored, @Nullable String cost) {
        Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        pose.rotate(-Mth.HALF_PI);
        graphics.text(font, stored, AMOUNT_TEXT_START, -TEXT_HALF_HEIGHT, TEXT_COLOR, true);
        if (cost != null) {
            graphics.text(font, cost, COST_TEXT_START, -TEXT_HALF_HEIGHT, TEXT_COLOR, true);
        }
        pose.popMatrix();
    }

    private static float perCast(ItemStack caster, LocalPlayer player, ResourceKey<IAspect> primal, int baseCost) {
        int charged = (int) (baseCost * WandVisHelper.costFactor(caster, player, primal, false));
        return charged / (float) WandEconomy.CENTIVIS_PER_VIS;
    }

    private void drawCentre(GuiGraphicsExtractor graphics, Font font, LocalPlayer player, @Nullable BlockState picked, ItemStack focus, int centreX, int centreY) {
        Item pickedItem = picked != null && picksBlocks(focus, player.registryAccess()) ? picked.getBlock().asItem() : Items.AIR;
        if (pickedItem == Items.AIR) {
            graphics.item(focus, centreX - ICON_HALF, centreY - ICON_HALF);
            return;
        }
        graphics.item(new ItemStack(pickedItem), centreX - ICON_HALF, centreY - ICON_HALF);
        drawCount(graphics, font, Integer.toString(carried(player, pickedItem)), centreX - ICON_HALF + ICON_SIZE, centreY + COUNT_DROP);
    }

    private static void drawCount(GuiGraphicsExtractor graphics, Font font, String text, int right, int top) {
        Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(right, top);
        pose.scale(COUNT_SCALE, COUNT_SCALE);
        int x = GLYPH_TAIL - font.width(text);
        graphics.text(font, text, x - OUTLINE_STEP, 0, OUTLINE_COLOR, false);
        graphics.text(font, text, x + OUTLINE_STEP, 0, OUTLINE_COLOR, false);
        graphics.text(font, text, x, -OUTLINE_STEP, OUTLINE_COLOR, false);
        graphics.text(font, text, x, OUTLINE_STEP, OUTLINE_COLOR, false);
        graphics.text(font, text, x, 0, TEXT_COLOR, false);
        pose.popMatrix();
    }

    private static int carried(LocalPlayer player, Item item) {
        int count = 0;
        for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
            if (stack.is(item)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static boolean picksBlocks(ItemStack focus, HolderLookup.Provider registries) {
        Spell spell = Spells.spellOf(focus);
        if (spell == null) {
            return false;
        }
        for (SpellNode node : spell.nodes()) {
            Optional<SpellPart> part = Spells.part(registries, node.part());
            if (part.isPresent() && part.get().behavior() instanceof IFocusBlockPicker) {
                return true;
            }
        }
        return false;
    }

    private Map<ResourceKey<IAspect>, Integer> baseCosts(ItemStack focus, LocalPlayer player) {
        boolean stale = player.tickCount - costTick >= COST_REFRESH_TICKS || player.tickCount < costTick;
        if (stale || !ItemStack.isSameItemSameComponents(focus, costFocus)) {
            HolderLookup.Provider registries = player.registryAccess();
            baseCosts = FocusItems.summary(focus, registries, player).map(summary -> FocusItems.visSplit(summary, 1.0F, registries)).orElse(Map.of());
            costFocus = focus.copy();
            costTick = player.tickCount;
        }
        return baseCosts;
    }

    private void refreshTrend(LocalPlayer player, ItemStack caster, int slot) {
        boolean due = player.tickCount - trendTick >= TREND_REFRESH_TICKS || player.tickCount < trendTick;
        if (slot != trendSlot || due) {
            List<ResourceKey<IAspect>> primals = TTAspects.PRIMALS;
            for (int i = 0; i < primals.size(); i++) {
                trendReference[i] = WandVisHelper.storedIn(caster, primals.get(i));
            }
            trendSlot = slot;
            trendTick = player.tickCount;
        }
    }

    private static ItemStack heldCaster(LocalPlayer player) {
        if (player.getMainHandItem().getItem() instanceof FocusCaster) {
            return player.getMainHandItem();
        }
        return player.getOffhandItem().getItem() instanceof FocusCaster ? player.getOffhandItem() : ItemStack.EMPTY;
    }

    private static final class DialSlot implements LeftHudStack.Gauge {
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
        public void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {}
    }
}
