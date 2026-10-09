package com.leclowndu93150.thaumaturge.data.worldgen.feature;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.content.aura.node.NodeFeatureConfig;
import com.leclowndu93150.thaumaturge.content.aura.node.NodeGenerator;
import com.leclowndu93150.thaumaturge.content.world.crystal.CrystalClusterConfig;
import com.leclowndu93150.thaumaturge.content.world.plant.MagicForestFloraConfig;
import com.leclowndu93150.thaumaturge.content.world.taint.TaintBiomeConfig;
import com.leclowndu93150.thaumaturge.content.world.tree.TTTreeGrowers;
import com.leclowndu93150.thaumaturge.content.world.tree.crown.CrownFoliagePlacer;
import com.leclowndu93150.thaumaturge.content.world.tree.crown.CrownRule;
import com.leclowndu93150.thaumaturge.content.world.tree.crown.CrownShape;
import com.leclowndu93150.thaumaturge.content.world.tree.crown.CrownTrunkPlacer;
import com.leclowndu93150.thaumaturge.content.world.tree.crown.SpiderNestDecorator;
import com.leclowndu93150.thaumaturge.content.world.tree.silverwood.DetachedLeafPruner;
import com.leclowndu93150.thaumaturge.content.world.tree.silverwood.LeafCellFoliagePlacer;
import com.leclowndu93150.thaumaturge.content.world.tree.silverwood.ScatteredFlowersDecorator;
import com.leclowndu93150.thaumaturge.content.world.tree.silverwood.SilverwoodTrunkPlacer;
import com.leclowndu93150.thaumaturge.registry.TTBlockTags;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTFeatures;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.data.worldgen.features.VegetationFeatures;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.data.worldgen.placement.TreePlacements;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HugeMushroomBlock;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.WeightedPlacedFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.HugeMushroomFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomBooleanFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ReplaceBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.VegetationPatchConfiguration;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.foliageplacers.BlobFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.trunkplacers.StraightTrunkPlacer;
import net.minecraft.world.level.levelgen.placement.BlockPredicateFilter;
import net.minecraft.world.level.levelgen.placement.CaveSurface;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.RandomOffsetPlacement;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;

public final class TTConfiguredFeatures {
    public static final ResourceKey<ConfiguredFeature<?, ?>> MAGIC_FOREST_BROWN_MUSHROOM = key("magic_forest_brown_mushroom");
    public static final ResourceKey<ConfiguredFeature<?, ?>> MAGIC_FOREST_RED_MUSHROOM = key("magic_forest_red_mushroom");
    public static final ResourceKey<ConfiguredFeature<?, ?>> GREATWOOD_TREE = key("greatwood_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> GREATWOOD_TREE_GROWN = TTTreeGrowers.GREATWOOD_TREE_GROWN;
    public static final ResourceKey<ConfiguredFeature<?, ?>> SILVERWOOD_TREE = key("silverwood_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> SILVERWOOD_TREE_GROWN = TTTreeGrowers.SILVERWOOD_TREE_GROWN;
    public static final ResourceKey<ConfiguredFeature<?, ?>> SILVERWOOD_TREE_CAVE = key("silverwood_tree_cave");
    public static final ResourceKey<ConfiguredFeature<?, ?>> BIG_MAGIC_TREE = key("big_magic_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> MAGIC_FOREST_TREES = key("magic_forest_trees");
    public static final ResourceKey<ConfiguredFeature<?, ?>> TAINTED_LANDS_TREES = key("tainted_lands_trees");
    public static final ResourceKey<ConfiguredFeature<?, ?>> TAINT_BIOME = key("taint_biome");
    public static final ResourceKey<ConfiguredFeature<?, ?>> MAGIC_FOREST_FLORA = key("magic_forest_flora");
    public static final ResourceKey<ConfiguredFeature<?, ?>> MAGICAL_CAVE_GRASS = key("magical_cave_grass");
    public static final ResourceKey<ConfiguredFeature<?, ?>> MAGICAL_CAVE_AMBIENT_GRASS = key("magical_cave_ambient_grass");
    public static final ResourceKey<ConfiguredFeature<?, ?>> MAGICAL_CAVE_POND = key("magical_cave_pond");
    public static final ResourceKey<ConfiguredFeature<?, ?>> MAGICAL_CAVE_TREES = key("magical_cave_trees");
    public static final ResourceKey<ConfiguredFeature<?, ?>> MAGICAL_CAVE_GREATWOOD_TREE = key("magical_cave_greatwood_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> MAGICAL_CAVE_BUSH = key("magical_cave_bush");
    public static final ResourceKey<ConfiguredFeature<?, ?>> MAGICAL_CAVE_MUSHROOMS = key("magical_cave_mushrooms");
    public static final ResourceKey<ConfiguredFeature<?, ?>> MAGICAL_CAVE_FLORA = key("magical_cave_flora");
    public static final ResourceKey<ConfiguredFeature<?, ?>> MAGICAL_CAVE_VISHROOM = key("magical_cave_vishroom");
    public static final ResourceKey<ConfiguredFeature<?, ?>> MAGICAL_CAVE_SHIMMERLEAF = key("magical_cave_shimmerleaf");
    public static final ResourceKey<ConfiguredFeature<?, ?>> MAGICAL_CAVE_CRYSTALS = key("magical_cave_crystals");
    public static final ResourceKey<ConfiguredFeature<?, ?>> MANA_PODS = key("mana_pods");
    public static final ResourceKey<ConfiguredFeature<?, ?>> CRYSTALS = key("crystals");
    public static final ResourceKey<ConfiguredFeature<?, ?>> NODES_WILD = key("nodes_wild");
    public static final ResourceKey<ConfiguredFeature<?, ?>> NODES_EERIE = key("nodes_eerie");
    public static final ResourceKey<ConfiguredFeature<?, ?>> OBSIDIAN_TOTEM = key("obsidian_totem");
    public static final ResourceKey<ConfiguredFeature<?, ?>> CRIMSON_PORTAL = key("crimson_portal");
    public static final ResourceKey<ConfiguredFeature<?, ?>> HILLTOP_STONES = key("hilltop_stones");
    public static final ResourceKey<ConfiguredFeature<?, ?>> ORE_CINNABAR = key("ore_cinnabar");
    public static final ResourceKey<ConfiguredFeature<?, ?>> ORE_QUARTZ = key("ore_quartz");
    public static final ResourceKey<ConfiguredFeature<?, ?>> ORE_AMBER = key("ore_amber");
    public static final ResourceKey<ConfiguredFeature<?, ?>> CINDERPEARL_PATCH = key("cinderpearl_patch");

    private static final int CROWN_TREE_BASE_HEIGHT = 11;
    private static final int CROWN_TREE_HEIGHT_SPREAD = 10;
    private static final int GREATWOOD_TRUNK_WIDTH = 2;
    private static final double GREATWOOD_HEIGHT_ATTENUATION = 0.618;
    private static final double GREATWOOD_BRANCH_SLOPE = 0.38;
    private static final double GREATWOOD_SCALE_WIDTH = 1.2;
    private static final float GREATWOOD_SPIDER_CHANCE = 0.125F;
    private static final double MAGIC_OAK_TRUNK_SHARE = 0.6618;
    private static final double MAGIC_OAK_BRANCH_SLOPE = 0.381;
    private static final double MAGIC_OAK_CROWN_WIDTH = 1.25;
    private static final int SILVERWOOD_NATURAL_MIN_HEIGHT = 8;
    private static final int SILVERWOOD_NATURAL_EXTRA_HEIGHT = 5;
    private static final int SILVERWOOD_GROWN_MIN_HEIGHT = 7;
    private static final int SILVERWOOD_GROWN_EXTRA_HEIGHT = 4;
    private static final float MAGIC_FOREST_SILVERWOOD_CHANCE = 1.0F / 18.0F;
    private static final float MAGIC_FOREST_GREATWOOD_CHANCE = 1.0F / 12.0F;
    private static final float TAINTED_LANDS_BIG_TREE_CHANCE = 1.0F / 8.0F;
    private static final float MAGICAL_CAVE_OAK_TREE_CHANCE = 0.70F;
    private static final float MAGICAL_CAVE_SILVERWOOD_CHANCE = 1.3F / 12.0F;
    private static final float MAGICAL_CAVE_GREATWOOD_CHANCE = MAGICAL_CAVE_SILVERWOOD_CHANCE;
    private static final float MAGICAL_CAVE_BROWN_MUSHROOM_CHANCE = 0.03F;
    private static final float MAGICAL_CAVE_RED_MUSHROOM_CHANCE = 0.025F;
    private static final float MAGICAL_CAVE_FLOWER_CHANCE = 0.12F;
    private static final int MAGICAL_CAVE_SILVERWOOD_BASE_HEIGHT = 6;
    private static final int MAGICAL_CAVE_SILVERWOOD_EXTRA_HEIGHT = 3;
    private static final int MAGICAL_CAVE_GREATWOOD_BASE_HEIGHT = 4;
    private static final int MAGICAL_CAVE_GREATWOOD_EXTRA_HEIGHT = 2;
    private static final int MAGICAL_CAVE_GREATWOOD_FOLIAGE_RADIUS = 2;
    private static final int MAGICAL_CAVE_GREATWOOD_FOLIAGE_HEIGHT = 3;
    private static final int MAGICAL_CAVE_CRYSTAL_ATTEMPTS = 1;
    private static final int MAGICAL_CAVE_CRYSTAL_MAX_TOTAL = 8;
    private static final int CAVE_GROUND_MIN_DEPTH = 1;
    private static final int CAVE_GROUND_MAX_DEPTH = 2;
    private static final int CAVE_GROUND_VERTICAL_RANGE = 5;
    private static final int CAVE_GROUND_MIN_RADIUS = 3;
    private static final int CAVE_GROUND_MAX_RADIUS = 6;
    private static final float CAVE_GROUND_EXTRA_EDGE_CHANCE = 0.3F;
    private static final int PATCH_XZ_SPREAD = 7;
    private static final int PATCH_Y_SPREAD = 3;
    private static final int MUSHROOM_PATCH_TRIES = 96;
    private static final int GRASS_PATCH_TRIES = 32;
    private static final int FLOWER_PATCH_TRIES = 64;
    private static final int TAINT_BIOME_MAX_CRUST_BLOBS = 2;
    private static final int TAINT_BIOME_MIN_CRUST_RADIUS = 1;
    private static final int TAINT_BIOME_MAX_CRUST_RADIUS = 2;
    private static final int TAINT_BIOME_GRASS_FIBRE_ATTEMPTS = 10;
    private static final int TAINT_BIOME_GENERAL_FIBRE_ATTEMPTS = 8;
    private static final int TAINT_BIOME_GROUND_SEARCH_DEPTH = 32;
    private static final int TAINT_BIOME_LANDMARK_RADIUS_CHUNKS = 4;
    private static final int TAINT_BIOME_LANDMARK_ATTEMPTS = 48;
    private static final int CRYSTAL_ATTEMPTS = 8;
    private static final int CRYSTAL_MAX_TOTAL = 64;
    private static final int CRYSTAL_BIOME_ASPECT_CHANCE = 3;
    private static final int FLORA_GRASS_ATTEMPTS = 3;
    private static final int FLORA_VISHROOM_ATTEMPTS = 5;
    private static final int FLORA_FLOWER_ATTEMPTS = 10;
    private static final int FLORA_HUGE_MUSHROOM_RARITY = 40;
    private static final int HUGE_MUSHROOM_FOLIAGE_RADIUS = 3;

    private TTConfiguredFeatures() {}

    private static ResourceKey<ConfiguredFeature<?, ?>> key(String path) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, TTIds.rl(path));
    }

    private static <C extends FeatureConfiguration> void add(BootstrapContext<ConfiguredFeature<?, ?>> context, ResourceKey<ConfiguredFeature<?, ?>> id, Feature<C> feature, C config) {
        context.register(id, new ConfiguredFeature<>(feature, config));
    }

    private static void addSingleBlock(BootstrapContext<ConfiguredFeature<?, ?>> context, ResourceKey<ConfiguredFeature<?, ?>> id, Block block) {
        add(context, id, Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(BlockStateProvider.simple(block)));
    }

    private static void addPlain(BootstrapContext<ConfiguredFeature<?, ?>> context, ResourceKey<ConfiguredFeature<?, ?>> id, Feature<NoneFeatureConfiguration> feature) {
        add(context, id, feature, NoneFeatureConfiguration.INSTANCE);
    }

    private static Holder<PlacedFeature> inline(Holder<ConfiguredFeature<?, ?>> feature) {
        return PlacementUtils.inlinePlaced(feature);
    }

    private static VegetationPatchConfiguration caveGround(Block ground) {
        return new VegetationPatchConfiguration(TTBlockTags.MAGICAL_CAVE_GROUND_REPLACEABLE, BlockStateProvider.simple(ground),
                PlacementUtils.inlinePlaced(Feature.NO_OP, NoneFeatureConfiguration.INSTANCE), CaveSurface.FLOOR, UniformInt.of(CAVE_GROUND_MIN_DEPTH, CAVE_GROUND_MAX_DEPTH), 0.0F,
                CAVE_GROUND_VERTICAL_RANGE, 0.0F, UniformInt.of(CAVE_GROUND_MIN_RADIUS, CAVE_GROUND_MAX_RADIUS), CAVE_GROUND_EXTRA_EDGE_CHANCE);
    }

    private static TreeConfiguration crownTree(Block log, Block leaves, CrownShape shape, CrownRule rule, boolean absorbForeignLeaves, List<TreeDecorator> decorators) {
        CrownTrunkPlacer trunk = new CrownTrunkPlacer(CROWN_TREE_BASE_HEIGHT, CROWN_TREE_HEIGHT_SPREAD, 0, shape, rule);
        CrownFoliagePlacer foliage = new CrownFoliagePlacer(ConstantInt.ZERO, ConstantInt.ZERO, absorbForeignLeaves);
        return new TreeConfiguration.TreeConfigurationBuilder(BlockStateProvider.simple(log), trunk, BlockStateProvider.simple(leaves), foliage, new TwoLayersFeatureSize(1, 0, 0)).ignoreVines()
                .decorators(decorators).build();
    }

    private static TreeConfiguration silverwoodTree(int minHeight, int extraHeight, boolean growNodes, boolean keepApart, Optional<Block> flower) {
        List<TreeDecorator> decorators = flower.<List<TreeDecorator>>map(block -> List.of(DetachedLeafPruner.INSTANCE, new ScatteredFlowersDecorator(block)))
                .orElseGet(() -> List.of(DetachedLeafPruner.INSTANCE));
        SilverwoodTrunkPlacer trunk = new SilverwoodTrunkPlacer(minHeight, extraHeight - 1, 0, growNodes, keepApart);
        TreeConfiguration.TreeConfigurationBuilder builder = new TreeConfiguration.TreeConfigurationBuilder(BlockStateProvider.simple(TTBlocks.LOG_SILVERWOOD.get()), trunk,
                BlockStateProvider.simple(TTBlocks.LEAVES_SILVERWOOD.get()), new LeafCellFoliagePlacer(ConstantInt.ZERO, ConstantInt.ZERO), new TwoLayersFeatureSize(1, 0, 0));
        return builder.ignoreVines().decorators(decorators).build();
    }

    private static TreeConfiguration caveGreatwoodTree() {
        BlobFoliagePlacer foliage = new BlobFoliagePlacer(ConstantInt.of(MAGICAL_CAVE_GREATWOOD_FOLIAGE_RADIUS), ConstantInt.of(0), MAGICAL_CAVE_GREATWOOD_FOLIAGE_HEIGHT);
        StraightTrunkPlacer trunk = new StraightTrunkPlacer(MAGICAL_CAVE_GREATWOOD_BASE_HEIGHT, MAGICAL_CAVE_GREATWOOD_EXTRA_HEIGHT, 0);
        return new TreeConfiguration.TreeConfigurationBuilder(BlockStateProvider.simple(TTBlocks.LOG_GREATWOOD.get()), trunk, BlockStateProvider.simple(TTBlocks.LEAVES_GREATWOOD.get()), foliage,
                new TwoLayersFeatureSize(1, 0, 1)).build();
    }

    private static Holder<PlacedFeature> patch(Holder<ConfiguredFeature<?, ?>> feature, int tries) {
        return PlacementUtils.inlinePlaced(feature, CountPlacement.of(tries), RandomOffsetPlacement.ofTriangle(PATCH_XZ_SPREAD, PATCH_Y_SPREAD),
                BlockPredicateFilter.forPredicate(BlockPredicate.ONLY_IN_AIR_PREDICATE));
    }

    private static HugeMushroomFeatureConfiguration hugeMushroom(Block cap, TagKey<Block> placeOn) {
        BlockStateProvider capProvider = BlockStateProvider.simple(cap.defaultBlockState().setValue(HugeMushroomBlock.DOWN, false));
        BlockStateProvider stemProvider = BlockStateProvider.simple(Blocks.MUSHROOM_STEM.defaultBlockState().setValue(HugeMushroomBlock.UP, false).setValue(HugeMushroomBlock.DOWN, false));
        return new HugeMushroomFeatureConfiguration(capProvider, stemProvider, HUGE_MUSHROOM_FOLIAGE_RADIUS, BlockPredicate.matchesTag(placeOn));
    }

    private static MagicForestFloraConfig.PlantPatch plantPatch(Block block, int attempts, int rarity) {
        return new MagicForestFloraConfig.PlantPatch(BlockStateProvider.simple(block), attempts, rarity);
    }

    private static NodeFeatureConfig node(boolean eerie) {
        return new NodeFeatureConfig(false, eerie, false, NodeGenerator.DEFAULT_SPECIAL_RARITY, NodeGenerator.DEFAULT_BASE_AURA);
    }

    private static CrystalClusterConfig.Entry crystal(ResourceKey<IAspect> aspect, Supplier<? extends Block> block) {
        return new CrystalClusterConfig.Entry(aspect, block.get());
    }

    private static ReplaceBlockConfiguration oreReplacement(Block stoneOre, Block deepslateOre) {
        OreConfiguration.TargetBlockState inStone = OreConfiguration.target(new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES), stoneOre.defaultBlockState());
        OreConfiguration.TargetBlockState inDeepslate = OreConfiguration.target(new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES), deepslateOre.defaultBlockState());
        return new ReplaceBlockConfiguration(List.of(inStone, inDeepslate));
    }

    public static void bootstrap(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        HolderGetter<PlacedFeature> placed = context.lookup(Registries.PLACED_FEATURE);
        HolderGetter<ConfiguredFeature<?, ?>> configured = context.lookup(Registries.CONFIGURED_FEATURE);
        registerSurfaceTrees(context);
        registerCaveTrees(context);
        registerForestSelectors(context, placed);
        registerTaintBiome(context);
        registerForestFlora(context, configured);
        registerCaveGround(context, configured);
        registerCaveVegetation(context, configured);
        registerStructures(context);
        registerCrystals(context);
        registerOres(context);
    }

    private static void registerSurfaceTrees(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        Block greatwoodLog = TTBlocks.LOG_GREATWOOD.get();
        Block greatwoodLeaves = TTBlocks.LEAVES_GREATWOOD.get();
        CrownShape greatwoodShape = new CrownShape(GREATWOOD_TRUNK_WIDTH, GREATWOOD_HEIGHT_ATTENUATION, GREATWOOD_BRANCH_SLOPE, GREATWOOD_SCALE_WIDTH, true);
        List<TreeDecorator> greatwoodNest = List.of(new SpiderNestDecorator(GREATWOOD_SPIDER_CHANCE, greatwoodLog, greatwoodLeaves));
        add(context, GREATWOOD_TREE, Feature.TREE, crownTree(greatwoodLog, greatwoodLeaves, greatwoodShape, CrownRule.OPEN_AIR, false, greatwoodNest));
        add(context, GREATWOOD_TREE_GROWN, Feature.TREE, crownTree(greatwoodLog, greatwoodLeaves, greatwoodShape, CrownRule.OPEN_AIR, false, List.of()));
        CrownShape magicOakShape = new CrownShape(1, MAGIC_OAK_TRUNK_SHARE, MAGIC_OAK_BRANCH_SLOPE, MAGIC_OAK_CROWN_WIDTH, false);
        add(context, BIG_MAGIC_TREE, Feature.TREE, crownTree(Blocks.OAK_LOG, Blocks.OAK_LEAVES, magicOakShape, CrownRule.REPLACEABLE, true, List.of()));
        Optional<Block> shimmerleaf = Optional.of(TTBlocks.PLANT_SHIMMERLEAF.get());
        add(context, SILVERWOOD_TREE, Feature.TREE, silverwoodTree(SILVERWOOD_NATURAL_MIN_HEIGHT, SILVERWOOD_NATURAL_EXTRA_HEIGHT, true, true, shimmerleaf));
        add(context, SILVERWOOD_TREE_GROWN, Feature.TREE, silverwoodTree(SILVERWOOD_GROWN_MIN_HEIGHT, SILVERWOOD_GROWN_EXTRA_HEIGHT, true, false, Optional.empty()));
    }

    private static void registerCaveTrees(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        add(context, SILVERWOOD_TREE_CAVE, Feature.TREE,
                silverwoodTree(MAGICAL_CAVE_SILVERWOOD_BASE_HEIGHT, MAGICAL_CAVE_SILVERWOOD_EXTRA_HEIGHT, false, false, Optional.of(TTBlocks.PLANT_SHIMMERLEAF.get())));
        add(context, MAGICAL_CAVE_GREATWOOD_TREE, Feature.TREE, caveGreatwoodTree());
    }

    private static void registerForestSelectors(BootstrapContext<ConfiguredFeature<?, ?>> context, HolderGetter<PlacedFeature> placed) {
        Holder<PlacedFeature> silverwood = placed.getOrThrow(TTPlacedFeatures.SILVERWOOD_CHECKED);
        Holder<PlacedFeature> greatwood = placed.getOrThrow(TTPlacedFeatures.GREATWOOD_CHECKED);
        Holder<PlacedFeature> bigMagic = placed.getOrThrow(TTPlacedFeatures.BIG_MAGIC_CHECKED);
        add(context, MAGIC_FOREST_TREES, Feature.RANDOM_SELECTOR, new RandomFeatureConfiguration(
                List.of(new WeightedPlacedFeature(silverwood, MAGIC_FOREST_SILVERWOOD_CHANCE), new WeightedPlacedFeature(greatwood, MAGIC_FOREST_GREATWOOD_CHANCE)), bigMagic));
        add(context, TAINTED_LANDS_TREES, Feature.RANDOM_SELECTOR,
                new RandomFeatureConfiguration(List.of(new WeightedPlacedFeature(bigMagic, TAINTED_LANDS_BIG_TREE_CHANCE)), placed.getOrThrow(TreePlacements.OAK_CHECKED)));
    }

    private static void registerTaintBiome(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        UniformInt crustRadius = UniformInt.of(TAINT_BIOME_MIN_CRUST_RADIUS, TAINT_BIOME_MAX_CRUST_RADIUS);
        add(context, TAINT_BIOME, TTFeatures.TAINT_BIOME.get(), new TaintBiomeConfig(TTBlocks.TAINT_CRUST.get(), TAINT_BIOME_MAX_CRUST_BLOBS, crustRadius, TAINT_BIOME_GRASS_FIBRE_ATTEMPTS,
                TAINT_BIOME_GENERAL_FIBRE_ATTEMPTS, TAINT_BIOME_GROUND_SEARCH_DEPTH, true, TAINT_BIOME_LANDMARK_RADIUS_CHUNKS, TAINT_BIOME_LANDMARK_ATTEMPTS));
    }

    private static void registerForestFlora(BootstrapContext<ConfiguredFeature<?, ?>> context, HolderGetter<ConfiguredFeature<?, ?>> configured) {
        add(context, MAGIC_FOREST_BROWN_MUSHROOM, Feature.HUGE_BROWN_MUSHROOM, hugeMushroom(Blocks.BROWN_MUSHROOM_BLOCK, BlockTags.HUGE_BROWN_MUSHROOM_CAN_PLACE_ON));
        add(context, MAGIC_FOREST_RED_MUSHROOM, Feature.HUGE_RED_MUSHROOM, hugeMushroom(Blocks.RED_MUSHROOM_BLOCK, BlockTags.HUGE_RED_MUSHROOM_CAN_PLACE_ON));
        List<MagicForestFloraConfig.PlantPatch> plants = List.of(plantPatch(Blocks.TALL_GRASS, 12, 1), plantPatch(Blocks.SHORT_GRASS, 10, 1), plantPatch(Blocks.FERN, 6, 1),
                plantPatch(Blocks.BROWN_MUSHROOM, 6, 4), plantPatch(Blocks.RED_MUSHROOM, 6, 8));
        HolderSet<ConfiguredFeature<?, ?>> hugeMushrooms = HolderSet.direct(configured.getOrThrow(MAGIC_FOREST_BROWN_MUSHROOM), configured.getOrThrow(MAGIC_FOREST_RED_MUSHROOM));
        HolderSet<Block> flowers = context.lookup(Registries.BLOCK).getOrThrow(TTBlockTags.MAGICAL_FOREST_FLOWERS);
        add(context, MAGIC_FOREST_FLORA, TTFeatures.MAGIC_FOREST_FLORA.get(), new MagicForestFloraConfig(TTBlocks.GRASS_AMBIENT.get(), TTBlocks.PLANT_VISHROOM.get(), FLORA_GRASS_ATTEMPTS,
                FLORA_VISHROOM_ATTEMPTS, flowers, FLORA_FLOWER_ATTEMPTS, plants, hugeMushrooms, FLORA_HUGE_MUSHROOM_RARITY));
    }

    private static void registerCaveGround(BootstrapContext<ConfiguredFeature<?, ?>> context, HolderGetter<ConfiguredFeature<?, ?>> configured) {
        add(context, MAGICAL_CAVE_GRASS, Feature.VEGETATION_PATCH, caveGround(Blocks.GRASS_BLOCK));
        add(context, MAGICAL_CAVE_AMBIENT_GRASS, Feature.VEGETATION_PATCH, caveGround(TTBlocks.GRASS_AMBIENT.get()));
        addPlain(context, MAGICAL_CAVE_POND, TTFeatures.MAGICAL_CAVE_POND.get());
        add(context, MAGICAL_CAVE_MUSHROOMS, Feature.RANDOM_BOOLEAN_SELECTOR,
                new RandomBooleanFeatureConfiguration(inline(configured.getOrThrow(TreeFeatures.HUGE_BROWN_MUSHROOM)), inline(configured.getOrThrow(TreeFeatures.HUGE_RED_MUSHROOM))));
    }

    private static void registerCaveVegetation(BootstrapContext<ConfiguredFeature<?, ?>> context, HolderGetter<ConfiguredFeature<?, ?>> configured) {
        List<WeightedPlacedFeature> caveTrees = List.of(new WeightedPlacedFeature(inline(configured.getOrThrow(SILVERWOOD_TREE_CAVE)), MAGICAL_CAVE_SILVERWOOD_CHANCE),
                new WeightedPlacedFeature(inline(configured.getOrThrow(MAGICAL_CAVE_GREATWOOD_TREE)), MAGICAL_CAVE_GREATWOOD_CHANCE),
                new WeightedPlacedFeature(inline(configured.getOrThrow(TreeFeatures.OAK)), MAGICAL_CAVE_OAK_TREE_CHANCE));
        add(context, MAGICAL_CAVE_TREES, Feature.RANDOM_SELECTOR, new RandomFeatureConfiguration(caveTrees, inline(configured.getOrThrow(MAGICAL_CAVE_BUSH))));
        addPlain(context, MAGICAL_CAVE_BUSH, TTFeatures.MAGICAL_CAVE_BUSH.get());
        List<WeightedPlacedFeature> caveFlora = List.of(
                new WeightedPlacedFeature(patch(configured.getOrThrow(VegetationFeatures.BROWN_MUSHROOM), MUSHROOM_PATCH_TRIES), MAGICAL_CAVE_BROWN_MUSHROOM_CHANCE),
                new WeightedPlacedFeature(patch(configured.getOrThrow(VegetationFeatures.RED_MUSHROOM), MUSHROOM_PATCH_TRIES), MAGICAL_CAVE_RED_MUSHROOM_CHANCE),
                new WeightedPlacedFeature(patch(configured.getOrThrow(VegetationFeatures.FLOWER_DEFAULT), FLOWER_PATCH_TRIES), MAGICAL_CAVE_FLOWER_CHANCE));
        add(context, MAGICAL_CAVE_FLORA, Feature.RANDOM_SELECTOR, new RandomFeatureConfiguration(caveFlora, patch(configured.getOrThrow(VegetationFeatures.GRASS), GRASS_PATCH_TRIES)));
        addSingleBlock(context, MAGICAL_CAVE_VISHROOM, TTBlocks.PLANT_VISHROOM.get());
        addSingleBlock(context, MAGICAL_CAVE_SHIMMERLEAF, TTBlocks.PLANT_SHIMMERLEAF.get());
    }

    private static void registerStructures(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        addPlain(context, MANA_PODS, TTFeatures.MANA_PODS.get());
        add(context, NODES_WILD, TTFeatures.NODE.get(), node(false));
        add(context, NODES_EERIE, TTFeatures.NODE.get(), node(true));
        addPlain(context, OBSIDIAN_TOTEM, TTFeatures.OBSIDIAN_TOTEM.get());
        addPlain(context, CRIMSON_PORTAL, TTFeatures.CRIMSON_PORTAL.get());
        addPlain(context, HILLTOP_STONES, TTFeatures.HILLTOP_STONES.get());
    }

    private static void registerCrystals(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        List<CrystalClusterConfig.Entry> entries = List.of(crystal(TTAspects.AER, TTBlocks.CRYSTAL_AER), crystal(TTAspects.IGNIS, TTBlocks.CRYSTAL_IGNIS),
                crystal(TTAspects.AQUA, TTBlocks.CRYSTAL_AQUA), crystal(TTAspects.TERRA, TTBlocks.CRYSTAL_TERRA), crystal(TTAspects.ORDO, TTBlocks.CRYSTAL_ORDO),
                crystal(TTAspects.PERDITIO, TTBlocks.CRYSTAL_PERDITIO));
        add(context, CRYSTALS, TTFeatures.CRYSTAL_CLUSTER.get(), new CrystalClusterConfig(entries, CRYSTAL_ATTEMPTS, CRYSTAL_MAX_TOTAL, CRYSTAL_BIOME_ASPECT_CHANCE));
        add(context, MAGICAL_CAVE_CRYSTALS, TTFeatures.CRYSTAL_CLUSTER.get(),
                new CrystalClusterConfig(entries, MAGICAL_CAVE_CRYSTAL_ATTEMPTS, MAGICAL_CAVE_CRYSTAL_MAX_TOTAL, CRYSTAL_BIOME_ASPECT_CHANCE, true));
    }

    private static void registerOres(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        add(context, ORE_CINNABAR, Feature.REPLACE_SINGLE_BLOCK, oreReplacement(TTBlocks.ORE_CINNABAR.get(), TTBlocks.DEEPSLATE_ORE_CINNABAR.get()));
        add(context, ORE_QUARTZ, Feature.REPLACE_SINGLE_BLOCK, oreReplacement(TTBlocks.ORE_QUARTZ.get(), TTBlocks.DEEPSLATE_ORE_QUARTZ.get()));
        add(context, ORE_AMBER, Feature.REPLACE_SINGLE_BLOCK, oreReplacement(TTBlocks.ORE_AMBER.get(), TTBlocks.DEEPSLATE_ORE_AMBER.get()));
        addSingleBlock(context, CINDERPEARL_PATCH, TTBlocks.PLANT_CINDERPEARL.get());
    }
}
