package com.leclowndu93150.thaumaturge.content.essentia.tube;

import com.leclowndu93150.thaumaturge.api.essentia.EssentiaCapabilities;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaTransport;
import com.leclowndu93150.thaumaturge.content.essentia.spill.EssentiaSpill;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public abstract class BlockEssentiaTransport extends BaseEntityBlock {
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;

    private static final Direction[] DIRECTIONS = Direction.values();
    private static final BooleanProperty[] DECLARED_PROPERTIES = {NORTH, EAST, SOUTH, WEST, UP, DOWN};
    private static final BooleanProperty[] BY_ORDINAL = {DOWN, UP, NORTH, SOUTH, WEST, EAST};
    private static final float LOOK_PARTIAL_TICK = 1.0F;

    private final TubeGeometry geometry;

    protected BlockEssentiaTransport(BlockBehaviour.Properties properties, TubeGeometry geometry) {
        super(properties);
        this.geometry = geometry;
        BlockState unconnected = stateDefinition.any();
        for (BooleanProperty property : DECLARED_PROPERTIES) {
            unconnected = unconnected.setValue(property, false);
        }
        registerDefaultState(unconnected);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos at, CollisionContext ctx) {
        return geometry.shape(state);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter getter, BlockPos at, CollisionContext ctx) {
        return getShape(state, getter, at, ctx);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(DECLARED_PROPERTIES);
    }

    public static int resolveSubHit(BlockState owner, BlockHitResult result, BlockPos origin) {
        Vec3 local = result.getLocation().subtract(origin.getX(), origin.getY(), origin.getZ());
        return geometryOf(owner).subHit(local);
    }

    static @Nullable BlockHitResult traceLook(Level level, Player player, BlockPos pos) {
        HitResult hit = player.pick(player.blockInteractionRange(), LOOK_PARTIAL_TICK, false);
        return hit instanceof BlockHitResult block && block.getType() == HitResult.Type.BLOCK && block.getBlockPos().equals(pos) ? block : null;
    }

    private static TubeGeometry geometryOf(BlockState state) {
        return state.getBlock() instanceof BlockEssentiaTransport transport ? transport.geometry : TubeGeometry.PIPE;
    }

    public static BooleanProperty propertyFor(Direction direction) {
        return BY_ORDINAL[direction.ordinal()];
    }

    public static BlockState recomputeConnections(BlockState state, LevelReader level, BlockPos pos, Direction... skipped) {
        if (level instanceof Level full) {
            BlockState result = state;
            for (Direction side : DIRECTIONS) {
                if (!isSkipped(skipped, side)) {
                    result = result.setValue(propertyFor(side), connects(full, pos, side));
                }
            }
            return result;
        }
        return state;
    }

    public static boolean canConnectTo(Level level, BlockPos neighbourPos, Direction faceFromNeighbour) {
        IEssentiaTransport peer = level.getCapability(EssentiaCapabilities.TRANSPORT, neighbourPos, faceFromNeighbour);
        return peer != null && peer.isConnectable(faceFromNeighbour);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction directionToNeighbour, BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
        if (!(level instanceof Level full)) {
            return state;
        }
        boolean linked = connects(full, pos, directionToNeighbour);
        return state.setValue(propertyFor(directionToNeighbour), linked);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext placement) {
        BlockPos placedAt = placement.getClickedPos();
        return recomputeConnections(defaultBlockState(), placement.getLevel(), placedAt);
    }

    @Override
    public BlockState playerWillDestroy(Level world, BlockPos at, BlockState broken, Player breaker) {
        spillEssentiaOnBreak(world, at);
        return super.playerWillDestroy(world, at, broken, breaker);
    }

    protected static void spillEssentiaOnBreak(Level level, BlockPos pos) {
        if (level instanceof ServerLevel server && level.getBlockEntity(pos) instanceof IEssentiaTransport transport) {
            EssentiaSpill.PIPE.release(server, pos, transport.getEssentiaAmount(null));
        }
    }

    public static void refreshConnectionsAround(LevelAccessor level, BlockPos pos) {
        refreshConnections(level, pos);
        for (Direction side : DIRECTIONS) {
            refreshConnections(level, pos.relative(side));
        }
    }

    public static @Nullable BlockState refreshConnections(LevelAccessor accessor, BlockPos pos) {
        BlockState current = accessor.getBlockState(pos);
        if (!(current.getBlock() instanceof BlockEssentiaTransport)) {
            return null;
        }
        BlockState refreshed = accessor instanceof Level ? recomputeConnections(current, accessor, pos) : current;
        if (!refreshed.equals(current)) {
            accessor.setBlock(pos, refreshed, Block.UPDATE_ALL);
        }
        return refreshed;
    }

    static void syncNeighbourSide(Level world, BlockPos origin, Direction side, boolean open) {
        BlockPos neighbourPos = origin.relative(side);
        Direction back = side.getOpposite();
        switch (world.getBlockEntity(neighbourPos)) {
            case BlockEntityTube tube -> {
                tube.setOpenSide(back, open);
                BlockEntityTube.pushUpdate(tube);
            }
            case BlockEntityTubeBuffer buffer -> buffer.setOpenSide(back, open);
            case null, default -> {
            }
        }
        refreshConnections(world, origin);
        refreshConnections(world, neighbourPos);
    }

    static Direction directionOrNorth(int ordinal) {
        return ordinal >= 0 && ordinal < DIRECTIONS.length ? DIRECTIONS[ordinal] : Direction.NORTH;
    }

    private static boolean connects(Level level, BlockPos pos, Direction side) {
        if (level.getBlockEntity(pos) instanceof IEssentiaTransport transport && !transport.isConnectable(side)) {
            return false;
        }
        return canConnectTo(level, pos.relative(side), side.getOpposite());
    }

    private static boolean isSkipped(Direction[] skipped, Direction side) {
        for (Direction candidate : skipped) {
            if (candidate == side) {
                return true;
            }
        }
        return false;
    }
}
