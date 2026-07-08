package com.leclowndu93150.thaumcraft.content.device;

import com.leclowndu93150.thaumcraft.registry.TCBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class BlockEntityRedstoneRelay extends BlockEntity {
    private static final int MAX_SIGNAL = 15;

    private int in = 1;
    private int out = 15;

    public BlockEntityRedstoneRelay(BlockPos pos, BlockState state) {
        super(TCBlockEntities.REDSTONE_RELAY.get(), pos, state);
    }

    public int getIn() {
        return in;
    }

    public int getOut() {
        return out;
    }

    public void increaseIn() {
        in++;
        if (in > MAX_SIGNAL) {
            in = 1;
        }
        setChanged();
        sync();
    }

    public void increaseOut() {
        out++;
        if (out > MAX_SIGNAL) {
            out = 1;
        }
        setChanged();
        sync();
    }

    private void sync() {
        if (level != null && !level.isClientSide()) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(getBlockPos(), state, state, 3);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        in = input.getByteOr("in", (byte) 1);
        out = input.getByteOr("out", (byte) 15);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putByte("in", (byte) in);
        output.putByte("out", (byte) out);
    }
}
