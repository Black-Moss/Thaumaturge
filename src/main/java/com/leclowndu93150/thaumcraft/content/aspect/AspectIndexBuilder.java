package com.leclowndu93150.thaumcraft.content.aspect;

import com.leclowndu93150.thaumcraft.api.aspect.AspectDataMaps;
import com.leclowndu93150.thaumcraft.api.aspect.AspectList;
import com.leclowndu93150.thaumcraft.api.aspect.IAspectIndex;
import com.leclowndu93150.thaumcraft.api.aspect.IAspectRecipeContributor;
import com.leclowndu93150.thaumcraft.api.aspect.RegisterAspectContributorsEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.bus.api.IEventBus;

public final class AspectIndexBuilder {
    public static final int MAX_AMOUNT_PER_ASPECT = 500;
    private static final int MAX_RECURSION_DEPTH = 100;

    private static final List<IAspectRecipeContributor> CONTRIBUTORS = new ArrayList<>();

    static {
        CONTRIBUTORS.add(new CrucibleAspectContributor());
        CONTRIBUTORS.add(new InfusionAspectContributor());
        CONTRIBUTORS.add(new CraftingAspectContributor());
    }

    private AspectIndexBuilder() {}

    public static void registerContributor(IAspectRecipeContributor contributor) {
        CONTRIBUTORS.add(contributor);
    }

    public static void fireContributorEvent(IEventBus modBus) {
        RegisterAspectContributorsEvent event = new RegisterAspectContributorsEvent();
        modBus.post(event);
        for (IAspectRecipeContributor contributor : event.contributors()) {
            CONTRIBUTORS.add(contributor);
        }
    }

    public static AspectIndex build(RecipeManager recipes, HolderLookup.Provider registries) {
        RecursiveIndex index = new RecursiveIndex(collectBase(), recipes, registries);
        for (Item item : BuiltInRegistries.ITEM) {
            index.resolve(item);
        }
        return AspectIndex.of(index.resolved());
    }

    private static Map<Item, AspectList> collectBase() {
        Map<Item, AspectList> map = new HashMap<>();
        for (Item item : BuiltInRegistries.ITEM) {
            AspectList declared = item.builtInRegistryHolder().getData(AspectDataMaps.BASE_ASPECTS);
            if (declared != null && !declared.isEmpty()) {
                map.put(item, cap(declared));
            }
        }
        return map;
    }

    private static AspectList cap(AspectList list) {
        AspectList result = AspectList.EMPTY;
        for (var entry : list.entries()) {
            int capped = Math.min(MAX_AMOUNT_PER_ASPECT, entry.amount());
            result = result.add(entry.aspect(), capped);
        }
        return result;
    }

    private static final class RecursiveIndex implements IAspectIndex {
        private final Map<Item, AspectList> resolved;
        private final Set<Item> computed = new HashSet<>();
        private final Set<Item> visiting = new HashSet<>();
        private final RecipeManager recipes;
        private final HolderLookup.Provider registries;

        RecursiveIndex(Map<Item, AspectList> base, RecipeManager recipes, HolderLookup.Provider registries) {
            this.resolved = new HashMap<>(base);
            this.computed.addAll(base.keySet());
            this.recipes = recipes;
            this.registries = registries;
        }

        Map<Item, AspectList> resolved() {
            return resolved;
        }

        AspectList resolve(Item item) {
            if (computed.contains(item)) {
                return resolved.getOrDefault(item, AspectList.EMPTY);
            }
            if (visiting.contains(item) || visiting.size() >= MAX_RECURSION_DEPTH) {
                return AspectList.EMPTY;
            }
            visiting.add(item);
            try {
                for (IAspectRecipeContributor contributor : CONTRIBUTORS) {
                    Optional<AspectList> result = contributor.derive(item, recipes, registries, this);
                    if (result.isPresent() && !result.get().isEmpty()) {
                        AspectList capped = cap(result.get());
                        resolved.put(item, capped);
                        computed.add(item);
                        return capped;
                    }
                }
                computed.add(item);
            } finally {
                visiting.remove(item);
            }
            return AspectList.EMPTY;
        }

        @Override
        public AspectList of(Item item) {
            return resolve(item);
        }

        @Override
        public AspectList of(ItemStack stack) {
            return resolve(stack.getItem());
        }
    }
}
