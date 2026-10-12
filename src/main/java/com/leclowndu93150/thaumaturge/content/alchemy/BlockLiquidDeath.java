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
    private static final int BUBBLE_ODDS = 20;
    private static final int POP_ODDS = 50;
    private static final float BUBBLE_RED_BASE = 0.3F;
    private static final float BUBBLE_RED_SPREAD = 0.1F;
    private static final float BUBBLE_BLUE_BASE = 0.4F;
    private static final float BUBBLE_BLUE_SPREAD = 0.1F;
    private static final double BUBBLE_BASE_HEIGHT = 0.1;
    private static final double BUBBLE_LEVEL_HEIGHT = 0.225;
    private static final double BUBBLE_LEVEL_DIVISOR = 2.0;
    private static final float BUBBLE_ALPHA = 0.8F;
    private static final float BUBBLE_MIN_SCALE = 0.075F;
    private static final float BUBBLE_SCALE_SPREAD = 0.075F;
    private static final int BUBBLE_MIN_AGE = 15;
    private static final int BUBBLE_AGE_SPREAD = 5;
    private static final double POP_HEIGHT = 0.5;
    private static final float POP_MIN_VOLUME = 0.1F;
    private static final float POP_VOLUME_SPREAD = 0.1F;
    private static final float POP_MIN_PITCH = 0.9F;
    private static final float POP_PITCH_SPREAD = 0.15F;

    public BlockLiquidDeath(FlowingFluid fluid, BlockBehaviour.Properties properties) {
        super(fluid, properties);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(BUBBLE_ODDS) == 0) {
            surfaceBubble(state, level, pos, random);
        }
        if (random.nextInt(POP_ODDS) == 0) {
            softPop(level, pos, random);
        }
    }

    private static void surfaceBubble(BlockState state, Level level, BlockPos pos, RandomSource random) {
        double surface = pos.getY() + BUBBLE_BASE_HEIGHT + BUBBLE_LEVEL_HEIGHT * (state.getFluidState().getAmount() / BUBBLE_LEVEL_DIVISOR);
        int color = ARGB.colorFromFloat(1.0F, BUBBLE_RED_BASE - random.nextFloat() * BUBBLE_RED_SPREAD, 0.0F, BUBBLE_BLUE_BASE + random.nextFloat() * BUBBLE_BLUE_SPREAD);
        float scale = BUBBLE_MIN_SCALE + random.nextFloat() * BUBBLE_SCALE_SPREAD;
        int age = BUBBLE_MIN_AGE + random.nextInt(BUBBLE_AGE_SPREAD);
        SlimyBubbleParticleOptions bubble = new SlimyBubbleParticleOptions(color, BUBBLE_ALPHA, scale, age);
        level.addParticle(bubble, pos.getX() + random.nextDouble(), surface, pos.getZ() + random.nextDouble(), 0.0, 0.0, 0.0);
    }

    private static void softPop(Level level, BlockPos pos, RandomSource random) {
        float volume = POP_MIN_VOLUME + random.nextFloat() * POP_VOLUME_SPREAD;
        float pitch = POP_MIN_PITCH + random.nextFloat() * POP_PITCH_SPREAD;
        level.playLocalSound(pos.getX() + random.nextDouble(), pos.getY() + POP_HEIGHT, pos.getZ() + random.nextDouble(), SoundEvents.LAVA_POP, SoundSource.BLOCKS, volume, pitch, false);
    }
}
