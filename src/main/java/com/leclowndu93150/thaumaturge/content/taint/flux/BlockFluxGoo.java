package com.leclowndu93150.thaumaturge.content.taint.flux;

import com.leclowndu93150.thaumaturge.content.particle.BubbleParticleOptions;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ARGB;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import org.jspecify.annotations.Nullable;

public final class BlockFluxGoo extends LiquidBlock implements PhysicalFluxBlock {
    public static final MapCodec<LiquidBlock> CODEC = simpleCodec(properties -> new BlockFluxGoo(FluxGooRefs.sourceFluid(), properties));

    private static final int REPLACEABLE_AMOUNT = 2;
    private static final float AURA_FLOOR_PER_QUANTUM = 0.5F;
    private static final float TAINT_WEIGHT_PER_QUANTUM = 1.0F;
    private static final int OUTBREAK_COST = 0;
    private static final int FUME_ROLL_RANGE = 44;
    private static final double FUME_HEIGHT_PER_STEP = 0.125;
    private static final float FUME_RED = 1.0F;
    private static final float FUME_GREEN = 0.0F;
    private static final float FUME_BLUE = 0.5F;
    private static final float FUME_COLOR_ALPHA = 1.0F;
    private static final float FUME_ALPHA = 0.25F;
    private static final float FUME_MIN_SCALE = 0.2F;
    private static final float FUME_SCALE_SPREAD = 0.3F;
    private static final int FUME_MIN_AGE = 2;
    private static final int FUME_AGE_SPREAD = 3;
    private static final float FUME_BUOYANCY = -0.01F;

    public BlockFluxGoo(FlowingFluid fluid, BlockBehaviour.Properties properties) {
        super(fluid, properties);
    }

    @Override
    public MapCodec<LiquidBlock> codec() {
        return CODEC;
    }

    @Override
    protected boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        return fluxAmount(state) <= REPLACEABLE_AMOUNT;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        int steps = fluxAmount(state) - 1;
        if (random.nextInt(FUME_ROLL_RANGE) > steps) {
            return;
        }
        int color = ARGB.colorFromFloat(FUME_COLOR_ALPHA, FUME_RED, FUME_GREEN, FUME_BLUE);
        float scale = FUME_MIN_SCALE + random.nextFloat() * FUME_SCALE_SPREAD;
        int age = FUME_MIN_AGE + random.nextInt(FUME_AGE_SPREAD);
        level.addParticle(new BubbleParticleOptions(color, FUME_ALPHA, scale, age, FUME_BUOYANCY, false), pos.getX() + random.nextDouble(), pos.getY() + FUME_HEIGHT_PER_STEP * steps,
                pos.getZ() + random.nextDouble(), 0.0, 0.0, 0.0);
    }

    @Override
    public int fluxAmount(BlockState state) {
        return Math.clamp(state.getFluidState().getAmount(), 1, PhysicalFlux.MAX_QUANTA);
    }

    @Override
    public BlockState withFluxAmount(int amount) {
        return FluxGooFluid.gooBlockState(amount);
    }

    @Override
    public void scheduleFluxTick(ServerLevel level, BlockPos pos) {
        FluidState fluidState = level.getFluidState(pos);
        if (!fluidState.isEmpty()) {
            level.scheduleTick(pos, fluidState.getType(), fluidState.getType().getTickDelay(level));
        }
    }

    @Override
    public float auraFloorPerQuantum() {
        return AURA_FLOOR_PER_QUANTUM;
    }

    @Override
    public float taintWeightPerQuantum() {
        return TAINT_WEIGHT_PER_QUANTUM;
    }

    @Override
    public int outbreakCost() {
        return OUTBREAK_COST;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (level instanceof ServerLevel serverLevel) {
            PhysicalFluxAuraFloor.observe(serverLevel, pos);
        }
    }

    @Override
    public ItemStack pickupBlock(@Nullable LivingEntity user, LevelAccessor level, BlockPos pos, BlockState state) {
        return ItemStack.EMPTY;
    }
}
