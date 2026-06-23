package com.leclowndu93150.thaumcraft.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class ThaumcraftServerConfig {
    public static final ModConfigSpec SPEC;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        SPEC = builder.build();
    }

    private ThaumcraftServerConfig() {}
}
