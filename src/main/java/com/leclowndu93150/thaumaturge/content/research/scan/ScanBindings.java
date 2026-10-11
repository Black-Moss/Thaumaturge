package com.leclowndu93150.thaumaturge.content.research.scan;

import com.leclowndu93150.thaumaturge.api.aspect.AspectIndexAccess;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeType;
import com.leclowndu93150.thaumaturge.api.capability.PlayerResearchAccess;
import com.leclowndu93150.thaumaturge.api.research.IResearchEntry;
import com.leclowndu93150.thaumaturge.api.research.scan.ScanningManager;
import com.leclowndu93150.thaumaturge.content.aspect.EntityAspects;
import com.leclowndu93150.thaumaturge.content.research.PlayerKnowledge;
import com.leclowndu93150.thaumaturge.content.research.ResearchGrants;
import com.leclowndu93150.thaumaturge.content.research.ResearchManager;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public final class ScanBindings implements ScanningManager.Bindings {
    @Override
    public boolean progressResearch(Player player, Identifier research) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return false;
        }
        boolean hasEntry = serverPlayer.registryAccess().lookupOrThrow(IResearchEntry.REGISTRY_KEY).get(ResourceKey.create(IResearchEntry.REGISTRY_KEY, research)).isPresent();
        PlayerKnowledge knowledge = (PlayerKnowledge) PlayerResearchAccess.of(serverPlayer);
        if (hasEntry) {
            if (!knowledge.knows(research)) {
                if (!ResearchManager.unlock(serverPlayer, research)) {
                    return false;
                }
                ResearchManager.advanceStage(serverPlayer, research);
                return true;
            }
            return ResearchManager.advanceStage(serverPlayer, research);
        }
        if (knowledge.knows(research)) {
            return false;
        }
        knowledge.learn(research);
        knowledge.markComplete(research);
        knowledge.queueSync(serverPlayer);
        return true;
    }

    @Override
    public boolean addKnowledge(Player player, KnowledgeType type, Identifier category, int amount) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return false;
        }
        ResearchGrants.grantConvertedKnowledge(serverPlayer, type, amount);
        return true;
    }

    @Override
    public AspectList itemAspects(ItemStack stack) {
        return AspectIndexAccess.index().of(stack);
    }

    @Override
    public AspectList entityAspects(Entity entity) {
        return EntityAspects.of(entity);
    }

    @Override
    public boolean hidesItemForm(BlockState state) {
        return state.is(TTBlocks.NODE.get()) || state.is(TTBlocks.SILVERWOOD_NODE_LOG.get());
    }
}
