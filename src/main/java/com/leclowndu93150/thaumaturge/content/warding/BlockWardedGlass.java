package com.leclowndu93150.thaumaturge.content.warding;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public final class BlockWardedGlass extends TransparentBlock {
    public static final MapCodec<BlockWardedGlass> CODEC = simpleCodec(BlockWardedGlass::new);

    public BlockWardedGlass(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<BlockWardedGlass> codec() {
        return CODEC;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level instanceof ServerLevel serverLevel && placer instanceof Player player) {
            WardHandler.ward(serverLevel, pos, player.getUUID());
        }
    }
}
