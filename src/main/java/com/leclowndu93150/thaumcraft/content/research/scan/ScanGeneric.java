package com.leclowndu93150.thaumcraft.content.research.scan;

import com.leclowndu93150.thaumcraft.api.aspect.AspectList;
import com.leclowndu93150.thaumcraft.api.capability.KnowledgeType;
import com.leclowndu93150.thaumcraft.api.research.IResearchCategory;
import com.leclowndu93150.thaumcraft.api.research.scan.IScanThing;
import com.leclowndu93150.thaumcraft.api.research.scan.ScanKeys;
import com.leclowndu93150.thaumcraft.api.research.scan.ScanningManager;
import com.leclowndu93150.thaumcraft.content.aspect.AspectIndexHolder;
import com.leclowndu93150.thaumcraft.content.aspect.EntityAspects;
import com.leclowndu93150.thaumcraft.content.research.ResearchManager;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class ScanGeneric implements IScanThing {
    @Override
    public boolean checkThing(Player player, @Nullable Object target) {
        return !aspectsOf(player, target).isEmpty();
    }

    @Override
    public void onSuccess(Player player, @Nullable Object target) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        AspectList aspects = aspectsOf(player, target);
        if (aspects.isEmpty()) {
            return;
        }
        for (Holder.Reference<IResearchCategory> category
                : serverPlayer.registryAccess().lookupOrThrow(IResearchCategory.REGISTRY_KEY).listElements().toList()) {
            int amount = category.value().applyFormula(aspects);
            ResearchManager.gainKnowledge(serverPlayer, KnowledgeType.OBSERVATION, category, amount);
        }
    }

    @Override
    public @Nullable Identifier getResearchKey(Player player, @Nullable Object target) {
        if (target instanceof Entity entity && !(target instanceof ItemEntity)) {
            return ScanKeys.entity(entity.getType());
        }
        ItemStack stack = ScanningManager.getItemFromParms(player, target);
        return stack.isEmpty() ? null : ScanKeys.item(stack.getItem());
    }

    private static AspectList aspectsOf(Player player, @Nullable Object target) {
        if (target == null) {
            return AspectList.EMPTY;
        }
        if (target instanceof Entity entity && !(target instanceof ItemEntity)) {
            return EntityAspects.of(entity);
        }
        ItemStack stack = ScanningManager.getItemFromParms(player, target);
        return stack.isEmpty() ? AspectList.EMPTY : AspectIndexHolder.get().of(stack);
    }
}
