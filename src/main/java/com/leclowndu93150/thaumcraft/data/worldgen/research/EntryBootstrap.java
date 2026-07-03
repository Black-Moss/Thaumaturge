package com.leclowndu93150.thaumcraft.data.worldgen.research;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.api.capability.KnowledgeType;
import com.leclowndu93150.thaumcraft.api.research.IResearchCategory;
import com.leclowndu93150.thaumcraft.api.research.IResearchEntry;
import com.leclowndu93150.thaumcraft.api.research.IResearchStage;
import com.leclowndu93150.thaumcraft.api.research.KnowledgeReward;
import com.leclowndu93150.thaumcraft.api.research.ResearchEntryMeta;
import com.leclowndu93150.thaumcraft.api.research.ResearchIcon;
import com.leclowndu93150.thaumcraft.api.research.ResearchRequirement;
import com.leclowndu93150.thaumcraft.api.research.TCResearchCategories;
import com.leclowndu93150.thaumcraft.content.research.ResearchEntry;
import com.leclowndu93150.thaumcraft.content.research.ResearchStage;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public final class EntryBootstrap {
    private EntryBootstrap() {}

    private static final Identifier ROOT_BASICS = id("basics_root");
    private static final Identifier CELESTIAL_SCANNING = id("celestial_scanning");
    private static final Identifier FLUX = id("flux");
    private static final Identifier UNLOCK_AUROMANCY = id("unlock_auromancy");
    private static final Identifier UNLOCK_ALCHEMY = id("unlock_alchemy");
    private static final Identifier UNLOCK_ARTIFICE = id("unlock_artifice");
    private static final Identifier UNLOCK_INFUSION = id("unlock_infusion");
    private static final Identifier UNLOCK_GOLEMANCY = id("unlock_golemancy");
    private static final Identifier UNLOCK_ELDRITCH = id("unlock_eldritch");

    public static void bootstrap(BootstrapContext<IResearchEntry> ctx) {
        Holder<IResearchCategory> basics = ctx.lookup(IResearchCategory.REGISTRY_KEY).getOrThrow(TCResearchCategories.BASICS);
        Holder<IResearchCategory> auromancy = ctx.lookup(IResearchCategory.REGISTRY_KEY).getOrThrow(TCResearchCategories.AUROMANCY);
        Holder<IResearchCategory> alchemy = ctx.lookup(IResearchCategory.REGISTRY_KEY).getOrThrow(TCResearchCategories.ALCHEMY);
        Holder<IResearchCategory> artifice = ctx.lookup(IResearchCategory.REGISTRY_KEY).getOrThrow(TCResearchCategories.ARTIFICE);
        Holder<IResearchCategory> infusion = ctx.lookup(IResearchCategory.REGISTRY_KEY).getOrThrow(TCResearchCategories.INFUSION);
        Holder<IResearchCategory> golemancy = ctx.lookup(IResearchCategory.REGISTRY_KEY).getOrThrow(TCResearchCategories.GOLEMANCY);
        Holder<IResearchCategory> eldritch = ctx.lookup(IResearchCategory.REGISTRY_KEY).getOrThrow(TCResearchCategories.ELDRITCH);

        registerBasics(ctx, basics);
        registerAuromancy(ctx, auromancy);
        registerAlchemy(ctx, alchemy);
        registerArtifice(ctx, artifice);
        registerInfusion(ctx, infusion);
        registerGolemancy(ctx, golemancy);
        registerEldritch(ctx, eldritch);
    }

    private static void registerBasics(BootstrapContext<IResearchEntry> ctx, Holder<IResearchCategory> basics) {
        register(ctx, ROOT_BASICS, basics, 0, 0,
                meta(ResearchEntryMeta.ROUND, ResearchEntryMeta.AUTOUNLOCK),
                stages(stage(
                        "research.thaumcraft.basics_root.stage_0",
                        List.of(),
                        List.of(req(Items.PAPER, 4)),
                        List.of(),
                        List.of(reward(KnowledgeType.OBSERVATION, basics, 8)),
                        List.of(),
                        0)));

        register(ctx, UNLOCK_AUROMANCY, basics, -2, -2,
                Set.of(ROOT_BASICS),
                meta(),
                stages(stage(
                        "research.thaumcraft.unlock_auromancy.stage_0",
                        List.of(),
                        List.of(req(Items.GLOWSTONE_DUST, 2)),
                        List.of(),
                        List.of(reward(KnowledgeType.OBSERVATION, basics, 4)),
                        List.of(),
                        0)));

        register(ctx, UNLOCK_ALCHEMY, basics, 2, -2,
                Set.of(ROOT_BASICS),
                meta(),
                stages(stage(
                        "research.thaumcraft.unlock_alchemy.stage_0",
                        List.of(),
                        List.of(req(Items.NETHER_WART, 2)),
                        List.of(),
                        List.of(reward(KnowledgeType.OBSERVATION, basics, 4)),
                        List.of(),
                        0)));

        register(ctx, UNLOCK_ARTIFICE, basics, 0, 2,
                Set.of(ROOT_BASICS),
                meta(),
                stages(stage(
                        "research.thaumcraft.unlock_artifice.stage_0",
                        List.of(),
                        List.of(req(Items.REDSTONE, 4)),
                        List.of(),
                        List.of(reward(KnowledgeType.OBSERVATION, basics, 4)),
                        List.of(),
                        0)));

        register(ctx, UNLOCK_INFUSION, basics, -2, 2,
                Set.of(UNLOCK_AUROMANCY, UNLOCK_ALCHEMY),
                meta(ResearchEntryMeta.SPIKY),
                stages(stage(
                        "research.thaumcraft.unlock_infusion.stage_0",
                        List.of(),
                        List.of(req(Items.DIAMOND, 1)),
                        List.of(),
                        List.of(reward(KnowledgeType.THEORY, basics, 2)),
                        List.of(),
                        0)));

        register(ctx, UNLOCK_GOLEMANCY, basics, 2, 2,
                Set.of(UNLOCK_ARTIFICE),
                meta(),
                stages(stage(
                        "research.thaumcraft.unlock_golemancy.stage_0",
                        List.of(),
                        List.of(req(Items.CLAY_BALL, 4)),
                        List.of(),
                        List.of(reward(KnowledgeType.OBSERVATION, basics, 4)),
                        List.of(),
                        0)));

        register(ctx, UNLOCK_ELDRITCH, basics, 0, 4,
                Set.of(UNLOCK_INFUSION),
                meta(ResearchEntryMeta.HEX, ResearchEntryMeta.HIDDEN),
                stages(stage(
                        "research.thaumcraft.unlock_eldritch.stage_0",
                        List.of(),
                        List.of(req(Items.ENDER_EYE, 1)),
                        List.of(),
                        List.of(reward(KnowledgeType.THEORY, basics, 4)),
                        List.of(),
                        4)));

        register(ctx, CELESTIAL_SCANNING, basics, 4, 1,
                Set.of(ROOT_BASICS),
                meta(ResearchEntryMeta.ROUND),
                stages(stage(
                        "research.thaumcraft.celestial_scanning.stage_0",
                        List.of(),
                        List.of(req(Items.PAPER, 1)),
                        List.of(),
                        List.of(reward(KnowledgeType.OBSERVATION, basics, 2)),
                        List.of(),
                        0)),
                researchTexture("r_celestial"));

        register(ctx, FLUX, basics, -4, -2,
                Set.of(),
                meta(ResearchEntryMeta.ROUND, ResearchEntryMeta.HIDDEN),
                stages(stage(
                        "research.thaumcraft.flux.stage_0",
                        List.of(),
                        List.of(),
                        List.of(),
                        List.of(),
                        List.of(),
                        0)),
                researchTexture("r_flux"));

        registerScanEntries(ctx, basics);
    }

    private static void registerScanEntries(BootstrapContext<IResearchEntry> ctx, Holder<IResearchCategory> basics) {
        scanEntry(ctx, basics, "wisp", -7, 0, "r_wisp");
        scanEntry(ctx, basics, "pech", -8, 0, "r_pech");
        scanEntry(ctx, basics, "thaumic_slime", -9, 0, "r_thaumslime");
        scanEntry(ctx, basics, "firebat", -10, 0, "r_firebat");
        scanEntry(ctx, basics, "taint_seed", -7, 1, "r_taintseed");
        scanEntry(ctx, basics, "taint_crawler", -8, 1, "r_taintcrawler");
        scanEntry(ctx, basics, "taintacle", -9, 1, "r_taintacle");
        scanEntry(ctx, basics, "taint_swarm", -10, 1, "r_taintswarm");
        scanEntry(ctx, basics, "cultist", -7, 2, "r_cultist");
        scanEntry(ctx, basics, "brainy_zombie", -8, 2, "r_angryzombie");
        scanEntry(ctx, basics, "eldritch_guardian", -9, 2, "r_eldritchguardian");
        scanEntry(ctx, basics, "eldritch_crab", -10, 2, "r_crab");
    }

    private static void scanEntry(BootstrapContext<IResearchEntry> ctx, Holder<IResearchCategory> basics,
                                  String entityPath, int column, int row, String iconName) {
        register(ctx, id("scanned/entity/thaumcraft/" + entityPath), basics, column, row,
                Set.of(),
                meta(ResearchEntryMeta.HEX, ResearchEntryMeta.HIDDEN),
                stages(stage(
                        "research.thaumcraft.scanned/entity/thaumcraft/" + entityPath + ".stage_0",
                        List.of(),
                        List.of(),
                        List.of(),
                        List.of(),
                        List.of(),
                        0)),
                researchTexture(iconName));
    }

    private static void registerAuromancy(BootstrapContext<IResearchEntry> ctx, Holder<IResearchCategory> auromancy) {
        register(ctx, id("aura_basics"), auromancy, 0, 0,
                meta(ResearchEntryMeta.ROUND),
                stages(stage(
                        "research.thaumcraft.aura_basics.stage_0",
                        List.of(),
                        List.of(req(Items.AMETHYST_SHARD, 2)),
                        List.of(),
                        List.of(reward(KnowledgeType.OBSERVATION, auromancy, 6)),
                        List.of(),
                        0)));

        register(ctx, id("vis_taps"), auromancy, 2, 0,
                Set.of(id("aura_basics")),
                meta(),
                stages(
                        stage(
                                "research.thaumcraft.vis_taps.stage_0",
                                List.of(),
                                List.of(req(Items.AMETHYST_BLOCK, 1)),
                                List.of(),
                                List.of(reward(KnowledgeType.OBSERVATION, auromancy, 4)),
                                List.of(),
                                0),
                        stage(
                                "research.thaumcraft.vis_taps.stage_1",
                                List.of(),
                                List.of(),
                                List.of(req(Items.LANTERN, 1)),
                                List.of(reward(KnowledgeType.THEORY, auromancy, 2)),
                                List.of(),
                                0)));
    }

    private static void registerAlchemy(BootstrapContext<IResearchEntry> ctx, Holder<IResearchCategory> alchemy) {
        register(ctx, id("crucible"), alchemy, 0, 0,
                meta(ResearchEntryMeta.ROUND),
                stages(stage(
                        "research.thaumcraft.crucible.stage_0",
                        List.of(),
                        List.of(req(Items.CAULDRON, 1)),
                        List.of(),
                        List.of(reward(KnowledgeType.OBSERVATION, alchemy, 6)),
                        List.of(),
                        0)));

        register(ctx, id("alembic"), alchemy, 2, 0,
                Set.of(id("crucible")),
                meta(),
                stages(stage(
                        "research.thaumcraft.alembic.stage_0",
                        List.of(),
                        List.of(req(Items.BREWING_STAND, 1)),
                        List.of(),
                        List.of(reward(KnowledgeType.OBSERVATION, alchemy, 4)),
                        List.of(),
                        0)));
    }

    private static void registerArtifice(BootstrapContext<IResearchEntry> ctx, Holder<IResearchCategory> artifice) {
        register(ctx, id("arcane_workbench"), artifice, 0, 0,
                meta(ResearchEntryMeta.ROUND),
                stages(stage(
                        "research.thaumcraft.arcane_workbench.stage_0",
                        List.of(),
                        List.of(req(Items.CRAFTING_TABLE, 1)),
                        List.of(),
                        List.of(reward(KnowledgeType.OBSERVATION, artifice, 6)),
                        List.of(),
                        0)));

        register(ctx, id("wand_crafting"), artifice, 2, 0,
                Set.of(id("arcane_workbench")),
                meta(),
                stages(stage(
                        "research.thaumcraft.wand_crafting.stage_0",
                        List.of(),
                        List.of(req(Items.STICK, 2), req(Items.IRON_INGOT, 2)),
                        List.of(),
                        List.of(reward(KnowledgeType.OBSERVATION, artifice, 4)),
                        List.of(),
                        0)));
    }

    private static void registerInfusion(BootstrapContext<IResearchEntry> ctx, Holder<IResearchCategory> infusion) {
        register(ctx, id("runic_matrix"), infusion, 0, 0,
                meta(ResearchEntryMeta.ROUND),
                stages(stage(
                        "research.thaumcraft.runic_matrix.stage_0",
                        List.of(),
                        List.of(req(Items.OBSIDIAN, 4)),
                        List.of(),
                        List.of(reward(KnowledgeType.OBSERVATION, infusion, 6)),
                        List.of(),
                        2)));
    }

    private static void registerGolemancy(BootstrapContext<IResearchEntry> ctx, Holder<IResearchCategory> golemancy) {
        register(ctx, id("golem_intro"), golemancy, 0, 0,
                meta(ResearchEntryMeta.ROUND),
                stages(stage(
                        "research.thaumcraft.golem_intro.stage_0",
                        List.of(),
                        List.of(req(Items.CLAY, 1)),
                        List.of(),
                        List.of(reward(KnowledgeType.OBSERVATION, golemancy, 6)),
                        List.of(),
                        0)));
    }

    private static void registerEldritch(BootstrapContext<IResearchEntry> ctx, Holder<IResearchCategory> eldritch) {
        register(ctx, id("eldritch_warning"), eldritch, 0, 0,
                meta(ResearchEntryMeta.HEX, ResearchEntryMeta.HIDDEN),
                stages(stage(
                        "research.thaumcraft.eldritch_warning.stage_0",
                        List.of(),
                        List.of(req(Items.ENDER_PEARL, 1)),
                        List.of(),
                        List.of(reward(KnowledgeType.THEORY, eldritch, 6)),
                        List.of(),
                        6)));
    }

    private static void register(
            BootstrapContext<IResearchEntry> ctx,
            Identifier id,
            Holder<IResearchCategory> category,
            int column,
            int row,
            Set<ResearchEntryMeta> meta,
            List<IResearchStage> stages
    ) {
        register(ctx, id, category, column, row, Set.of(), meta, stages);
    }

    private static void register(
            BootstrapContext<IResearchEntry> ctx,
            Identifier id,
            Holder<IResearchCategory> category,
            int column,
            int row,
            Set<Identifier> parents,
            Set<ResearchEntryMeta> meta,
            List<IResearchStage> stages
    ) {
        register(ctx, id, category, column, row, parents, meta, stages, List.of());
    }

    private static void register(
            BootstrapContext<IResearchEntry> ctx,
            Identifier id,
            Holder<IResearchCategory> category,
            int column,
            int row,
            Set<Identifier> parents,
            Set<ResearchEntryMeta> meta,
            List<IResearchStage> stages,
            List<ResearchIcon> icons
    ) {
        String nameKey = "research.thaumcraft." + id.getPath() + ".title";
        ResearchEntry entry = new ResearchEntry(
                category,
                nameKey,
                new LinkedHashSet<>(parents),
                Set.of(),
                column,
                row,
                stages,
                meta.isEmpty() ? EnumSet.noneOf(ResearchEntryMeta.class) : EnumSet.copyOf(meta),
                icons);
        ctx.register(ResourceKey.create(IResearchEntry.REGISTRY_KEY, id), entry);
    }

    private static List<ResearchIcon> researchTexture(String name) {
        return List.of(ResearchIcon.ofTexture(id("textures/research/" + name + ".png")));
    }

    private static ResearchStage stage(
            String textKey,
            List<Identifier> recipes,
            List<ResearchRequirement> obtain,
            List<ResearchRequirement> craft,
            List<KnowledgeReward> knowledge,
            List<Identifier> requiredResearch,
            int warp
    ) {
        return new ResearchStage(textKey, recipes, obtain, craft, knowledge, requiredResearch, warp);
    }

    @SafeVarargs
    private static List<IResearchStage> stages(IResearchStage... stages) {
        return List.of(stages);
    }

    private static ResearchRequirement req(Item item, int amount) {
        return new ResearchRequirement(HolderSet.direct(BuiltInRegistries.ITEM.wrapAsHolder(item)), amount);
    }

    private static KnowledgeReward reward(KnowledgeType type, Holder<IResearchCategory> category, int amount) {
        return new KnowledgeReward(type, category, amount);
    }

    private static Set<ResearchEntryMeta> meta(ResearchEntryMeta... flags) {
        return flags.length == 0 ? Set.of() : Set.of(flags);
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(TCIds.MODID, path);
    }
}
