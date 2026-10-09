package com.leclowndu93150.thaumaturge.content.device;

import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.content.blockentity.AbstractSyncedBlockEntity;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class BlockEntityDioptra extends AbstractSyncedBlockEntity {
    public static final int GRID_SIZE = 13;
    public static final int GRID_LENGTH = GRID_SIZE * GRID_SIZE;

    private static final int SAMPLE_INTERVAL = 20;
    private static final int CHUNK_OFFSET = GRID_SIZE / 2;
    private static final float AURA_SCALE = 500.0F;
    private static final float GRID_MAX = 64.0F;
    private static final String GRID_KEY = "grid_a";

    private final byte[] grid = new byte[GRID_LENGTH];
    private final BlockPos.MutableBlockPos samplePos = new BlockPos.MutableBlockPos();
    private int ticks;

    public BlockEntityDioptra(BlockPos pos, BlockState state) {
        super(TTBlockEntities.DIOPTRA.get(), pos, state);
    }

    public byte gridValue(int index) {
        return index >= 0 && index < GRID_LENGTH ? grid[index] : 0;
    }

    public byte[] grid() {
        return grid;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityDioptra dioptra) {
        if (++dioptra.ticks % SAMPLE_INTERVAL == 0) {
            dioptra.refresh(level, state);
        }
    }

    private void refresh(Level level, BlockState state) {
        boolean showVis = state.getValue(BlockStateProperties.ENABLED);
        int centerX = SectionPos.blockToSectionCoord(worldPosition.getX());
        int centerZ = SectionPos.blockToSectionCoord(worldPosition.getZ());
        boolean changed = false;
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int column = 0; column < GRID_SIZE; column++) {
                samplePos.set(SectionPos.sectionToBlockCoord(centerX + column - CHUNK_OFFSET), 0, SectionPos.sectionToBlockCoord(centerZ + row - CHUNK_OFFSET));
                float value = showVis ? AuraHelper.getVis(level, samplePos) : AuraHelper.getFlux(level, samplePos);
                byte sample = toSample(value);
                int index = row * GRID_SIZE + column;
                if (grid[index] != sample) {
                    grid[index] = sample;
                    changed = true;
                }
            }
        }
        if (changed) {
            setChanged();
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_ALL);
        }
    }

    private static byte toSample(float value) {
        if (value <= 0.0F) {
            return 0;
        }
        return (byte) (int) Math.min(GRID_MAX, value / AURA_SCALE * GRID_MAX);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.getIntArray(GRID_KEY).ifPresent(this::loadGrid);
    }

    private void loadGrid(int[] values) {
        if (values.length != GRID_LENGTH) {
            return;
        }
        for (int index = 0; index < GRID_LENGTH; index++) {
            grid[index] = (byte) values[index];
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        int[] values = new int[GRID_LENGTH];
        for (int index = 0; index < GRID_LENGTH; index++) {
            values[index] = grid[index];
        }
        output.putIntArray(GRID_KEY, values);
    }
}
