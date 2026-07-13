package com.leclowndu93150.thaumcraft.content.research;

import com.leclowndu93150.thaumcraft.api.aspect.IAspect;
import com.leclowndu93150.thaumcraft.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumcraft.api.capability.KnowledgeType;
import com.leclowndu93150.thaumcraft.api.research.IResearchCategory;
import com.leclowndu93150.thaumcraft.api.research.IResearchEntry;
import com.leclowndu93150.thaumcraft.api.research.scan.ScanKeys;
import com.leclowndu93150.thaumcraft.content.research.pool.AspectPools;
import com.leclowndu93150.thaumcraft.network.ClientboundKnowledgeGainPayload;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

public final class ResearchGrants {

    private ResearchGrants() {}

    public static int grantAll(ServerPlayer player) {
        PlayerKnowledge knowledge = (PlayerKnowledge) KnowledgeAccess.of(player);
        int granted = 0;
        for (Holder.Reference<IResearchEntry> entry
                : player.registryAccess().lookupOrThrow(IResearchEntry.REGISTRY_KEY).listElements().toList()) {
            Identifier id = entry.key().identifier();
            if (knowledge.isResearchComplete(id)) {
                continue;
            }
            if (ResearchManager.complete(player, id)) {
                ResearchManager.setStage(player, id, entry.value().stages().size());
                granted++;
                Optional<ResourceKey<IResearchCategory>> category = entry.value().category().unwrapKey();
                PacketDistributor.sendToPlayer(player,
                        new ClientboundKnowledgeGainPayload(KnowledgeType.OBSERVATION, category));
            }
        }
        for (Holder.Reference<IAspect> aspect
                : player.registryAccess().lookupOrThrow(IAspect.REGISTRY_KEY).listElements().toList()) {
            ResearchManager.unlock(player, ScanKeys.aspect(aspect.key()));
        }
        AspectPools.grantAllForCommand(player, AspectPools.SOFT_CAP);
        return granted;
    }
}
