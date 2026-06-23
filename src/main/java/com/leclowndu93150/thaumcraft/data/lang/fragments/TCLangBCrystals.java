package com.leclowndu93150.thaumcraft.data.lang.fragments;

import net.neoforged.neoforge.common.data.LanguageProvider;

public final class TCLangBCrystals {
    private TCLangBCrystals() {}

    public static void register(LanguageProvider provider) {
        provider.add("block.thaumcraft.crystal_aer", "Air Crystal");
        provider.add("block.thaumcraft.crystal_ignis", "Fire Crystal");
        provider.add("block.thaumcraft.crystal_aqua", "Water Crystal");
        provider.add("block.thaumcraft.crystal_terra", "Earth Crystal");
        provider.add("block.thaumcraft.crystal_ordo", "Order Crystal");
        provider.add("block.thaumcraft.crystal_perditio", "Entropy Crystal");
        provider.add("block.thaumcraft.crystal_vitium", "Flux Crystal");
    }
}
