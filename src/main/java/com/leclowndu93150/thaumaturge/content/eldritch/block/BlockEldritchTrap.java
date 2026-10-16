package com.leclowndu93150.thaumaturge.content.eldritch.block;

import com.leclowndu93150.thaumaturge.content.particle.BlockRunesParticleOptions;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public final class BlockEldritchTrap extends BaseEntityBlock {
    public static final MapCodec<BlockEldritchTrap> CODEC = simpleCodec(BlockEldritchTrap::new);

    private static final float CELL_CENTER = 0.5F;
    private static final float NO_GRAVITY = 0.0F;
    private static final int OFFSET_ROLL_BOUND = 2;
    private static final float RED_BASE = 0.5F;
    private static final float RED_SPREAD = 0.5F;
    private static final float GREEN_MAX = 0.3F;
    private static final float BLUE_BASE = 0.9F;
    private static final float BLUE_SPREAD = 0.1F;
    private static final int DURATION_BASE = 16;
    private static final int DURATION_SPREAD = 4;

    public BlockEldritchTrap(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<BlockEldritchTrap> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        BlockPos cell = pos.offset(offset(random), offset(random), offset(random));
        if (!level.getBlockState(cell).isAir()) {
            return;
        }
        float red = RED_BASE + random.nextFloat() * RED_SPREAD;
        float green = random.nextFloat() * GREEN_MAX;
        float blue = BLUE_BASE + random.nextFloat() * BLUE_SPREAD;
        int duration = DURATION_BASE + random.nextInt(DURATION_SPREAD);
        BlockRunesParticleOptions rune = new BlockRunesParticleOptions(red, green, blue, duration, NO_GRAVITY, false);
        level.addParticle(rune, cell.getX() + CELL_CENTER, cell.getY() + CELL_CENTER, cell.getZ() + CELL_CENTER, 0.0, 0.0, 0.0);
    }

    private static int offset(RandomSource random) {
        return random.nextInt(OFFSET_ROLL_BOUND) - random.nextInt(OFFSET_ROLL_BOUND);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityEldritchTrap(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        return createTickerHelper(type, TTBlockEntities.ELDRITCH_TRAP.get(), (tickLevel, pos, tickState, trap) -> trap.serverTick(tickLevel, pos));
    }

}
