package com.leclowndu93150.thaumaturge.content.essentia.spill;

import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.content.effect.Effects;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

public final class EssentiaSpill {
    public static final EssentiaSpill PIPE = new EssentiaSpill(SpillBurst.PIPE_FLUX);

    private static final double BLOCK_CENTER = 0.5;
    private static final int MIN_UNITS = 1;
    private static final double NO_DRIFT = 0.0;

    private final SpillBurst burst;

    public EssentiaSpill(SpillBurst burst) {
        this.burst = burst;
    }

    public void release(ServerLevel level, BlockPos pos, int units) {
        if (units < MIN_UNITS) {
            return;
        }
        AuraHelper.addFlux(level, pos, units * burst.fluxPerUnit());
        RandomSource random = level.getRandom();
        burst.sound().play(level, new Vec3(pos.getX() + BLOCK_CENTER, pos.getY() + BLOCK_CENTER, pos.getZ() + BLOCK_CENTER), random);
        for (int i = 0; i < burst.ventCount(); i++) {
            Vec3 origin = SpreadSampler.insideBlock(random, pos, burst.boxFraction());
            double rise = burst.riseSpeed().sample(random);
            Effects.Vent vent = Effects.vent2(level, origin).motion(NO_DRIFT, rise, NO_DRIFT).color(burst.color()).scale(burst.scale());
            if (burst.flame()) {
                vent.withFlame();
            }
            vent.send();
        }
    }
}
