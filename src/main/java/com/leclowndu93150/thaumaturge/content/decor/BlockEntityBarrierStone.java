package com.leclowndu93150.thaumaturge.content.decor;

import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public final class BlockEntityBarrierStone extends BlockEntity {
    private static final long PHASE_MODULUS = 100L;
    private static final long REPEL_INTERVAL = 5L;
    private static final long RAISE_INTERVAL = 100L;
    private static final double REPEL_COLUMN_EXTRA_HEIGHT = 2.0;
    private static final double REPEL_MARGIN = 0.1;
    private static final double REPEL_HORIZONTAL_SPEED = 0.2;
    private static final double REPEL_VERTICAL_SPEED = -0.1;
    private static final int COLUMN_HEIGHT = 2;

    public BlockEntityBarrierStone(BlockPos pos, BlockState state) {
        super(TTBlockEntities.BARRIER_STONE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityBarrierStone stone) {
        long clock = level.getGameTime() + Math.floorMod(pos.asLong(), PHASE_MODULUS);
        if (clock % REPEL_INTERVAL == 0L && !level.hasNeighborSignal(pos)) {
            repel(level, pos);
        }
        if (clock % RAISE_INTERVAL == 0L) {
            raise(level, pos);
        }
    }

    private static void repel(Level level, BlockPos pos) {
        AABB region = new AABB(pos).expandTowards(0.0, REPEL_COLUMN_EXTRA_HEIGHT, 0.0).inflate(REPEL_MARGIN);
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, region, candidate -> !(candidate instanceof Player) && !candidate.onGround())) {
            Direction away = entity.getDirection().getOpposite();
            entity.push(away.getStepX() * REPEL_HORIZONTAL_SPEED, REPEL_VERTICAL_SPEED, away.getStepZ() * REPEL_HORIZONTAL_SPEED);
        }
    }

    private static void raise(Level level, BlockPos pos) {
        BlockState column = TTBlocks.BARRIER.get().defaultBlockState();
        for (int height = 1; height <= COLUMN_HEIGHT; height++) {
            BlockPos target = pos.above(height);
            if (level.getBlockState(target).isAir()) {
                level.setBlock(target, column, Block.UPDATE_ALL);
            }
        }
    }
}
