package com.leclowndu93150.thaumaturge.client.screen.research.detail.requirement;

import com.leclowndu93150.thaumaturge.api.capability.IPlayerKnowledge;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeType;
import com.leclowndu93150.thaumaturge.api.research.IResearchStage;
import com.leclowndu93150.thaumaturge.api.research.KnowledgeReward;
import com.leclowndu93150.thaumaturge.api.research.ResearchRequirement;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.EntryDetailModel;
import com.leclowndu93150.thaumaturge.content.research.ResearchManager;
import com.leclowndu93150.thaumaturge.content.research.note.ResearchNoteData;
import com.leclowndu93150.thaumaturge.content.research.note.ResearchNotes;
import com.leclowndu93150.thaumaturge.content.research.pool.AspectPools;
import java.util.List;
import java.util.stream.IntStream;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class StageRequirementCheck {
    private StageRequirementCheck() {}

    public static int countMatching(Player player, ResearchRequirement requirement) {
        Inventory inventory = player.getInventory();
        return IntStream.range(0, inventory.getContainerSize()).mapToObj(inventory::getItem).filter(stack -> !stack.isEmpty() && requirement.matches(stack)).mapToInt(ItemStack::getCount).sum();
    }

    public static boolean allSatisfied(Player player, EntryDetailModel model, IResearchStage stage, int displayedStage) {
        if (!EntryDetailModel.hasRequirements(stage)) {
            return false;
        }
        IPlayerKnowledge knowledge = KnowledgeAccess.of(player);
        return stage.requiredResearch().stream().allMatch(knowledge::isResearchComplete) && stage.obtain().stream().allMatch(requirement -> countMatching(player, requirement) >= requirement.amount())
                && stage.craft().stream().allMatch(requirement -> ResearchManager.isCraftSatisfied(player, knowledge, requirement))
                && knowledgeSatisfied(player, knowledge, model, stage, displayedStage);
    }

    private static boolean knowledgeSatisfied(Player player, IPlayerKnowledge knowledge, EntryDetailModel model, IResearchStage stage, int displayedStage) {
        List<KnowledgeReward> rewards = stage.requiredKnowledge();
        int theoryOrdinal = ResearchNotes.theoryRowsBefore(model.research(), displayedStage);
        boolean satisfied = true;
        for (KnowledgeReward reward : rewards) {
            if (reward.type() == KnowledgeType.THEORY) {
                Identifier learnKey = ResearchNoteData.learnKey(model.entryId(), theoryOrdinal++);
                satisfied &= knowledge.isResearchKnown(learnKey);
            } else {
                satisfied &= AspectPools.canAfford(player, ResearchNotes.observationCost(model.research(), reward.amount()));
            }
        }
        return satisfied;
    }
}
