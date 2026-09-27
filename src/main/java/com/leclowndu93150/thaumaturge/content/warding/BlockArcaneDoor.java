package com.leclowndu93150.thaumaturge.content.warding;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public final class BlockArcaneDoor extends DoorBlock {
    public static final MapCodec<BlockArcaneDoor> CODEC = simpleCodec(BlockArcaneDoor::new);

    public BlockArcaneDoor(Properties properties) {
        super(BlockSetType.OAK, properties);
    }

    @Override
    public MapCodec<BlockArcaneDoor> codec() {
        return CODEC;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level instanceof ServerLevel serverLevel && placer instanceof Player player) {
            WardHandler.ward(serverLevel, pos, player.getUUID());
            WardHandler.ward(serverLevel, pos.above(), player.getUUID());
            ArcaneAccess.lock(serverLevel, pos, player.getUUID());
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }
        if (!ArcaneAccess.canAccess(serverLevel, ArcaneAccess.lockOrigin(state, pos), player)) {
            return InteractionResult.FAIL;
        }
        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
        WardHandler.clear(level, pos);
        ArcaneAccess.removeLock(level, ArcaneAccess.lockOrigin(state, pos));
    }
}
