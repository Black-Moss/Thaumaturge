package com.leclowndu93150.thaumaturge.client.screen.research.detail;

import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeType;
import com.leclowndu93150.thaumaturge.api.research.IResearchStage;
import com.leclowndu93150.thaumaturge.api.research.KnowledgeReward;
import com.leclowndu93150.thaumaturge.client.render.research.RecipeDisplayCache;
import com.leclowndu93150.thaumaturge.client.render.research.RecipeDisplayWidget;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.bookmark.BookmarkKind;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.bookmark.BookmarkLayout;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.bookmark.BookmarkSlot;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.draw.BookSprites;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.insert.AspectInsertRenderer;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.page.NavigationLayout;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.requirement.RequirementLayout;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.requirement.StageRequirementCheck;
import com.leclowndu93150.thaumaturge.content.research.note.ResearchNoteData;
import com.leclowndu93150.thaumaturge.content.research.note.ResearchNotes;
import com.leclowndu93150.thaumaturge.network.ServerboundRequestItemRecipePayload;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public final class DetailClickMap {
    private final EntryDetailModel model;
    private final PreviewController preview;
    private final DetailActions actions;

    public DetailClickMap(EntryDetailModel model, PreviewController preview, DetailActions actions) {
        this.model = model;
        this.preview = preview;
        this.actions = actions;
    }

    public boolean handle(DetailFrame frame, double mouseX, double mouseY, BooleanSupplier widgetFallback) {
        for (ClickRegion region : regions(frame, mouseX, mouseY, widgetFallback)) {
            if (region.area().contains(mouseX, mouseY) && region.handler().getAsBoolean()) {
                return true;
            }
        }
        return false;
    }

    private List<ClickRegion> regions(DetailFrame frame, double mouseX, double mouseY, BooleanSupplier widgetFallback) {
        Player player = frame.player();
        IResearchStage stage = model.displayedStageData(player);
        List<BookmarkSlot> slots = BookmarkLayout.slots(frame, model, stage);
        List<ClickRegion> regions = new ArrayList<>();
        addPageTurns(regions, frame);
        addStageHistory(regions, frame);
        for (BookmarkSlot slot : BookmarkLayout.ofKind(slots, BookmarkKind.ASPECTS)) {
            regions.add(ClickRegion.consuming(slot.clickArea(), actions::toggleAspectsInsert));
        }
        for (BookmarkSlot slot : BookmarkLayout.ofKind(slots, BookmarkKind.KNOWLEDGE)) {
            regions.add(ClickRegion.consuming(slot.clickArea(), actions::toggleKnowledgeInsert));
        }
        addAspectPager(regions, frame);
        addPreviewControls(regions, widgetFallback);
        addRecipePage(regions, frame, mouseX, mouseY);
        for (BookmarkSlot slot : BookmarkLayout.ofKind(slots, BookmarkKind.RECIPE)) {
            regions.add(ClickRegion.consuming(slot.clickArea(), () -> actions.toggleRecipeInsert(slot.recipeId())));
        }
        for (BookmarkSlot slot : BookmarkLayout.ofKind(slots, BookmarkKind.CONSTRUCT)) {
            regions.add(ClickRegion.consuming(slot.clickArea(), actions::toggleConstructInsert));
        }
        addStageControls(regions, frame, stage);
        return regions;
    }

    private void addPageTurns(List<ClickRegion> regions, DetailFrame frame) {
        boolean insertOpen = model.insertOpen();
        if (!insertOpen && model.leftPage() > 0) {
            regions.add(ClickRegion.consuming(NavigationLayout.pageLeftHit(frame), actions::turnPageBack));
        }
        if (!insertOpen && model.hasNextSpread()) {
            regions.add(ClickRegion.consuming(NavigationLayout.pageRightHit(frame), actions::turnPageForward));
        }
        regions.add(ClickRegion.consuming(NavigationLayout.backButton(frame), actions::navigateBack));
    }

    private void addStageHistory(List<ClickRegion> regions, DetailFrame frame) {
        Player player = frame.player();
        if (!model.canNavigateStageHistory(player)) {
            return;
        }
        int displayedStage = model.displayedStage(player);
        int progressStage = model.progressStage(player);
        if (displayedStage > 0) {
            regions.add(ClickRegion.consuming(NavigationLayout.stageLeftHit(frame), () -> actions.showStage(displayedStage - 1)));
        }
        if (displayedStage < progressStage) {
            regions.add(ClickRegion.consuming(NavigationLayout.stageRightHit(frame), () -> actions.showStage(displayedStage + 1)));
        }
    }

    private void addAspectPager(List<ClickRegion> regions, DetailFrame frame) {
        if (!model.aspectsOpen()) {
            return;
        }
        int pageCount = AspectInsertRenderer.pageCount(frame.player());
        if (model.aspectsPageIndex() > 0) {
            regions.add(ClickRegion.consuming(NavigationLayout.pagerLeftHit(frame), () -> actions.turnAspectsPage(-1)));
        }
        if (model.aspectsPageIndex() < pageCount - 1) {
            regions.add(ClickRegion.consuming(NavigationLayout.pagerRightHit(frame), () -> actions.turnAspectsPage(1)));
        }
    }

    private void addPreviewControls(List<ClickRegion> regions, BooleanSupplier widgetFallback) {
        for (Rect buttonArea : preview.layerButtonAreas()) {
            regions.add(ClickRegion.consuming(buttonArea, widgetFallback::getAsBoolean));
        }
        Rect rotationArea = preview.rotationArea();
        if (rotationArea != null) {
            regions.add(ClickRegion.consuming(rotationArea, preview::beginRotation));
        }
    }

    private void addRecipePage(List<ClickRegion> regions, DetailFrame frame, double mouseX, double mouseY) {
        Identifier recipeId = model.openRecipe();
        if (recipeId == null) {
            return;
        }
        regions.add(ClickRegion.declining(new Rect(0, 0, frame.screenWidth(), frame.screenHeight()), () -> requestIngredientRecipe(frame, recipeId, mouseX, mouseY)));
        List<RecipeDisplay> variants = RecipeDisplayCache.get(recipeId);
        int lastVariant = variants == null ? 0 : variants.size() - 1;
        if (model.recipeVariant() > 0) {
            regions.add(ClickRegion.consuming(NavigationLayout.pagerLeftHit(frame), () -> actions.turnRecipeVariant(-1)));
        }
        if (model.recipeVariant() < lastVariant) {
            regions.add(ClickRegion.consuming(NavigationLayout.pagerRightHit(frame), () -> actions.turnRecipeVariant(1)));
        }
    }

    private void addStageControls(List<ClickRegion> regions, DetailFrame frame, IResearchStage stage) {
        Player player = frame.player();
        boolean stageOpen = model.leftPage() == 0 && !model.completedStageView(player) && !model.insertOpen();
        if (!stageOpen) {
            return;
        }
        RequirementLayout layout = RequirementLayout.of(stage, frame.top());
        addTheoryNotes(regions, frame, stage, layout);
        if (!model.isHolding() && layout.any()) {
            int displayedStage = model.displayedStage(player);
            regions.add(ClickRegion.declining(layout.completeButton(frame.left()), () -> submitIfSatisfied(player, stage, displayedStage)));
        }
    }

    private void addTheoryNotes(List<ClickRegion> regions, DetailFrame frame, IResearchStage stage, RequirementLayout layout) {
        List<KnowledgeReward> rewards = stage.requiredKnowledge();
        if (rewards.isEmpty()) {
            return;
        }
        int spacing = RequirementLayout.spacingFor(rewards.size());
        int observationChips = ResearchNotes.stageObservationCost(model.research(), stage).entries().size();
        int[] slotXs = RequirementLayout.knowledgeSlotXs(frame.left(), rewards, spacing, observationChips);
        int ordinal = ResearchNotes.theoryRowsBefore(model.research(), model.displayedStage(frame.player()));
        for (int index = 0; index < rewards.size(); index++) {
            if (rewards.get(index).type() != KnowledgeType.THEORY) {
                continue;
            }
            int noteOrdinal = ordinal++;
            Rect slotArea = new Rect(slotXs[index], layout.knowledgeY(), BookSprites.SLOT_SIZE, BookSprites.SLOT_SIZE);
            regions.add(ClickRegion.declining(slotArea, () -> requestNote(frame.player(), noteOrdinal)));
        }
    }

    private boolean requestNote(Player player, int ordinal) {
        Identifier learnKey = ResearchNoteData.learnKey(model.entryId(), ordinal);
        if (KnowledgeAccess.of(player).isResearchKnown(learnKey) || ResearchNotes.hasNoteFor(player, learnKey)) {
            return false;
        }
        actions.obtainNote(ordinal);
        return true;
    }

    private boolean submitIfSatisfied(Player player, IResearchStage stage, int displayedStage) {
        if (!StageRequirementCheck.allSatisfied(player, model, stage, displayedStage)) {
            return false;
        }
        actions.submitStageCompletion();
        return true;
    }

    private boolean requestIngredientRecipe(DetailFrame frame, Identifier recipeId, double mouseX, double mouseY) {
        List<RecipeDisplay> variants = RecipeDisplayCache.get(recipeId);
        if (variants == null || variants.isEmpty()) {
            return false;
        }
        RecipeDisplay current = variants.get(Math.min(model.recipeVariant(), variants.size() - 1));
        int centerX = frame.screenWidth() / 2;
        int centerY = frame.screenHeight() / 2;
        ItemStack hovered = RecipeDisplayWidget.hoverStackForDisplay(centerX - RecipeDisplayWidget.width() / 2, centerY - RecipeDisplayWidget.height() / 2, current, frame.gameTime(), (int) mouseX,
                (int) mouseY);
        if (hovered == null || hovered.isEmpty()) {
            return false;
        }
        Identifier itemId = hovered.getItem().builtInRegistryHolder().unwrapKey().map(ResourceKey::identifier).orElse(null);
        if (itemId == null) {
            return false;
        }
        ClientPacketDistributor.sendToServer(new ServerboundRequestItemRecipePayload(itemId));
        return true;
    }
}
