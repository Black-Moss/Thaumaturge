package com.leclowndu93150.thaumaturge.content.research.decon;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import net.minecraft.core.Holder;

public final class AspectPrimals {
    private AspectPrimals() {}

    public static AspectList reduce(AspectList aspects) {
        AspectList result = AspectList.EMPTY;
        for (AspectInstance instance : aspects.entries()) {
            result = reduceInto(result, instance.aspect(), instance.amount());
        }
        return result;
    }

    private static AspectList reduceInto(AspectList result, Holder<IAspect> aspect, int amount) {
        if (aspect.value().isPrimal()) {
            return result.add(aspect, amount);
        }
        for (Holder<IAspect> component : aspect.value().components()) {
            result = reduceInto(result, component, amount);
        }
        return result;
    }
}
