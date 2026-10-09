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
    private static final int BUBBLE_ONE_IN = 10;
    private static final int POP_ONE_IN = 50;
    private static final double LEVEL_HEIGHT = 0.125;
    private static final int BUBBLE_COLOR = 0xFFFFFF;
    private static final float BUBBLE_ALPHA = 0.25F;
    private static final float SCALE_BASE = 0.3F;
    private static final float SCALE_SPREAD = 0.3F;
    private static final int AGE_BASE = 10;
    private static final int AGE_VARIATION = 10;
    private static final float BUBBLE_BUOYANCY = -0.01F;
    private static final double POP_HEIGHT = 0.5;
    private static final float POP_VOLUME_BASE = 0.1F;
    private static final float POP_VOLUME_SPREAD = 0.1F;
    private static final float POP_PITCH_BASE = 0.9F;
    private static final float POP_PITCH_SPREAD = 0.15F;

    public BlockPurifyingFluid(FlowingFluid fluid, BlockBehaviour.Properties properties) {
        super(fluid, properties);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(BUBBLE_ONE_IN) == 0) {
            spawnBubble(state, level, pos, random);
        }
        if (random.nextInt(POP_ONE_IN) == 0) {
            playPop(level, pos, random);
        }
    }

    private static void spawnBubble(BlockState state, Level level, BlockPos pos, RandomSource random) {
        double x = pos.getX() + random.nextDouble();
        double y = pos.getY() + LEVEL_HEIGHT * state.getFluidState().getAmount();
        double z = pos.getZ() + random.nextDouble();
        float scale = SCALE_BASE + random.nextFloat() * SCALE_SPREAD;
        int age = AGE_BASE + random.nextInt(AGE_VARIATION);
        level.addParticle(new BubbleParticleOptions(BUBBLE_COLOR, BUBBLE_ALPHA, scale, age, BUBBLE_BUOYANCY, false), x, y, z, 0.0, 0.0, 0.0);
    }

    private static void playPop(Level level, BlockPos pos, RandomSource random) {
        double x = pos.getX() + random.nextDouble();
        double y = pos.getY() + POP_HEIGHT;
        double z = pos.getZ() + random.nextDouble();
        float volume = POP_VOLUME_BASE + random.nextFloat() * POP_VOLUME_SPREAD;
        float pitch = POP_PITCH_BASE + random.nextFloat() * POP_PITCH_SPREAD;
        level.playLocalSound(x, y, z, SoundEvents.LAVA_POP, SoundSource.BLOCKS, volume, pitch, false);
    }
}
