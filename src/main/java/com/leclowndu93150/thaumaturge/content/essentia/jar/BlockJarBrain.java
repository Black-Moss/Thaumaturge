package com.leclowndu93150.thaumaturge.content.essentia.jar;

import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public final class BlockJarBrain extends BaseEntityBlock {
    public static final MapCodec<BlockJarBrain> CODEC = simpleCodec(BlockJarBrain::new);

    private static final int EAT_DELAY_TICKS = 40;
    private static final int MAX_RELEASE = 63;
    private static final float CLICK_VOLUME = 0.2F;
    private static final float CLICK_PITCH = 1.0F;

    public BlockJarBrain(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<BlockJarBrain> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return BlockJar.SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return BlockJar.SHAPE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityJarBrain(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof BlockEntityJarBrain brain)) {
            return InteractionResult.PASS;
        }
        brain.setEatDelay(EAT_DELAY_TICKS);
        if (level instanceof ServerLevel server) {
            int release = server.getRandom().nextInt(Math.min(brain.xp(), MAX_RELEASE) + 1);
            if (release > 0) {
                brain.setXp(brain.xp() - release);
                ExperienceOrb.award(server, pos.getCenter(), release);
                brain.setChangedAndSync();
            }
            return InteractionResult.SUCCESS_SERVER;
        }
        level.playSound(player, pos, TTSounds.JAR.get(), SoundSource.BLOCKS, CLICK_VOLUME, CLICK_PITCH);
        return InteractionResult.SUCCESS;
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide()
                ? createTickerHelper(type, TTBlockEntities.JAR_BRAIN.get(), BlockEntityJarBrain::clientTick)
                : createTickerHelper(type, TTBlockEntities.JAR_BRAIN.get(), BlockEntityJarBrain::serverTick);
    }
}
