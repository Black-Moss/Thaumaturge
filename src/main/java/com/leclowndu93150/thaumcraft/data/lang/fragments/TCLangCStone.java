package com.leclowndu93150.thaumcraft.data.lang.fragments;

import net.neoforged.neoforge.common.data.LanguageProvider;

public final class TCLangCStone {
    private TCLangCStone() {}

    public static void register(LanguageProvider provider) {
        provider.add("block.thaumcraft.stone_arcane", "Arcane Stone");
        provider.add("block.thaumcraft.stone_arcane_brick", "Arcane Stone Brick");
        provider.add("block.thaumcraft.stone_ancient", "Ancient Stone");
        provider.add("block.thaumcraft.stone_ancient_tile", "Ancient Stone Tile");
        provider.add("block.thaumcraft.stone_ancient_rock", "Ancient Rock");
        provider.add("block.thaumcraft.stone_ancient_glyphed", "Glyphed Stone");
        provider.add("block.thaumcraft.stone_ancient_doorway", "Ancient Barrier");
        provider.add("block.thaumcraft.stone_eldritch_tile", "Eldritch Stone");
        provider.add("block.thaumcraft.stone_porous", "Porous Stone");
        provider.add("block.thaumcraft.stairs_arcane", "Arcane Stone Stairs");
        provider.add("block.thaumcraft.stairs_arcane_brick", "Arcane Brick Stairs");
        provider.add("block.thaumcraft.stairs_ancient", "Ancient Stone Stairs");
    }
}
