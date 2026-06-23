package com.leclowndu93150.thaumcraft.data.lang.fragments;

import net.neoforged.neoforge.common.data.LanguageProvider;

public final class TCLangGTools {
    private TCLangGTools() {}

    public static void register(LanguageProvider provider) {
        provider.add("item.thaumcraft.thaumometer", "Thaumometer");
        provider.add("item.thaumcraft.scribing_tools", "Scribing Tools");
        provider.add("item.thaumcraft.thaumometer.scan.todo", "Scanning is not yet wired");
    }
}
