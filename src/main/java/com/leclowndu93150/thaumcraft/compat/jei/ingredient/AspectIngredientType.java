package com.leclowndu93150.thaumcraft.compat.jei.ingredient;

import com.leclowndu93150.thaumcraft.api.aspect.IAspect;
import mezz.jei.api.ingredients.IIngredientType;
import net.minecraft.core.Holder;

public final class AspectIngredientType implements IIngredientType<Holder<IAspect>> {
    public static final AspectIngredientType INSTANCE = new AspectIngredientType();
    public static final String UID = "thaumcraft:aspect";

    private AspectIngredientType() {}

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends Holder<IAspect>> getIngredientClass() {
        return (Class<? extends Holder<IAspect>>) (Class<?>) Holder.class;
    }

    @Override
    public String getUid() {
        return UID;
    }
}
