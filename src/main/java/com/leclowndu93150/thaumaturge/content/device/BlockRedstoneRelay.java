package com.leclowndu93150.thaumaturge.content.device;

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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public final class BlockRedstoneRelay extends DiodeBlock implements EntityBlock {
    public static final MapCodec<BlockRedstoneRelay> CODEC = simpleCodec(BlockRedstoneRelay::new);

    private static final int DELAY_TICKS = 2;
    private static final int DEFAULT_IN = 1;
    private static final int DEFAULT_OUT = 15;
    private static final float CLICK_VOLUME = 0.5F;
    private static final float CLICK_PITCH = 1.0F;
    private static final int SOUTH_TO_NORTH_QUARTERS = 2;
    private static final Map<Direction, VoxelShape> SHAPES = DeviceShapes
            .facingShapesFromNorth(DeviceShapes.rotate(Shapes.or(box(0.0, 0.0, 0.0, 16.0, 1.0, 16.0), box(1.0, 1.0, 1.0, 15.0, 2.0, 15.0), box(2.0, 2.0, 2.0, 6.0, 3.0, 6.0),
                    box(3.0, 3.0, 3.0, 5.0, 4.0, 5.0), box(6.0, 2.0, 10.0, 10.0, 3.0, 14.0), box(7.0, 2.0, 3.0, 9.0, 7.0, 5.0), box(7.0, 3.0, 11.0, 9.0, 4.0, 13.0)), 0, SOUTH_TO_NORTH_QUARTERS));

    public BlockRedstoneRelay(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(getStateDefinition().any().setValue(FACING, Direction.NORTH).setValue(POWERED, false));
    }

    @Override
    public MapCodec<BlockRedstoneRelay> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING));
    }

    @Override
    protected int getDelay(BlockState state) {
        return DELAY_TICKS;
    }

    @Override
    protected boolean shouldTurnOn(Level level, BlockPos pos, BlockState state) {
        int needed = level.getBlockEntity(pos) instanceof BlockEntityRedstoneRelay relay ? relay.getIn() : DEFAULT_IN;
        return getInputSignal(level, pos, state) >= needed;
    }

    @Override
    protected int getOutputSignal(BlockGetter level, BlockPos pos, BlockState state) {
        return level.getBlockEntity(pos) instanceof BlockEntityRedstoneRelay relay ? relay.getOut() : DEFAULT_OUT;
    }

    private static void adjustSetting(BlockEntityRedstoneRelay relay, Direction facing, Vec3 offsetFromCenter) {
        double towardFront = offsetFromCenter.x * facing.getStepX() + offsetFromCenter.z * facing.getStepZ();
        if (towardFront < 0.0) {
            relay.increaseOut();
            return;
        }
        relay.increaseIn();
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!player.mayBuild()) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof BlockEntityRedstoneRelay relay) {
            adjustSetting(relay, state.getValue(FACING), hitResult.getLocation().subtract(Vec3.atCenterOf(pos)));
            level.playSound(null, pos, TTSounds.KEY.get(), SoundSource.BLOCKS, CLICK_VOLUME, CLICK_PITCH);
            checkTickOnNeighbor(level, pos, state);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityRedstoneRelay(pos, state);
    }
}
