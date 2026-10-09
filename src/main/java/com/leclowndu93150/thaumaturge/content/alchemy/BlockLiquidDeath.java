package com.leclowndu93150.thaumaturge.content.alchemy;

import com.leclowndu93150.thaumaturge.content.particle.SlimyBubbleParticleOptions;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ARGB;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;

public class BlockLiquidDeath extends LiquidBlock {
    private static final int BUBBLE_ONE_IN = 20;
    private static final int POP_ONE_IN = 50;
    private static final double SURFACE_BASE = 0.1;
    private static final double SURFACE_PER_HALF_LEVEL = 0.225;
    private static final double LEVEL_DIVISOR = 2.0;
    private static final float OPAQUE = 1.0F;
    private static final float RED_BASE = 0.3F;
    private static final float BLUE_BASE = 0.4F;
    private static final float COLOR_SPREAD = 0.1F;
    private static final float PARTICLE_ALPHA = 0.8F;
    private static final float SCALE_BASE = 0.075F;
    private static final float SCALE_SPREAD = 0.075F;
    private static final int AGE_BASE = 15;
    private static final int AGE_VARIATION = 5;
    private static final double POP_HEIGHT = 0.5;
    private static final float POP_VOLUME_BASE = 0.1F;
    private static final float POP_VOLUME_SPREAD = 0.1F;
    private static final float POP_PITCH_BASE = 0.9F;
    private static final float POP_PITCH_SPREAD = 0.15F;

    public BlockLiquidDeath(FlowingFluid fluid, BlockBehaviour.Properties properties) {
        super(fluid, properties);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        boolean bubbles = oneIn(random, BUBBLE_ONE_IN);
        if (bubbles) {
            spawnBubble(level, pos, random);
        }
        boolean pops = oneIn(random, POP_ONE_IN);
        if (pops) {
            playPop(level, pos, random);
        }
    }

    private static boolean oneIn(RandomSource random, int bound) {
        return random.nextInt(bound) == 0;
    }

    private static double scatter(int origin, RandomSource random) {
        return origin + random.nextDouble();
    }

    private static void spawnBubble(Level level, BlockPos pos, RandomSource random) {
        int amount = level.getFluidState(pos).getAmount();
        double x = scatter(pos.getX(), random);
        double z = scatter(pos.getZ(), random);
        double y = pos.getY() + SURFACE_BASE + SURFACE_PER_HALF_LEVEL * (amount / LEVEL_DIVISOR);
        float red = RED_BASE - random.nextFloat() * COLOR_SPREAD;
        float blue = BLUE_BASE + random.nextFloat() * COLOR_SPREAD;
        float scale = SCALE_BASE + random.nextFloat() * SCALE_SPREAD;
        int age = AGE_BASE + random.nextInt(AGE_VARIATION);
        SlimyBubbleParticleOptions options = new SlimyBubbleParticleOptions(ARGB.colorFromFloat(OPAQUE, red, 0.0F, blue), PARTICLE_ALPHA, scale, age);
        level.addParticle(options, x, y, z, 0.0, 0.0, 0.0);
    }

    private static void playPop(Level level, BlockPos pos, RandomSource random) {
        double x = scatter(pos.getX(), random);
        double z = scatter(pos.getZ(), random);
        float volume = POP_VOLUME_BASE + random.nextFloat() * POP_VOLUME_SPREAD;
        float pitch = POP_PITCH_BASE + random.nextFloat() * POP_PITCH_SPREAD;
        level.playLocalSound(x, pos.getY() + POP_HEIGHT, z, SoundEvents.LAVA_POP, SoundSource.BLOCKS, volume, pitch, false);
    }
}
