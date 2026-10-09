package com.leclowndu93150.thaumaturge.content.device;

import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public final class BlockEntityVisGenerator extends BlockEntity implements EnergyHandler {
    private static final int CAPACITY = 1000;
    private static final int OUTPUT_PER_TICK = 20;
    private static final float VIS_PER_REFILL = 1.0F;
    private static final String ENERGY_KEY = "energy";

    private int energy;

    public BlockEntityVisGenerator(BlockPos pos, BlockState state) {
        super(TTBlockEntities.VIS_GENERATOR.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityVisGenerator generator) {
        if (!state.getValue(BlockStateProperties.ENABLED)) {
            return;
        }
        if (generator.energy == 0) {
            generator.refill(level, pos);
        }
        generator.push(level, pos, state.getValue(BlockStateProperties.FACING));
    }

    private void refill(Level level, BlockPos pos) {
        float vis = AuraHelper.drainVis(level, pos, VIS_PER_REFILL, false);
        energy = (int) (vis * CAPACITY);
        if (energy > 0) {
            setChanged();
        }
    }

    private void push(Level level, BlockPos pos, Direction facing) {
        if (energy <= 0) {
            return;
        }
        EnergyHandler target = level.getCapability(Capabilities.Energy.BLOCK, pos.relative(facing), facing.getOpposite());
        if (target == null) {
            return;
        }
        try (Transaction transaction = Transaction.openRoot()) {
            int accepted = target.insert(Math.min(energy, OUTPUT_PER_TICK), transaction);
            if (accepted > 0) {
                transaction.commit();
                energy -= accepted;
                setChanged();
            }
        }
    }

    public Direction outputFace() {
        return getBlockState().getValue(BlockStateProperties.FACING);
    }

    @Override
    public long getAmountAsLong() {
        return energy;
    }

    @Override
    public long getCapacityAsLong() {
        return CAPACITY;
    }

    @Override
    public int insert(int amount, TransactionContext transaction) {
        return 0;
    }

    @Override
    public int extract(int amount, TransactionContext transaction) {
        return 0;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        energy = input.getIntOr(ENERGY_KEY, 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt(ENERGY_KEY, energy);
    }
}
