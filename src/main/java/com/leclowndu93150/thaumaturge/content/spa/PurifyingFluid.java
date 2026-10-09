package com.leclowndu93150.thaumaturge.content.spa;

import com.leclowndu93150.thaumaturge.api.warp.WarpHelper;
import com.leclowndu93150.thaumaturge.api.warp.WarpType;
import com.leclowndu93150.thaumaturge.registry.TTMobEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;

public abstract class PurifyingFluid extends BaseFlowingFluid {
    private static final int SOURCE_AMOUNT = 8;
    private static final int DEFAULT_FLOWING_LEVEL = 7;
    private static final double SLOWDOWN_FACTOR = 0.5;
    private static final int WARD_MAX_DURATION = 32000;
    private static final int WARD_BASE_DURATION = 200000;
    private static final int WARD_AMPLIFIER = 0;

    protected PurifyingFluid(Properties properties) {
        super(properties);
    }

    @Override
    protected void entityInside(Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier) {
        if (!entity.blockPosition().equals(pos)) {
            return;
        }
        FluidState state = level.getFluidState(pos);
        if (!isSame(state.getType())) {
            return;
        }
        double damp = 1.0 - (double) state.getAmount() / SOURCE_AMOUNT * SLOWDOWN_FACTOR;
        Vec3 motion = entity.getDeltaMovement();
        entity.setDeltaMovement(motion.x * damp, motion.y, motion.z * damp);
        if (!level.isClientSide() && state.isSource() && entity instanceof ServerPlayer player && !player.hasEffect(TTMobEffects.WARP_WARD)) {
            grantWard(level, pos, player);
        }
    }

    private static void grantWard(Level level, BlockPos pos, ServerPlayer player) {
        int permanent = WarpHelper.getWarp(player).get(WarpType.PERMANENT);
        int divisor = permanent <= 0 ? 1 : Math.max(1, (int) Math.sqrt(permanent));
        int duration = Math.min(WARD_MAX_DURATION, WARD_BASE_DURATION / divisor);
        player.addEffect(new MobEffectInstance(TTMobEffects.WARP_WARD, duration, WARD_AMPLIFIER, true, true));
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
    }

    public static final class Source extends PurifyingFluid {
        public Source(Properties properties) {
            super(properties);
        }

        @Override
        public int getAmount(FluidState state) {
            return SOURCE_AMOUNT;
        }

        @Override
        public boolean isSource(FluidState state) {
            return true;
        }
    }

    public static final class Flowing extends PurifyingFluid {
        public Flowing(Properties properties) {
            super(properties);
            registerDefaultState(getStateDefinition().any().setValue(LEVEL, DEFAULT_FLOWING_LEVEL));
        }

        @Override
        protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }

        @Override
        public int getAmount(FluidState state) {
            return state.getValue(LEVEL);
        }

        @Override
        public boolean isSource(FluidState state) {
            return false;
        }
    }
}
