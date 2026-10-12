package com.leclowndu93150.thaumaturge.client.golem;

import com.leclowndu93150.thaumaturge.api.client.golems.GolemPartRenderHook;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemPartModel;
import java.util.Map;
import net.minecraft.resources.Identifier;

public final class GolemPartRenderHooks {
    private static volatile Map<Identifier, GolemPartRenderHook> hooks = Map.of();

    private GolemPartRenderHooks() {}

    static void install(Map<Identifier, GolemPartRenderHook> registered) {
        hooks = Map.copyOf(registered);
    }

    public static GolemPartRenderHook hookFor(GolemPartModel model) {
        return hooks.getOrDefault(model.objModel(), Unhooked.INSTANCE);
    }

    private enum Unhooked implements GolemPartRenderHook {
        INSTANCE
    }
}
