package com.leclowndu93150.thaumcraft.content.research.scan;

import com.leclowndu93150.thaumcraft.api.aspect.AspectList;
import com.leclowndu93150.thaumcraft.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumcraft.api.capability.KnowledgeType;
import com.leclowndu93150.thaumcraft.api.research.IResearchCategory;
import com.leclowndu93150.thaumcraft.api.research.IResearchEntry;
import com.leclowndu93150.thaumcraft.api.research.scan.ScanningManager;
import com.leclowndu93150.thaumcraft.content.aspect.AspectIndexHolder;
import com.leclowndu93150.thaumcraft.content.aspect.EntityAspects;
import com.leclowndu93150.thaumcraft.content.research.ResearchManager;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class ScanBindings implements ScanningManager.Bindings {
    @Override
    public boolean progressResearch(Player player, Identifier research) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return false;
        }
        boolean hasEntry = serverPlayer.registryAccess().lookupOrThrow(IResearchEntry.REGISTRY_KEY)
                .get(ResourceKey.create(IResearchEntry.REGISTRY_KEY, research)).isPresent();
        if (hasEntry && KnowledgeAccess.of(serverPlayer).isResearchKnown(research)) {
            return ResearchManager.advanceStage(serverPlayer, research);
        }
        return ResearchManager.unlock(serverPlayer, research);
    }

    @Override
    public boolean addKnowledge(Player player, KnowledgeType type, Identifier category, int amount) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return false;
        }
        Optional<Holder.Reference<IResearchCategory>> holder =
                serverPlayer.registryAccess().lookupOrThrow(IResearchCategory.REGISTRY_KEY)
                        .get(ResourceKey.create(IResearchCategory.REGISTRY_KEY, category));
        return holder.isPresent() && ResearchManager.gainKnowledge(serverPlayer, type, holder.get(), amount);
    }

    @Override
    public AspectList itemAspects(ItemStack stack) {
        return AspectIndexHolder.get().of(stack);
    }

    @Override
    public AspectList entityAspects(Entity entity) {
        return EntityAspects.of(entity);
    }
}
