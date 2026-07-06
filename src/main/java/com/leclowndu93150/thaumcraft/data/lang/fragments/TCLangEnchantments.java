package com.leclowndu93150.thaumcraft.data.lang.fragments;

import net.neoforged.neoforge.common.data.LanguageProvider;

public final class TCLangEnchantments {
    private TCLangEnchantments() {}

    public static void register(LanguageProvider provider) {
        provider.add("enchantment.thaumcraft.collector", "Collector");
        provider.add("enchantment.thaumcraft.destructive", "Destructive");
        provider.add("enchantment.thaumcraft.burrowing", "Burrowing");
        provider.add("enchantment.thaumcraft.sounding", "Sounding");
        provider.add("enchantment.thaumcraft.refining", "Refining");
        provider.add("enchantment.thaumcraft.arcing", "Arcing");
        provider.add("enchantment.thaumcraft.essence", "Essence Harvester");
        provider.add("enchantment.thaumcraft.lamplight", "Lamplighter");
        provider.add("block.thaumcraft.effect_glimmer", "Glimmer");
    }
}
