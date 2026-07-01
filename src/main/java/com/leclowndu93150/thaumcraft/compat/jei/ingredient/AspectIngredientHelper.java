package com.leclowndu93150.thaumcraft.compat.jei.ingredient;

import com.leclowndu93150.thaumcraft.api.aspect.AspectComponents;
import com.leclowndu93150.thaumcraft.api.aspect.IAspect;
import com.leclowndu93150.thaumcraft.content.item.PhialItem;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;

public final class AspectIngredientHelper implements IIngredientHelper<Holder<IAspect>> {
    public static final AspectIngredientHelper INSTANCE = new AspectIngredientHelper();

    private static final Identifier UNKNOWN = Identifier.fromNamespaceAndPath("thaumcraft", "unknown");

    private AspectIngredientHelper() {}

    @Override
    public IIngredientType<Holder<IAspect>> getIngredientType() {
        return AspectIngredientType.INSTANCE;
    }

    @Override
    public String getDisplayName(Holder<IAspect> ingredient) {
        return AspectComponents.name(ingredient).getString();
    }

    @Override
    public Object getUid(Holder<IAspect> ingredient, UidContext context) {
        return ingredient.unwrapKey()
                .map(ResourceKey::identifier)
                .orElse(UNKNOWN);
    }

    @Override
    public Identifier getIdentifier(Holder<IAspect> ingredient) {
        return ingredient.unwrapKey()
                .map(ResourceKey::identifier)
                .orElse(UNKNOWN);
    }

    @Override
    public Holder<IAspect> copyIngredient(Holder<IAspect> ingredient) {
        return ingredient;
    }

    @Override
    public String getErrorInfo(Holder<IAspect> ingredient) {
        if (ingredient == null) {
            return "null";
        }
        return ingredient.unwrapKey()
                .map(key -> key.identifier().toString())
                .orElse("unbound aspect holder");
    }

    @Override
    public boolean isValidIngredient(Holder<IAspect> ingredient) {
        return ingredient != null && ingredient.isBound();
    }

    @Override
    public ItemStack getCheatItemStack(Holder<IAspect> ingredient) {
        return PhialItem.makeFilled(ingredient);
    }
}
