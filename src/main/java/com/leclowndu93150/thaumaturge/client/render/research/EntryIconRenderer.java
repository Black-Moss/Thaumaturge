package com.leclowndu93150.thaumaturge.client.render.research;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.research.IResearchEntry;
import com.leclowndu93150.thaumaturge.api.research.IResearchStage;
import com.leclowndu93150.thaumaturge.api.research.ResearchEntryMeta;
import com.leclowndu93150.thaumaturge.api.research.ResearchIcon;
import com.leclowndu93150.thaumaturge.api.research.ResearchRequirement;
import com.leclowndu93150.thaumaturge.api.spell.part.SpellPart;
import com.leclowndu93150.thaumaturge.client.effect.pipeline.TTRenderPipelines;
import com.leclowndu93150.thaumaturge.client.screen.TTScreenTextures;
import com.leclowndu93150.thaumaturge.client.screen.casters.SpellPartIcons;
import com.mojang.blaze3d.textures.GpuTexture;
import java.util.List;
import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class EntryIconRenderer {
    public static final int ICON_REFERENCE_SIZE = 16;
    public static final int HIT_PADDING = 2;
    public static final Identifier NODE_TEXTURE = TTIds.rl("textures/misc/auranodes.png");

    private static final int OPAQUE = 0xFF;
    private static final int ALPHA_SHIFT = 24;
    private static final int RED_SHIFT = 16;
    private static final int GREEN_SHIFT = 8;
    private static final int COMPLETE_COLOR = 0xFFFFFFFF;
    private static final int UNKNOWN_COLOR = 0xFF4D4D4D;
    private static final int LOCKED_TEXTURE_TINT = 0xFF333333;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int LOCKED_ITEM_OVERLAY = 0x7F000000;
    private static final int WARP_TINT = 0xE5540070;
    private static final long PULSE_PERIOD_MILLIS = 600L;
    private static final float PULSE_BASE = 0.75F;
    private static final float PULSE_AMPLITUDE = 0.25F;
    private static final float CHANNEL_MAX = 255.0F;
    private static final int FRAME_SIZE = 32;
    private static final int FRAME_OFFSET = 8;
    private static final int FRAME_U_NORMAL = 80;
    private static final int FRAME_U_HEX = 112;
    private static final int FRAME_U_ROUND = 144;
    private static final int FRAME_U_SPIKY = 176;
    private static final int FRAME_V_VISIBLE = 48;
    private static final int FRAME_V_HIDDEN = 80;
    private static final int BADGE_SIZE = 32;
    private static final float BADGE_SCALE = 0.5F;
    private static final int BADGE_OFFSET = 9;
    private static final int BADGE_V = 16;
    private static final int BADGE_RESEARCH_U = 176;
    private static final int BADGE_PAGE_U = 208;
    private static final int ICON_CENTER_OFFSET = 8;
    private static final int AURA_SIZE = 90;
    private static final int AURA_FRAME = 64;
    private static final int AURA_SHEET = 2048;
    private static final int AURA_GRID = 32;
    private static final int AURA_FIRST_FRAME = 160;
    private static final long ICON_CYCLE_TICKS = 20L;
    private static final int LOCKED_FOCUS_ALPHA = 50;
    private static final int FOCUS_ALPHA = 220;
    private static final int FOCUS_BASE_SIZE = 24;
    private static final float FOCUS_BACK_SCALE = 0.9F;
    private static final int FOCUS_GLYPH_DIVISOR = 2;
    private static final long TEXTURE_FRAME_MILLIS = 150L;

    public enum Status {
        UNKNOWN, IN_PROGRESS, COMPLETE
    }

    public record FocusIcon(Identifier elementId) {
    }

    private EntryIconRenderer() {}

    public static int colorForStatus(Status status) {
        return switch (status) {
            case COMPLETE -> COMPLETE_COLOR;
            case UNKNOWN -> UNKNOWN_COLOR;
            case IN_PROGRESS -> pulseColor();
        };
    }

    public static int pulseColor() {
        int grey = greyLevel(pulsePhase());
        return grey | grey << GREEN_SHIFT | grey << RED_SHIFT | OPAQUE << ALPHA_SHIFT;
    }

    private static float pulsePhase() {
        long elapsed = Util.getMillis() % PULSE_PERIOD_MILLIS;
        return elapsed / (float) PULSE_PERIOD_MILLIS;
    }

    private static int greyLevel(float phase) {
        float brightness = PULSE_AMPLITUDE * Mth.sin((float) (2.0 * Math.PI * phase)) + PULSE_BASE;
        return Mth.clamp(Math.round(brightness * CHANNEL_MAX), 0, OPAQUE);
    }

    public static void render(GuiGraphicsExtractor graphics, int x, int y, IResearchEntry entry, Status status, ItemStack stack, boolean hasWarp, boolean newResearch, boolean newPage, int ticks) {
        render(graphics, x, y, entry, status, (Object) stack, hasWarp, newResearch, newPage);
    }

    public static void render(GuiGraphicsExtractor graphics, int x, int y, IResearchEntry entry, Status status, Object icon, boolean hasWarp, boolean newResearch, boolean newPage) {
        if (hasWarp) {
            drawForbidden(graphics, x, y);
        }
        int tint = colorForStatus(status);
        int frameV = entry.hasMeta(ResearchEntryMeta.HIDDEN) ? FRAME_V_HIDDEN : FRAME_V_VISIBLE;
        int frameU = entry.hasMeta(ResearchEntryMeta.ROUND) ? FRAME_U_ROUND : entry.hasMeta(ResearchEntryMeta.HEX) ? FRAME_U_HEX : FRAME_U_NORMAL;
        blitFrame(graphics, x - FRAME_OFFSET, y - FRAME_OFFSET, frameU, frameV, tint);
        if (entry.hasMeta(ResearchEntryMeta.SPIKY)) {
            blitFrame(graphics, x - FRAME_OFFSET, y - FRAME_OFFSET, FRAME_U_SPIKY, frameV, tint);
        }
        drawResearchIcon(graphics, x, y, icon, status == Status.UNKNOWN);
        if (newResearch) {
            blitBadge(graphics, x - BADGE_OFFSET, y - BADGE_OFFSET, BADGE_RESEARCH_U);
        }
        if (newPage) {
            blitBadge(graphics, x - BADGE_OFFSET, y + BADGE_OFFSET, BADGE_PAGE_U);
        }
    }

    public static Object resolveIcon(IResearchEntry entry, long ticks) {
        long cycle = ticks / ICON_CYCLE_TICKS;
        List<ResearchIcon> icons = entry.icons();
        if (!icons.isEmpty()) {
            ResearchIcon icon = icons.get((int) Math.floorMod(cycle, (long) icons.size()));
            return switch (icon.kind()) {
                case FOCUS -> new FocusIcon(icon.id());
                case TEXTURE -> icon.id();
                case ITEM -> BuiltInRegistries.ITEM.getOptional(icon.id()).map(ItemStack::new).orElse(ItemStack.EMPTY);
            };
        }
        ResearchRequirement lead = leadingRequirement(entry);
        if (lead == null) {
            return ItemStack.EMPTY;
        }
        HolderSet<Item> pool = lead.items();
        if (pool.size() == 0) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(pool.get((int) Math.floorMod(cycle, (long) pool.size())));
    }

    public static void drawResearchIcon(GuiGraphicsExtractor graphics, int x, int y, Object icon, boolean locked) {
        if (icon instanceof FocusIcon focus) {
            drawFocusIcon(graphics, x, y, focus.elementId(), locked);
            return;
        }
        if (icon instanceof Identifier texture) {
            drawTextureIcon(graphics, x, y, texture, locked);
            return;
        }
        if (icon instanceof ItemStack stack && !stack.isEmpty()) {
            graphics.item(stack, x, y);
            if (locked) {
                graphics.fill(x, y, x + ICON_REFERENCE_SIZE, y + ICON_REFERENCE_SIZE, LOCKED_ITEM_OVERLAY);
            }
        }
    }

    public static void drawFocusIcon(GuiGraphicsExtractor graphics, int x, int y, Identifier elementId, boolean locked) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        int back = Math.round(FOCUS_BASE_SIZE * FOCUS_BACK_SCALE);
        int glyph = Math.round(FOCUS_BASE_SIZE / (float) FOCUS_GLYPH_DIVISOR);
        float alpha = (locked ? LOCKED_FOCUS_ALPHA : FOCUS_ALPHA) / CHANNEL_MAX;
        ResourceKey<SpellPart> key = ResourceKey.create(SpellPart.REGISTRY_KEY, elementId);
        SpellPartIcons.draw(graphics, level.registryAccess(), key, x + ICON_CENTER_OFFSET, y + ICON_CENTER_OFFSET, back, glyph, FOCUS_BASE_SIZE, alpha);
    }

    public static void drawForbidden(GuiGraphicsExtractor graphics, int x, int y) {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        int frame = AURA_FIRST_FRAME + player.tickCount % AURA_GRID;
        float u = (frame % AURA_GRID) * AURA_FRAME;
        float v = (frame / AURA_GRID) * AURA_FRAME;
        int half = AURA_SIZE / 2;
        graphics.blit(TTRenderPipelines.GUI_TEXTURED_ADDITIVE, NODE_TEXTURE, x + ICON_CENTER_OFFSET - half, y + ICON_CENTER_OFFSET - half, u, v, AURA_SIZE, AURA_SIZE, AURA_FRAME, AURA_FRAME,
                AURA_SHEET, AURA_SHEET, WARP_TINT);
    }

    private static @Nullable ResearchRequirement leadingRequirement(IResearchEntry entry) {
        for (IResearchStage stage : entry.stages()) {
            List<ResearchRequirement> source = stage.obtain().isEmpty() ? stage.craft() : stage.obtain();
            if (!source.isEmpty()) {
                return source.get(0);
            }
        }
        return null;
    }

    private static void blitFrame(GuiGraphicsExtractor graphics, int x, int y, int u, int v, int tint) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, TTScreenTextures.RESEARCH_BROWSER, x, y, (float) u, (float) v, FRAME_SIZE, FRAME_SIZE, FRAME_SIZE, FRAME_SIZE, TTScreenTextures.TEX_SIZE,
                TTScreenTextures.TEX_SIZE, tint);
    }

    private static void blitBadge(GuiGraphicsExtractor graphics, int x, int y, int u) {
        graphics.pose().pushMatrix();
        graphics.pose().translate((float) x, (float) y);
        graphics.pose().scale(BADGE_SCALE, BADGE_SCALE);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TTScreenTextures.RESEARCH_BROWSER, 0, 0, (float) u, (float) BADGE_V, BADGE_SIZE, BADGE_SIZE, BADGE_SIZE, BADGE_SIZE, TTScreenTextures.TEX_SIZE,
                TTScreenTextures.TEX_SIZE);
        graphics.pose().popMatrix();
    }

    private static void drawTextureIcon(GuiGraphicsExtractor graphics, int x, int y, Identifier texture, boolean locked) {
        TextureSize size = textureSize(texture);
        Region region = size.width() > 0 && size.height() > 0 ? Region.animated(size, (int) (Util.getMillis() / TEXTURE_FRAME_MILLIS)) : Region.WHOLE;
        int tint = locked ? LOCKED_TEXTURE_TINT : WHITE;
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, region.u, region.v, ICON_REFERENCE_SIZE, ICON_REFERENCE_SIZE, region.w, region.h, region.sheetW, region.sheetH, tint);
    }

    private static TextureSize textureSize(Identifier texture) {
        GpuTexture gpu;
        try {
            gpu = Minecraft.getInstance().getTextureManager().getTexture(texture).getTexture();
        } catch (IllegalStateException notReady) {
            return TextureSize.NOT_READY;
        }
        return new TextureSize(gpu.getWidth(0), gpu.getHeight(0));
    }

    private record TextureSize(int width, int height) {
        static final TextureSize NOT_READY = new TextureSize(0, 0);
    }

    private record Region(float u, float v, int w, int h, int sheetW, int sheetH) {
        static final Region WHOLE = new Region(0.0F, 0.0F, 1, 1, 1, 1);

        static Region animated(TextureSize size, int frameIndex) {
            int shortSide = Math.min(size.width(), size.height());
            int longSide = Math.max(size.width(), size.height());
            boolean strip = longSide > shortSide && longSide % shortSide == 0;
            if (!strip) {
                return new Region(0.0F, 0.0F, size.width(), size.height(), size.width(), size.height());
            }
            float offset = (float) (frameIndex % (longSide / shortSide)) * shortSide;
            boolean vertical = size.height() > size.width();
            return new Region(vertical ? 0.0F : offset, vertical ? offset : 0.0F, shortSide, shortSide, size.width(), size.height());
        }
    }
}
