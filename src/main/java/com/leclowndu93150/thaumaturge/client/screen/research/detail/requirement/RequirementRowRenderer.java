package com.leclowndu93150.thaumaturge.client.screen.research.detail.requirement;

import com.leclowndu93150.thaumaturge.api.aspect.AspectComponents;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.capability.IPlayerKnowledge;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeType;
import com.leclowndu93150.thaumaturge.api.research.IResearchEntry;
import com.leclowndu93150.thaumaturge.api.research.IResearchStage;
import com.leclowndu93150.thaumaturge.api.research.KnowledgeReward;
import com.leclowndu93150.thaumaturge.api.research.ResearchRequirement;
import com.leclowndu93150.thaumaturge.api.research.scan.ScanKeys;
import com.leclowndu93150.thaumaturge.client.render.aspect.AspectTagRenderer;
import com.leclowndu93150.thaumaturge.client.render.research.EntryIconRenderer;
import com.leclowndu93150.thaumaturge.client.screen.TTScreenTextures;
import com.leclowndu93150.thaumaturge.client.screen.TTTooltips;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.DetailFrame;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.EntryDetailModel;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.HitRecorder;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.Rect;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.draw.AspectTileDrawer;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.draw.BookBlit;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.draw.BookSprites;
import com.leclowndu93150.thaumaturge.content.research.ResearchManager;
import com.leclowndu93150.thaumaturge.content.research.note.ResearchNoteData;
import com.leclowndu93150.thaumaturge.content.research.note.ResearchNotes;
import com.leclowndu93150.thaumaturge.content.research.pool.AspectPools;
import com.leclowndu93150.thaumaturge.network.ServerboundAdvanceStagePayload;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jspecify.annotations.Nullable;

public final class RequirementRowRenderer {
    private static final int LABEL_U = 200;
    private static final int LABEL_WIDTH = 56;
    private static final int LABEL_HEIGHT = 16;
    private static final int LABEL_OFFSET_X = -12;
    private static final int LABEL_DRAW_Y_SHIFT = -1;
    private static final int LABEL_HOVER_DIVISOR = 4;
    private static final int LABEL_TINT = 0x40FFFFFF;

    private static final int PREREQ_ICON_SIZE = 16;
    private static final int PREREQ_TEXTURE_SIZE = 32;
    private static final int PREREQ_UNKNOWN_TINT = 0xFF80BFFF;
    private static final String PREREQ_MAP_PREFIX = "m_";
    private static final String PREREQ_CHEST_PREFIX = "c_";
    private static final String PREREQ_FLASK_PREFIX = "f_";

    private static final int OBSERVATION_UNKNOWN_TINT = 0x80808080;
    private static final long PULSE_PERIOD_MILLIS = 600L;
    private static final float PULSE_AMPLITUDE = 0.25F;
    private static final float PULSE_FLOOR = 0.75F;
    private static final long ROTATION_MILLIS = 1000L;

    private static final int COMPLETE_DIVIDER_INSET = 4;
    private static final int COMPLETE_DIVIDER_Y_OFFSET = -2;
    private static final int COMPLETE_BUTTON_U = 84;
    private static final int COMPLETE_BUTTON_V = 216;
    private static final int COMPLETE_BUTTON_TINT_IDLE = 0xFFCCCCE6;
    private static final int COMPLETE_CAPTION_CENTER_X = 52;
    private static final int COMPLETE_CAPTION_Y_OFFSET = -4;

    private final HitRecorder hits;

    public RequirementRowRenderer(HitRecorder hits) {
        this.hits = hits;
    }

    public void render(GuiGraphicsExtractor graphics, DetailFrame frame, EntryDetailModel model, IResearchStage stage, int displayedStage, int mouseX, int mouseY) {
        if (model.leftPage() > 0) {
            return;
        }
        Player player = frame.player();
        boolean completedStage = model.completedStageView(player);
        IPlayerKnowledge knowledge = KnowledgeAccess.of(player);
        RequirementLayout layout = RequirementLayout.of(stage, frame.top());
        boolean allMet = true;
        if (layout.hasResearch()) {
            drawRowLabel(graphics, frame, layout.researchY(), RequirementRowKind.RESEARCH, mouseX, mouseY);
            allMet &= drawResearchRow(graphics, frame, stage.requiredResearch(), knowledge, layout.researchY(), completedStage, mouseX, mouseY);
        }
        if (layout.hasObtain()) {
            drawRowLabel(graphics, frame, layout.obtainY(), RequirementRowKind.OBTAIN, mouseX, mouseY);
            allMet &= drawItemRow(graphics, frame, stage.obtain(), layout.obtainY(), true, completedStage, mouseX, mouseY);
        }
        if (layout.hasCraft()) {
            drawRowLabel(graphics, frame, layout.craftY(), RequirementRowKind.CRAFT, mouseX, mouseY);
            allMet &= drawItemRow(graphics, frame, stage.craft(), layout.craftY(), false, completedStage, mouseX, mouseY);
        }
        if (layout.hasKnowledge()) {
            drawRowLabel(graphics, frame, layout.knowledgeY(), RequirementRowKind.KNOWLEDGE, mouseX, mouseY);
            allMet &= drawKnowledgeRow(graphics, frame, model, stage, displayedStage, layout.knowledgeY(), knowledge, completedStage, mouseX, mouseY);
        }
        if (layout.any()) {
            drawCompletionBar(graphics, frame, model, layout, allMet, completedStage, mouseX, mouseY);
        } else if (!completedStage) {
            int storedStage = knowledge.researchStage(model.entryId());
            if (model.claimAutoAdvance(storedStage)) {
                ClientPacketDistributor.sendToServer(new ServerboundAdvanceStagePayload(model.entryId()));
            }
        }
    }

    private void drawCompletionBar(GuiGraphicsExtractor graphics, DetailFrame frame, EntryDetailModel model, RequirementLayout layout, boolean allMet, boolean completedStage, int mouseX, int mouseY) {
        BookBlit.sprite(graphics, frame.left() + COMPLETE_DIVIDER_INSET, layout.dividerY() + COMPLETE_DIVIDER_Y_OFFSET, BookSprites.DIVIDER_U, BookSprites.DIVIDER_V, BookSprites.DIVIDER_WIDTH,
                BookSprites.DIVIDER_TALL_HEIGHT);
        if (!allMet) {
            return;
        }
        if (completedStage) {
            drawCaption(graphics, frame, layout, "gui.thaumaturge.thaumonomicon.stage_completed");
        } else if (model.isHolding()) {
            drawCaption(graphics, frame, layout, "gui.thaumaturge.thaumonomicon.stage_hold");
        } else {
            Rect button = layout.completeButton(frame.left());
            int tint = button.contains(mouseX, mouseY) ? BookSprites.WHITE : COMPLETE_BUTTON_TINT_IDLE;
            BookBlit.tintedSprite(graphics, button.x(), button.y(), COMPLETE_BUTTON_U, COMPLETE_BUTTON_V, button.width(), button.height(), tint);
            drawCaption(graphics, frame, layout, "gui.thaumaturge.thaumonomicon.stage_complete");
        }
    }

    private void drawCaption(GuiGraphicsExtractor graphics, DetailFrame frame, RequirementLayout layout, String translationKey) {
        Component caption = Component.translatable(translationKey);
        graphics.text(frame.font(), caption, frame.left() + COMPLETE_CAPTION_CENTER_X - frame.font().width(caption) / 2, layout.dividerY() + COMPLETE_CAPTION_Y_OFFSET, BookSprites.WHITE, true);
    }

    private void drawRowLabel(GuiGraphicsExtractor graphics, DetailFrame frame, int rowY, RequirementRowKind kind, int mouseX, int mouseY) {
        int labelX = frame.left() + LABEL_OFFSET_X;
        BookBlit.tintedSprite(graphics, labelX, rowY + LABEL_DRAW_Y_SHIFT, LABEL_U, kind.labelV(), LABEL_WIDTH, LABEL_HEIGHT, LABEL_TINT);
        if (Rect.inside(labelX, rowY, LABEL_WIDTH / LABEL_HOVER_DIVISOR, LABEL_HEIGHT, mouseX, mouseY)) {
            graphics.setTooltipForNextFrame(Component.translatable(kind.tooltipKey()), mouseX, mouseY);
        }
    }

    private boolean drawResearchRow(GuiGraphicsExtractor graphics, DetailFrame frame, List<Identifier> prerequisites, IPlayerKnowledge knowledge, int rowY, boolean completedStage, int mouseX, int mouseY) {
        int spacing = RequirementLayout.spacingFor(prerequisites.size());
        boolean allMet = true;
        for (int index = 0; index < prerequisites.size(); index++) {
            Identifier prerequisite = prerequisites.get(index);
            int slotX = RequirementLayout.slotX(frame.left(), index, spacing);
            drawPrerequisiteIcon(graphics, frame, slotX, rowY, prerequisite);
            boolean met = completedStage || knowledge.isResearchComplete(prerequisite);
            allMet &= met;
            if (met) {
                BookBlit.checkmark(graphics, slotX, rowY);
            }
            if (Rect.inside(slotX, rowY, BookSprites.SLOT_SIZE, BookSprites.SLOT_SIZE, mouseX, mouseY)) {
                graphics.setTooltipForNextFrame(frame.font(), TTTooltips.prereqEntryName(prerequisite), mouseX, mouseY);
            }
        }
        return allMet;
    }

    private void drawPrerequisiteIcon(GuiGraphicsExtractor graphics, DetailFrame frame, int x, int y, Identifier prerequisite) {
        HolderLookup.Provider registries = frame.player().registryAccess();
        Optional<Holder.Reference<IResearchEntry>> linkedEntry = registries.lookup(IResearchEntry.REGISTRY_KEY)
                .flatMap(lookup -> lookup.get(ResourceKey.create(IResearchEntry.REGISTRY_KEY, prerequisite)));
        if (linkedEntry.isPresent()) {
            EntryIconRenderer.drawResearchIcon(graphics, x, y, EntryIconRenderer.resolveIcon(linkedEntry.get().value(), frame.tick()), false);
            return;
        }
        Identifier flagIcon = flagIconFor(prerequisite.getPath());
        if (flagIcon != null) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, flagIcon, x, y, 0.0F, 0.0F, PREREQ_ICON_SIZE, PREREQ_ICON_SIZE, PREREQ_TEXTURE_SIZE, PREREQ_TEXTURE_SIZE, PREREQ_TEXTURE_SIZE,
                    PREREQ_TEXTURE_SIZE);
            return;
        }
        Optional<Holder.Reference<IAspect>> scannedAspect = registries.lookupOrThrow(IAspect.REGISTRY_KEY).listElements().filter(holder -> ScanKeys.aspect(holder.key()).equals(prerequisite))
                .findFirst();
        if (scannedAspect.isPresent()) {
            hits.recordAspect(new AspectInstance(scannedAspect.get(), 1), x, y);
            AspectTileDrawer.known(graphics, x, y, scannedAspect.get());
            return;
        }
        BookBlit.iconTile(graphics, AspectTileDrawer.UNKNOWN_TILE, x, y, PREREQ_UNKNOWN_TINT);
    }

    private static @Nullable Identifier flagIconFor(String path) {
        if (path.startsWith(PREREQ_MAP_PREFIX)) {
            return TTScreenTextures.RESEARCH_PREREQ_MAP;
        }
        if (path.startsWith(PREREQ_CHEST_PREFIX)) {
            return TTScreenTextures.RESEARCH_PREREQ_CHEST;
        }
        if (path.startsWith(PREREQ_FLASK_PREFIX)) {
            return TTScreenTextures.RESEARCH_PREREQ_FLASK;
        }
        return null;
    }

    private boolean drawItemRow(GuiGraphicsExtractor graphics, DetailFrame frame, List<ResearchRequirement> requirements, int rowY, boolean obtainRow, boolean completedStage, int mouseX, int mouseY) {
        Player player = frame.player();
        Font font = frame.font();
        int spacing = RequirementLayout.spacingFor(requirements.size());
        boolean allMet = true;
        for (int index = 0; index < requirements.size(); index++) {
            ResearchRequirement requirement = requirements.get(index);
            int slotX = RequirementLayout.slotX(frame.left(), index, spacing);
            ItemStack shown = rotatingStack(requirement, index);
            if (!shown.isEmpty()) {
                hits.recordItem(shown, slotX, rowY);
                graphics.item(shown, slotX, rowY);
                graphics.itemDecorations(font, shown, slotX, rowY);
            }
            boolean met = completedStage || (obtainRow
                    ? StageRequirementCheck.countMatching(player, requirement) >= requirement.amount()
                    : ResearchManager.isCraftSatisfied(player, KnowledgeAccess.of(player), requirement));
            allMet &= met;
            if (met) {
                BookBlit.checkmark(graphics, slotX, rowY);
            }
            if (Rect.inside(slotX, rowY, BookSprites.SLOT_SIZE, BookSprites.SLOT_SIZE, mouseX, mouseY)) {
                if (shown.isEmpty()) {
                    graphics.setTooltipForNextFrame(font, TTTooltips.need(obtainRow ? "obtain" : "craft"), mouseX, mouseY);
                } else {
                    graphics.setTooltipForNextFrame(font, shown, mouseX, mouseY);
                }
            }
        }
        return allMet;
    }

    private static ItemStack rotatingStack(ResearchRequirement requirement, int slotIndex) {
        int candidates = requirement.items().size();
        if (candidates == 0) {
            return ItemStack.EMPTY;
        }
        long rotationStep = slotIndex + System.currentTimeMillis() / ROTATION_MILLIS;
        int pick = (int) Math.floorMod(rotationStep, (long) candidates);
        return new ItemStack(requirement.items().get(pick), Math.max(1, requirement.amount()), requirement.components());
    }

    private boolean drawKnowledgeRow(GuiGraphicsExtractor graphics, DetailFrame frame, EntryDetailModel model, IResearchStage stage, int displayedStage, int rowY, IPlayerKnowledge knowledge, boolean completedStage, int mouseX, int mouseY) {
        Player player = frame.player();
        List<KnowledgeReward> rewards = stage.requiredKnowledge();
        AspectList observationCost = ResearchNotes.stageObservationCost(model.research(), stage);
        int spacing = RequirementLayout.spacingFor(rewards.size());
        int[] slotXs = RequirementLayout.knowledgeSlotXs(frame.left(), rewards, spacing, observationCost.entries().size());
        int theoryOrdinal = ResearchNotes.theoryRowsBefore(model.research(), displayedStage);
        boolean observationAffordable = completedStage || observationCost.isEmpty() || AspectPools.canAfford(player, observationCost);
        boolean observationDrawn = false;
        boolean allMet = true;
        for (int index = 0; index < rewards.size(); index++) {
            KnowledgeReward reward = rewards.get(index);
            if (reward.type() == KnowledgeType.THEORY) {
                Identifier learnKey = ResearchNoteData.learnKey(model.entryId(), theoryOrdinal++);
                allMet &= drawTheoryNote(graphics, frame, model, reward, learnKey, slotXs[index], rowY, knowledge, completedStage, mouseX, mouseY);
            } else {
                allMet &= observationAffordable;
                if (!observationDrawn) {
                    observationDrawn = true;
                    drawObservationChips(graphics, frame, observationCost, slotXs[index], rowY, spacing, completedStage, mouseX, mouseY);
                }
            }
        }
        return allMet;
    }

    private boolean drawTheoryNote(GuiGraphicsExtractor graphics, DetailFrame frame, EntryDetailModel model, KnowledgeReward reward, Identifier learnKey, int slotX, int rowY, IPlayerKnowledge knowledge, boolean completedStage, int mouseX, int mouseY) {
        Font font = frame.font();
        boolean met = completedStage || knowledge.isResearchKnown(learnKey);
        ItemStack note = new ItemStack(TTItems.RESEARCH_NOTE.get());
        hits.recordItem(note, slotX, rowY);
        graphics.item(note, slotX, rowY);
        boolean hovered = Rect.inside(slotX, rowY, BookSprites.SLOT_SIZE, BookSprites.SLOT_SIZE, mouseX, mouseY);
        if (hovered) {
            List<Component> lines = new ArrayList<>();
            lines.add(Component.translatable("tooltip.thaumaturge.research_note.theory", Component.translatable(model.research().nameKey())));
            if (!met) {
                String hintKey = ResearchNotes.hasNoteFor(frame.player(), learnKey) ? "tooltip.thaumaturge.research_note.table" : "gui.thaumaturge.thaumonomicon.note_click";
                lines.add(Component.translatable(hintKey).withStyle(ChatFormatting.GRAY));
            }
            graphics.setTooltipForNextFrame(font, lines, Optional.empty(), mouseX, mouseY);
        }
        if (met) {
            BookBlit.checkmark(graphics, slotX, rowY);
        }
        if (hovered) {
            reward.category().unwrapKey().ifPresent(categoryKey -> graphics.setTooltipForNextFrame(font, TTTooltips.knowledgeLabel(reward.type(), categoryKey), mouseX, mouseY));
        }
        return met;
    }

    private void drawObservationChips(GuiGraphicsExtractor graphics, DetailFrame frame, AspectList cost, int firstX, int rowY, int spacing, boolean completedStage, int mouseX, int mouseY) {
        Player player = frame.player();
        Font font = frame.font();
        List<AspectInstance> chips = cost.entries();
        for (int index = 0; index < chips.size(); index++) {
            AspectInstance chip = chips.get(index);
            int chipX = firstX + index * spacing;
            boolean discovered = AspectPools.isDiscovered(player, chip.aspect());
            boolean hovered = Rect.inside(chipX, rowY, BookSprites.SLOT_SIZE, BookSprites.SLOT_SIZE, mouseX, mouseY);
            if (discovered) {
                hits.recordAspect(chip, chipX, rowY);
                int have = AspectPools.amount(player, chip.aspect());
                float alpha = have < chip.amount() ? Mth.sin(System.currentTimeMillis() % PULSE_PERIOD_MILLIS / (float) PULSE_PERIOD_MILLIS * Mth.TWO_PI) * PULSE_AMPLITUDE + PULSE_FLOOR : 1.0F;
                AspectTagRenderer.render(graphics, font, chipX, rowY, chip.aspect(), chip.amount(), 0, alpha, false);
                if (hovered) {
                    List<Component> lines = new ArrayList<>();
                    lines.add(Component.translatable("gui.thaumaturge.thaumonomicon.research_cost"));
                    lines.add(Component.translatable("tooltip.thaumaturge.amount_needed", AspectComponents.name(chip.aspect()), have, chip.amount())
                            .withStyle(have >= chip.amount() ? ChatFormatting.GREEN : ChatFormatting.RED));
                    graphics.setTooltipForNextFrame(font, lines, Optional.empty(), mouseX, mouseY);
                }
            } else {
                BookBlit.iconTile(graphics, AspectTileDrawer.UNKNOWN_TILE, chipX, rowY, OBSERVATION_UNKNOWN_TINT);
                if (hovered) {
                    List<Component> lines = new ArrayList<>();
                    lines.add(Component.translatable("tooltip.thaumaturge.aspect.unknown"));
                    lines.add(Component.translatable("message.thaumaturge.research.discovery_error", AspectComponents.help(chip.aspect())).withStyle(ChatFormatting.GRAY));
                    graphics.setTooltipForNextFrame(font, lines, Optional.empty(), mouseX, mouseY);
                }
            }
            if (completedStage || discovered && AspectPools.amount(player, chip.aspect()) >= chip.amount()) {
                BookBlit.checkmark(graphics, chipX, rowY);
            }
        }
    }
}
