package com.leclowndu93150.thaumcraft.data.lang.fragments;

import net.neoforged.neoforge.common.data.LanguageProvider;

public final class TCLangEPlants {
    private TCLangEPlants() {}

    public static void register(LanguageProvider provider) {
        provider.add("block.thaumcraft.shimmerleaf", "Shimmerleaf");
        provider.add("block.thaumcraft.cinderpearl", "Cinderpearl");
        provider.add("block.thaumcraft.vishroom", "Vishroom");
        provider.add("block.thaumcraft.grass_ambient", "Enchanted Grass");
        provider.add("biome.thaumcraft.magical_forest", "Magical Forest");
        provider.add("biome.thaumcraft.eerie", "Eerie");
        provider.add("biome.thaumcraft.eldritch", "Eldritch");
    }
}
