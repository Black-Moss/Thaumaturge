package com.leclowndu93150.thaumcraft.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class ThaumcraftCommonConfig {
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue WUSS_MODE;
    public static final ModConfigSpec.DoubleValue TAINT_SPREAD_RATE;
    public static final ModConfigSpec.IntValue TAINT_SPREAD_AREA;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("world");
        WUSS_MODE = builder.define("wussMode", false);
        TAINT_SPREAD_RATE = builder.defineInRange("taintSpreadRate", 100.0, 0.0, 100.0);
        TAINT_SPREAD_AREA = builder.defineInRange("taintSpreadArea", 32, 1, 128);
        builder.pop();
        SPEC = builder.build();
    }

    private ThaumcraftCommonConfig() {}
}
