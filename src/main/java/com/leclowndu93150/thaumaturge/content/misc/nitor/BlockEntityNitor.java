package com.leclowndu93150.thaumaturge.content.misc.nitor;

import com.leclowndu93150.thaumaturge.client.effect.ClientEffects;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class BlockEntityNitor extends BlockEntity {
    private static final int FALLBACK_COLOR = 0xFFFFFF;
    private static final double CENTRE = 0.5;
    private static final double FLAME_CENTRE_Y = 0.45;
    private static final double CORE_CENTRE_Y = 0.49;
    private static final double FLAME_JITTER = 0.025;
    private static final double FLAME_WANDER = 0.0025;
    private static final float FLAME_RISE = 0.06F;
    private static final int FLAME_DELAY = 0;
    private static final int CORE_INTERVAL = 10;

    private final int flameColor;
    private int ticks;

    public BlockEntityNitor(BlockPos pos, BlockState state) {
        super(TTBlockEntities.NITOR.get(), pos, state);
        this.flameColor = state.getBlock() instanceof BlockNitor nitor ? nitor.dyeColor() : FALLBACK_COLOR;
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, BlockEntityNitor nitor) {
        RandomSource random = level.getRandom();
        nitor.emitFlame(level, pos, random);
        if (nitor.ticks % CORE_INTERVAL == 0) {
            ClientEffects.nitorCore(level, pos.getX() + CENTRE, pos.getY() + CORE_CENTRE_Y, pos.getZ() + CENTRE, 0.0, 0.0, 0.0, nitor.flameColor);
        }
        nitor.ticks++;
    }

    private void emitFlame(Level level, BlockPos pos, RandomSource random) {
        double x = pos.getX() + CENTRE + random.nextGaussian() * FLAME_JITTER;
        double y = pos.getY() + FLAME_CENTRE_Y + random.nextGaussian() * FLAME_JITTER;
        double z = pos.getZ() + CENTRE + random.nextGaussian() * FLAME_JITTER;
        double driftX = random.nextGaussian() * FLAME_WANDER;
        double driftZ = random.nextGaussian() * FLAME_WANDER;
        double rise = random.nextFloat() * FLAME_RISE;
        ClientEffects.nitorFlames(level, x, y, z, driftX, rise, driftZ, flameColor, FLAME_DELAY);
    }
}
