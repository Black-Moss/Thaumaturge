package com.leclowndu93150.thaumaturge.content.aura.node;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public final class NodeFeature extends Feature<NodeFeatureConfig> {
    private static final int MAXIMUM_RISE = 4;

    public NodeFeature(Codec<NodeFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NodeFeatureConfig> context) {
        WorldGenLevel level = context.level();
        NodeFeatureConfig config = context.config();
        BlockPos start = level.getBlockState(context.origin().above()).isAir() ? context.origin().above() : context.origin();
        BlockPos raised = start.above(context.random().nextInt(MAXIMUM_RISE));
        BlockState raisedState = level.getBlockState(raised);
        BlockPos target = raisedState.isAir() || raisedState.canBeReplaced() ? raised : start;
        if (target.getY() > level.getMaxY()) {
            return false;
        }
        return NodeGenerator.createRandomNodeAt(level, target, context.random(), config.silverwood(), config.eerie(), config.small(), config.specialRarity(), config.baseAura());
    }
}
