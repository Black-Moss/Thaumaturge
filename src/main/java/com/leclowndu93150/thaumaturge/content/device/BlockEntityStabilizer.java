package com.leclowndu93150.thaumaturge.content.device;

import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.content.entity.EntityFluxRift;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;

public final class BlockEntityStabilizer extends BlockEntity {
    private static final int MAX_ENERGY = 15;
    private static final int CHARGE_INTERVAL = 20;
    private static final int STABILISE_INTERVAL = 5;
    private static final int STABILISE_DELAY = 5;
    private static final double RIFT_RANGE = 8.0;
    private static final float CHARGE_POLLUTION = 0.25F;
    private static final String ENERGY_KEY = "energy";

    private int energy;
    private int ticks;
    private int delay;

    public BlockEntityStabilizer(BlockPos pos, BlockState state) {
        super(TTBlockEntities.STABILIZER.get(), pos, state);
    }

    public int getEnergy() {
        return energy;
    }

    public boolean mitigate(int amount) {
        if (energy < amount) {
            return false;
        }
        energy -= amount;
        energyChanged();
        return true;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityStabilizer stabilizer) {
        stabilizer.ticks++;
        if (stabilizer.ticks % CHARGE_INTERVAL == 0 && stabilizer.energy < MAX_ENERGY) {
            stabilizer.energy++;
            AuraHelper.polluteAura(level, pos, CHARGE_POLLUTION, true);
            stabilizer.energyChanged();
        }
        if (stabilizer.ticks % STABILISE_INTERVAL == 0 && stabilizer.energy > 0 && stabilizer.delay <= 0) {
            stabilizer.stabilise(level, pos);
        }
        if (stabilizer.delay > 0) {
            stabilizer.delay--;
        }
    }

    private void stabilise(Level level, BlockPos pos) {
        for (EntityFluxRift rift : level.getEntitiesOfClass(EntityFluxRift.class, new AABB(pos).inflate(RIFT_RANGE))) {
            if (energy <= 0) {
                return;
            }
            if (rift.isRemoved() || rift.stabilityTier() == EntityFluxRift.Stability.VERY_STABLE) {
                continue;
            }
            energy--;
            rift.nudgeStability();
            delay += STABILISE_DELAY;
            energyChanged();
        }
    }

    private void energyChanged() {
        setChanged();
        if (level != null) {
            level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        energy = Math.min(MAX_ENERGY, input.getIntOr(ENERGY_KEY, 0));
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt(ENERGY_KEY, energy);
    }
}
