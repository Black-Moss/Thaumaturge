package com.leclowndu93150.thaumaturge.content.essentia.tube;

import com.leclowndu93150.thaumaturge.content.device.DeviceShapes;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import com.mojang.serialization.MapCodec;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public final class BlockTubeValve extends BlockTube {
    public static final MapCodec<BlockTubeValve> CODEC = simpleCodec(BlockTubeValve::new);

    private static final double OPEN_DROP = 0.5;
    private static final double CLOSED_DROP = 2.0;
    private static final double STEM_MIN = 7.0;
    private static final double STEM_MAX = 9.0;
    private static final double STEM_BOTTOM = 10.0;
    private static final double STEM_TOP = 14.0;
    private static final double CAP_MIN = 5.0;
    private static final double CAP_MAX = 11.0;
    private static final double CAP_BOTTOM = 13.0;
    private static final double CAP_TOP = 15.0;
    private static final float SQUEEK_VOLUME = 0.7F;
    private static final float SQUEEK_PITCH_MIN = 0.9F;
    private static final float SQUEEK_PITCH_SPREAD = 0.2F;
    private static final Map<Direction, VoxelShape> OPEN_HEADS = DeviceShapes.facingShapesFromUp(headShape(OPEN_DROP));
    private static final Map<Direction, VoxelShape> CLOSED_HEADS = DeviceShapes.facingShapesFromUp(headShape(CLOSED_DROP));

    public BlockTubeValve(BlockBehaviour.Properties properties) {
        super(properties);
    }

    private static VoxelShape headShape(double drop) {
        VoxelShape stem = box(STEM_MIN, STEM_BOTTOM, STEM_MIN, STEM_MAX, STEM_TOP - drop, STEM_MAX);
        VoxelShape cap = box(CAP_MIN, CAP_BOTTOM - drop, CAP_MIN, CAP_MAX, CAP_TOP - drop, CAP_MAX);
        return Shapes.or(stem, cap);
    }

    @Override
    protected MapCodec<? extends BlockTube> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape base = super.getShape(state, level, pos, context);
        if (!(level.getBlockEntity(pos) instanceof BlockEntityTubeValve valve)) {
            return base;
        }
        Map<Direction, VoxelShape> heads = valve.allowFlow() ? OPEN_HEADS : CLOSED_HEADS;
        return Shapes.or(base, heads.get(valve.flowSide()));
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityTubeValve(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        BlockEntityType<BlockEntityTubeValve> valveType = TTBlockEntities.TUBE_VALVE.get();
        if (level.isClientSide()) {
            return createTickerHelper(type, valveType, (lvl, pos, st, valve) -> valve.tickClient(lvl, pos, st));
        }
        return createTickerHelper(type, valveType, (lvl, pos, st, valve) -> valve.tickServer(lvl, pos, st));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof BlockEntityTubeValve valve)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            valve.setAllowFlow(!valve.allowFlow());
            float pitch = SQUEEK_PITCH_MIN + level.getRandom().nextFloat() * SQUEEK_PITCH_SPREAD;
            level.playSound(null, pos, TTSounds.SQUEEK.get(), SoundSource.BLOCKS, SQUEEK_VOLUME, pitch);
        }
        return InteractionResult.SUCCESS;
    }
}
