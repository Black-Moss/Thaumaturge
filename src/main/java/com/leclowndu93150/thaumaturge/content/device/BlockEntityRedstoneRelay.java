package com.leclowndu93150.thaumaturge.content.device;

import com.leclowndu93150.thaumaturge.content.blockentity.AbstractSyncedBlockEntity;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class BlockEntityRedstoneRelay extends AbstractSyncedBlockEntity {
    private static final int MIN_LEVEL = 1;
    private static final int MAX_LEVEL = 15;
    private static final String IN_KEY = "in";
    private static final String OUT_KEY = "out";

    private int in = MIN_LEVEL;
    private int out = MAX_LEVEL;

    public BlockEntityRedstoneRelay(BlockPos pos, BlockState state) {
        super(TTBlockEntities.REDSTONE_RELAY.get(), pos, state);
    }

    public int getIn() {
        return in;
    }

    public int getOut() {
        return out;
    }

    public void increaseIn() {
        in = next(in);
        setChangedAndSync();
    }

    public void increaseOut() {
        out = next(out);
        setChangedAndSync();
    }

    private static int next(int value) {
        return value >= MAX_LEVEL ? MIN_LEVEL : value + 1;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        in = input.getByteOr(IN_KEY, (byte) MIN_LEVEL);
        out = input.getByteOr(OUT_KEY, (byte) MAX_LEVEL);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putByte(IN_KEY, (byte) in);
        output.putByte(OUT_KEY, (byte) out);
    }
}
