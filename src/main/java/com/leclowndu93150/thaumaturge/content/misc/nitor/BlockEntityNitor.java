package com.leclowndu93150.thaumaturge.content.misc.nitor;

import com.leclowndu93150.thaumaturge.client.effect.ClientEffects;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class BlockEntityNitor extends BlockEntity {
    private static final double CENTER = 0.5;
    private static final double FLAME_HEIGHT = 0.45;
    private static final double FLAME_JITTER = 0.025;
    private static final double FLAME_SIDE_SPEED = 0.0025;
    private static final double FLAME_RISE_SPEED = 0.06;
    private static final int FLAME_DELAY = 0;
    private static final double CORE_HEIGHT = 0.49;
    private static final int CORE_INTERVAL = 10;

    private int age;

    public BlockEntityNitor(BlockPos pos, BlockState state) {
        super(TTBlockEntities.NITOR.get(), pos, state);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, BlockEntityNitor entity) {
        if (!(level.getBlockState(pos).getBlock() instanceof BlockNitor nitor)) {
            return;
        }
        int color = nitor.dyeColor();
        RandomSource random = level.getRandom();
        double x = pos.getX();
        double y = pos.getY();
        double z = pos.getZ();
        ClientEffects.nitorFlames(level, x + CENTER + random.nextGaussian() * FLAME_JITTER, y + FLAME_HEIGHT + random.nextGaussian() * FLAME_JITTER, z + CENTER + random.nextGaussian() * FLAME_JITTER,
                random.nextGaussian() * FLAME_SIDE_SPEED, random.nextDouble() * FLAME_RISE_SPEED, random.nextGaussian() * FLAME_SIDE_SPEED, color, FLAME_DELAY);
        if (entity.age % CORE_INTERVAL == 0) {
            ClientEffects.nitorCore(level, x + CENTER, y + CORE_HEIGHT, z + CENTER, 0.0, 0.0, 0.0, color);
        }
        entity.age++;
    }
}
