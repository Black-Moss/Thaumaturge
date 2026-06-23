package com.leclowndu93150.thaumcraft.content.world.tree;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class BlockLeavesTC extends LeavesBlock {
    public static final MapCodec<BlockLeavesTC> CODEC = RecordCodecBuilder.mapCodec(
            i -> i.group(
                    ExtraCodecs.floatRange(0.0F, 1.0F).fieldOf("leaf_particle_chance").forGetter(e -> e.leafParticleChance),
                    propertiesCodec()
            ).apply(i, BlockLeavesTC::new)
    );

    public BlockLeavesTC(float leafParticleChance, BlockBehaviour.Properties properties) {
        super(leafParticleChance, properties);
    }

    @Override
    protected void spawnFallingLeavesParticle(Level level, BlockPos pos, RandomSource random) {
        ParticleUtils.spawnParticleBelow(level, pos, random, ParticleTypes.PALE_OAK_LEAVES);
    }

    @Override
    public MapCodec<BlockLeavesTC> codec() {
        return CODEC;
    }
}
