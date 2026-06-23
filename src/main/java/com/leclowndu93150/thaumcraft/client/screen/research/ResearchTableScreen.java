package com.leclowndu93150.thaumcraft.client.screen.research;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.api.research.IResearchCategory;
import com.leclowndu93150.thaumcraft.api.research.theorycraft.ITheorycraftAid;
import com.leclowndu93150.thaumcraft.api.research.theorycraft.TheorycraftAccess;
import com.leclowndu93150.thaumcraft.api.research.theorycraft.TheorycraftCard;
import com.leclowndu93150.thaumcraft.client.render.research.card.CardSheetRenderer;
import com.leclowndu93150.thaumcraft.client.screen.AbstractTCContainerScreen;
import com.leclowndu93150.thaumcraft.client.screen.TCScreenTextures;
import com.leclowndu93150.thaumcraft.client.screen.TCTooltips;
import com.leclowndu93150.thaumcraft.client.screen.widget.TCImageButton;
import com.leclowndu93150.thaumcraft.content.research.table.MenuResearchTable;
import com.leclowndu93150.thaumcraft.content.research.theorycraft.AidScanner;
import com.leclowndu93150.thaumcraft.content.research.theorycraft.ResearchTableData;
import com.leclowndu93150.thaumcraft.content.research.theorycraft.TheorycraftManager;
import com.leclowndu93150.thaumcraft.network.ServerboundCardAnimationCompletePayload;
import com.leclowndu93150.thaumcraft.network.ServerboundDrawCardsPayload;
import com.leclowndu93150.thaumcraft.network.ServerboundEndSessionPayload;
import com.leclowndu93150.thaumcraft.network.ServerboundPlayCardPayload;
import com.leclowndu93150.thaumcraft.network.ServerboundStartTheoryPayload;
import com.leclowndu93150.thaumcraft.network.ServerboundTableActionPayload;
import com.leclowndu93150.thaumcraft.registry.TCSounds;
import com.leclowndu93150.thaumcraft.registry.TCTheorycraft;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public final class ResearchTableScreen extends AbstractTCContainerScreen<MenuResearchTable> {
    public static final int PANEL_W = 255;
    public static final int PANEL_H = 255;

    private static final int INSPIRATION_ANCHOR_X = 128;
    private static final int INSPIRATION_ANCHOR_Y = 16;
    private static final int INSPIRATION_STRIDE_PRESCALE = 20;
    private static final float INSPIRATION_SCALE = 0.5F;
    private static final int INK_BOTTLE_SIZE = 16;
    private static final int INK_BOTTLE_FILLED_U = 32;
    private static final int INK_BOTTLE_EMPTY_U = 48;
    private static final int INK_BOTTLE_V = 96;

    private static final int BONUS_DRAW_ANCHOR_X = 15;
    private static final int BONUS_DRAW_ANCHOR_Y = 150;
    private static final int BONUS_DRAW_U = 64;
    private static final int BONUS_DRAW_V = 96;
    private static final int BONUS_DRAW_SIZE = 16;
    private static final int BONUS_DRAW_STRIDE_X = 2;
    private static final int BONUS_DRAW_STRIDE_Y = 1;

    private static final int DECK_ANCHOR_X = 65;
    private static final int DECK_ANCHOR_Y = 100;
    private static final int DECK_HIT_OFFSET_X = 25;
    private static final int DECK_HIT_OFFSET_Y = 55;
    private static final int DECK_HIT_W = 75;
    private static final int DECK_HIT_H = 90;
    private static final long DECK_SHEET_SEED = 55L;
    private static final int DECK_SHEETS_PER_PAPER = 4;
    private static final float QUESTION_SCALE_INACTIVE = 1.5F;
    private static final float QUESTION_SCALE_HOVER = 1.75F;
    private static final float QUESTION_ALPHA_INACTIVE = 0.5F;
    private static final float QUESTION_ALPHA_HOVER = 1.0F;
    private static final int QUESTION_HALF = 8;
    private static final int QUESTION_QUAD_SIZE = 16;
    private static final int QUESTION_SRC_SIZE = 32;
    private static final int QUESTION_TEXTURE_SIZE = 32;
    private static final Identifier QUESTION_TEXTURE =
            Identifier.fromNamespaceAndPath(TCIds.MODID, "textures/aspects/_unknown.png");

    private static final int SAVED_CARD_ANCHOR_X = 191;
    private static final int SAVED_CARD_ANCHOR_Y = 100;

    private static final int CARD_ANCHOR_Y = 100;
    private static final int CARD_STRIDE = 110;
    private static final int CARD_BASE_X = 128;
    private static final int CARD_HALF_STRIDE = 55;
    private static final int CARD_HIT_OFFSET_X = 5;
    private static final int CARD_HIT_OFFSET_Y = -60;
    private static final int CARD_ITEM_STRIDE = 18;
    private static final int CARD_ITEM_HIT_SIZE = 15;
    private static final int CARD_ITEM_HIT_OFFSET_Y = 36;

    private static final int CATEGORY_SIDEBAR_X = 253;
    private static final int CATEGORY_SIDEBAR_Y = 16;
    private static final int CATEGORY_SIDEBAR_STRIDE = 18;
    private static final int CATEGORY_SIDEBAR_GAP = 4;
    private static final float CATEGORY_ICON_SCALE = 0.0625F;
    private static final int CATEGORY_LABEL_OFFSET_X = 23;
    private static final int CATEGORY_LABEL_OFFSET_Y = 4;
    private static final int CATEGORY_ICON_HIT_SIZE = 16;
    private static final int CATEGORY_ICON_HIT_OFFSET_X = 3;

    private static final int CATEGORY_LABEL_COLOR_BLOCKED = 0xFF606060;
    private static final int CATEGORY_LABEL_COLOR_NORMAL = 0xFF00E0C0;
    private static final int CATEGORY_LABEL_COLOR_PENALTY = 0xFFFFFFFF;

    private static final int BUTTON_FACE_U = 37;
    private static final int BUTTON_FACE_V = 66;
    private static final int BUTTON_W = 49;
    private static final int BUTTON_H = 11;
    private static final int BUTTON_FACE_W = 51;
    private static final int BUTTON_FACE_H = 13;

    private static final int CREATE_BUTTON_X = 128;
    private static final int CREATE_BUTTON_Y = 22;
    private static final int COMPLETE_BUTTON_X = 191;
    private static final int COMPLETE_BUTTON_Y = 96;
    private static final int SCRAP_BUTTON_X = 128;
    private static final int SCRAP_BUTTON_Y = 168;

    private static final int CREATE_BUTTON_TINT = 0xFF88FFAA;
    private static final int COMPLETE_BUTTON_TINT = 0xFF88FFAA;
    private static final int SCRAP_BUTTON_TINT = 0xFFFF2222;

    private static final int MAX_HAND_SIZE = 3;
    private static final float ZOOM_GATE = 0.6F;
    private static final float ZOOM_OUT_TICK_FRACTION = 1.0F / 5.0F;
    private static final float ZOOM_IN_TICK_FRACTION = 1.0F / 3.0F;
    private static final float ZOOM_INACTIVE_TICK_RATE = 0.3F;
    private static final float HOVER_MAX = 0.25F;
    private static final float HOVER_TICK_FRACTION = 1.0F / 3.0F;
    private static final float HOVER_DECAY_PER_TICK = 0.1F;
    private static final float MIN_TICK_DELTA = 0.0025F;
    private static final float HOVER_ACTIVATE_THRESHOLD = 0.95F;

    private static final float WRITE_SOUND_VOLUME = 0.3F;
    private static final float CLACK_SOUND_VOLUME = 0.4F;
    private static final float SOUND_PITCH = 1.0F;

    private final float[] cardZoomOut = new float[MAX_HAND_SIZE];
    private final float[] cardZoomIn = new float[MAX_HAND_SIZE];
    private final float[] cardHover = new float[MAX_HAND_SIZE];
    private final boolean[] cardActive = new boolean[MAX_HAND_SIZE];
    private final boolean[] cardZoomOutStarted = new boolean[MAX_HAND_SIZE];
    private boolean cardSelected;
    private boolean cardSelectedSoundPlayed;
    private int previousHandSignature;

    private TCImageButton createButton;
    private TCImageButton completeButton;
    private TCImageButton scrapButton;

    private final LinkedHashSet<ResourceKey<ITheorycraftAid>> availableAids = new LinkedHashSet<>();
    private final LinkedHashSet<ResourceKey<ITheorycraftAid>> selectedAids = new LinkedHashSet<>();
    private int aidScanTickTimer;
    private int availableInspiration;

    private final HashMap<ResourceKey<IResearchCategory>, Integer> tempCatTotals = new HashMap<>();
    private final HashSet<ResourceKey<IResearchCategory>> sparkleQueue = new HashSet<>();

    private static final int AID_SCAN_INTERVAL_TICKS = 100;
    private static final int AID_ROW_MAX = 6;
    private static final int AID_ANCHOR_X = 128;
    private static final int AID_ROW_Y = 85;
    private static final int AID_COL_STRIDE = 20;
    private static final int AID_ROW_STRIDE = 35;
    private static final int AID_ICON_SIZE = 16;
    private static final int AID_SELECTED_U = 0;
    private static final int AID_SELECTED_V = 96;
    private static final int AID_HOVER_U = 0;
    private static final int AID_HOVER_V = 96;
    private static final int AID_HOVER_TINT = 0x33FFFFFF;
    private static final int DUMMY_INSP_ANCHOR_Y = 55;

    private static final int WARNING_CENTER_X = 116;
    private static final int WARNING_BASE_Y = 60;
    private static final int WARNING_GAP_Y = 40;

    public ResearchTableScreen(MenuResearchTable menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, TCScreenTextures.RESEARCH_TABLE, PANEL_W, PANEL_H);
        resetCardAnimations();
    }

    private void resetCardAnimations() {
        for (int i = 0; i < MAX_HAND_SIZE; i++) {
            cardZoomOut[i] = 0.0F;
            cardZoomIn[i] = 0.0F;
            cardHover[i] = 0.0F;
            cardActive[i] = true;
            cardZoomOutStarted[i] = false;
        }
        cardSelected = false;
        cardSelectedSoundPlayed = false;
    }

    @Override
    protected void extractBackgroundOverlay(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        if (minecraft == null || minecraft.player == null) return;
        ResearchTableData data = sessionData();
        boolean inSession = data != null && data.inspirationStart() > 0;
        if (!inSession) {
            renderDummyInspirationRow(graphics);
            renderAidsBackground(graphics);
            return;
        }
        syncCardAnimationState(data);
        renderInspirationRow(graphics, data);
        renderBonusDraws(graphics, data);
        renderSavedCards(graphics, data);
        renderLastDraw(graphics, data);
        renderDeckOrCards(graphics, data, mouseX, mouseY, partialTick);
        renderCategorySidebar(graphics, data);
        renderNoSuppliesWarning(graphics, data, mouseX, mouseY);
    }

    @Override
    public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractContents(graphics, mouseX, mouseY, partialTick);
        if (minecraft == null || minecraft.player == null) return;
        ResearchTableData data = sessionData();
        boolean inSession = data != null && data.inspirationStart() > 0;
        if (!inSession) {
            renderAidsHoverOverlay(graphics, mouseX, mouseY);
            return;
        }
        renderCardItemTooltips(graphics, data, mouseX, mouseY);
        renderCategorySidebarTooltip(graphics, data, mouseX, mouseY);
    }

    private void renderDummyInspirationRow(GuiGraphicsExtractor graphics) {
        if (availableInspiration <= 0) return;
        int used = selectedAids.size();
        graphics.pose().pushMatrix();
        graphics.pose().translate(leftPos + AID_ANCHOR_X - availableInspiration * 5.0F, topPos + DUMMY_INSP_ANCHOR_Y);
        graphics.pose().scale(INSPIRATION_SCALE, INSPIRATION_SCALE);
        for (int i = 0; i < availableInspiration; i++) {
            int u = availableInspiration - used <= i ? INK_BOTTLE_EMPTY_U : INK_BOTTLE_FILLED_U;
            graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    TCScreenTextures.GUI_BASE,
                    INSPIRATION_STRIDE_PRESCALE * i, 0,
                    (float) u, (float) INK_BOTTLE_V,
                    INK_BOTTLE_SIZE, INK_BOTTLE_SIZE,
                    INK_BOTTLE_SIZE, INK_BOTTLE_SIZE,
                    TCScreenTextures.TEX_SIZE, TCScreenTextures.TEX_SIZE
            );
        }
        graphics.pose().popMatrix();
    }

    private void renderAidsBackground(GuiGraphicsExtractor graphics) {
        if (availableAids.isEmpty()) return;
        int total = availableAids.size();
        int rowSize = Math.min(total, AID_ROW_MAX);
        int row = 0;
        int col = 0;
        for (ResourceKey<ITheorycraftAid> key : availableAids) {
            int x = leftPos + AID_ANCHOR_X + AID_COL_STRIDE * col - rowSize * 10;
            int y = topPos + AID_ROW_Y + AID_ROW_STRIDE * row;
            boolean selected = selectedAids.contains(key);
            if (selected) {
                graphics.blit(
                        RenderPipelines.GUI_TEXTURED,
                        TCScreenTextures.GUI_BASE,
                        x, y,
                        (float) AID_SELECTED_U, (float) AID_SELECTED_V,
                        AID_ICON_SIZE, AID_ICON_SIZE,
                        AID_ICON_SIZE, AID_ICON_SIZE,
                        TCScreenTextures.TEX_SIZE, TCScreenTextures.TEX_SIZE
                );
            }
            aidHolder(key).ifPresent(holder -> {
                ItemStack stack = holder.value().displayStack();
                if (!stack.isEmpty()) {
                    graphics.item(stack, x, y);
                }
            });
            col++;
            if (col >= rowSize) {
                row++;
                col = 0;
            }
        }
    }

    private void renderAidsHoverOverlay(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (availableAids.isEmpty()) return;
        int total = availableAids.size();
        int rowSize = Math.min(total, AID_ROW_MAX);
        int row = 0;
        int col = 0;
        ResourceKey<ITheorycraftAid> hoveredKey = null;
        for (ResourceKey<ITheorycraftAid> key : availableAids) {
            int x = leftPos + AID_ANCHOR_X + AID_COL_STRIDE * col - rowSize * 10;
            int y = topPos + AID_ROW_Y + AID_ROW_STRIDE * row;
            boolean hovered = mouseX >= x && mouseX < x + AID_ICON_SIZE
                    && mouseY >= y && mouseY < y + AID_ICON_SIZE;
            boolean selected = selectedAids.contains(key);
            if (hovered && !selected) {
                graphics.blit(
                        RenderPipelines.GUI_TEXTURED,
                        TCScreenTextures.GUI_BASE,
                        x, y,
                        (float) AID_HOVER_U, (float) AID_HOVER_V,
                        AID_ICON_SIZE, AID_ICON_SIZE,
                        AID_ICON_SIZE, AID_ICON_SIZE,
                        TCScreenTextures.TEX_SIZE, TCScreenTextures.TEX_SIZE,
                        AID_HOVER_TINT
                );
            }
            if (hovered) {
                hoveredKey = key;
            }
            col++;
            if (col >= rowSize) {
                row++;
                col = 0;
            }
        }
        if (hoveredKey != null) {
            Holder.Reference<ITheorycraftAid> holder = aidHolder(hoveredKey).orElse(null);
            if (holder != null) {
                graphics.setTooltipForNextFrame(font, holder.value().description(), mouseX, mouseY);
            }
        }
    }

    private Optional<Holder.Reference<ITheorycraftAid>> aidHolder(ResourceKey<ITheorycraftAid> key) {
        if (minecraft == null || minecraft.player == null) return Optional.empty();
        return minecraft.player.registryAccess()
                .lookup(TCTheorycraft.AIDS_REGISTRY_KEY)
                .flatMap(lookup -> lookup.get(key));
    }

    private int aidIndexAt(double mx, double my) {
        if (availableAids.isEmpty()) return -1;
        int total = availableAids.size();
        int rowSize = Math.min(total, AID_ROW_MAX);
        int row = 0;
        int col = 0;
        int i = 0;
        for (ResourceKey<ITheorycraftAid> key : availableAids) {
            int x = leftPos + AID_ANCHOR_X + AID_COL_STRIDE * col - rowSize * 10;
            int y = topPos + AID_ROW_Y + AID_ROW_STRIDE * row;
            if (mx >= x && mx < x + AID_ICON_SIZE && my >= y && my < y + AID_ICON_SIZE) {
                return i;
            }
            col++;
            if (col >= rowSize) {
                row++;
                col = 0;
            }
            i++;
        }
        return -1;
    }

    private ResourceKey<ITheorycraftAid> aidAt(int index) {
        int i = 0;
        for (ResourceKey<ITheorycraftAid> key : availableAids) {
            if (i == index) return key;
            i++;
        }
        return null;
    }

    private void rescanAids() {
        if (minecraft == null || minecraft.player == null) return;
        availableAids.clear();
        availableAids.addAll(AidScanner.scan(minecraft.player.level(), menu.pos()));
        selectedAids.retainAll(availableAids);
        availableInspiration = computeAvailableInspiration();
        int budget = Math.max(0, availableInspiration - 1);
        while (selectedAids.size() > budget) {
            ResourceKey<ITheorycraftAid> last = null;
            for (ResourceKey<ITheorycraftAid> key : selectedAids) last = key;
            if (last == null) break;
            selectedAids.remove(last);
        }
    }

    private int computeAvailableInspiration() {
        return TheorycraftManager.computeInspirationFor(minecraft.player);
    }

    private void renderNoSuppliesWarning(GuiGraphicsExtractor graphics, ResearchTableData data, int mouseX, int mouseY) {
        if (data.inspirationStart() <= 0) return;
        int y = topPos + WARNING_BASE_Y;
        if (!menu.hasUsableScribeTools()) {
            int sx = Math.max(
                    font.width(TCTooltips.noInkLine0()),
                    font.width(TCTooltips.noInkLine1())
            ) / 2;
            int x = leftPos - sx + WARNING_CENTER_X;
            graphics.setTooltipForNextFrame(
                    font,
                    List.of(TCTooltips.noInkLine0(), TCTooltips.noInkLine1()),
                    Optional.empty(),
                    x,
                    y);
            y += WARNING_GAP_Y;
        }
        if (!menu.hasPaperReady()) {
            int sx = font.width(TCTooltips.noPaperLine0()) / 2;
            int x = leftPos - sx + WARNING_CENTER_X;
            graphics.setTooltipForNextFrame(
                    font,
                    List.of(TCTooltips.noPaperLine0()),
                    Optional.empty(),
                    x,
                    y);
        }
    }

    private ResearchTableData sessionData() {
        if (minecraft == null || minecraft.player == null) return null;
        return TheorycraftAccess.of(minecraft.player) instanceof ResearchTableData rt ? rt : null;
    }

    private void renderInspirationRow(GuiGraphicsExtractor graphics, ResearchTableData data) {
        int total = data.inspirationStart();
        if (total <= 0) return;
        int remaining = data.inspiration();
        graphics.pose().pushMatrix();
        graphics.pose().translate(leftPos + INSPIRATION_ANCHOR_X - total * 5.0F, topPos + INSPIRATION_ANCHOR_Y);
        graphics.pose().scale(INSPIRATION_SCALE, INSPIRATION_SCALE);
        for (int i = 0; i < total; i++) {
            int u = remaining <= i ? INK_BOTTLE_EMPTY_U : INK_BOTTLE_FILLED_U;
            graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    TCScreenTextures.GUI_BASE,
                    INSPIRATION_STRIDE_PRESCALE * i, 0,
                    (float) u, (float) INK_BOTTLE_V,
                    INK_BOTTLE_SIZE, INK_BOTTLE_SIZE,
                    INK_BOTTLE_SIZE, INK_BOTTLE_SIZE,
                    TCScreenTextures.TEX_SIZE, TCScreenTextures.TEX_SIZE
            );
        }
        graphics.pose().popMatrix();
    }

    private void renderBonusDraws(GuiGraphicsExtractor graphics, ResearchTableData data) {
        int bonus = data.bonusDraws();
        if (bonus <= 0) return;
        for (int i = 0; i < bonus; i++) {
            graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    TCScreenTextures.GUI_BASE,
                    leftPos + BONUS_DRAW_ANCHOR_X + i * BONUS_DRAW_STRIDE_X,
                    topPos + BONUS_DRAW_ANCHOR_Y + i * BONUS_DRAW_STRIDE_Y,
                    (float) BONUS_DRAW_U, (float) BONUS_DRAW_V,
                    BONUS_DRAW_SIZE, BONUS_DRAW_SIZE,
                    BONUS_DRAW_SIZE, BONUS_DRAW_SIZE,
                    TCScreenTextures.TEX_SIZE, TCScreenTextures.TEX_SIZE
            );
        }
    }

    private void renderDeckOrCards(GuiGraphicsExtractor graphics, ResearchTableData data, int mouseX, int mouseY, float partialTick) {
        List<ResearchTableData.CardChoice> choices = data.cardChoices();
        if (choices.isEmpty()) {
            renderDeck(graphics, data, mouseX, mouseY);
            return;
        }
        HolderLookup.Provider registries = minecraft.player.registryAccess();
        int count = Math.min(choices.size(), MAX_HAND_SIZE);
        RenderedCard[] renderedCards = new RenderedCard[count];
        int activeCompleteIndex = -1;
        for (int i = 0; i < count; i++) {
            ResearchTableData.CardChoice choice = choices.get(i);
            boolean active = cardActive[i] || choice.selected();
            float prevZoomIn = cardZoomIn[i];
            updateCardAnimation(i, count, active, mouseX, mouseY, partialTick);
            float dx = CARD_HALF_STRIDE + CARD_BASE_X - CARD_HALF_STRIDE * count + (float) CARD_STRIDE * i - DECK_ANCHOR_X;
            float fx = leftPos + DECK_ANCHOR_X + dx * cardZoomOut[i];
            float qx = leftPos + SAVED_CARD_ANCHOR_X - fx;
            if (active) {
                fx += qx * cardZoomIn[i];
            }
            float scale = CardSheetRenderer.DEFAULT_SCALE + cardZoomOut[i] * 2.0F - cardZoomIn[i] * 2.0F + cardHover[i];
            float alphaActive = active ? 1.0F : 1.0F - cardZoomIn[i];
            float tilt = Math.max(1.0F - cardZoomOut[i], cardZoomIn[i]);
            renderedCards[i] = new RenderedCard(i, choice, active, fx, topPos + CARD_ANCHOR_Y, scale, tilt, alphaActive);
            if (cardSelected && active && cardZoomIn[i] >= 1.0F && prevZoomIn < 1.0F) {
                activeCompleteIndex = i;
            }
        }
        int activeIndex = activeCardIndex(renderedCards);
        for (RenderedCard rendered : renderedCards) {
            if (rendered.index() != activeIndex) {
                renderCard(graphics, registries, rendered);
            }
        }
        if (activeIndex >= 0) {
            renderCard(graphics, registries, renderedCards[activeIndex]);
        }
        if (activeCompleteIndex >= 0) {
            playLocalSound(TCSounds.WRITE.get(), WRITE_SOUND_VOLUME, SOUND_PITCH);
            ClientPacketDistributor.sendToServer(ServerboundCardAnimationCompletePayload.INSTANCE);
        }
    }

    private void renderCardItemTooltips(GuiGraphicsExtractor graphics, ResearchTableData data, int mouseX, int mouseY) {
        if (cardSelected) return;
        List<ResearchTableData.CardChoice> choices = data.cardChoices();
        if (choices.isEmpty()) return;
        HolderLookup.Provider registries = minecraft.player.registryAccess();
        int count = Math.min(choices.size(), MAX_HAND_SIZE);
        for (int i = 0; i < count; i++) {
            if (cardZoomOut[i] < 1.0F) continue;
            ResearchTableData.CardChoice choice = choices.get(i);
            TheorycraftCard card = choice.hydrate(registries);
            if (card == null) continue;
            List<TheorycraftCard.CardItemRequirement> items = card.requiredItems();
            if (items.isEmpty()) continue;
            float dx = CARD_HALF_STRIDE + CARD_BASE_X - CARD_HALF_STRIDE * count + (float) CARD_STRIDE * i - DECK_ANCHOR_X;
            float centerX = leftPos + DECK_ANCHOR_X + dx * cardZoomOut[i];
            float centerY = topPos + CARD_ANCHOR_Y;
            int itemCount = items.size();
            for (int a = 0; a < itemCount; a++) {
                int hitX = (int) (centerX - 9 * itemCount + CARD_ITEM_STRIDE * a);
                int hitY = (int) (centerY + CARD_ITEM_HIT_OFFSET_Y);
                if (mouseX < hitX || mouseX >= hitX + CARD_ITEM_HIT_SIZE) continue;
                if (mouseY < hitY || mouseY >= hitY + CARD_ITEM_HIT_SIZE) continue;
                ItemStack stack = items.get(a).stack();
                if (!stack.isEmpty()) {
                    graphics.setTooltipForNextFrame(font, stack, mouseX, mouseY);
                } else {
                    graphics.setTooltipForNextFrame(font, TCTooltips.cardUnknown(), mouseX, mouseY);
                }
            }
        }
    }

    private void renderCard(GuiGraphicsExtractor graphics, HolderLookup.Provider registries, RenderedCard card) {
        if (card == null) return;
        CardSheetRenderer.render(
                graphics, font, registries,
                card.centerX(), card.centerY(),
                card.scale(), card.tilt(), card.alpha(),
                card.choice()
        );
    }

    private int activeCardIndex(RenderedCard[] renderedCards) {
        if (!cardSelected) return -1;
        for (RenderedCard rendered : renderedCards) {
            if (rendered != null && rendered.active()) return rendered.index();
        }
        return -1;
    }

    private void updateCardAnimation(int index, int count, boolean active, int mouseX, int mouseY, float partialTick) {
        if (cardZoomOut[index] >= HOVER_ACTIVATE_THRESHOLD && !cardSelected) {
            double hitX = leftPos + CARD_HIT_OFFSET_X + CARD_BASE_X - CARD_HALF_STRIDE * count + (double) CARD_STRIDE * index;
            double hitY = topPos + CARD_ANCHOR_Y + CARD_HIT_OFFSET_Y;
            boolean hovered = mouseX >= hitX && mouseY >= hitY
                    && mouseX < hitX + CardSheetRenderer.CARD_HIT_WIDTH
                    && mouseY < hitY + CardSheetRenderer.CARD_HIT_HEIGHT;
            if (hovered) {
                cardHover[index] += Math.max((HOVER_MAX - cardHover[index]) * HOVER_TICK_FRACTION * partialTick, MIN_TICK_DELTA);
            } else {
                cardHover[index] -= HOVER_DECAY_PER_TICK * partialTick;
            }
        }
        boolean nextIsRevealed = index == count - 1 || cardZoomOut[index + 1] > ZOOM_GATE;
        if (nextIsRevealed) {
            float prev = cardZoomOut[index];
            cardZoomOut[index] += Math.max((1.0F - cardZoomOut[index]) * ZOOM_OUT_TICK_FRACTION * partialTick, MIN_TICK_DELTA);
            if (!cardZoomOutStarted[index] && cardZoomOut[index] > 0.0F && prev == 0.0F) {
                cardZoomOutStarted[index] = true;
                playLocalSound(TCSounds.PAGE.get(), 1.0F, SOUND_PITCH);
            }
        }
        if (cardSelected) {
            float delta = active
                    ? Math.max((1.0F - cardZoomIn[index]) * ZOOM_IN_TICK_FRACTION * partialTick, MIN_TICK_DELTA)
                    : ZOOM_INACTIVE_TICK_RATE * partialTick;
            cardZoomIn[index] += delta;
            cardHover[index] = 1.0F - cardZoomIn[index];
        }
        cardZoomIn[index] = Mth.clamp(cardZoomIn[index], 0.0F, 1.0F);
        cardHover[index] = Mth.clamp(cardHover[index], 0.0F, HOVER_MAX);
        cardZoomOut[index] = Mth.clamp(cardZoomOut[index], 0.0F, 1.0F);
    }

    private void renderDeck(GuiGraphicsExtractor graphics, ResearchTableData data, int mouseX, int mouseY) {
        if (data.isComplete()) return;
        int paperCount = menu.tableItems().getAmountAsInt(1);
        if (paperCount <= 0) return;
        int sheets = 1 + paperCount / DECK_SHEETS_PER_PAPER;
        Random r = new Random(DECK_SHEET_SEED);
        for (int i = 0; i < sheets; i++) {
            CardSheetRenderer.renderBlank(graphics,
                    leftPos + DECK_ANCHOR_X, topPos + DECK_ANCHOR_Y,
                    CardSheetRenderer.DEFAULT_SCALE, 1.0F, 1.0F,
                    r.nextLong(), false);
        }
        int hitOriginX = leftPos + DECK_HIT_OFFSET_X;
        int hitOriginY = topPos + DECK_HIT_OFFSET_Y;
        boolean highlight = mouseX >= hitOriginX && mouseY >= hitOriginY
                && mouseX < hitOriginX + DECK_HIT_W
                && mouseY < hitOriginY + DECK_HIT_H;
        float scale = highlight ? QUESTION_SCALE_HOVER : QUESTION_SCALE_INACTIVE;
        float alpha = highlight ? QUESTION_ALPHA_HOVER : QUESTION_ALPHA_INACTIVE;
        int color = packAlpha(alpha);
        graphics.pose().pushMatrix();
        graphics.pose().translate(leftPos + DECK_ANCHOR_X, topPos + DECK_ANCHOR_Y);
        graphics.pose().scale(scale, scale);
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                QUESTION_TEXTURE,
                -QUESTION_HALF, -QUESTION_HALF,
                0.0F, 0.0F,
                QUESTION_QUAD_SIZE, QUESTION_QUAD_SIZE,
                QUESTION_SRC_SIZE, QUESTION_SRC_SIZE,
                QUESTION_TEXTURE_SIZE, QUESTION_TEXTURE_SIZE,
                color
        );
        graphics.pose().popMatrix();
    }

    private static int packAlpha(float alpha) {
        int a = Math.max(0, Math.min(255, Math.round(alpha * 255.0F)));
        return (a << 24) | 0xFFFFFF;
    }

    private void renderSavedCards(GuiGraphicsExtractor graphics, ResearchTableData data) {
        for (long seed : data.savedCards()) {
            CardSheetRenderer.renderBlank(graphics,
                    leftPos + SAVED_CARD_ANCHOR_X, topPos + SAVED_CARD_ANCHOR_Y,
                    CardSheetRenderer.DEFAULT_SCALE, 1.0F, 1.0F,
                    seed, false);
        }
    }

    private void renderLastDraw(GuiGraphicsExtractor graphics, ResearchTableData data) {
        ResearchTableData.CardChoice last = data.lastDraw();
        if (last == null) return;
        HolderLookup.Provider registries = minecraft.player.registryAccess();
        CardSheetRenderer.render(graphics, font, registries,
                leftPos + SAVED_CARD_ANCHOR_X, topPos + SAVED_CARD_ANCHOR_Y,
                CardSheetRenderer.DEFAULT_SCALE, 1.0F, 1.0F,
                last);
    }

    private void renderCategorySidebar(GuiGraphicsExtractor graphics, ResearchTableData data) {
        Map<ResourceKey<IResearchCategory>, Integer> sorted = sortedTempCategoryTotals();
        if (sorted.isEmpty()) return;
        int i = 0;
        for (Map.Entry<ResourceKey<IResearchCategory>, Integer> entry : sorted.entrySet()) {
            int iconGap = i > 0 ? CATEGORY_SIDEBAR_GAP : 0;
            int textGap = i > data.penaltyStart() ? CATEGORY_SIDEBAR_GAP : 0;
            int iconX = leftPos + CATEGORY_SIDEBAR_X;
            int iconY = topPos + CATEGORY_SIDEBAR_Y + i * CATEGORY_SIDEBAR_STRIDE + iconGap;
            int textX = leftPos + CATEGORY_SIDEBAR_X + CATEGORY_LABEL_OFFSET_X;
            int textY = topPos + CATEGORY_SIDEBAR_Y + CATEGORY_LABEL_OFFSET_Y + i * CATEGORY_SIDEBAR_STRIDE + textGap;
            int percent = entry.getValue();
            ResourceKey<IResearchCategory> key = entry.getKey();
            int color = data.isCategoryBlocked(key)
                    ? CATEGORY_LABEL_COLOR_BLOCKED
                    : (i <= data.penaltyStart()
                            ? CATEGORY_LABEL_COLOR_NORMAL
                            : CATEGORY_LABEL_COLOR_PENALTY);
            minecraft.player.registryAccess().lookup(IResearchCategory.REGISTRY_KEY).flatMap(lookup -> lookup.get(key)).ifPresent(holder -> {
                graphics.pose().pushMatrix();
                graphics.pose().translate(iconX, iconY);
                graphics.pose().scale(CATEGORY_ICON_SCALE, CATEGORY_ICON_SCALE);
                graphics.blit(
                        RenderPipelines.GUI_TEXTURED,
                        holder.value().icon(),
                        0, 0,
                        0.0F, 0.0F,
                        TCScreenTextures.TEX_SIZE, TCScreenTextures.TEX_SIZE,
                        TCScreenTextures.TEX_SIZE, TCScreenTextures.TEX_SIZE,
                        TCScreenTextures.TEX_SIZE, TCScreenTextures.TEX_SIZE
                );
                graphics.pose().popMatrix();
            });
            String label = percent + "%";
            if (i > data.penaltyStart()) {
                int q = percent / 3;
                label = label + " (-" + q + ")";
            }
            graphics.text(font, Component.literal(label), textX, textY, color, true);
            i++;
        }
    }

    private void renderCategorySidebarTooltip(GuiGraphicsExtractor graphics, ResearchTableData data, int mouseX, int mouseY) {
        Map<ResourceKey<IResearchCategory>, Integer> sorted = sortedTempCategoryTotals();
        if (sorted.isEmpty()) return;
        int i = 0;
        for (Map.Entry<ResourceKey<IResearchCategory>, Integer> entry : sorted.entrySet()) {
            int textGap = i > data.penaltyStart() ? CATEGORY_SIDEBAR_GAP : 0;
            int hitX = leftPos + CATEGORY_SIDEBAR_X + CATEGORY_ICON_HIT_OFFSET_X;
            int hitY = topPos + CATEGORY_SIDEBAR_Y + i * CATEGORY_SIDEBAR_STRIDE + textGap;
            if (mouseX >= hitX && mouseX < hitX + CATEGORY_ICON_HIT_SIZE
                    && mouseY >= hitY && mouseY < hitY + CATEGORY_ICON_HIT_SIZE) {
                graphics.setTooltipForNextFrame(font, TCTooltips.categoryName(entry.getKey()), mouseX, mouseY);
                return;
            }
            i++;
        }
    }

    private Map<ResourceKey<IResearchCategory>, Integer> sortedTempCategoryTotals() {
        if (tempCatTotals.isEmpty()) return Map.of();
        Map<ResourceKey<IResearchCategory>, Integer> filtered = new LinkedHashMap<>();
        for (Map.Entry<ResourceKey<IResearchCategory>, Integer> entry : tempCatTotals.entrySet()) {
            if (entry.getValue() != 0) {
                filtered.put(entry.getKey(), entry.getValue());
            }
        }
        return filtered.entrySet().stream()
                .sorted(Map.Entry.<ResourceKey<IResearchCategory>, Integer>comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new));
    }

    private void tickTempCategoryTotals(ResearchTableData data) {
        if (minecraft == null || minecraft.player == null) return;
        sparkleQueue.clear();
        Set<ResourceKey<IResearchCategory>> all = new LinkedHashSet<>(tempCatTotals.keySet());
        all.addAll(data.categoriesWithTotal());
        minecraft.player.registryAccess().lookup(IResearchCategory.REGISTRY_KEY).ifPresent(lookup ->
                lookup.listElements().forEach(holder -> holder.unwrapKey().ifPresent(all::add)));
        for (ResourceKey<IResearchCategory> key : all) {
            int t0 = data.categoryTotal(key);
            Integer current = tempCatTotals.get(key);
            int t1 = current == null ? 0 : current;
            if (t0 == 0 && t1 == 0) {
                tempCatTotals.remove(key);
                continue;
            }
            if (t1 > t0) {
                t1--;
            }
            if (t1 < t0) {
                t1++;
                sparkleQueue.add(key);
            }
            tempCatTotals.put(key, t1);
        }
    }

    @Override
    protected void init() {
        super.init();
        createButton = TCImageButton.centered(
                leftPos + CREATE_BUTTON_X, topPos + CREATE_BUTTON_Y,
                BUTTON_W, BUTTON_H,
                TCScreenTextures.GUI_BASE,
                BUTTON_FACE_U, BUTTON_FACE_V,
                BUTTON_FACE_W, BUTTON_FACE_H,
                TCScreenTextures.TEX_SIZE, TCScreenTextures.TEX_SIZE,
                Component.translatable("button.thaumcraft.create_theory"),
                () -> {
                    playLocalSound(TCSounds.CLACK.get(), CLACK_SOUND_VOLUME, SOUND_PITCH);
                    ClientPacketDistributor.sendToServer(new ServerboundStartTheoryPayload(new ArrayList<>(selectedAids)));
                }
        );
        createButton.setTintColor(CREATE_BUTTON_TINT);
        completeButton = TCImageButton.centered(
                leftPos + COMPLETE_BUTTON_X, topPos + COMPLETE_BUTTON_Y,
                BUTTON_W, BUTTON_H,
                TCScreenTextures.GUI_BASE,
                BUTTON_FACE_U, BUTTON_FACE_V,
                BUTTON_FACE_W, BUTTON_FACE_H,
                TCScreenTextures.TEX_SIZE, TCScreenTextures.TEX_SIZE,
                Component.translatable("button.thaumcraft.complete_theory"),
                () -> {
                    playLocalSound(TCSounds.CLACK.get(), CLACK_SOUND_VOLUME, SOUND_PITCH);
                    tempCatTotals.clear();
                    sparkleQueue.clear();
                    ClientPacketDistributor.sendToServer(new ServerboundTableActionPayload(ServerboundTableActionPayload.Action.COMPLETE));
                }
        );
        completeButton.setTintColor(COMPLETE_BUTTON_TINT);
        scrapButton = TCImageButton.centered(
                leftPos + SCRAP_BUTTON_X, topPos + SCRAP_BUTTON_Y,
                BUTTON_W, BUTTON_H,
                TCScreenTextures.GUI_BASE,
                BUTTON_FACE_U, BUTTON_FACE_V,
                BUTTON_FACE_W, BUTTON_FACE_H,
                TCScreenTextures.TEX_SIZE, TCScreenTextures.TEX_SIZE,
                Component.translatable("button.thaumcraft.scrap_theory"),
                () -> {
                    playLocalSound(TCSounds.CLACK.get(), CLACK_SOUND_VOLUME, SOUND_PITCH);
                    tempCatTotals.clear();
                    sparkleQueue.clear();
                    ClientPacketDistributor.sendToServer(new ServerboundTableActionPayload(ServerboundTableActionPayload.Action.SCRAP));
                }
        );
        scrapButton.setTintColor(SCRAP_BUTTON_TINT);
        addRenderableWidget(createButton);
        addRenderableWidget(completeButton);
        addRenderableWidget(scrapButton);
        updateButtonVisibility();
    }

    private void updateButtonVisibility() {
        ResearchTableData data = sessionData();
        boolean inSession = data != null && data.inspirationStart() > 0;
        boolean complete = data != null && data.isComplete();
        createButton.visible = !inSession;
        createButton.active = !inSession && menu.hasUsableScribeTools() && menu.hasPaperReady();
        completeButton.visible = inSession && complete;
        completeButton.active = inSession && complete;
        scrapButton.visible = inSession && !complete;
        scrapButton.active = inSession && !complete;
    }

    @Override
    public void containerTick() {
        super.containerTick();
        updateButtonVisibility();
        syncCardAnimationState();
        tickAidScan();
        tickTempCategoryTotals();
    }

    private void tickTempCategoryTotals() {
        ResearchTableData data = sessionData();
        if (data == null || data.inspirationStart() <= 0) {
            tempCatTotals.clear();
            sparkleQueue.clear();
            return;
        }
        tickTempCategoryTotals(data);
    }

    private void tickAidScan() {
        ResearchTableData data = sessionData();
        boolean inSession = data != null && data.inspirationStart() > 0;
        if (inSession) {
            availableAids.clear();
            selectedAids.clear();
            aidScanTickTimer = 0;
            return;
        }
        if (aidScanTickTimer <= 0) {
            rescanAids();
            aidScanTickTimer = AID_SCAN_INTERVAL_TICKS;
        } else {
            aidScanTickTimer--;
        }
    }

    private void syncCardAnimationState() {
        syncCardAnimationState(sessionData());
    }

    private void syncCardAnimationState(ResearchTableData data) {
        if (data == null) {
            previousHandSignature = 0;
            resetCardAnimations();
            return;
        }
        List<ResearchTableData.CardChoice> choices = data.cardChoices();
        int signature = handSignature(choices);
        if (signature != previousHandSignature) {
            previousHandSignature = signature;
            resetCardAnimations();
        }
        int selectedIndex = selectedCardIndex(choices);
        if (selectedIndex >= 0) {
            for (int i = 0; i < MAX_HAND_SIZE; i++) {
                cardActive[i] = i < choices.size() && i < MAX_HAND_SIZE && choices.get(i).selected();
            }
            if (!cardSelected && !cardSelectedSoundPlayed) {
                playLocalSound(TCSounds.PAGETURN.get(), 1.0F, SOUND_PITCH);
                cardSelectedSoundPlayed = true;
            }
            cardSelected = true;
        } else if (cardSelected) {
            resetCardAnimations();
        }
    }

    private void playLocalSound(SoundEvent sound, float volume, float pitch) {
        if (minecraft == null || minecraft.player == null) return;
        minecraft.player.playSound(sound, volume, pitch);
    }

    private static int selectedCardIndex(List<ResearchTableData.CardChoice> choices) {
        int count = Math.min(choices.size(), MAX_HAND_SIZE);
        for (int i = 0; i < count; i++) {
            if (choices.get(i).selected()) return i;
        }
        return -1;
    }

    private static int handSignature(List<ResearchTableData.CardChoice> choices) {
        int count = Math.min(choices.size(), MAX_HAND_SIZE);
        int hash = count;
        for (int i = 0; i < count; i++) {
            ResearchTableData.CardChoice choice = choices.get(i);
            hash = 31 * hash + choice.key().hashCode();
            hash = 31 * hash + Long.hashCode(choice.seed());
            hash = 31 * hash + Boolean.hashCode(choice.fromAid());
        }
        return hash;
    }

    private record RenderedCard(
            int index,
            ResearchTableData.CardChoice choice,
            boolean active,
            float centerX,
            float centerY,
            float scale,
            float tilt,
            float alpha
    ) {}

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0 && minecraft != null && minecraft.player != null) {
            ResearchTableData data = sessionData();
            boolean inSession = data != null && data.inspirationStart() > 0;
            if (!inSession) {
                int aidIndex = aidIndexAt(event.x(), event.y());
                if (aidIndex >= 0) {
                    toggleAid(aidIndex);
                    return true;
                }
            } else if (!cardSelected) {
                int hitCard = hitCard(event.x(), event.y());
                if (hitCard >= 0) {
                    if (menu.hasUsableScribeTools()) {
                        ClientPacketDistributor.sendToServer(new ServerboundPlayCardPayload(hitCard));
                    }
                    return true;
                }
                if (data.cardChoices().isEmpty() && hitDeck(event.x(), event.y()) && menu.hasPaperReady()) {
                    ClientPacketDistributor.sendToServer(ServerboundDrawCardsPayload.INSTANCE);
                    return true;
                }
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    private boolean hitDeck(double mx, double my) {
        double hitOriginX = leftPos + DECK_HIT_OFFSET_X;
        double hitOriginY = topPos + DECK_HIT_OFFSET_Y;
        return mx >= hitOriginX && my >= hitOriginY
                && mx < hitOriginX + DECK_HIT_W
                && my < hitOriginY + DECK_HIT_H;
    }

    private void toggleAid(int aidIndex) {
        ResourceKey<ITheorycraftAid> key = aidAt(aidIndex);
        if (key == null) return;
        if (selectedAids.contains(key)) {
            selectedAids.remove(key);
            return;
        }
        if (selectedAids.size() + 1 < availableInspiration) {
            selectedAids.add(key);
        }
    }

    private int hitCard(double mx, double my) {
        ResearchTableData data = sessionData();
        if (data == null) return -1;
        List<ResearchTableData.CardChoice> choices = data.cardChoices();
        int count = Math.min(choices.size(), MAX_HAND_SIZE);
        for (int i = 0; i < count; i++) {
            if (cardZoomOut[i] < HOVER_ACTIVATE_THRESHOLD || cardSelected) continue;
            double cardX = leftPos + CARD_HIT_OFFSET_X + CARD_BASE_X - CARD_HALF_STRIDE * count + (double) CARD_STRIDE * i;
            double cardY = topPos + CARD_ANCHOR_Y + CARD_HIT_OFFSET_Y;
            if (mx >= cardX && mx < cardX + CardSheetRenderer.CARD_HIT_WIDTH
                    && my >= cardY && my < cardY + CardSheetRenderer.CARD_HIT_HEIGHT) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void onClose() {
        ClientPacketDistributor.sendToServer(ServerboundEndSessionPayload.INSTANCE);
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
