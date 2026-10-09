package com.leclowndu93150.thaumaturge.content.essentia.spill;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

public final class SpreadSampler {
    private SpreadSampler() {}

    public static Vec3 insideBlock(RandomSource random, BlockPos pos, SpillRange fraction) {
        double x = pos.getX() + fraction.sample(random);
        double y = pos.getY() + fraction.sample(random);
        double z = pos.getZ() + fraction.sample(random);
        return new Vec3(x, y, z);
    }
}
