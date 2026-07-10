package com.leclowndu93150.thaumcraft.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class ThaumcraftCommonConfig {
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue WUSS_MODE;
    public static final ModConfigSpec.DoubleValue TAINT_SPREAD_RATE;
    public static final ModConfigSpec.IntValue TAINT_SPREAD_AREA;
    public static final ModConfigSpec.BooleanValue ALLOW_CHAMPION_MOBS;
    public static final ModConfigSpec.BooleanValue NO_SLEEP;
    public static final ModConfigSpec.BooleanValue NO_STRESS;
    public static final ModConfigSpec.BooleanValue SHOW_GOLEM_EMOTES;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("world");
        WUSS_MODE = builder
                .comment("Setting this to true disables Warp, Taint spread and similar mechanics. You wuss.")
                .define("wussMode", false);
        TAINT_SPREAD_RATE = builder
                .comment("The % chance of taint fibres spreading on a block tick. Setting this to 0 will effectively stop taint fibre spread.")
                .defineInRange("taintSpreadRate", 100.0, 0.0, 100.0);
        TAINT_SPREAD_AREA = builder
                .comment("The range at which taint can spread from a taint seed. This value is only a base and will be modified by flux levels.")
                .defineInRange("taintSpreadArea", 32, 1, 128);
        ALLOW_CHAMPION_MOBS = builder
                .comment("Setting this to false will disable spawning champion mobs.")
                .define("allowChampionMobs", true);
        NO_SLEEP = builder
                .comment("Setting this to true will make you get the recipe book for salis mundus without having to sleep first.")
                .define("noSleep", false);
        builder.pop();
        builder.push("sounds");
        NO_STRESS = builder
                .comment("Set to true to disable anxiety triggers like the heartbeat sound and warp-event jump scares.")
                .define("nostress", false);
        builder.pop();
        builder.push("golems");
        SHOW_GOLEM_EMOTES = builder
                .comment("Will golems display emote particles if they receive orders or encounter problems.")
                .define("showGolemEmotes", true);
        builder.pop();
        SPEC = builder.build();
    }

    private ThaumcraftCommonConfig() {}
}
