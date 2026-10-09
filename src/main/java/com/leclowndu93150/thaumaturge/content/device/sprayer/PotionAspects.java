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
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;

final class PotionAspects {
    private static final float REDUCTION_FACTOR = 0.66F;
    private static final int ALKIMIA_PER_REAGENT = 3;
    private static final int FALLBACK_AMOUNT = 5;
    private static final int ASPECT_CAP = 10;
    private static final int MAX_REAGENT_DEPTH = 16;

    private PotionAspects() {}

    static AspectList of(ServerLevel level, ItemStack stack) {
        Optional<Holder<Potion>> potion = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).potion();
        List<ItemStack> reagents = potion.isPresent() ? reagentsOf(level, potion.get()) : List.of();
        AspectList result = reagents.isEmpty() ? fallback(level) : reduce(total(level, reagents));
        return cap(result);
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
        AspectList total = AspectList.EMPTY;
        for (ItemStack reagent : reagents) {
            total = total.add(AspectIndexAccess.of(reagent));
        }
        return total.add(aspect(level, TTAspects.ALKIMIA), ALKIMIA_PER_REAGENT * reagents.size());
    }

    private static AspectList reduce(AspectList total) {
        AspectList reduced = AspectList.EMPTY;
        for (AspectInstance entry : total.entries()) {
            int kept = entry.amount() - (int) (entry.amount() * REDUCTION_FACTOR);
            if (kept > 0) {
                reduced = reduced.add(entry.aspect(), kept);
            }
        }
        return reduced;
    }

    private static AspectList cap(AspectList list) {
        AspectList capped = list;
        while (capped.size() > ASPECT_CAP) {
            AspectInstance smallest = capped.entries().getFirst();
            for (AspectInstance entry : capped.entries()) {
                if (entry.amount() < smallest.amount()) {
                    smallest = entry;
                }
            }
            capped = capped.without(smallest.aspect());
        }
        return capped;
    }

    private static List<ItemStack> reagentsOf(ServerLevel level, Holder<Potion> target) {
        List<?> mixes = ((PotionBrewingAccessor) level.potionBrewing()).thaumaturge$getPotionMixes();
        TreeMap<Integer, ItemStack> byItemId = new TreeMap<>();
        Set<Identifier> visited = new HashSet<>();
        List<Identifier> frontier = new ArrayList<>();
        Optional<Identifier> start = keyOf(target);
        if (start.isPresent()) {
            frontier.add(start.get());
            visited.add(start.get());
        }
        for (int depth = 0; depth <= MAX_REAGENT_DEPTH && !frontier.isEmpty(); depth++) {
            List<Identifier> next = new ArrayList<>();
            for (Object raw : mixes) {
                PotionBrewingMixAccessor mix = (PotionBrewingMixAccessor) raw;
                Optional<Identifier> output = keyOf(mix.thaumaturge$getTo());
                if (output.isEmpty() || !frontier.contains(output.get())) {
                    continue;
                }
                addReagent(byItemId, mix);
                Optional<Identifier> input = keyOf(mix.thaumaturge$getFrom());
                if (input.isPresent() && visited.add(input.get())) {
                    next.add(input.get());
                }
            }
            frontier = next;
        }
        return new ArrayList<>(byItemId.values());
    }

    private static void addReagent(TreeMap<Integer, ItemStack> byItemId, PotionBrewingMixAccessor mix) {
        Optional<Holder<Item>> lowest = mix.thaumaturge$getIngredient().items().min(Comparator.comparingInt(PotionAspects::itemId));
        if (lowest.isPresent()) {
            byItemId.putIfAbsent(itemId(lowest.get()), new ItemStack(lowest.get()));
        }
    }

    private static int itemId(Holder<Item> item) {
        return BuiltInRegistries.ITEM.getId(item.value());
    }

    private static Optional<Identifier> keyOf(Holder<?> holder) {
        return holder.unwrapKey().map(ResourceKey::identifier).filter(id -> !Potions.WATER.is(id));
    }
}
