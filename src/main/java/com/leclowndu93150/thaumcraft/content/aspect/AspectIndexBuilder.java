package com.leclowndu93150.thaumcraft.content.aspect;

import com.leclowndu93150.thaumcraft.api.aspect.AspectList;
import com.leclowndu93150.thaumcraft.api.aspect.IAspectIndex;
import com.leclowndu93150.thaumcraft.api.aspect.IAspectRecipeContributor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;

public final class AspectIndexBuilder {
    public static final int MAX_AMOUNT_PER_ASPECT = 500;

    private static final List<IAspectRecipeContributor> CONTRIBUTORS = new ArrayList<>();

    static {
        CONTRIBUTORS.add(new VanillaCraftingAspectContributor());
    }

    private AspectIndexBuilder() {}

    public static void registerContributor(IAspectRecipeContributor contributor) {
        CONTRIBUTORS.add(contributor);
    }

    public static AspectIndex build(RecipeManager recipes, HolderLookup.Provider registries) {
        Map<Item, AspectList> derived = new HashMap<>(collectBase());
        IAspectIndex partial = new MapBackedIndex(derived);

        boolean progressed;
        int pass = 0;
        int maxPasses = 8;
        do {
            progressed = false;
            for (Item item : BuiltInRegistries.ITEM) {
                if (derived.containsKey(item)) {
                    continue;
                }
                for (IAspectRecipeContributor contributor : CONTRIBUTORS) {
                    Optional<AspectList> result = contributor.derive(item, recipes, registries, partial);
                    if (result.isPresent() && !result.get().isEmpty()) {
                        derived.put(item, cap(result.get()));
                        progressed = true;
                        break;
                    }
                }
            }
            pass++;
        } while (progressed && pass < maxPasses);

        return AspectIndex.of(derived);
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

    private record MapBackedIndex(Map<Item, AspectList> map) implements IAspectIndex {
        @Override
        public AspectList of(Item item) {
            AspectList result = map.get(item);
            return result == null ? AspectList.EMPTY : result;
        }

        @Override
        public AspectList of(ItemStack stack) {
            return of(stack.getItem());
        }
    }
}
