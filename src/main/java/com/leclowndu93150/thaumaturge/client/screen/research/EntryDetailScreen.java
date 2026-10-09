package com.leclowndu93150.thaumaturge.client.screen.research;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.capability.ResearchFlag;
import com.leclowndu93150.thaumaturge.api.research.IResearchEntry;
import com.leclowndu93150.thaumaturge.api.research.IResearchStage;
import com.leclowndu93150.thaumaturge.api.research.ResearchAddendum;
import com.leclowndu93150.thaumaturge.client.render.research.PageParser;
import com.leclowndu93150.thaumaturge.client.render.research.RecipeDisplayCache;
import com.leclowndu93150.thaumaturge.client.render.research.RecipeDisplayWidget.ItemHit;
import com.leclowndu93150.thaumaturge.client.screen.AbstractTTScreen;
import com.leclowndu93150.thaumaturge.client.screen.TTScreenTextures;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.DetailActions;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.DetailClickMap;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.DetailFrame;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.EntryDetailModel;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.HitRecorder;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.PreviewController;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.bookmark.BookmarkJitter;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.bookmark.BookmarkLayout;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.bookmark.BookmarkPainter;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.bookmark.BookmarkSlot;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.insert.AspectInsertRenderer;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.insert.ConstructInsertRenderer;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.insert.RecipeInsertRenderer;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.knowledge.KnowledgeGridRenderer;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.page.NavigationPainter;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.page.PageTextLayout;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.page.WarpIndicatorRenderer;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.requirement.RequirementRowRenderer;
import com.leclowndu93150.thaumaturge.client.screen.widget.TTPlusMinusButton;
import com.leclowndu93150.thaumaturge.network.ServerboundAdvanceStagePayload;
import com.leclowndu93150.thaumaturge.network.ServerboundClearResearchFlagsPayload;
import com.leclowndu93150.thaumaturge.network.ServerboundObtainNotePayload;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jspecify.annotations.Nullable;
import org.joml.Matrix3x2fStack;
import org.lwjgl.glfw.GLFW;

public final class EntryDetailScreen extends AbstractTTScreen {
    private static final float BOOK_ZOOM = 1.3F;
    private static final int BOOK_TEX_W = 256;
    private static final int BOOK_TEX_H = 181;
    private static final List<ResearchFlag> OPEN_CLEARED_FLAGS = List.of(ResearchFlag.RESEARCH, ResearchFlag.PAGE);
    private static final int BLOCKED_POINTER = Integer.MIN_VALUE;

    private static final Cue TURN = new Cue(0.66F, 1.0F);
    private static final Cue SELECT = new Cue(0.7F, 0.9F);
    private static final Cue DISMISS = new Cue(0.4F, 1.1F);
    private static final Cue WRITE = new Cue(0.66F, 1.0F);

    private record Cue(float volume, float pitch) {
    }

    private final EntryDetailModel model;
    private final @Nullable Screen parent;
    private final HitRecorder hits = new HitRecorder();
    private final PreviewController preview = new PreviewController();
    private final RequirementRowRenderer requirementRows = new RequirementRowRenderer(hits);
    private final BookmarkPainter bookmarkPainter = new BookmarkPainter(hits);
    private final DetailClickMap clickMap;
    private int bookLeft;
    private int bookTop;

    public record AspectHit(AspectInstance aspect, int x, int y) {
        private static final int SLOT_HIT_SIZE = 16;

        public boolean contains(double mouseX, double mouseY) {
            double dx = mouseX - x;
            double dy = mouseY - y;
            return dx >= 0 && dx < SLOT_HIT_SIZE && dy >= 0 && dy < SLOT_HIT_SIZE;
        }
    }

    public EntryDetailScreen(Holder<IResearchEntry> entry, Identifier entryId, @Nullable Screen parent) {
        super(Component.translatable(entry.value().nameKey()));
        this.parent = parent;
        this.model = new EntryDetailModel(entry, entryId);
        ThaumonomiconBrowserScreen.rememberEntry(entryId);
        this.clickMap = new DetailClickMap(model, preview, new ScreenActions());
    }

    @Override
    protected void init() {
        hits.clear();
        super.init();
        bookLeft = (width - BOOK_TEX_W) / 2;
        bookTop = (height - BOOK_TEX_H) / 2;
        model.restorePage();
        requestRecipeDisplays();
        addLayerButtons();
        rebuildPages();
        clearFlagsOnce();
    }

    private void addLayerButtons() {
        Component previousLabel = Component.translatable("gui.thaumaturge.thaumonomicon.preview.previous_layer");
        Component nextLabel = Component.translatable("gui.thaumaturge.thaumonomicon.preview.next_layer");
        TTPlusMinusButton down = addRenderableWidget(TTPlusMinusButton.minus(0, 0, previousLabel, () -> preview.shiftLayer(-1)));
        TTPlusMinusButton up = addRenderableWidget(TTPlusMinusButton.plus(0, 0, nextLabel, () -> preview.shiftLayer(1)));
        preview.bind(down, up);
    }

    private void clearFlagsOnce() {
        if (!model.claimFlagClear()) {
            return;
        }
        ServerboundClearResearchFlagsPayload payload = new ServerboundClearResearchFlagsPayload(model.entryId(), OPEN_CLEARED_FLAGS);
        ClientPacketDistributor.sendToServer(payload);
    }

    private void requestRecipeDisplays() {
        for (IResearchStage stage : model.stages()) {
            stage.recipes().forEach(RecipeDisplayCache::ensureRequested);
        }
        for (ResearchAddendum addendum : model.research().addenda()) {
            addendum.recipes().forEach(RecipeDisplayCache::ensureRequested);
        }
    }

    private @Nullable Player localPlayer() {
        return minecraft == null ? null : minecraft.player;
    }

    private DetailFrame frameFor(Player player) {
        return new DetailFrame(font, player, minecraft.level, bookLeft, bookTop, width, height);
    }

    private void rebuildPages() {
        Player player = localPlayer();
        int displayedStage = model.displayedStage(player);
        int progressStage = model.progressStage(player);
        List<ResearchAddendum> addenda = model.unlockedAddenda(player);
        model.replacePages(composePages(player, displayedStage, progressStage, addenda));
        model.markBuilt(displayedStage, progressStage, addenda.size());
    }

    private List<PageParser.Page> composePages(@Nullable Player player, int displayedStage, int progressStage, List<ResearchAddendum> addenda) {
        IResearchStage shown = model.stages().get(displayedStage);
        int knowledgeTypes = KnowledgeGridRenderer.activeTypeCount(player);
        return PageTextLayout.compose(font, model.entryId(), shown, addenda, knowledgeTypes, model.stages().size(), progressStage);
    }

    @Override
    public void tick() {
        super.tick();
        if (model.needsRebuild(localPlayer())) {
            rebuildPages();
            model.restorePage();
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        hits.clear();
        preview.beginFrame();
        drawPaneBackground(graphics);
        Player player = localPlayer();
        if (player == null) {
            return;
        }
        DetailFrame frame = frameFor(player);
        int displayedStage = model.displayedStage(player);
        IResearchStage stage = model.stages().get(displayedStage);
        boolean insertOpen = model.insertOpen();
        int pageMouseX = pointerOrBlocked(insertOpen, mouseX);
        int pageMouseY = pointerOrBlocked(insertOpen, mouseY);
        PageTextLayout.drawSpread(graphics, frame, getTitle(), model.pages(), model.leftPage());
        requirementRows.render(graphics, frame, model, stage, displayedStage, pageMouseX, pageMouseY);
        WarpIndicatorRenderer.render(graphics, frame, model, stage, pageMouseX, pageMouseY);
        if (insertOpen) {
            hits.clear();
        }
        if (EntryDetailModel.knowsResearch(player, EntryDetailModel.KNOWLEDGE_TYPES_RESEARCH) && model.isKnowledgeTypesEntry()) {
            KnowledgeGridRenderer.drawInPage(graphics, frame, pageMouseX, pageMouseY);
        }
        drawInsert(graphics, frame, stage, mouseX, mouseY);
        List<BookmarkSlot> slots = BookmarkLayout.slots(frame, model, stage);
        bookmarkPainter.paint(graphics, frame, model, stage, slots, BookmarkJitter.forEntry(model.entryId(), displayedStage), mouseX, mouseY);
        NavigationPainter.paint(graphics, frame, model, mouseX, mouseY);
    }

    private static int pointerOrBlocked(boolean blocked, int coordinate) {
        return blocked ? BLOCKED_POINTER : coordinate;
    }

    private void drawInsert(GuiGraphicsExtractor graphics, DetailFrame frame, IResearchStage stage, int mouseX, int mouseY) {
        if (model.aspectsOpen()) {
            AspectInsertRenderer.render(graphics, frame, model, mouseX, mouseY);
            return;
        }
        if (model.knowledgeOpen()) {
            KnowledgeGridRenderer.drawInsert(graphics, frame, mouseX, mouseY);
            return;
        }
        if (model.constructOpen()) {
            boolean drawn = ConstructInsertRenderer.render(graphics, frame, stage, preview, mouseX, mouseY);
            if (!drawn) {
                model.dismissConstruct();
            }
            return;
        }
        if (model.openRecipe() == null) {
            return;
        }
        RecipeInsertRenderer.render(graphics, frame, model, preview, mouseX, mouseY);
    }

    private void drawPaneBackground(GuiGraphicsExtractor graphics) {
        float shiftX = (width - BOOK_TEX_W * BOOK_ZOOM) * 0.5F;
        float shiftY = (height - BOOK_TEX_H * BOOK_ZOOM) * 0.5F;
        Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(shiftX, shiftY).scale(BOOK_ZOOM);
        blitBookTexture(graphics);
        pose.popMatrix();
    }

    private static void blitBookTexture(GuiGraphicsExtractor graphics) {
        int atlas = TTScreenTextures.TEX_SIZE;
        graphics.blit(RenderPipelines.GUI_TEXTURED, TTScreenTextures.RESEARCH_BOOK, 0, 0, 0.0F, 0.0F, BOOK_TEX_W, BOOK_TEX_H, BOOK_TEX_W, BOOK_TEX_H, atlas, atlas);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 1) {
            returnToBrowser();
            return true;
        }
        Player player = localPlayer();
        if (event.button() == 0 && player != null && clickMap.handle(frameFor(player), event.x(), event.y(), () -> super.mouseClicked(event, doubleClick))) {
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (event.button() == 0 && preview.isRotating()) {
            preview.dragRotation(dragX);
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() == 0 && preview.isRotating()) {
            preview.endRotation();
            return true;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (minecraft != null && minecraft.options.keyInventory.matches(event)) {
            onClose();
            return true;
        }
        switch (event.key()) {
            case GLFW.GLFW_KEY_LEFT, GLFW.GLFW_KEY_UP, GLFW.GLFW_KEY_PAGE_UP -> {
                if (!model.insertOpen()) {
                    turnPage(false);
                }
                return true;
            }
            case GLFW.GLFW_KEY_RIGHT, GLFW.GLFW_KEY_DOWN, GLFW.GLFW_KEY_PAGE_DOWN -> {
                if (!model.insertOpen()) {
                    turnPage(true);
                }
                return true;
            }
            case GLFW.GLFW_KEY_BACKSPACE -> {
                navigateBack();
                return true;
            }
            default -> {
                return super.keyPressed(event);
            }
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (model.insertOpen() || scrollY == 0.0) {
            return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }
        if (minecraft != null && minecraft.hasShiftDown()) {
            if (model.hasStageHistory(localPlayer())) {
                stepStageHistory(scrollY > 0.0 ? -1 : 1);
            }
            return true;
        }
        turnPage(scrollY <= 0.0);
        return true;
    }

    @Override
    public void onClose() {
        if (!closeInsert()) {
            super.onClose();
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public @Nullable ItemHit itemUnderMouse(double mouseX, double mouseY) {
        if (minecraft == null || minecraft.player == null || minecraft.level == null) {
            return null;
        }
        ItemHit recorded = hits.topItemAt(mouseX, mouseY);
        if (recorded != null) {
            return recorded;
        }
        return RecipeInsertRenderer.hoverItem(model, width, height, mouseX, mouseY);
    }

    public @Nullable AspectHit aspectUnderMouse(double mouseX, double mouseY) {
        if (minecraft == null || minecraft.player == null || minecraft.level == null || model.insertOpen()) {
            return null;
        }
        return hits.topKnownAspectAt(mouseX, mouseY);
    }

    public void openRecipeFromNavigation(Identifier recipeId) {
        if (!model.showLinkedRecipe(recipeId)) {
            return;
        }
        preview.reset();
        playCue(SELECT);
    }

    private void turnPage(boolean forward) {
        boolean turned = forward ? model.turnSpreadForward() : model.turnSpreadBack();
        if (turned) {
            playCue(TURN);
        }
    }

    private void navigateBack() {
        if (model.canStepBack()) {
            stepBackInsert();
        } else {
            returnToBrowser();
        }
    }

    private void stepBackInsert() {
        boolean construct = model.constructOpen();
        boolean trail = !construct && model.hasRecipeTrail();
        if (construct) {
            model.dismissConstruct();
        } else if (!trail) {
            model.clearOpenRecipe();
            return;
        }
        playCue(TURN);
        if (trail) {
            preview.reset();
            model.restoreTrailRecipe();
        }
    }

    private void stepStageHistory(int delta) {
        Player player = localPlayer();
        int target = model.displayedStage(player) + delta;
        if (target >= 0 && target <= model.progressStage(player)) {
            showStage(target);
        }
    }

    private void showStage(int stageIndex) {
        model.pinStage(stageIndex, model.progressStage(localPlayer()));
        rebuildPages();
        playCue(SELECT);
    }

    private boolean closeInsert() {
        boolean closed = model.closeInserts();
        if (closed) {
            playCue(DISMISS);
        }
        return closed;
    }

    private void returnToBrowser() {
        if (closeInsert()) {
            return;
        }
        ThaumonomiconBrowserScreen.rememberEntry(null);
        if (minecraft != null) {
            minecraft.setScreen(parent);
        }
    }

    private void playCue(Cue cue) {
        Player listener = localPlayer();
        if (listener == null) {
            return;
        }
        listener.playSound(TTSounds.PAGE.get(), cue.volume(), cue.pitch());
    }

    private final class ScreenActions implements DetailActions {
        @Override
        public void turnPageBack() {
            EntryDetailScreen.this.turnPage(false);
        }

        @Override
        public void turnPageForward() {
            EntryDetailScreen.this.turnPage(true);
        }

        @Override
        public void navigateBack() {
            EntryDetailScreen.this.navigateBack();
        }

        @Override
        public void showStage(int stageIndex) {
            EntryDetailScreen.this.showStage(stageIndex);
        }

        @Override
        public void toggleAspectsInsert() {
            model.toggleAspects(AspectInsertRenderer.pageCount(localPlayer()));
            playCue(SELECT);
        }

        @Override
        public void toggleKnowledgeInsert() {
            model.toggleKnowledge();
            playCue(SELECT);
        }

        @Override
        public void turnAspectsPage(int delta) {
            model.shiftAspectsPage(delta);
            playCue(SELECT);
        }

        @Override
        public void turnRecipeVariant(int delta) {
            model.setRecipeVariant(model.recipeVariant() + delta);
            preview.reset();
            playCue(SELECT);
        }

        @Override
        public void toggleRecipeInsert(Identifier recipeId) {
            preview.reset();
            model.toggleRecipe(recipeId);
            playCue(SELECT);
        }

        @Override
        public void toggleConstructInsert() {
            preview.reset();
            model.toggleConstruct();
            playCue(SELECT);
        }

        @Override
        public void submitStageCompletion() {
            Player player = localPlayer();
            Identifier id = model.entryId();
            model.beginHold(KnowledgeAccess.of(player).researchStage(id), player.level().getGameTime());
            ClientPacketDistributor.sendToServer(new ServerboundAdvanceStagePayload(id));
            player.playSound(TTSounds.WRITE.get(), WRITE.volume(), WRITE.pitch());
        }

        @Override
        public void obtainNote(int ordinal) {
            ClientPacketDistributor.sendToServer(new ServerboundObtainNotePayload(model.entryId(), ordinal));
        }
    }
}
