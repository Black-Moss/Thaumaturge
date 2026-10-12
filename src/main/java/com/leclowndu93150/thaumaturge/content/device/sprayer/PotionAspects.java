package com.leclowndu93150.thaumaturge.content.device.sprayer;

import com.leclowndu93150.thaumaturge.api.aspect.AspectIndexAccess;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.content.essentia.EssentiaTransportHelper;
import com.leclowndu93150.thaumaturge.mixin.world.item.alchemy.PotionBrewingAccessor;
import com.leclowndu93150.thaumaturge.mixin.world.item.alchemy.PotionBrewingMixAccessor;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;

final class PotionAspects {
    private static final int REDUCTION_PERCENT = 66;
    private static final int PERCENT = 100;
    private static final int ALKIMIA_PER_REAGENT = 3;
    private static final int FALLBACK_AMOUNT = 5;
    private static final int ASPECT_CAP = 10;
    private static final int MAX_SEARCH_LEVEL = 16;

    private PotionAspects() {}

    static AspectList of(ServerLevel level, ItemStack stack) {
        Optional<Holder<Potion>> potion = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).potion();
        List<ItemStack> reagents = potion.isPresent() ? reagentsAlongRoute(level, potion.get()) : List.of();
        if (reagents.isEmpty()) {
            return fallback(level);
        }
        return trimToCap(reduce(total(level, reagents)));
    }

    private static AspectList fallback(ServerLevel level) {
        return AspectList.EMPTY.add(aspect(level, TTAspects.PRAECANTATIO), FALLBACK_AMOUNT).add(aspect(level, TTAspects.ALKIMIA), FALLBACK_AMOUNT);
    }

    private static Holder<IAspect> aspect(ServerLevel level, ResourceKey<IAspect> key) {
        Holder<IAspect> holder = EssentiaTransportHelper.resolve(level, key);
        if (holder == null) {
            throw new IllegalStateException("Aspect " + key.identifier() + " is not registered");
        }
        return holder;
    }

    private static AspectList total(ServerLevel level, List<ItemStack> reagents) {
        Holder<IAspect> alkimia = aspect(level, TTAspects.ALKIMIA);
        AspectList sum = AspectList.EMPTY;
        for (ItemStack reagent : reagents) {
            sum = sum.add(AspectIndexAccess.of(reagent)).add(alkimia, ALKIMIA_PER_REAGENT);
        }
        return sum;
    }

    private static AspectList reduce(AspectList sum) {
        AspectList reduced = AspectList.EMPTY;
        for (AspectInstance entry : sum.entries()) {
            int remaining = entry.amount() - entry.amount() * REDUCTION_PERCENT / PERCENT;
            if (remaining > 0) {
                reduced = reduced.add(entry.aspect(), remaining);
            }
        }
        return reduced;
    }

    private static AspectList trimToCap(AspectList list) {
        AspectList trimmed = list;
        while (trimmed.size() > ASPECT_CAP) {
            AspectInstance weakest = trimmed.entries().get(0);
            for (AspectInstance entry : trimmed.entries()) {
                if (entry.amount() < weakest.amount()) {
                    weakest = entry;
                }
            }
            trimmed = trimmed.without(weakest.aspect());
        }
        return trimmed;
    }

    private static List<ItemStack> reagentsAlongRoute(ServerLevel level, Holder<Potion> target) {
        List<?> mixes = ((PotionBrewingAccessor) level.potionBrewing()).thaumaturge$getPotionMixes();
        Map<Item, ItemStack> reagents = new LinkedHashMap<>();
        collectReagents(mixes, target, 0, new HashSet<>(), reagents);
        return new ArrayList<>(reagents.values());
    }

    private static void collectReagents(List<?> mixes, Holder<?> potion, int level, Set<ResourceKey<?>> searched, Map<Item, ItemStack> reagents) {
        if (level > MAX_SEARCH_LEVEL) {
            return;
        }
        Optional<? extends ResourceKey<?>> key = potion.unwrapKey();
        if (key.isEmpty() || !searched.add(key.get())) {
            return;
        }
        for (Object raw : mixes) {
            PotionBrewingMixAccessor mix = (PotionBrewingMixAccessor) raw;
            if (mix.thaumaturge$getTo().unwrapKey().filter(key.get()::equals).isEmpty()) {
                continue;
            }
            reagentItem(mix).ifPresent(item -> reagents.putIfAbsent(item, new ItemStack(item)));
            collectReagents(mixes, mix.thaumaturge$getFrom(), level + 1, searched, reagents);
        }
    }

    private static Optional<Item> reagentItem(PotionBrewingMixAccessor mix) {
        return mix.thaumaturge$getIngredient().items().map(Holder::value).findFirst();
    }
}
