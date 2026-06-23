package com.leclowndu93150.thaumcraft;

import net.minecraft.resources.Identifier;

public final class TCIds {
    public static final String MODID = "thaumcraft";

    private TCIds() {}

    public static Identifier rl(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }
}
