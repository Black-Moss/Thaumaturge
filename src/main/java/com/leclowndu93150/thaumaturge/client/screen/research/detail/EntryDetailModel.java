package com.leclowndu93150.thaumaturge.client.screen.research.detail;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.capability.IPlayerKnowledge;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.research.IResearchEntry;
import com.leclowndu93150.thaumaturge.api.research.IResearchStage;
import com.leclowndu93150.thaumaturge.api.research.ResearchAddendum;
import com.leclowndu93150.thaumaturge.client.render.research.PageParser;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

public final class EntryDetailModel {
    public static final Identifier FIRST_STEPS_RESEARCH = TTIds.rl("first_steps");
    public static final Identifier KNOWLEDGE_TYPES_RESEARCH = TTIds.rl("knowledge_types");

    private static final long HOLD_TIMEOUT_TICKS = 60L;
    private static final int NONE = -1;

    private final Holder<IResearchEntry> entry;
    private final Identifier entryId;
    private final Deque<Identifier> recipeTrail = new ArrayDeque<>();
    private List<PageParser.Page> pages = List.of();
    private int leftPage;
    private boolean aspectsOpen;
    private boolean knowledgeOpen;
    private boolean constructOpen;
    private @Nullable Identifier openRecipe;
    private int recipeVariant;
    private int aspectsPageIndex;
    private int pinnedStage = NONE;
    private boolean complete;
    private boolean holding;
    private int stageWhenHeld;
    private long heldSinceTick;
    private int lastAutoAdvanceStage = NONE;
    private boolean flagsCleared;
    private int builtDisplayedStage = NONE;
    private int builtProgressStage = NONE;
    private boolean builtComplete;
    private int builtAddendaCount = NONE;

    public EntryDetailModel(Holder<IResearchEntry> entry, Identifier entryId) {
        this.entry = entry;
        this.entryId = entryId;
    }

    public static boolean knowsResearch(@Nullable Player player, Identifier research) {
        return player != null && KnowledgeAccess.of(player).isResearchComplete(research);
    }

    public static boolean hasRequirements(IResearchStage stage) {
        return !stage.requiredResearch().isEmpty() || !stage.obtain().isEmpty() || !stage.craft().isEmpty() || !stage.requiredKnowledge().isEmpty();
    }

    public static boolean hasRedundantFinalStage(List<IResearchStage> stages) {
        if (stages.size() < 2) {
            return false;
        }
        IResearchStage previous = stages.get(stages.size() - 2);
        IResearchStage last = stages.getLast();
        return hasRequirements(previous) && !hasRequirements(last) && previous.textKey().equals(last.textKey()) && last.recipes().containsAll(previous.recipes())
                && previous.construct().equals(last.construct()) && previous.knowledge().equals(last.knowledge()) && previous.warp() == last.warp();
    }

    public Holder<IResearchEntry> entry() {
        return entry;
    }

    public IResearchEntry research() {
        return entry.value();
    }

    public Identifier entryId() {
        return entryId;
    }

    public List<IResearchStage> stages() {
        return entry.value().stages();
    }

    public boolean isKnowledgeTypesEntry() {
        return entryId.equals(KNOWLEDGE_TYPES_RESEARCH);
    }

    public boolean isComplete() {
        return complete;
    }

    public int progressStage(@Nullable Player player) {
        if (player == null) {
            return 0;
        }
        IPlayerKnowledge knowledge = KnowledgeAccess.of(player);
        complete = knowledge.isResearchComplete(entryId);
        int stageCount = stages().size();
        int storedStage = knowledge.researchStage(entryId);
        int resolved = Math.max(storedStage, 0);
        if (complete || resolved >= stageCount) {
            resolved = stageCount - 1;
        }
        boolean holdExpired = complete || storedStage > stageWhenHeld || player.level().getGameTime() - heldSinceTick > HOLD_TIMEOUT_TICKS;
        if (holding && holdExpired) {
            holding = false;
        }
        return Math.min(Math.max(0, resolved), stageCount - 1);
    }

    public int displayedStage(@Nullable Player player) {
        int progress = progressStage(player);
        if (pinnedStage >= 0) {
            return Math.min(pinnedStage, progress);
        }
        if (progress == stages().size() - 1 && hasRedundantFinalStage(stages())) {
            return progress - 1;
        }
        return progress;
    }

    public IResearchStage displayedStageData(@Nullable Player player) {
        return stages().get(displayedStage(player));
    }

    public boolean completedStageView(@Nullable Player player) {
        int progress = progressStage(player);
        return complete || displayedStage(player) < progress;
    }

    public boolean hasStageHistory(@Nullable Player player) {
        return stages().size() > 1 && progressStage(player) > 0 && !hasRedundantFinalStage(stages()) && !insertOpen() && recipeTrail.isEmpty();
    }

    public boolean canNavigateStageHistory(@Nullable Player player) {
        return leftPage == 0 && hasStageHistory(player);
    }

    public void pinStage(int requestedStage, int progress) {
        int clamped = Mth.clamp(requestedStage, 0, progress);
        pinnedStage = clamped == progress ? NONE : clamped;
        leftPage = 0;
    }

    public List<ResearchAddendum> unlockedAddenda(@Nullable Player player) {
        if (!complete || player == null) {
            return List.of();
        }
        IPlayerKnowledge knowledge = KnowledgeAccess.of(player);
        return research().addenda().stream().filter(addendum -> addendum.requiredResearch().stream().allMatch(knowledge::isResearchComplete)).toList();
    }

    public List<Identifier> displayRecipes(IResearchStage stage, @Nullable Player player) {
        List<Identifier> recipes = new ArrayList<>(stage.recipes());
        List<IResearchStage> stages = stages();
        int finalIndex = stages.size() - 1;
        if (hasRedundantFinalStage(stages) && progressStage(player) == finalIndex && stage == stages.get(finalIndex - 1)) {
            appendMissing(recipes, stages.get(finalIndex).recipes());
        }
        for (ResearchAddendum addendum : unlockedAddenda(player)) {
            appendMissing(recipes, addendum.recipes());
        }
        return recipes;
    }

    private static void appendMissing(List<Identifier> target, List<Identifier> additions) {
        for (Identifier recipe : additions) {
            if (!target.contains(recipe)) {
                target.add(recipe);
            }
        }
    }

    public List<PageParser.Page> pages() {
        return pages;
    }

    public void replacePages(List<PageParser.Page> rebuilt) {
        pages = rebuilt;
    }

    public int leftPage() {
        return leftPage;
    }

    public void restorePage() {
        leftPage = clampPage(leftPage);
    }

    public int clampPage(int page) {
        int last = Math.max(0, pages.size() - 1);
        return Math.min(Math.max(0, page), last);
    }

    public boolean hasNextSpread() {
        return leftPage < pages.size() - 2;
    }

    public boolean turnSpreadForward() {
        if (!hasNextSpread()) {
            return false;
        }
        leftPage += 2;
        return true;
    }

    public boolean turnSpreadBack() {
        if (leftPage < 2) {
            return false;
        }
        leftPage -= 2;
        return true;
    }

    public boolean needsRebuild(@Nullable Player player) {
        int progress = progressStage(player);
        return displayedStage(player) != builtDisplayedStage || progress != builtProgressStage || complete != builtComplete || unlockedAddenda(player).size() != builtAddendaCount;
    }

    public void markBuilt(int displayedStage, int progressStage, int addendaCount) {
        builtDisplayedStage = displayedStage;
        builtProgressStage = progressStage;
        builtComplete = complete;
        builtAddendaCount = addendaCount;
    }

    public boolean insertOpen() {
        return aspectsOpen || knowledgeOpen || constructOpen || openRecipe != null;
    }

    public boolean aspectsOpen() {
        return aspectsOpen;
    }

    public boolean knowledgeOpen() {
        return knowledgeOpen;
    }

    public boolean constructOpen() {
        return constructOpen;
    }

    public @Nullable Identifier openRecipe() {
        return openRecipe;
    }

    public boolean onlyRecipeOpen() {
        return !aspectsOpen && !knowledgeOpen && !constructOpen && openRecipe != null;
    }

    public int recipeVariant() {
        return recipeVariant;
    }

    public void setRecipeVariant(int variant) {
        recipeVariant = variant;
    }

    public int aspectsPageIndex() {
        return aspectsPageIndex;
    }

    public void shiftAspectsPage(int delta) {
        aspectsPageIndex += delta;
    }

    public void toggleAspects(int pageCount) {
        openRecipe = null;
        knowledgeOpen = false;
        constructOpen = false;
        aspectsOpen = !aspectsOpen;
        recipeTrail.clear();
        if (aspectsPageIndex > pageCount) {
            aspectsPageIndex = 0;
        }
    }

    public void toggleKnowledge() {
        openRecipe = null;
        aspectsOpen = false;
        constructOpen = false;
        knowledgeOpen = !knowledgeOpen;
        recipeTrail.clear();
    }

    public void toggleRecipe(Identifier recipeId) {
        openRecipe = recipeId.equals(openRecipe) ? null : recipeId;
        aspectsOpen = false;
        knowledgeOpen = false;
        constructOpen = false;
        recipeTrail.clear();
    }

    public void toggleConstruct() {
        constructOpen = !constructOpen;
        openRecipe = null;
        aspectsOpen = false;
        knowledgeOpen = false;
        recipeTrail.clear();
    }

    public void dismissConstruct() {
        constructOpen = false;
    }

    public boolean showLinkedRecipe(Identifier recipeId) {
        if (recipeId.equals(openRecipe)) {
            return false;
        }
        if (openRecipe != null) {
            recipeTrail.push(openRecipe);
        }
        openRecipe = recipeId;
        recipeVariant = 0;
        aspectsOpen = false;
        knowledgeOpen = false;
        return true;
    }

    public boolean closeInserts() {
        if (!insertOpen()) {
            return false;
        }
        openRecipe = null;
        aspectsOpen = false;
        knowledgeOpen = false;
        constructOpen = false;
        recipeTrail.clear();
        return true;
    }

    public boolean canStepBack() {
        return constructOpen || !recipeTrail.isEmpty();
    }

    public boolean hasRecipeTrail() {
        return !recipeTrail.isEmpty();
    }

    public void restoreTrailRecipe() {
        openRecipe = recipeTrail.pop();
    }

    public void clearOpenRecipe() {
        openRecipe = null;
    }

    public boolean isHolding() {
        return holding;
    }

    public void beginHold(int storedStage, long gameTime) {
        stageWhenHeld = storedStage;
        heldSinceTick = gameTime;
        holding = true;
    }

    public boolean claimAutoAdvance(int storedStage) {
        if (lastAutoAdvanceStage == storedStage) {
            return false;
        }
        lastAutoAdvanceStage = storedStage;
        return true;
    }

    public boolean claimFlagClear() {
        if (flagsCleared) {
            return false;
        }
        flagsCleared = true;
        return true;
    }
}
