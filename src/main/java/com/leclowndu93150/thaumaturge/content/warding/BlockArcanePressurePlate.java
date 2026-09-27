package com.leclowndu93150.thaumaturge.content.warding;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public final class BlockArcanePressurePlate extends PressurePlateBlock {
    public static final MapCodec<BlockArcanePressurePlate> CODEC = simpleCodec(BlockArcanePressurePlate::new);
    public static final int MODE_EVERYONE = 0;
    public static final int MODE_ACCESS_ONLY = 1;
    public static final int MODE_ALL_BUT_ACCESS = 2;
    public static final IntegerProperty MODE = IntegerProperty.create("mode", MODE_EVERYONE, MODE_ALL_BUT_ACCESS);

    private static final int MODE_COUNT = MODE_ALL_BUT_ACCESS + 1;
    private static final int FULL_SIGNAL = 15;
    private static final String[] MODE_MESSAGES = {"message.thaumaturge.arcane_pressure_plate_mode.0", "message.thaumaturge.arcane_pressure_plate_mode.1",
            "message.thaumaturge.arcane_pressure_plate_mode.2"};

    public BlockArcanePressurePlate(Properties properties) {
        super(BlockSetType.OAK, properties);
    }

    @Override
    @SuppressWarnings("unchecked")
    public MapCodec<PressurePlateBlock> codec() {
        return (MapCodec<PressurePlateBlock>) (MapCodec<?>) CODEC;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level instanceof ServerLevel serverLevel && placer instanceof Player player) {
            WardHandler.ward(serverLevel, pos, player.getUUID());
            ArcaneAccess.lock(serverLevel, pos, player.getUUID());
        }
    }

    @Override
    protected int getSignalStrength(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return 0;
        }
        int mode = level.getBlockState(pos).getValue(MODE);
        for (Entity entity : level.getEntitiesOfClass(Entity.class, TOUCH_AABB.move(pos), EntitySelector.NO_SPECTATORS.and(entity -> !entity.isIgnoringBlockTriggers()))) {
            if (triggers(serverLevel, pos, mode, entity)) {
                return FULL_SIGNAL;
            }
        }
        return 0;
    }

    private static boolean triggers(ServerLevel level, BlockPos pos, int mode, Entity entity) {
        if (mode == MODE_EVERYONE) {
            return true;
        }
        boolean hasAccess = entity instanceof Player player && ArcaneAccess.canAccess(level, pos, player);
        return mode == MODE_ACCESS_ONLY ? hasAccess : !hasAccess;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }
        if (!ArcaneAccess.canAccess(serverLevel, pos, player)) {
            return InteractionResult.FAIL;
        }
        int mode = (state.getValue(MODE) + 1) % MODE_COUNT;
        level.setBlock(pos, state.setValue(MODE, mode), UPDATE_CLIENTS);
        player.sendSystemMessage(Component.translatable(MODE_MESSAGES[mode]));
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
        WardHandler.clear(level, pos);
        ArcaneAccess.removeLock(level, pos);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(MODE);
    }
}
