package com.leclowndu93150.thaumcraft.content.aspect;

import com.leclowndu93150.thaumcraft.api.aspect.IAspectIndex;
import java.util.concurrent.atomic.AtomicReference;

public final class AspectIndexHolder {
    private static final AtomicReference<AspectIndex> CURRENT = new AtomicReference<>(AspectIndex.EMPTY);

    private AspectIndexHolder() {}

    public static IAspectIndex get() {
        return CURRENT.get();
    }

    public static AspectIndex getConcrete() {
        return CURRENT.get();
    }

    public static void set(AspectIndex index) {
        CURRENT.set(index);
    }
}
