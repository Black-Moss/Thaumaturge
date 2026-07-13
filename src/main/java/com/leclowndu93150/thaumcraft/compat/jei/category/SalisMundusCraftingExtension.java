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
        SlotDisplay crystal = crystalDisplay();
        return List.of(
                new SlotDisplay.ItemSlotDisplay(Items.FLINT),
                new SlotDisplay.ItemSlotDisplay(Items.BOWL),
                new SlotDisplay.ItemSlotDisplay(Items.REDSTONE),
                crystal, crystal, crystal);
    }

    private static SlotDisplay crystalDisplay() {
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
                    return new SlotDisplay.Composite(variants);
                }
            }
        }
        return new SlotDisplay.ItemSlotDisplay(TCItems.ESSENTIA_CRYSTAL.get());
    }
}
