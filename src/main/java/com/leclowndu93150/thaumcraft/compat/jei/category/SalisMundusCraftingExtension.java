package com.leclowndu93150.thaumcraft.compat.jei.category;

import com.leclowndu93150.thaumcraft.api.aspect.IAspect;
import com.leclowndu93150.thaumcraft.compat.jei.ThaumcraftJEIPlugin;
import com.leclowndu93150.thaumcraft.content.recipe.SalisMundusRecipe;
import com.leclowndu93150.thaumcraft.content.taint.item.EssentiaCrystalFactory;
import com.leclowndu93150.thaumcraft.registry.TCItems;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.display.SlotDisplay;

public final class SalisMundusCraftingExtension implements ICraftingCategoryExtension<SalisMundusRecipe> {
    public static final SalisMundusCraftingExtension INSTANCE = new SalisMundusCraftingExtension();

    private SalisMundusCraftingExtension() {}

    @Override
    public List<SlotDisplay> getIngredients(RecipeHolder<SalisMundusRecipe> recipeHolder) {
        List<SlotDisplay> primals = primalCrystalDisplays();
        return List.of(
                new SlotDisplay.ItemSlotDisplay(Items.FLINT),
                new SlotDisplay.ItemSlotDisplay(Items.BOWL),
                new SlotDisplay.ItemSlotDisplay(Items.REDSTONE),
                primals.get(0 % primals.size()),
                primals.get(1 % primals.size()),
                primals.get(2 % primals.size()));
    }

    private static List<SlotDisplay> primalCrystalDisplays() {
        RegistryAccess registryAccess = ThaumcraftJEIPlugin.clientRegistryAccess();
        if (registryAccess != null) {
            Optional<Registry<IAspect>> registry = registryAccess.lookup(IAspect.REGISTRY_KEY);
            if (registry.isPresent()) {
                List<SlotDisplay> variants = new ArrayList<>();
                for (Holder.Reference<IAspect> aspect : registry.get().listElements().toList()) {
                    if (aspect.value().isPrimal()) {
                        variants.add(new SlotDisplay.ItemStackSlotDisplay(
                                ItemStackTemplate.fromNonEmptyStack(EssentiaCrystalFactory.of(aspect))));
                    }
                }
                if (!variants.isEmpty()) {
                    return variants;
                }
            }
        }
        SlotDisplay fallback = new SlotDisplay.ItemSlotDisplay(TCItems.ESSENTIA_CRYSTAL.get());
        return List.of(fallback);
    }
}
