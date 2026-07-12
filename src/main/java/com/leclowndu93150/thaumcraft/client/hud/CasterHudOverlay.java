package com.leclowndu93150.thaumcraft.client.hud;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.api.aspect.IAspect;
import com.leclowndu93150.thaumcraft.content.wands.WandEconomy;
import com.leclowndu93150.thaumcraft.content.wands.WandVisHelper;
import com.leclowndu93150.thaumcraft.api.aspect.TCAspects;
import net.minecraft.resources.ResourceKey;
import com.leclowndu93150.thaumcraft.api.casters.ICaster;
import com.leclowndu93150.thaumcraft.config.ThaumcraftClientConfig;
import com.leclowndu93150.thaumcraft.content.casters.ItemFocus;
import java.text.DecimalFormat;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.Identifier;
import com.leclowndu93150.thaumcraft.registry.TCAttachments;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.gui.GuiLayer;

public final class CasterHudOverlay implements GuiLayer {
    private static final Identifier HUD = TCIds.rl("textures/gui/hud.png");
    private static final int TEX_SIZE = 256;

    public static final int STACK_HEIGHT = 33;
    private static final int DIAL_SIZE = 32;
    private static final int DIAL_SRC_SIZE = 64;
    private static final int ANCHOR = 16;
    private static final int BAR_OFFSET_X = 16;
    private static final int BAR_OFFSET_Y = -10;
    private static final float BAR_SPACE_SCALE = 0.5F;
    private static final int BAR_MAX_HEIGHT = 30;
    private static final int BAR_BOTTOM = 35;
    private static final float BAR_FILL_U = 104.0F;
    private static final int BAR_FILL_W = 8;
    private static final int BAR_FRAME_X = -8;
    private static final int BAR_FRAME_Y = -3;
    private static final float BAR_FRAME_U = 72.0F;
    private static final int BAR_FRAME_W = 16;
    private static final int BAR_FRAME_H = 42;
    private static final float BAR_FILL_ALPHA = 0.8F;
    private static final int AMOUNT_TEXT_X = -32;
    private static final int AMOUNT_TEXT_Y = -4;
    private static final int COST_TEXT_Y = 32;
    private static final int ITEM_HALF = 8;
    private static final int COUNT_TEXT_LIFT = 9;
    private static final int COUNT_TEXT_X = 16;
    private static final int COUNT_TEXT_Y = 24;
    private static final float COUNT_TEXT_SCALE = 0.5F;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int OUTLINE_BLACK = 0xFF000000;
    private static final int DEFAULT_ENERGY_COLOR = 0xC0FFFF;

    private static final DecimalFormat AMOUNT_FORMAT = new DecimalFormat("#######.#");

    public CasterHudOverlay() {}

    public static boolean isVisible(Minecraft mc) {
        LocalPlayer player = mc.player;
        if (player == null || mc.options.hideGui || !mc.mouseHandler.isMouseGrabbed()) {
            return false;
        }
        return player.getMainHandItem().getItem() instanceof ICaster
                || player.getOffhandItem().getItem() instanceof ICaster;
    }

    public static LeftHudStack.Gauge dialGauge() {
        return new LeftHudStack.Gauge() {
            @Override
            public boolean visible(Minecraft mc, LocalPlayer player) {
                return isVisible(mc) && !ThaumcraftClientConfig.dialBottom();
            }

            @Override
            public int height() {
                return STACK_HEIGHT;
            }

            @Override
            public String exclusiveGroup() {
                return null;
            }

            @Override
            public void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
                renderDial(graphics, 0);
            }
        };
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (!isVisible(mc) || !ThaumcraftClientConfig.dialBottom()) {
            return;
        }
        renderDial(graphics, graphics.guiHeight() - DIAL_SIZE);
    }

    private static void renderDial(GuiGraphicsExtractor graphics, int dialY) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        ItemStack casterStack = player.getMainHandItem();
        if (!(casterStack.getItem() instanceof ICaster)) {
            casterStack = player.getOffhandItem();
        }
        ICaster wand = (ICaster) casterStack.getItem();

        int max = WandVisHelper.getMaxVis(casterStack) * WandEconomy.PRIMAL_COUNT;
        int amt = 0;
        for (ResourceKey<IAspect> primal : TCAspects.PRIMALS) {
            amt += WandVisHelper.getVis(casterStack, primal);
        }

        graphics.blit(RenderPipelines.GUI_TEXTURED, HUD,
                0, dialY, 0.0F, 0.0F, DIAL_SIZE, DIAL_SIZE, DIAL_SRC_SIZE, DIAL_SRC_SIZE, TEX_SIZE, TEX_SIZE);

        ItemStack focusStack = wand.getFocusStack(casterStack);
        boolean hasFocus = focusStack.getItem() instanceof ItemFocus;

        graphics.pose().pushMatrix();
        graphics.pose().translate(ANCHOR + BAR_OFFSET_X, dialY + ANCHOR + BAR_OFFSET_Y);
        graphics.pose().scale(BAR_SPACE_SCALE, BAR_SPACE_SCALE);
        int loc = max > 0 ? (int) (BAR_MAX_HEIGHT * (float) amt / max) : 0;
        if (loc > 0) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, HUD,
                    -BAR_FILL_W / 2, BAR_BOTTOM - loc, BAR_FILL_U, 0.0F,
                    BAR_FILL_W, loc, BAR_FILL_W, loc, TEX_SIZE, TEX_SIZE,
                    ARGB.color(Math.round(BAR_FILL_ALPHA * 255.0F), energyColor(mc)));
        }
        graphics.blit(RenderPipelines.GUI_TEXTURED, HUD,
                BAR_FRAME_X, BAR_FRAME_Y, BAR_FRAME_U, 0.0F,
                BAR_FRAME_W, BAR_FRAME_H, BAR_FRAME_W, BAR_FRAME_H, TEX_SIZE, TEX_SIZE);
        if (player.isShiftKeyDown()) {
            graphics.pose().pushMatrix();
            graphics.pose().rotate((float) Math.toRadians(-90.0));
            graphics.text(mc.font, AMOUNT_FORMAT.format(amt / (float) WandEconomy.CENTIVIS_PER_VIS),
                    AMOUNT_TEXT_X, AMOUNT_TEXT_Y, WHITE, false);
            graphics.pose().popMatrix();
            if (hasFocus && focusStack.getItem() instanceof ItemFocus focus) {
                float cost = focus.getVisCost(focusStack);
                if (cost > 0.0F) {
                    float modifier = wand.getConsumptionModifier(casterStack, player, false);
                    String msg = AMOUNT_FORMAT.format(cost * modifier);
                    graphics.text(mc.font, msg,
                            AMOUNT_TEXT_X - mc.font.width(msg) / 2, COST_TEXT_Y, WHITE, false);
                }
            }
        }
        graphics.pose().popMatrix();

        if (hasFocus) {
            BlockState picked = wand.getPickedBlock(player.getMainHandItem());
            ItemStack pickedStack = picked == null ? ItemStack.EMPTY : new ItemStack(picked.getBlock().asItem());
            if (!pickedStack.isEmpty()) {
                renderTradeHud(graphics, mc, player, pickedStack, dialY);
            } else {
                graphics.item(focusStack, ANCHOR - ITEM_HALF, dialY + ANCHOR - ITEM_HALF);
            }
        }
    }

    private static void renderTradeHud(GuiGraphicsExtractor graphics, Minecraft mc, LocalPlayer player,
                                       ItemStack picked, int dialY) {
        int amount = 0;
        NonNullList<ItemStack> main = player.getInventory().getNonEquipmentItems();
        for (ItemStack stack : main) {
            if (!stack.isEmpty() && ItemStack.isSameItem(stack, picked)) {
                amount += stack.getCount();
            }
        }
        graphics.item(picked, ANCHOR - ITEM_HALF, dialY + ANCHOR - ITEM_HALF);
        String text = Integer.toString(amount);
        int width = mc.font.width(text);
        graphics.pose().pushMatrix();
        graphics.pose().translate(ANCHOR, dialY + ANCHOR - COUNT_TEXT_LIFT);
        graphics.pose().scale(COUNT_TEXT_SCALE, COUNT_TEXT_SCALE);
        for (int a = -1; a <= 1; a++) {
            for (int b = -1; b <= 1; b++) {
                if ((a == 0 || b == 0) && (a != 0 || b != 0)) {
                    graphics.text(mc.font, text, a + COUNT_TEXT_X - width, b + COUNT_TEXT_Y, OUTLINE_BLACK, false);
                }
            }
        }
        graphics.text(mc.font, text, COUNT_TEXT_X - width, COUNT_TEXT_Y, WHITE, false);
        graphics.pose().popMatrix();
    }

    private static int energyColor(Minecraft mc) {
        if (mc.level == null) {
            return DEFAULT_ENERGY_COLOR;
        }
        return mc.level.registryAccess().lookupOrThrow(IAspect.REGISTRY_KEY)
                .get(TCAspects.POTENTIA)
                .map(holder -> holder.value().color())
                .orElse(DEFAULT_ENERGY_COLOR);
    }
}
