package com.leclowndu93150.thaumcraft.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class ThaumcraftClientConfig {
    public static final ModConfigSpec SPEC;

    private static final ModConfigSpec.BooleanValue SHOW_ASPECTS_BY_DEFAULT;
    private static final ModConfigSpec.BooleanValue LARGE_TAG_TEXT;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        SHOW_ASPECTS_BY_DEFAULT = builder
                .comment("When true, item aspects render on tooltips by default and Shift hides them. When false, Shift reveals them.")
                .define("tooltip.show_aspects_by_default", false);

        LARGE_TAG_TEXT = builder
                .comment("When true, aspect tag amount and bonus counters render at full font size. When false, they render half-scale to fit narrow GUIs (TC default).")
                .define("graphics.large_tag_text", false);

        SPEC = builder.build();
    }

    private ThaumcraftClientConfig() {}

    public static boolean showAspectsByDefault() {
        return SHOW_ASPECTS_BY_DEFAULT.get();
    }

    public static boolean largeTagText() {
        return LARGE_TAG_TEXT.get();
    }
}
