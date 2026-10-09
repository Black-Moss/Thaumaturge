package com.leclowndu93150.thaumaturge.content.recipe.label;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaCapabilities;
import com.leclowndu93150.thaumaturge.api.essentia.IItemEssentia;
import com.leclowndu93150.thaumaturge.content.item.LabelItem;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class LabelFilterRecipe extends CustomRecipe {

    public static final LabelFilterRecipe INSTANCE = new LabelFilterRecipe();

    public static final RecipeSerializer<LabelFilterRecipe> SERIALIZER = new RecipeSerializer<>(MapCodec.unit(INSTANCE), StreamCodec.unit(INSTANCE));

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return checkAndGetAspectFromInput(input) != null;
    }

    private @Nullable Holder<IAspect> checkAndGetAspectFromInput(CraftingInput input) {
        int labelCount = 0;
        int phialCount = 0;
        Holder<IAspect> found = null;
        for (int index = 0; index < input.size(); index++) {
            ItemStack stack = input.getItem(index);
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.is(TTItems.LABEL)) {
                labelCount++;
            } else if (stack.is(TTItems.PHIAL)) {
                phialCount++;
                found = firstAspectOf(stack);
                if (found == null) {
                    return null;
                }
            } else {
                return null;
            }
            if (labelCount > 1 || phialCount > 1) {
                return null;
            }
        }
        return labelCount == 1 ? found : null;
    }

    private static @Nullable Holder<IAspect> firstAspectOf(ItemStack phial) {
        IItemEssentia essentia = phial.getCapability(EssentiaCapabilities.CONTAINER);
        if (essentia == null || essentia.getAspects().isEmpty()) {
            return null;
        }
        return essentia.getAspects().entries().getFirst().aspect();
    }

    @Override
    public @NonNull ItemStack assemble(CraftingInput craftingInput) {
        Holder<IAspect> aspect = checkAndGetAspectFromInput(craftingInput);
        if (aspect == null)
            return ItemStack.EMPTY;
        return LabelItem.withAspect(aspect);
    }

    @Override
    public @NonNull RecipeSerializer<? extends CustomRecipe> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public @NonNull NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        NonNullList<ItemStack> remaining = NonNullList.create();
        for (ItemStack stack : input.items()) {
            remaining.add(remainderOf(stack));
        }
        return remaining;
    }

    private static ItemStack remainderOf(ItemStack stack) {
        if (stack.is(TTItems.PHIAL)) {
            return stack.copyWithCount(1);
        }
        ItemStackTemplate template = stack.getCraftingRemainder();
        return template == null ? ItemStack.EMPTY : template.create();
    }
}
