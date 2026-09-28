package com.leclowndu93150.thaumaturge.compat.iris;

import net.irisshaders.iris.api.v0.IrisApi;
import net.irisshaders.iris.pathways.HandRenderer;

public final class IrisHandPass {
    private IrisHandPass() {}

    static boolean isSolidHandPass() {
        return IrisApi.getInstance().isShaderPackInUse() && HandRenderer.INSTANCE.isActive() && HandRenderer.INSTANCE.isRenderingSolid();
    }
}
