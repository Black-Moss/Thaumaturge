package com.leclowndu93150.thaumcraft.data.lang.fragments;

import net.neoforged.neoforge.common.data.LanguageProvider;

public final class TCLangHContainers {
    private TCLangHContainers() {}

    public static void register(LanguageProvider provider) {
        provider.add("item.thaumcraft.phial.empty", "Glass Phial");
        provider.add("item.thaumcraft.phial.filled", "Phial of %1$s Essential");
        provider.add("item.thaumcraft.primordial_pearl.pearl", "Primordial Pearl");
        provider.add("item.thaumcraft.primordial_pearl.nodule", "Primordial Nodule");
        provider.add("item.thaumcraft.primordial_pearl.mote", "Primordial Mote");
    }
}
