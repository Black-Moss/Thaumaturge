package com.leclowndu93150.thaumaturge.content.infernalfurnace;

import com.leclowndu93150.thaumaturge.config.ThaumaturgeServerConfig;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class BlockInfernalFurnace extends BaseEntityBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    private static final MapCodec<BlockInfernalFurnace> CODEC = simpleCodec(BlockInfernalFurnace::new);
    private static final VoxelShape COLLISION = Block.box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0);
    private static final VoxelShape OCCLUSION = Shapes.box(-1.0, -1.0, -1.0, 2.0, 2.0, 2.0);
    private static final int SHELL_RADIUS = 1;
    private static final double NUDGE_LOW = 0.3;
    private static final double NUDGE_HIGH = 0.7;
    private static final double NUDGE_SPEED = 0.0001;
    private static final int BURN_INTERVAL = 10;
    private static final float CONTACT_DAMAGE = 3.0F;
    private static final float CONTACT_BURN_SECONDS = 10.0F;
    private static final int BLAZE_REGENERATION_TICKS = 6000;
    private static final int BLAZE_REGENERATION_AMPLIFIER = 2;
    private static final int BLAZE_RESISTANCE_TICKS = 12000;
    private static final int BLAZE_RESISTANCE_AMPLIFIER = 0;
    private static final double BLAZE_CENTER = 0.5;
    private static final double BLAZE_HEIGHT = 1.0;

    public BlockInfernalFurnace(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(getStateDefinition().any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection());
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return COLLISION;
    }

    @Override
    protected VoxelShape getOcclusionShape(BlockState state) {
        return OCCLUSION;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityInfernalFurnace(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, TTBlockEntities.INFERNAL_FURNACE.get(), BlockEntityInfernalFurnace::staticTick);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier, boolean isPrecise) {
        super.entityInside(state, level, pos, entity, effectApplier, isPrecise);
        nudgeToCenter(pos, entity);
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (entity instanceof ItemEntity item) {
            if (serverLevel.getBlockEntity(pos) instanceof BlockEntityInfernalFurnace furnace) {
                item.setItem(furnace.feed(item.getItem()));
            }
        } else if (entity instanceof LivingEntity living && !living.fireImmune() && living.tickCount % BURN_INTERVAL == 0) {
            living.hurtServer(serverLevel, serverLevel.damageSources().lava(), CONTACT_DAMAGE);
            living.igniteForSeconds(CONTACT_BURN_SECONDS);
        }
    }

    private static void nudgeToCenter(BlockPos pos, Entity entity) {
        double pushX = nudge(entity.getX() - pos.getX());
        double pushZ = nudge(entity.getZ() - pos.getZ());
        if (pushX != 0.0 || pushZ != 0.0) {
            entity.setDeltaMovement(entity.getDeltaMovement().add(pushX, 0.0, pushZ));
        }
    }

    private static double nudge(double offset) {
        if (offset < NUDGE_LOW) {
            return NUDGE_SPEED;
        }
        return offset > NUDGE_HIGH ? -NUDGE_SPEED : 0.0;
    }

    @Override
    public void destroy(LevelAccessor level, BlockPos pos, BlockState state) {
        super.destroy(level, pos, state);
        if (!level.isClientSide()) {
            destroyFurnace(level, pos, state, pos);
        }
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        if (level.getBlockEntity(pos) instanceof BlockEntityInfernalFurnace furnace) {
            furnace.spill(level, pos);
        }
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
    }

    protected static void destroyFurnace(LevelAccessor level, BlockPos furnacePos, BlockState furnaceState, BlockPos broken) {
        if (!(level instanceof ServerLevelAccessor serverAccessor)) {
            return;
        }
        restoreShell(level, furnacePos, broken);
        BlockPos outside = furnacePos.relative(furnaceState.getValue(FACING).getOpposite());
        if (!outside.equals(broken) && level.getBlockState(outside).isAir()) {
            level.setBlock(outside, Blocks.IRON_BARS.defaultBlockState(), Block.UPDATE_ALL);
        }
        if (ThaumaturgeServerConfig.INFERNAL_FURNACE_TURN_TO_BLAZE.get()) {
            level.setBlock(furnacePos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            spawnBlaze(serverAccessor, furnacePos);
        } else {
            level.setBlock(furnacePos, Blocks.LAVA.defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    private static void restoreShell(LevelAccessor level, BlockPos furnacePos, BlockPos broken) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = -SHELL_RADIUS; x <= SHELL_RADIUS; x++) {
            for (int y = -SHELL_RADIUS; y <= SHELL_RADIUS; y++) {
                for (int z = -SHELL_RADIUS; z <= SHELL_RADIUS; z++) {
                    cursor.setWithOffset(furnacePos, x, y, z);
                    if (cursor.equals(broken)) {
                        continue;
                    }
                    BlockState current = level.getBlockState(cursor);
                    if (current.is(TTBlocks.NETHER_BRICKS_PLACEHOLDER)) {
                        level.setBlock(cursor.immutable(), Blocks.NETHER_BRICKS.defaultBlockState(), Block.UPDATE_ALL);
                    } else if (current.is(TTBlocks.OBSIDIAN_PLACEHOLDER)) {
                        level.setBlock(cursor.immutable(), Blocks.OBSIDIAN.defaultBlockState(), Block.UPDATE_ALL);
                    }
                }
            }
        }
    }

    private static void spawnBlaze(ServerLevelAccessor level, BlockPos furnacePos) {
        Mob blaze = EntityType.BLAZE.create(level.getLevel(), EntitySpawnReason.TRIGGERED);
        if (blaze == null) {
            return;
        }
        Vec3 position = new Vec3(furnacePos.getX() + BLAZE_CENTER, furnacePos.getY() + BLAZE_HEIGHT, furnacePos.getZ() + BLAZE_CENTER);
        blaze.setPos(position);
        blaze.addEffect(new MobEffectInstance(MobEffects.REGENERATION, BLAZE_REGENERATION_TICKS, BLAZE_REGENERATION_AMPLIFIER));
        blaze.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, BLAZE_RESISTANCE_TICKS, BLAZE_RESISTANCE_AMPLIFIER));
        level.addFreshEntity(blaze);
    }
}
