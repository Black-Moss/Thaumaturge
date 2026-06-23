package com.leclowndu93150.thaumcraft.data.lang.fragments;

import net.neoforged.neoforge.common.data.LanguageProvider;

public final class TCLangMAuraHud {
    private TCLangMAuraHud() {}

    public static void register(LanguageProvider provider) {
        provider.add("item.thaumcraft.goggles_revealing", "Goggles of Revealing");
        provider.add("hud.thaumcraft.aura.title", "Aura");
        provider.add("hud.thaumcraft.aura.vis", "Vis: %1$s");
        provider.add("hud.thaumcraft.aura.flux", "Flux: %1$s");
        provider.add("hud.thaumcraft.aura.base", "Base: %1$s");
    }
}
