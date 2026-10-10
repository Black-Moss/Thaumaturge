package com.leclowndu93150.thaumaturge.content.misc.nitor;

import com.leclowndu93150.thaumaturge.content.particle.NitorCoreParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.WispFlameParticleOptions;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class BlockEntityNitor extends BlockEntity {
    private static final int FALLBACK_COLOR = 0xFFFFFF;
    private static final double CENTRE = 0.5;
    private static final double FLAME_DROP = 0.06;
    private static final double FLAME_JITTER = 0.025;
    private static final double FLAME_RISE_BASE = 0.010;
    private static final double FLAME_RISE_SPREAD = 0.008;
    private static final double FLAME_WANDER = 0.0025;
    private static final float FLAME_ALPHA = 0.72F;
    private static final float FLAME_SCALE_BASE = 2.6F;
    private static final float FLAME_SCALE_SPREAD = 0.9F;
    private static final float FLAME_END_SCALE = 0.06F;
    private static final int FLAME_DELAY = 0;
    private static final int CORE_INTERVAL = 10;

    private final int flameColor;
    private final NitorCoreParticleOptions core;

    public BlockEntityNitor(BlockPos pos, BlockState state) {
        super(TTBlockEntities.NITOR.get(), pos, state);
        this.flameColor = state.getBlock() instanceof BlockNitor nitor ? nitor.dyeColor() : FALLBACK_COLOR;
        this.core = new NitorCoreParticleOptions(ARGB.redFloat(flameColor), ARGB.greenFloat(flameColor), ARGB.blueFloat(flameColor));
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, BlockEntityNitor nitor) {
        RandomSource random = level.getRandom();
        nitor.emitFlame(level, pos, random);
        if (Math.floorMod(level.getGameTime() + pos.asLong(), CORE_INTERVAL) == 0) {
            level.addParticle(nitor.core, pos.getX() + CENTRE, pos.getY() + CENTRE, pos.getZ() + CENTRE, 0.0, 0.0, 0.0);
        }
    }

    private void emitFlame(Level level, BlockPos pos, RandomSource random) {
        double x = pos.getX() + CENTRE + random.triangle(0.0, FLAME_JITTER);
        double y = pos.getY() + CENTRE - FLAME_DROP + random.triangle(0.0, FLAME_JITTER);
        double z = pos.getZ() + CENTRE + random.triangle(0.0, FLAME_JITTER);
        double rise = FLAME_RISE_BASE + random.nextDouble() * FLAME_RISE_SPREAD;
        float scale = FLAME_SCALE_BASE + random.nextFloat() * FLAME_SCALE_SPREAD;
        WispFlameParticleOptions flame = new WispFlameParticleOptions(flameColor, FLAME_ALPHA, scale, FLAME_END_SCALE, FLAME_DELAY);
        level.addParticle(flame, x, y, z, random.triangle(0.0, FLAME_WANDER), rise, random.triangle(0.0, FLAME_WANDER));
    }
}
