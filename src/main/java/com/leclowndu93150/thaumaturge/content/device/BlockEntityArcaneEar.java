package com.leclowndu93150.thaumaturge.content.device;

import com.leclowndu93150.thaumaturge.registry.TTAttachments;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class BlockEntityArcaneEar extends BlockEntity {
    private static final int NOTE_COUNT = 25;
    private static final int NOTE_MAX = NOTE_COUNT - 1;
    private static final int NOTE_CENTER = 12;
    private static final double PITCH_BASE = 2.0;
    private static final double PITCH_STEP = 12.0;
    private static final float NOTE_VOLUME = 3.0F;
    private static final double BLOCK_CENTER = 0.5;
    private static final double PARTICLE_SPEED = 1.0;
    private static final int PULSE_TICKS = 10;
    private static final NoteBlockInstrument[] INSTRUMENTS = NoteBlockInstrument.values();
    private static final String NOTE_KEY = "note";
    private static final String TONE_KEY = "tone";

    private int note;
    private int tone;
    private int pulseTimer;

    public BlockEntityArcaneEar(BlockPos pos, BlockState state) {
        super(TTBlockEntities.ARCANE_EAR.get(), pos, state);
    }

    public int note() {
        return note;
    }

    public NoteBlockInstrument instrument() {
        return INSTRUMENTS[Math.floorMod(tone, INSTRUMENTS.length)];
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide()) {
            level.getData(TTAttachments.EAR_INDEX).add(worldPosition.immutable());
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level != null && !level.isClientSide()) {
            level.getData(TTAttachments.EAR_INDEX).remove(worldPosition);
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityArcaneEar ear) {
        if (ear.pulseTimer > 0 && --ear.pulseTimer == 0) {
            ear.setEnabled(false);
        }
    }

    public void updateTone() {
        if (level == null) {
            return;
        }
        Direction facing = getBlockState().getValue(BlockStateProperties.FACING);
        tone = level.getBlockState(worldPosition.relative(facing.getOpposite())).instrument().ordinal();
        setChanged();
    }

    public void changePitch() {
        note = (note + 1) % NOTE_COUNT;
        setChanged();
    }

    public void playNote() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        float pitch = (float) Math.pow(PITCH_BASE, (note - NOTE_CENTER) / PITCH_STEP);
        double x = worldPosition.getX() + BLOCK_CENTER;
        double y = worldPosition.getY() + BLOCK_CENTER;
        double z = worldPosition.getZ() + BLOCK_CENTER;
        serverLevel.playSound(null, x, y, z, instrument().getSoundEvent().value(), SoundSource.BLOCKS, NOTE_VOLUME, pitch);
        serverLevel.sendParticles(ParticleTypes.NOTE, x, y, z, 0, note / (double) NOTE_MAX, 0.0, 0.0, PARTICLE_SPEED);
    }

    public boolean matches(NoteBlockInstrument instrument, int note) {
        return instrument == instrument() && note == this.note;
    }

    public void trigger() {
        if (level == null || level.isClientSide()) {
            return;
        }
        playNote();
        BlockState state = getBlockState();
        boolean enabled = state.getValue(BlockStateProperties.ENABLED);
        if (state.getBlock() instanceof BlockArcaneEar ear && ear.isToggle()) {
            setEnabled(!enabled);
            return;
        }
        pulseTimer = PULSE_TICKS;
        if (!enabled) {
            setEnabled(true);
        }
    }

    private void setEnabled(boolean enabled) {
        if (level == null) {
            return;
        }
        BlockState state = getBlockState();
        level.setBlock(worldPosition, state.setValue(BlockStateProperties.ENABLED, enabled), Block.UPDATE_ALL);
        Block block = state.getBlock();
        Direction facing = state.getValue(BlockStateProperties.FACING);
        level.updateNeighborsAt(worldPosition, block, null);
        level.updateNeighborsAt(worldPosition.relative(facing.getOpposite()), block, null);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        int stored = input.getByteOr(NOTE_KEY, (byte) 0);
        note = stored < 0 || stored > NOTE_MAX ? 0 : stored;
        tone = input.getByteOr(TONE_KEY, (byte) 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putByte(NOTE_KEY, (byte) note);
        output.putByte(TONE_KEY, (byte) tone);
    }
}
