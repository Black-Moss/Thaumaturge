package com.leclowndu93150.thaumaturge.content.eldritch.lock;

import com.leclowndu93150.thaumaturge.api.labyrinth.MazeId;
import com.leclowndu93150.thaumaturge.content.blockentity.AbstractSyncedBlockEntity;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class BlockEntityEldritchLock extends AbstractSyncedBlockEntity {
    private static final String MAZE = "maze";
    private static final String CHARGED_AT = "charged_at";

    private Optional<MazeId> maze = Optional.empty();
    private long chargedAt = -1L;

    public BlockEntityEldritchLock(BlockPos pos, BlockState state) {
        super(TTBlockEntities.ELDRITCH_LOCK.get(), pos, state);
    }

    public Optional<MazeId> maze() {
        return maze;
    }

    public void bind(MazeId id) {
        maze = Optional.of(id);
        setChanged();
    }

    public void showCharge(long gameTime) {
        chargedAt = gameTime;
        setChangedAndSync();
    }

    public void showSealed() {
        chargedAt = -1L;
        setChangedAndSync();
    }

    public boolean isIdle() {
        return chargedAt < 0;
    }

    public int getCount() {
        if (chargedAt < 0 || level == null) {
            return -1;
        }
        return (int) Math.min(Integer.MAX_VALUE, Math.max(0L, level.getGameTime() - chargedAt));
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        maze = input.read(MAZE, MazeId.CODEC);
        chargedAt = input.getLongOr(CHARGED_AT, -1L);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.storeNullable(MAZE, MazeId.CODEC, maze.orElse(null));
        output.putLong(CHARGED_AT, chargedAt);
    }

}
