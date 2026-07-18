package com.leclowndu93150.thaumcraft.compat.jei;

import com.leclowndu93150.thaumcraft.api.aspect.AspectInstance;
import com.leclowndu93150.thaumcraft.api.aspect.AspectList;
import com.leclowndu93150.thaumcraft.api.aspect.IAspect;
import com.leclowndu93150.thaumcraft.compat.jei.ingredient.AspectIngredientType;
import com.leclowndu93150.thaumcraft.content.research.pool.AspectPools;
import com.leclowndu93150.thaumcraft.network.ServerboundRequestSyncAspectPoolPayload;
import com.leclowndu93150.thaumcraft.registry.TCDataComponents;
import com.leclowndu93150.thaumcraft.registry.TCItems;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jspecify.annotations.Nullable;

public final class AspectJeiSync {
    private static @Nullable IJeiRuntime runtime;
    private static final Set<Identifier> visibleAspects = new HashSet<>();
    private static final Set<Identifier> hiddenStackAspects = new HashSet<>();
    private static final Map<Identifier, List<ItemStack>> gatedStacks = new HashMap<>();

    private AspectJeiSync() {}

    static void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        runtime = jeiRuntime;
        visibleAspects.clear();
        hiddenStackAspects.clear();
        gatedStacks.clear();
        IIngredientManager ingredients = jeiRuntime.getIngredientManager();
        for (AspectInstance instance : ingredients.getAllIngredients(AspectIngredientType.INSTANCE)) {
            visibleAspects.add(AspectPools.idOf(instance.aspect()));
        }
        for (ItemStack stack : ingredients.getAllIngredients(VanillaTypes.ITEM_STACK)) {
            Identifier aspect = gatedAspectOf(stack);
            if (aspect != null) {
                gatedStacks.computeIfAbsent(aspect, k -> new ArrayList<>()).add(stack);
            }
        }
        ClientPacketDistributor.sendToServer(ServerboundRequestSyncAspectPoolPayload.INSTANCE);
    }

    private static @Nullable Identifier gatedAspectOf(ItemStack stack) {
        if (stack.is(TCItems.ESSENTIA_CRYSTAL.get())) {
            AspectInstance instance = stack.get(TCDataComponents.CRYSTAL_ASPECT.get());
            return instance == null ? null : AspectPools.idOf(instance.aspect());
        }
        if (stack.is(TCItems.PHIAL.get())) {
            AspectList aspects = stack.get(TCDataComponents.ASPECTS.get());
            if (aspects == null || aspects.isEmpty()) {
                return null;
            }
            return AspectPools.idOf(aspects.entries().get(0).aspect());
        }
        return null;
    }

    public static void syncDiscovered() {
        IJeiRuntime current = runtime;
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (current == null || player == null) {
            return;
        }
        IIngredientManager ingredients = current.getIngredientManager();
        List<AspectInstance> addedAspects = new ArrayList<>();
        List<AspectInstance> removedAspects = new ArrayList<>();
        List<ItemStack> addedStacks = new ArrayList<>();
        List<ItemStack> removedStacks = new ArrayList<>();
        player.registryAccess().lookupOrThrow(IAspect.REGISTRY_KEY).listElements().forEach(ref -> {
            Identifier id = ref.key().identifier();
            boolean discovered = AspectPools.isDiscovered(player, ref);
            if (discovered && !visibleAspects.contains(id)) {
                visibleAspects.add(id);
                addedAspects.add(new AspectInstance(ref, 1));
            } else if (!discovered && visibleAspects.contains(id)) {
                visibleAspects.remove(id);
                removedAspects.add(new AspectInstance(ref, 1));
            }
            List<ItemStack> stacks = gatedStacks.get(id);
            if (stacks != null) {
                if (discovered && hiddenStackAspects.remove(id)) {
                    addedStacks.addAll(stacks);
                } else if (!discovered && hiddenStackAspects.add(id)) {
                    removedStacks.addAll(stacks);
                }
            }
        });
        if (!removedAspects.isEmpty()) {
            ingredients.removeIngredientsAtRuntime(AspectIngredientType.INSTANCE, removedAspects);
        }
        if (!addedAspects.isEmpty()) {
            ingredients.addIngredientsAtRuntime(AspectIngredientType.INSTANCE, addedAspects);
        }
        if (!removedStacks.isEmpty()) {
            ingredients.removeIngredientsAtRuntime(VanillaTypes.ITEM_STACK, removedStacks);
        }
        if (!addedStacks.isEmpty()) {
            ingredients.addIngredientsAtRuntime(VanillaTypes.ITEM_STACK, addedStacks);
        }
    }
}
