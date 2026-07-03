package com.leclowndu93150.thaumcraft.content.research.scan;

import com.leclowndu93150.thaumcraft.api.aspect.AspectList;
import com.leclowndu93150.thaumcraft.api.aspect.IAspect;
import com.leclowndu93150.thaumcraft.api.capability.KnowledgeType;
import com.leclowndu93150.thaumcraft.api.research.TCResearchCategories;
import com.leclowndu93150.thaumcraft.api.research.scan.IScanThing;
import com.leclowndu93150.thaumcraft.api.research.scan.ScanKeys;
import com.leclowndu93150.thaumcraft.api.research.scan.ScanningManager;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class ScanAspectDiscovery implements IScanThing {
    private static final int DISCOVERY_OBSERVATION_POINTS = 1;

    private final ResourceKey<IAspect> aspect;

    public ScanAspectDiscovery(ResourceKey<IAspect> aspect) {
        this.aspect = aspect;
    }

    @Override
    public boolean checkThing(Player player, @Nullable Object target) {
        if (target == null) {
            return false;
        }
        Holder<IAspect> holder = player.registryAccess().lookupOrThrow(IAspect.REGISTRY_KEY)
                .get(aspect).orElse(null);
        if (holder == null) {
            return false;
        }
        AspectList aspects;
        if (target instanceof Entity entity && !(target instanceof ItemEntity)) {
            aspects = ScanningManager.entityAspects(entity);
        } else {
            ItemStack stack = ScanningManager.getItemFromParms(player, target);
            if (stack.isEmpty()) {
                return false;
            }
            aspects = ScanningManager.itemAspects(stack);
        }
        return aspects.amountOf(holder) > 0;
    }

    @Override
    public void onSuccess(Player player, @Nullable Object target) {
        ScanningManager.addKnowledge(player, KnowledgeType.OBSERVATION,
                TCResearchCategories.AUROMANCY.identifier(), DISCOVERY_OBSERVATION_POINTS);
        ScanningManager.addKnowledge(player, KnowledgeType.OBSERVATION,
                TCResearchCategories.BASICS.identifier(), DISCOVERY_OBSERVATION_POINTS);
        ScanningManager.addKnowledge(player, KnowledgeType.OBSERVATION,
                TCResearchCategories.ALCHEMY.identifier(), DISCOVERY_OBSERVATION_POINTS);
    }

    @Override
    public Identifier getResearchKey(Player player, @Nullable Object target) {
        return ScanKeys.aspect(aspect);
    }
}
