package com.leclowndu93150.thaumcraft.content.research.pool;

import com.leclowndu93150.thaumcraft.api.aspect.IAspect;
import java.util.function.Predicate;
import net.minecraft.core.Holder;

public final class AspectDiscoveryView {
    private static Predicate<Holder<IAspect>> view = aspect -> true;

    private AspectDiscoveryView() {}

    public static void bind(Predicate<Holder<IAspect>> predicate) {
        view = predicate;
    }

    public static boolean isDiscovered(Holder<IAspect> aspect) {
        return view.test(aspect);
    }
}
