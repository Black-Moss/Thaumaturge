package com.leclowndu93150.thaumaturge.content.decor;

import com.leclowndu93150.thaumaturge.content.particle.BlockRunesParticleOptions;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public final class BlockPavingStone extends BaseEntityBlock {
    public static final MapCodec<BlockPavingStone> CODEC = RecordCodecBuilder
            .mapCodec(instance -> instance.group(Codec.BOOL.fieldOf("barrier").forGetter(block -> block.barrier), propertiesCodec()).apply(instance, BlockPavingStone::new));
    private static final VoxelShape SHAPE = box(0.0, 0.0, 0.0, 16.0, 15.0, 16.0);
    private static final int SPEED_AMPLIFIER = 1;
    private static final int JUMP_AMPLIFIER = 0;
    private static final int STEP_EFFECT_TICKS = 40;
    private static final int COLUMN_HEIGHT = 2;
    private static final double RUNE_CENTER = 0.5;
    private static final double RUNE_HEIGHT = 1.2;
    private static final RuneStyle POWERED_STYLE = new RuneStyle(4, 0.2F, 0.4F, 0.0F, 0.3F, 0.8F, 0.2F, 20, -0.02F);
    private static final RuneStyle ACTIVE_STYLE = new RuneStyle(6, 0.9F, 0.1F, 0.0F, 0.3F, 0.0F, 0.3F, 24, -0.02F);
    private static final RuneStyle WARNING_STYLE = new RuneStyle(1, 0.6F, 0.4F, 0.0F, 0.0F, 0.3F, 0.7F, 20, 0.0F);
    private static final double WARNING_MARGIN = 1.0;
    private static final double WARNING_BASE_HEIGHT = 0.6;
    private static final double WARNING_MIN_SPREAD = 0.8;
    private static final double WARNING_EXTRA_HEIGHT = 0.5;

    private final boolean barrier;

    public BlockPavingStone(boolean barrier, Properties properties) {
        super(properties);
        this.barrier = barrier;
    }

    @Override
    protected MapCodec<BlockPavingStone> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        super.stepOn(level, pos, state, entity);
        if (!barrier && !level.isClientSide() && entity instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.SPEED, STEP_EFFECT_TICKS, SPEED_AMPLIFIER, false, false));
            living.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, STEP_EFFECT_TICKS, JUMP_AMPLIFIER, false, false));
        }
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return barrier ? new BlockEntityBarrierStone(pos, state) : null;
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (!barrier || level.isClientSide()) {
            return null;
        }
        return createTickerHelper(type, TTBlockEntities.BARRIER_STONE.get(), BlockEntityBarrierStone::serverTick);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!barrier) {
            return;
        }
        if (level.hasNeighborSignal(pos)) {
            spawnRunes(level, pos, random, POWERED_STYLE);
        } else if (hasColumn(level, pos)) {
            spawnRunes(level, pos, random, ACTIVE_STYLE);
        } else {
            spawnWarning(level, pos, random);
        }
    }

    private static boolean hasColumn(Level level, BlockPos pos) {
        for (int height = 1; height <= COLUMN_HEIGHT; height++) {
            if (level.getBlockState(pos.above(height)).is(TTBlocks.BARRIER.get())) {
                return true;
            }
        }
        return false;
    }

    private static void spawnRunes(Level level, BlockPos pos, RandomSource random, RuneStyle style) {
        for (int i = 0; i < style.count(); i++) {
            level.addParticle(style.create(random), pos.getX() + RUNE_CENTER, pos.getY() + RUNE_HEIGHT, pos.getZ() + RUNE_CENTER, 0.0, 0.0, 0.0);
        }
    }

    private static void spawnWarning(Level level, BlockPos pos, RandomSource random) {
        List<LivingEntity> nearby = level.getEntitiesOfClass(LivingEntity.class, new AABB(pos).inflate(WARNING_MARGIN), candidate -> !(candidate instanceof Player));
        if (nearby.isEmpty()) {
            return;
        }
        double height = pos.getY() + WARNING_BASE_HEIGHT + random.nextDouble() * Math.max(WARNING_MIN_SPREAD, nearby.getFirst().getEyeHeight()) + WARNING_EXTRA_HEIGHT;
        level.addParticle(WARNING_STYLE.create(random), pos.getX() + RUNE_CENTER, height, pos.getZ() + RUNE_CENTER, 0.0, 0.0, 0.0);
    }

    private record RuneStyle(int count, float redMin, float redRange, float greenMin, float greenRange, float blueMin, float blueRange, int duration, float gravity) {
        BlockRunesParticleOptions create(RandomSource random) {
            return new BlockRunesParticleOptions(redMin + random.nextFloat() * redRange, greenMin + random.nextFloat() * greenRange, blueMin + random.nextFloat() * blueRange, duration, gravity,
                    false);
        }
    }
}
