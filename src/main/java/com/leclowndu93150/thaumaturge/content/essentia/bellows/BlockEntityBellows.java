package com.leclowndu93150.thaumaturge.content.essentia.bellows;

import com.leclowndu93150.thaumaturge.content.essentia.IBellowsPower;
import com.leclowndu93150.thaumaturge.mixin.world.level.block.entity.AbstractFurnaceBlockEntityAccessor;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class BlockEntityBellows extends BlockEntity implements IBellowsPower {
    private static final float PUFF_VOLUME = 0.02F;
    private static final float PUFF_BASE_PITCH = 0.5F;
    private static final float PUFF_PITCH_SPREAD = 0.08F;
    private static final int BOOST_INTERVAL = 2;
    private static final int BOOST_STEP = 1;
    private static final int BOOST_MARGIN = 5;

    public BlockEntityBellows(BlockPos pos, BlockState state) {
        super(TTBlockEntities.BELLOWS.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityBellows bellows) {
        if (!state.getValue(BlockBellows.ENABLED)) {
            return;
        }
        long gameTime = level.getGameTime();
        if (BellowsStroke.startsSqueeze(gameTime, pos)) {
            RandomSource random = level.getRandom();
            level.playSound(null, pos, SoundEvents.GHAST_SHOOT, SoundSource.BLOCKS, PUFF_VOLUME, PUFF_BASE_PITCH + (random.nextFloat() - random.nextFloat()) * PUFF_PITCH_SPREAD);
        }
        if (gameTime % BOOST_INTERVAL == 0) {
            bellows.boostFurnace(level, pos.relative(state.getValue(BlockBellows.FACING)));
        }
    }

    private void boostFurnace(Level level, BlockPos target) {
        if (!(level.getBlockEntity(target) instanceof AbstractFurnaceBlockEntity furnace)) {
            return;
        }
        AbstractFurnaceBlockEntityAccessor access = (AbstractFurnaceBlockEntityAccessor) furnace;
        int progress = access.thaumaturge$getCookTime();
        if (progress > 0 && progress < access.thaumaturge$getCookTimeTotal() - BOOST_MARGIN) {
            access.thaumaturge$setCookTime(progress + BOOST_STEP);
            furnace.setChanged();
        }
    }

    public float inflation(float partialTick) {
        if (level == null || !bellowsEnabled()) {
            return BellowsStroke.FULL;
        }
        return BellowsStroke.inflation(level.getGameTime(), partialTick, worldPosition);
    }

    @Override
    public Direction bellowsFacing() {
        return getBlockState().getValue(BlockBellows.FACING);
    }

    @Override
    public boolean bellowsEnabled() {
        return getBlockState().getValue(BlockBellows.ENABLED);
    }
}
