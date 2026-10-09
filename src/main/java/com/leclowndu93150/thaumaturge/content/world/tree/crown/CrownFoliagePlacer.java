package com.leclowndu93150.thaumaturge.content.world.tree.crown;

import com.leclowndu93150.thaumaturge.content.world.tree.TreeLeafUpdater;
import com.leclowndu93150.thaumaturge.registry.TTTreePlacers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;

public final class CrownFoliagePlacer extends FoliagePlacer {
    public static final MapCodec<CrownFoliagePlacer> CODEC = RecordCodecBuilder.mapCodec(
            instance -> foliagePlacerParts(instance).and(Codec.BOOL.fieldOf("absorb_foreign_leaves").forGetter(placer -> placer.absorbForeignLeaves)).apply(instance, CrownFoliagePlacer::new));

    private static final int LAYER_COUNT = 4;
    private static final int LAST_LAYER = LAYER_COUNT - 1;
    private static final int SQUARE_RADIUS = 2;
    private static final int EDGE_REACH = 1;

    private final boolean absorbForeignLeaves;

    public CrownFoliagePlacer(IntProvider radius, IntProvider offset, boolean absorbForeignLeaves) {
        super(radius, offset);
        this.absorbForeignLeaves = absorbForeignLeaves;
    }

    @Override
    protected FoliagePlacerType<?> type() {
        return TTTreePlacers.CROWN_FOLIAGE.get();
    }

    @Override
    protected void createFoliage(WorldGenLevel level, FoliageSetter setter, RandomSource random, TreeConfiguration config, int treeHeight, FoliageAttachment attachment, int foliageHeight, int leafRadius, int offset) {
        BlockPos anchor = attachment.pos();
        BlockState leaf = config.foliageProvider.getState(level, random, anchor);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int layer = 0; layer < LAYER_COUNT; layer++) {
            boolean plus = layer == 0 || layer == LAST_LAYER;
            for (int dx = -SQUARE_RADIUS; dx <= SQUARE_RADIUS; dx++) {
                for (int dz = -SQUARE_RADIUS; dz <= SQUARE_RADIUS; dz++) {
                    if (inLayer(plus, dx, dz)) {
                        cursor.setWithOffset(anchor, dx, layer, dz);
                        placeLeaf(level, setter, cursor, leaf);
                    }
                }
            }
        }
    }

    @Override
    public int foliageHeight(RandomSource random, int treeHeight, TreeConfiguration config) {
        return 0;
    }

    @Override
    protected boolean shouldSkipLocation(RandomSource random, int dx, int y, int dz, int currentRadius, boolean doubleTrunk) {
        return false;
    }

    private static boolean inLayer(boolean plus, int dx, int dz) {
        int absX = Math.abs(dx);
        int absZ = Math.abs(dz);
        if (plus) {
            return absX + absZ <= EDGE_REACH;
        }
        return !(absX == SQUARE_RADIUS && absZ == SQUARE_RADIUS);
    }

    private void placeLeaf(WorldGenLevel level, FoliageSetter setter, BlockPos pos, BlockState leaf) {
        if (level.isOutsideBuildHeight(pos)) {
            return;
        }
        BlockState existing = level.getBlockState(pos);
        if (existing.isAir()) {
            setter.set(pos, leaf);
        } else if (absorbForeignLeaves && existing.is(BlockTags.LEAVES) && !existing.is(leaf.getBlock())) {
            setter.set(pos, TreeLeafUpdater.carryDistance(leaf, existing));
        }
    }
}
