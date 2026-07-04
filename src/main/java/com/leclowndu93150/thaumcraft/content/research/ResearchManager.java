package com.leclowndu93150.thaumcraft.content.research;

import com.leclowndu93150.thaumcraft.api.capability.IPlayerKnowledge;
import com.leclowndu93150.thaumcraft.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumcraft.config.ThaumcraftCommonConfig;
import com.leclowndu93150.thaumcraft.api.warp.WarpHelper;
import com.leclowndu93150.thaumcraft.api.warp.WarpType;
import com.leclowndu93150.thaumcraft.network.ClientboundKnowledgeGainPayload;
import com.leclowndu93150.thaumcraft.api.capability.KnowledgeType;
import com.leclowndu93150.thaumcraft.api.recipe.DustTrigger;
import com.leclowndu93150.thaumcraft.api.recipe.ResearchGate;
import com.leclowndu93150.thaumcraft.api.research.IResearchCategory;
import com.leclowndu93150.thaumcraft.api.research.IResearchEntry;
import com.leclowndu93150.thaumcraft.api.research.IResearchStage;
import com.leclowndu93150.thaumcraft.api.research.KnowledgeReward;
import com.leclowndu93150.thaumcraft.api.research.ResearchEvent;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

public final class ResearchManager {
    private ResearchManager() {}

    public static boolean unlock(ServerPlayer player, Identifier research) {
        if (research == null) return false;
        PlayerKnowledge knowledge = (PlayerKnowledge) KnowledgeAccess.of(player);
        if (knowledge.isResearchKnown(research)) return false;
        ResearchEvent.Unlocked event = new ResearchEvent.Unlocked(player, research);
        if (NeoForge.EVENT_BUS.post(event).isCanceled()) return false;
        boolean changed = knowledge.addResearch(research);
        if (changed) {
            knowledge.sync(player);
        }
        return changed;
    }

    public static boolean advanceStage(ServerPlayer player, Identifier research) {
        if (research == null) return false;
        IResearchEntry entry = entry(player, research).orElse(null);
        if (entry == null) return false;
        PlayerKnowledge knowledge = (PlayerKnowledge) KnowledgeAccess.of(player);
        if (!knowledge.isResearchKnown(research)) return false;
        if (knowledge.isResearchComplete(research)) return false;
        int currentStage = knowledge.researchStage(research);
        int totalStages = entry.stages().size();
        if (currentStage >= totalStages) return false;
        int next = currentStage + 1;
        ResearchEvent.StageAdvanced event = new ResearchEvent.StageAdvanced(player, research, currentStage, next);
        if (NeoForge.EVENT_BUS.post(event).isCanceled()) return false;
        if (next < totalStages) {
            knowledge.setResearchStage(research, next);
        }
        IResearchStage finished = entry.stages().get(Math.max(0, currentStage));
        applyStageEffects(player, knowledge, finished);
        if (next >= totalStages) {
            markCompleteInternal(player, knowledge, research);
        }
        knowledge.sync(player);
        return true;
    }

    public static boolean setStage(ServerPlayer player, Identifier research, int stage) {
        if (research == null || stage <= 0) return false;
        PlayerKnowledge knowledge = (PlayerKnowledge) KnowledgeAccess.of(player);
        if (!knowledge.isResearchKnown(research)) return false;
        int previous = knowledge.researchStage(research);
        if (previous == stage) return false;
        ResearchEvent.StageAdvanced event = new ResearchEvent.StageAdvanced(player, research, previous, stage);
        if (NeoForge.EVENT_BUS.post(event).isCanceled()) return false;
        knowledge.setResearchStage(research, stage);
        knowledge.sync(player);
        return true;
    }

    public static boolean complete(ServerPlayer player, Identifier research) {
        if (research == null) return false;
        PlayerKnowledge knowledge = (PlayerKnowledge) KnowledgeAccess.of(player);
        if (!knowledge.isResearchKnown(research)) {
            if (!unlockSilent(knowledge, research)) return false;
        }
        if (knowledge.isResearchComplete(research)) return false;
        boolean changed = markCompleteInternal(player, knowledge, research);
        if (changed) knowledge.sync(player);
        return changed;
    }

    public static boolean gainKnowledge(ServerPlayer player, KnowledgeType type, Holder<IResearchCategory> category, int amount) {
        if (type == null || amount == 0) return false;
        ResearchEvent.KnowledgeGained event = new ResearchEvent.KnowledgeGained(player, type, category, amount);
        if (NeoForge.EVENT_BUS.post(event).isCanceled()) return false;
        PlayerKnowledge knowledge = (PlayerKnowledge) KnowledgeAccess.of(player);
        ResourceKey<IResearchCategory> key = category == null ? null : category.unwrapKey().orElse(null);
        int pointsBefore = knowledge.knowledge(type, key);
        boolean changed = knowledge.addKnowledge(type, key, amount);
        if (changed) knowledge.sync(player);
        int pointsGained = knowledge.knowledge(type, key) - pointsBefore;
        for (int point = 0; point < pointsGained; point++) {
            PacketDistributor.sendToPlayer(player,
                    new ClientboundKnowledgeGainPayload(type, Optional.ofNullable(key)));
        }
        return changed;
    }

    public static void applyAutoUnlock(ServerPlayer player) {
        PlayerKnowledge knowledge = (PlayerKnowledge) KnowledgeAccess.of(player);
        knowledge.applyAutoUnlock(player.registryAccess());
        knowledge.sync(player);
    }

    public static boolean doesPassGate(Player player, @Nullable ResearchGate gate) {
        if (gate == null) return true;
        IPlayerKnowledge knowledge = KnowledgeAccess.of(player);
        boolean known = gate.stage().isPresent()
                ? knowledge.isResearchKnown(gate.entry(), gate.stage().get())
                : knowledge.isResearchKnown(gate.entry());
        return gate.negate() != known;
    }

    public static IPlayerKnowledge of(ServerPlayer player) {
        return KnowledgeAccess.of(player);
    }

    private static boolean markCompleteInternal(ServerPlayer player, PlayerKnowledge knowledge, Identifier research) {
        ResearchEvent.Completed event = new ResearchEvent.Completed(player, research);
        if (NeoForge.EVENT_BUS.post(event).isCanceled()) return false;
        return knowledge.markComplete(research);
    }

    private static boolean unlockSilent(PlayerKnowledge knowledge, Identifier research) {
        return knowledge.addResearch(research);
    }

    private static void applyStageEffects(ServerPlayer player, PlayerKnowledge knowledge, IResearchStage stage) {
        for (KnowledgeReward reward : stage.knowledge()) {
            ResourceKey<IResearchCategory> key = reward.category().unwrapKey().orElse(null);
            knowledge.addKnowledge(reward.type(), key, reward.amount());
        }
        if (stage.warp() > 0) {
            applyWarp(player, stage.warp());
        }
    }

    public static void applyWarp(ServerPlayer player, int amount) {
        if (amount <= 0 || ThaumcraftCommonConfig.WUSS_MODE.get()) {
            return;
        }
        if (amount > 1) {
            int normal = amount / 2;
            WarpHelper.addWarp(player, normal, WarpType.NORMAL);
            WarpHelper.addWarp(player, amount - normal, WarpType.PERMANENT);
        } else {
            WarpHelper.addWarp(player, amount, WarpType.PERMANENT);
        }
    }

    private static Optional<IResearchEntry> entry(ServerPlayer player, Identifier research) {
        return player.registryAccess()
                .lookup(IResearchEntry.REGISTRY_KEY)
                .flatMap(lookup -> lookup.get(ResourceKey.create(IResearchEntry.REGISTRY_KEY, research)))
                .map(Holder.Reference::value);
    }
}
