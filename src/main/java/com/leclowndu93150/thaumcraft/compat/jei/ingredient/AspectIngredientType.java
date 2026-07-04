package com.leclowndu93150.thaumcraft.compat.jei.ingredient;

import com.leclowndu93150.thaumcraft.api.aspect.AspectInstance;
import com.leclowndu93150.thaumcraft.api.aspect.IAspect;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import net.minecraft.core.Holder;

public final class AspectIngredientType implements IIngredientTypeWithSubtypes<Holder<IAspect>, AspectInstance> {
    public static final AspectIngredientType INSTANCE = new AspectIngredientType();
    public static final String UID = "thaumcraft:aspect";

    private AspectIngredientType() {}

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends Holder<IAspect>> getIngredientBaseClass() {
        return (Class<? extends Holder<IAspect>>) (Class<?>) Holder.class;
    }

    @Override
    public Holder<IAspect> getBase(AspectInstance ingredient) {
        return ingredient.aspect();
    }

    @Override
    public AspectInstance getDefaultIngredient(Holder<IAspect> base) {
        return new AspectInstance(base,1);
    }

    @Override
    public Class<? extends AspectInstance> getIngredientClass() {
        return AspectInstance.class;
    }

    @Override
    public String getUid() {
        return UID;
    }
}
