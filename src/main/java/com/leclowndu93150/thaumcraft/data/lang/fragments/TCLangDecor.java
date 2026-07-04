package com.leclowndu93150.thaumcraft.data.lang.fragments;

import net.minecraft.world.item.DyeColor;
import net.neoforged.neoforge.common.data.LanguageProvider;
import org.apache.commons.lang3.StringUtils;

public final class TCLangDecor {
    private TCLangDecor() {}

    public static void register(LanguageProvider provider) {
        provider.add("item.thaumcraft.tallow", "Magic Tallow");
        for (DyeColor dye : DyeColor.values()) {
            provider.add("block.thaumcraft.candle_" + dye.getName(), dyeName(dye) + " Tallow Candle");
            provider.add("block.thaumcraft.banner_" + dye.getName(), dyeName(dye) + " Banner");
            provider.add("block.thaumcraft.wall_banner_" + dye.getName(), dyeName(dye) + " Banner");
        }
        provider.add("block.thaumcraft.banner_crimson_cult", "Crimson Cult Banner");
        provider.add("block.thaumcraft.wall_banner_crimson_cult", "Crimson Cult Banner");
    }

    private static String dyeName(DyeColor dye) {
        String[] parts = dye.getName().split("_");
        StringBuilder name = new StringBuilder();
        for (String part : parts) {
            if (!name.isEmpty()) {
                name.append(' ');
            }
            name.append(StringUtils.capitalize(part));
        }
        return name.toString();
    }
}
