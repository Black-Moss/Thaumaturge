package com.leclowndu93150.thaumcraft.content.research.scan;

import com.leclowndu93150.thaumcraft.api.aspect.AspectList;
import com.leclowndu93150.thaumcraft.api.aspect.AspectComponents;
import com.leclowndu93150.thaumcraft.api.aspect.IAspect;
import net.minecraft.network.chat.Component;
import com.leclowndu93150.thaumcraft.api.aspect.AspectInstance;
import com.leclowndu93150.thaumcraft.api.research.scan.IScanThing;
import com.leclowndu93150.thaumcraft.api.research.scan.ScanKeys;
import com.leclowndu93150.thaumcraft.api.research.scan.ScanningManager;
import com.leclowndu93150.thaumcraft.content.aspect.AspectIndexHolder;
import com.leclowndu93150.thaumcraft.content.aspect.EntityAspects;
import com.leclowndu93150.thaumcraft.content.research.pool.AspectPools;
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
    public @Nullable Component scanFailure(Player player, @Nullable Object target) {
        for (AspectInstance instance : aspectsOf(player, target).entries()) {
            if (AspectPools.hasDiscoveredComponents(player, instance.aspect())) {
                continue;
            }
            for (Holder<IAspect> component : instance.aspect().value().components()) {
                if (!AspectPools.isDiscovered(player, component)) {
                    return Component.translatable("tc.discoveryerror", AspectComponents.help(component));
                }
            }
        }
        return null;
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
        AspectPools.grantAll(serverPlayer, aspects);
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
