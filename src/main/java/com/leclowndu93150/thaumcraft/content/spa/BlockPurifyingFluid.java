package com.leclowndu93150.thaumcraft.content.spa;

import com.leclowndu93150.thaumcraft.content.fx.data.FXGenericData;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;

public class BlockPurifyingFluid extends LiquidBlock {
    private static final int MOTE_CHANCE = 10;
    private static final int POP_CHANCE = 50;
    private static final int MOTE_FRAME = 64;
    private static final float LEVEL_HEIGHT = 0.125F;
    private static final int MAX_AMOUNT = 8;

    public BlockPurifyingFluid(FlowingFluid fluid, Properties properties) {
        super(fluid, properties);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(MOTE_CHANCE) == 0) {
            int amount = level.getFluidState(pos).getAmount();
            FXGenericData data = FXGenericData.builder()
                    .maxAge(10 + random.nextInt(10))
                    .scale(random.nextFloat() * 0.3F + 0.3F)
                    .color(1.0F, 1.0F, 1.0F)
                    .random(0.001F, 0.001F, 0.001F)
                    .gravity(-0.01F)
                    .alpha(0.25F)
                    .particle(MOTE_FRAME)
                    .finalFrames(65, 66)
                    .build();
            level.addParticle(data,
                    pos.getX() + random.nextFloat(),
                    pos.getY() + LEVEL_HEIGHT * amount,
                    pos.getZ() + random.nextFloat(),
                    0.0, 0.0, 0.0);
        }
        if (random.nextInt(POP_CHANCE) == 0) {
            level.playLocalSound(
                    pos.getX() + random.nextFloat(), pos.getY() + 0.5, pos.getZ() + random.nextFloat(),
                    SoundEvents.LAVA_POP, SoundSource.BLOCKS,
                    0.1F + random.nextFloat() * 0.1F, 0.9F + random.nextFloat() * 0.15F, false);
        }
    }
}
