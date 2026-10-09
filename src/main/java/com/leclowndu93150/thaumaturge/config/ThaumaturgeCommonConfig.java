package com.leclowndu93150.thaumaturge.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class ThaumaturgeCommonConfig {
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue WUSS_MODE;
    public static final ModConfigSpec.DoubleValue TAINT_SPREAD_RATE;
    public static final ModConfigSpec.IntValue TAINT_SPREAD_AREA;
    public static final ModConfigSpec.IntValue TAINT_FRONTIER_RATE;
    public static final ModConfigSpec.BooleanValue TAINT_FROM_FLUX;
    public static final ModConfigSpec.BooleanValue PHYSICAL_FLUX_AURA_FLOOR;
    public static final ModConfigSpec.BooleanValue PHYSICAL_FLUX_TAINT_OUTBREAKS;
    public static final ModConfigSpec.BooleanValue FLUX_PRESSURE_EVENTS;
    public static final ModConfigSpec.DoubleValue ENERGIZED_NODE_VIS_PER_POINT;
    public static final ModConfigSpec.IntValue CRIMSON_PORTAL_RARITY;
    public static final ModConfigSpec.DoubleValue WILD_NODE_CHANCE;
    public static final ModConfigSpec.DoubleValue MAGICAL_NODE_CHANCE;
    public static final ModConfigSpec.DoubleValue EERIE_NODE_CHANCE;
    public static final ModConfigSpec.DoubleValue NETHER_NODE_CHANCE;
    public static final ModConfigSpec.DoubleValue DARK_NODE_CHANCE;
    public static final ModConfigSpec.DoubleValue UNSTABLE_NODE_CHANCE;
    public static final ModConfigSpec.DoubleValue PURE_NODE_CHANCE;
    public static final ModConfigSpec.DoubleValue TAINTED_NODE_CHANCE;
    public static final ModConfigSpec.DoubleValue HUNGRY_NODE_CHANCE;
    public static final ModConfigSpec.IntValue HUNGRY_NODE_BLOCK_EAT_RANGE;
    public static final ModConfigSpec.BooleanValue SCALE_HUNGRY_NODE_RANGE_BY_MODIFIER;
    public static final ModConfigSpec.IntValue HUNGRY_NODE_MINIMUM_BLOCK_EAT_RANGE;
    public static final ModConfigSpec.IntValue HUNGRY_NODE_MAXIMUM_BLOCK_EAT_RANGE;
    public static final ModConfigSpec.DoubleValue HUNGRY_NODE_BLOCK_HARDNESS;
    public static final ModConfigSpec.IntValue HUNGRY_NODE_BLOCK_EAT_INTERVAL;
    public static final ModConfigSpec.IntValue SHIELD_RECHARGE;
    public static final ModConfigSpec.IntValue SHIELD_WAIT;
    public static final ModConfigSpec.DoubleValue SHIELD_COST;
    public static final ModConfigSpec.BooleanValue ALLOW_CHAMPION_MOBS;
    public static final ModConfigSpec.BooleanValue NO_SLEEP;
    public static final ModConfigSpec.BooleanValue NO_STRESS;
    public static final ModConfigSpec.BooleanValue SHOW_GOLEM_EMOTES;
    public static final ModConfigSpec.IntValue FLUX_SCRUBBER_CHARGES_PER_ROLL;
    public static final ModConfigSpec.DoubleValue FLUX_SCRUBBER_ESSENTIA_CHANCE;
    public static final ModConfigSpec.IntValue FLUX_SCRUBBER_ESSENTIA_PER_ROLL;
    public static final ModConfigSpec.IntValue FLUX_SCRUBBER_ESSENTIA_CAPACITY;

    private static final double PERCENT_MAX = 100.0;
    private static final double TYPE_CHANCE_THIRD = 5.0 / 3.0;
    private static final double NODE_SPAWN_DEFAULT = 100.0 / 36.0;
    private static final int TICKS_MAX = 12000;
    private static final int EAT_RANGE_MIN = 1;
    private static final int EAT_RANGE_MAX = 64;

    private static ModConfigSpec.BooleanValue flag(ModConfigSpec.Builder b, String key, boolean initial, String... text) {
        return b.comment(text).define(key, initial);
    }

    private static ModConfigSpec.IntValue whole(ModConfigSpec.Builder b, String key, int initial, int low, int high, String... text) {
        return b.comment(text).defineInRange(key, initial, low, high);
    }

    private static ModConfigSpec.DoubleValue decimal(ModConfigSpec.Builder b, String key, double initial, double low, double high, String... text) {
        return b.comment(text).defineInRange(key, initial, low, high);
    }

    private static ModConfigSpec.DoubleValue percent(ModConfigSpec.Builder b, String key, double initial, String... text) {
        return decimal(b, key, initial, 0.0, PERCENT_MAX, text);
    }

    private static ModConfigSpec.IntValue eatRange(ModConfigSpec.Builder b, String key, int initial, String... text) {
        return whole(b, key, initial, EAT_RANGE_MIN, EAT_RANGE_MAX, text);
    }

    static {
        ModConfigSpec.Builder spec = new ModConfigSpec.Builder();
        spec.push("world");
        WUSS_MODE = flag(spec, "wussMode", false, "Setting this to true disables Warp, Taint spread and similar mechanics. You wuss.");
        TAINT_SPREAD_RATE = percent(spec, "taintSpreadRate", PERCENT_MAX, "The % chance of taint fibres spreading on a block tick. Setting this to 0 will effectively stop taint fibre spread.");
        TAINT_SPREAD_AREA = whole(spec, "taintSpreadArea", 32, 1, 128, "The range at which taint can spread from a taint seed. This value is only a base and will be modified by flux levels.");
        TAINT_FRONTIER_RATE = whole(spec, "taintFrontierRate", 200, 0, 100000,
                "How quickly the Tainted Lands biome spreads outward, Thaumcraft 4 style. Fibrous taint tries to take over a neighbouring column with a chance of 1 in (taintFrontierRate * 5) per random tick, and only with at least two adjacent taint blocks. Higher is slower. 0 stops the biome from spreading while existing taint stays active.");
        TAINT_FROM_FLUX = flag(spec, "taintFromFlux", true, "Whether deep, exposed Flux Goo can fester into Fibrous Taint and Tainted Lands, as in Thaumcraft 4.");
        PHYSICAL_FLUX_AURA_FLOOR = flag(spec, "physicalFluxAuraFloor", true,
                "Whether physical Flux Goo and Flux Gas keep a capped minimum of Aura Flux in their chunk. Turning this off leaves the goo and gas and their direct effects in place.");
        PHYSICAL_FLUX_TAINT_OUTBREAKS = flag(spec, "physicalFluxTaintOutbreaks", true,
                "Whether a large build-up of physical Flux Goo and Flux Gas in one area can start a Tainted Lands outbreak on its own, apart from single deep goo blocks festering. Has no effect while taintFromFlux is off.");
        FLUX_PRESSURE_EVENTS = flag(spec, "fluxPressureEvents", true,
                "Whether high Aura Flux can trigger the Thaumcraft 5 style flux pressure events alongside Flux Rifts. Turning this off does not disable Flux Rifts.");
        ENERGIZED_NODE_VIS_PER_POINT = decimal(spec, "energizedNodeVisPerPoint", 6.0, 0.0, PERCENT_MAX,
                "Raw vis an energized node drains from the chunk aura to restore one aspect point. Normal nodes refine at 3.0 per point; higher values make energized nodes more wasteful. 0 makes their refill free.");
        CRIMSON_PORTAL_RARITY = whole(spec, "crimsonPortalRarity", 500, 0, 1000000, "Average number of chunks per wild lesser crimson portal. Higher is rarer. 0 disables wild portals entirely.");
        spec.push("nodes");
        WILD_NODE_CHANCE = percent(spec, "wildSpawnChance", NODE_SPAWN_DEFAULT,
                "Chance from 0 to 100 for a wild node placement attempt in each Overworld chunk. The default is about one attempt per 36 chunks. 0 disables this source.");
        MAGICAL_NODE_CHANCE = percent(spec, "magicalBonusSpawnChance", 0.0,
                "Optional additional chance from 0 to 100 in each Magical Forest chunk. This stacks with wildSpawnChance. Disabled by default to match TC4.");
        EERIE_NODE_CHANCE = percent(spec, "eerieBonusSpawnChance", 12.5,
                "Additional chance from 0 to 100 in each Eerie biome chunk. This stacks with wildSpawnChance and its node is always dark. 12.5 means one attempt per 8 chunks.");
        NETHER_NODE_CHANCE = percent(spec, "netherSpawnChance", NODE_SPAWN_DEFAULT,
                "Chance from 0 to 100 for a node placement attempt in each Nether chunk. The default is about one attempt per 36 chunks. 0 disables Nether nodes.");
        spec.comment(
                "The following values are percentages among ordinary random nodes. At the default special_rarity of 18, their total is 6.6667%, leaving 93.3333% normal nodes. Datapack special_rarity scales these percentages by 18 / special_rarity. If the scaled total exceeds 100, it is normalized and no normal nodes spawn. Silverwood and eerie nodes keep their forced types.")
                .push("types");
        DARK_NODE_CHANCE = percent(spec, "darkChance", TYPE_CHANCE_THIRD, "Dark-node percentage, from 0 to 100. Default: 1.6667%.");
        UNSTABLE_NODE_CHANCE = percent(spec, "unstableChance", TYPE_CHANCE_THIRD, "Unstable-node percentage, from 0 to 100. Default: 1.6667%.");
        PURE_NODE_CHANCE = percent(spec, "pureChance", TYPE_CHANCE_THIRD, "Pure-node percentage, from 0 to 100. Default: 1.6667%.");
        TAINTED_NODE_CHANCE = percent(spec, "taintedChance", 10.0 / 9.0,
                "Tainted-node percentage, from 0 to 100. Default: 1.1111%, Thaumcraft 5's effective natural tainted-node chance. Wuss mode turns this type off.");
        HUNGRY_NODE_CHANCE = percent(spec, "hungryChance", 5.0 / 9.0, "Hungry-node percentage, from 0 to 100. Default: 0.5556%, approximately one hungry node per 180 ordinary nodes.");
        spec.pop(2);

        HUNGRY_NODE_BLOCK_EAT_RANGE = eatRange(spec, "hungryNodeBlockEatRange", 16, "Maximum length in blocks of a hungry node's random block-eating ray.",
                "Default: 16. Range: 1 to 64. A larger area gives each attempt more possible targets; it does not guarantee a distant block will be selected.",
                "Dropped items are pulled from this range plus half a block. The entity pulling range is not affected.");
        SCALE_HUNGRY_NODE_RANGE_BY_MODIFIER = flag(spec, "scaleHungryNodeBlockEatRangeByModifier", false, "Whether a hungry node's block-eating range scales with its current modifier (quality).",
                "False: hungryNodeBlockEatRange is always used. True: the minimum/maximum settings below override hungryNodeBlockEatRange.",
                "Fading uses the minimum, pale and normal are evenly spaced between them, and bright uses the maximum.");
        HUNGRY_NODE_MINIMUM_BLOCK_EAT_RANGE = eatRange(spec, "hungryNodeMinimumBlockEatRange", 16, "Block-eating range of a fading hungry node when modifier scaling is enabled.",
                "Default: 16. Range: 1 to 64. This setting does nothing while scaleHungryNodeBlockEatRangeByModifier is false.");
        HUNGRY_NODE_MAXIMUM_BLOCK_EAT_RANGE = eatRange(spec, "hungryNodeMaximumBlockEatRange", 32, "Block-eating range of a bright hungry node when modifier scaling is enabled.",
                "Default: 32. Range: 1 to 64. This overrides hungryNodeBlockEatRange while scaling is enabled.", "If set below the minimum range, the minimum is used for every modifier.");
        HUNGRY_NODE_BLOCK_HARDNESS = percent(spec, "hungryNodeBlockEatHardness", 5.0, "Maximum block hardness a hungry node can eat. The comparison is strictly below this value, not equal to it.",
                "Examples: dirt 0.5, stone 1.5, most logs 2, ores/deepslate 3, iron and diamond blocks 5, obsidian 50.",
                "Default: 5.0, so it can eat ordinary terrain and ores, but not hardness-5 metal/gem blocks or obsidian.",
                "Range: 0 to 100. 0 disables block destruction. Unbreakable blocks such as bedrock have negative hardness and are never eaten.");
        HUNGRY_NODE_BLOCK_EAT_INTERVAL = whole(spec, "hungryNodeBlockEatInterval", 50, 1, TICKS_MAX,
                "Ticks between hungry-node block-eating attempts. Minecraft normally runs at 20 ticks per second; lower values are faster.",
                "Examples: 1 = 20 attempts/second, 20 = once/second, 50 = once/2.5 seconds, 1200 = once/minute.",
                "Default: 50. Range: 1 to 12000. An attempt may miss or hit an ineligible block, so this is not a guaranteed destruction interval.");
        SHIELD_RECHARGE = whole(spec, "shieldRecharge", 40, 1, TICKS_MAX, "Ticks between each point of runic shielding recharge.");
        SHIELD_WAIT = whole(spec, "shieldWait", 80, 0, TICKS_MAX, "Ticks runic shielding waits before recharging after being fully depleted.");
        SHIELD_COST = percent(spec, "shieldCost", 1.0, "Vis drained from the local aura per point of runic shielding recharged. 0 makes recharging free.");
        ALLOW_CHAMPION_MOBS = flag(spec, "allowChampionMobs", true, "Setting this to false will disable spawning champion mobs.");
        NO_SLEEP = flag(spec, "noSleep", false, "Setting this to true will make you get the recipe book for salis mundus without having to sleep first.");
        spec.pop();

        spec.push("sounds");
        NO_STRESS = flag(spec, "nostress", false, "Set to true to disable anxiety triggers like the heartbeat sound and warp-event jump scares.");
        spec.pop();

        spec.push("golems");
        SHOW_GOLEM_EMOTES = flag(spec, "showGolemEmotes", true, "Will golems display emote particles if they receive orders or encounter problems.");
        spec.pop();

        spec.push("fluxScrubber");
        FLUX_SCRUBBER_CHARGES_PER_ROLL = whole(spec, "chargesPerRoll", 4, 1, 64,
                "Physical Flux quanta (Goo or Gas) the Flux Scrubber must clean before it rolls for Praecantatio. Lower values recover Praecantatio faster.");
        FLUX_SCRUBBER_ESSENTIA_CHANCE = decimal(spec, "essentiaChance", 0.25, 0.0, 1.0, "Chance (0 to 1) that a roll succeeds and yields Praecantatio. 1.0 always succeeds.");
        FLUX_SCRUBBER_ESSENTIA_PER_ROLL = whole(spec, "essentiaPerRoll", 1, 0, 64,
                "Praecantatio produced per successful roll. Raise essentiaCapacity so large rolls can build up before a tube drains them.");
        FLUX_SCRUBBER_ESSENTIA_CAPACITY = whole(spec, "essentiaCapacity", 4, 1, 1024, "Most Praecantatio the Flux Scrubber holds before an attached tube or jar has to drain it.");
        spec.pop();
        SPEC = spec.build();
    }

    private ThaumaturgeCommonConfig() {}
}
