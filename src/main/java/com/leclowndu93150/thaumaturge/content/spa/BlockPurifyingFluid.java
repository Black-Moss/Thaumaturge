package com.leclowndu93150.thaumaturge.content.spa;

import com.leclowndu93150.thaumaturge.content.particle.BubbleParticleOptions;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;

public class BlockPurifyingFluid extends LiquidBlock {
    private static final double LEVEL_HEIGHT = 0.125;
    private static final int BUBBLE_COLOR = 0xFFFFFF;
    private static final int BUBBLE_ODDS = 10;
    private static final float BUBBLE_ALPHA = 0.3F;
    private static final float BUBBLE_MIN_SCALE = 0.6F;
    private static final float BUBBLE_SCALE_SPREAD = 0.8F;
    private static final int BUBBLE_MIN_AGE = 10;
    private static final int BUBBLE_AGE_SPREAD = 10;
    private static final float BUBBLE_BUOYANCY = -0.02F;
    private static final double BUBBLE_RISE = 0.01;
    private static final int POP_ODDS = 50;
    private static final double POP_HEIGHT = 0.5;
    private static final float POP_MIN_VOLUME = 0.15F;
    private static final float POP_VOLUME_SPREAD = 0.15F;
    private static final float POP_MIN_PITCH = 0.9F;
    private static final float POP_PITCH_SPREAD = 0.2F;

    public BlockPurifyingFluid(FlowingFluid fluid, BlockBehaviour.Properties properties) {
        super(fluid, properties);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(BUBBLE_ODDS) == 0) {
            spawnBubble(state, level, pos, random);
        }
        if (random.nextInt(POP_ODDS) == 0) {
            playPop(level, pos, random);
        }
    }

    private static void spawnBubble(BlockState state, Level level, BlockPos pos, RandomSource random) {
        double surface = pos.getY() + state.getFluidState().getAmount() * LEVEL_HEIGHT;
        float scale = BUBBLE_MIN_SCALE + random.nextFloat() * BUBBLE_SCALE_SPREAD;
        int age = BUBBLE_MIN_AGE + random.nextInt(BUBBLE_AGE_SPREAD);
        BubbleParticleOptions bubble = new BubbleParticleOptions(BUBBLE_COLOR, BUBBLE_ALPHA, scale, age, BUBBLE_BUOYANCY, false);
        level.addParticle(bubble, pos.getX() + random.nextDouble(), surface, pos.getZ() + random.nextDouble(), 0.0, BUBBLE_RISE, 0.0);
    }

    private static void playPop(Level level, BlockPos pos, RandomSource random) {
        float volume = POP_MIN_VOLUME + random.nextFloat() * POP_VOLUME_SPREAD;
        float pitch = POP_MIN_PITCH + random.nextFloat() * POP_PITCH_SPREAD;
        level.playLocalSound(pos.getX() + random.nextDouble(), pos.getY() + POP_HEIGHT, pos.getZ() + random.nextDouble(), SoundEvents.LAVA_POP, SoundSource.BLOCKS, volume, pitch, false);
    }

}
