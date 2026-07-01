package com.leclowndu93150.thaumcraft.content.taint.flux;

import com.leclowndu93150.thaumcraft.api.aura.AuraHelper;
import com.leclowndu93150.thaumcraft.api.entity.ITaintedMob;
import com.leclowndu93150.thaumcraft.content.entity.ThaumicSlime;
import com.leclowndu93150.thaumcraft.content.taint.FluxImmunityHelper;
import com.leclowndu93150.thaumcraft.registry.TCBlocks;
import com.leclowndu93150.thaumcraft.registry.TCEntities;
import com.leclowndu93150.thaumcraft.registry.TCMobEffects;
import com.leclowndu93150.thaumcraft.registry.TCSounds;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;

public abstract class FluxGooFluid extends BaseFlowingFluid {
    private static final int SLIME_SPAWN_CHANCE = 50;
    private static final int DECAY_ROLL_CHANCE = 4;
    private static final int SMALL_SLIME_AMOUNT_MIN = 2;
    private static final int SMALL_SLIME_AMOUNT_MAX = 5;
    private static final int LARGE_SLIME_AMOUNT_MIN = 6;
    private static final int VIS_EXHAUST_DURATION = 600;
    private static final float POLLUTE_AMOUNT = 1.0F;
    private static final float MAX_AMOUNT = 8.0F;

    protected FluxGooFluid(Properties properties) {
        super(properties);
    }

    @Override
    protected boolean isRandomlyTicking() {
        return true;
    }

    @Override
    protected void randomTick(ServerLevel level, BlockPos pos, FluidState state, RandomSource random) {
        int amount = state.getAmount();
        boolean airAbove = level.getBlockState(pos.above()).isAir();

        if (amount >= SMALL_SLIME_AMOUNT_MIN && amount <= SMALL_SLIME_AMOUNT_MAX
                && airAbove && random.nextInt(SLIME_SPAWN_CHANCE) == 0) {
            spawnSlime(level, pos, 1);
            return;
        }
        if (amount >= LARGE_SLIME_AMOUNT_MIN && airAbove && random.nextInt(SLIME_SPAWN_CHANCE) == 0) {
            spawnSlime(level, pos, 2);
            return;
        }
        if (random.nextInt(DECAY_ROLL_CHANCE) == 0) {
            if (amount <= 1) {
                AuraHelper.polluteAura(level, pos, POLLUTE_AMOUNT, true);
                if (random.nextBoolean()) {
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                } else {
                    level.setBlock(pos, taintFibreState(), Block.UPDATE_ALL);
                }
            } else {
                AuraHelper.polluteAura(level, pos, POLLUTE_AMOUNT, true);
                FluidState dropped = getFlowing(amount - 1, false);
                level.setBlock(pos, dropped.createLegacyBlock(), Block.UPDATE_ALL);
                level.scheduleTick(pos, dropped.getType(), getTickDelay(level));
            }
        }
    }

    @Override
    protected void entityInside(Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier) {
        FluidState fs = level.getFluidState(pos);
        int amount = fs.getAmount();
        if (entity instanceof LivingEntity living) {
            if (!(living instanceof ITaintedMob) && !FluxImmunityHelper.isImmune(living)) {
                int amp = Math.max(0, amount / 3);
                living.addEffect(new MobEffectInstance(TCMobEffects.VIS_EXHAUST,
                        VIS_EXHAUST_DURATION, amp, true, true, false));
            }
        }
        float quanta = amount / MAX_AMOUNT;
        Vec3 motion = entity.getDeltaMovement();
        double damp = 1.0 - quanta;
        entity.setDeltaMovement(motion.x * damp, motion.y, motion.z * damp);
    }

    private static BlockState taintFibreState() {
        return TCBlocks.TAINT_FIBRE.get().defaultBlockState();
    }

    private static void spawnSlime(ServerLevel level, BlockPos pos, int size) {
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        ThaumicSlime slime = TCEntities.THAUMIC_SLIME.get().create(level, EntitySpawnReason.NATURAL);
        if (slime == null) {
            return;
        }
        slime.setSize(size, true);
        slime.setPos(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        level.addFreshEntity(slime);
        level.playSound(null, pos, TCSounds.GORE.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    public static final class Source extends FluxGooFluid {
        public Source(Properties properties) {
            super(properties);
        }

        @Override
        public int getAmount(FluidState state) {
            return 8;
        }

        @Override
        public boolean isSource(FluidState state) {
            return true;
        }
    }

    public static final class Flowing extends FluxGooFluid {
        public Flowing(Properties properties) {
            super(properties);
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
