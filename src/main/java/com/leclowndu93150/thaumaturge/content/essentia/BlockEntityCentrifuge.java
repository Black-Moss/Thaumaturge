package com.leclowndu93150.thaumaturge.content.essentia;

import com.leclowndu93150.thaumaturge.api.aspect.Aspects;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaTransport;
import com.leclowndu93150.thaumaturge.content.blockentity.AbstractSyncedBlockEntity;
import com.leclowndu93150.thaumaturge.content.essentia.flow.EssentiaFlowHandler;
import com.leclowndu93150.thaumaturge.content.legacy.LegacyIds;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public final class BlockEntityCentrifuge extends AbstractSyncedBlockEntity implements IEssentiaTransport {
    private static final String INPUT_KEY = "AspectIn";
    private static final String OUTPUT_KEY = "AspectOut";
    private static final int PROCESS_TICKS = 39;
    private static final int PULL_INTERVAL = 5;
    private static final int IDLE_SUCTION = 128;
    private static final int BUSY_SUCTION = 64;
    private static final int SINGLE_POINT = 1;
    private static final int IDLE_MINIMUM = 0;
    private static final int OUTPUT_CHOICES = 2;
    private static final float MAX_SPIN = 20.0F;
    private static final float SPIN_RISE = 2.0F;
    private static final float SPIN_FALL = 0.5F;
    private static final float HALF_TURN = 180.0F;
    private static final double CENTER_OFFSET = 0.5;
    private static final float PUMP_VOLUME = 1.0F;
    private static final float PUMP_PITCH = 1.0F;
    private static final float POLLUTION = 1.0F;

    public float rotation;
    public float rotationSpeed;

    private @Nullable ResourceKey<IAspect> inputAspect;
    private @Nullable ResourceKey<IAspect> outputAspect;
    private int countdown;
    private int pullTicks;

    public BlockEntityCentrifuge(BlockPos pos, BlockState state) {
        super(TTBlockEntities.CENTRIFUGE.get(), pos, state);
    }

    public boolean isSpinning() {
        return inputAspect != null;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityCentrifuge centrifuge) {
        if (level.hasNeighborSignal(pos)) {
            return;
        }
        centrifuge.countdown = Math.max(centrifuge.countdown - 1, 0);
        if (centrifuge.outputAspect != null) {
            return;
        }
        if (centrifuge.inputAspect == null) {
            centrifuge.pullTicks++;
            if (centrifuge.pullTicks >= PULL_INTERVAL) {
                centrifuge.pullTicks = 0;
                centrifuge.pullFromBelow(level, pos);
            }
        } else if (centrifuge.countdown == 0) {
            centrifuge.split(level);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, BlockEntityCentrifuge centrifuge) {
        if (centrifuge.inputAspect != null && !level.hasNeighborSignal(pos)) {
            centrifuge.rotationSpeed = Math.min(centrifuge.rotationSpeed + SPIN_RISE, MAX_SPIN);
        } else {
            centrifuge.rotationSpeed = Math.max(centrifuge.rotationSpeed - SPIN_FALL, 0.0F);
        }
        float before = centrifuge.rotation;
        centrifuge.rotation += centrifuge.rotationSpeed;
        if (centrifuge.rotationSpeed >= MAX_SPIN && Math.floor(before / HALF_TURN) != Math.floor(centrifuge.rotation / HALF_TURN)) {
            level.playLocalSound(pos.getX() + CENTER_OFFSET, pos.getY() + CENTER_OFFSET, pos.getZ() + CENTER_OFFSET, TTSounds.PUMP.get(), SoundSource.BLOCKS, PUMP_VOLUME, PUMP_PITCH, false);
        }
    }

    private void split(Level level) {
        ResourceKey<IAspect> consumed = inputAspect;
        inputAspect = null;
        Holder<IAspect> chosen = chooseComponent(level, consumed);
        if (chosen != null) {
            outputAspect = chosen.unwrapKey().orElse(null);
        }
        setChangedAndSync();
    }

    private static @Nullable Holder<IAspect> chooseComponent(Level level, @Nullable ResourceKey<IAspect> compound) {
        if (compound == null) {
            return null;
        }
        Holder<IAspect> source = Aspects.resolve(level, compound);
        if (source == null) {
            return null;
        }
        List<Holder<IAspect>> parts = source.value().components();
        return parts.size() < OUTPUT_CHOICES ? null : parts.get(level.getRandom().nextInt(OUTPUT_CHOICES));
    }

    private void pullFromBelow(Level level, BlockPos pos) {
        IEssentiaTransport below = EssentiaFlowHandler.transport(level, pos.below(), Direction.UP);
        if (below == null) {
            return;
        }
        Holder<IAspect> offered = sampleOffer(below);
        if (offered == null || !outranksSupplier(below)) {
            return;
        }
        if (below.takeEssentia(offered, SINGLE_POINT, Direction.UP) != SINGLE_POINT) {
            return;
        }
        startProcessing(offered.unwrapKey().orElse(null));
    }

    private static @Nullable Holder<IAspect> sampleOffer(IEssentiaTransport supplier) {
        if (!supplier.canOutputTo(Direction.UP)) {
            return null;
        }
        Holder<IAspect> offered = supplier.getEssentiaType(Direction.UP);
        if (offered == null || offered.value().isPrimal()) {
            return null;
        }
        return supplier.getEssentiaAmount(Direction.UP) >= SINGLE_POINT ? offered : null;
    }

    private boolean outranksSupplier(IEssentiaTransport supplier) {
        int ownPull = getSuctionAmount(Direction.DOWN);
        return ownPull > supplier.getSuctionAmount(Direction.UP) && ownPull >= supplier.getMinimumSuction();
    }

    private void startProcessing(@Nullable ResourceKey<IAspect> aspect) {
        inputAspect = aspect;
        countdown = PROCESS_TICKS;
        setChangedAndSync();
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        inputAspect = input.read(INPUT_KEY, LegacyIds.ASPECT_KEY_CODEC).orElse(null);
        outputAspect = input.read(OUTPUT_KEY, LegacyIds.ASPECT_KEY_CODEC).orElse(null);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.storeNullable(INPUT_KEY, LegacyIds.ASPECT_KEY_CODEC, inputAspect);
        output.storeNullable(OUTPUT_KEY, LegacyIds.ASPECT_KEY_CODEC, outputAspect);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (outputAspect != null && level instanceof ServerLevel server) {
            AuraHelper.polluteAura(server, pos, POLLUTION, true);
        }
        super.preRemoveSideEffects(pos, state);
    }

    @Override
    public boolean isConnectable(Direction side) {
        return canInputFrom(side) || canOutputTo(side);
    }

    @Override
    public boolean canInputFrom(Direction side) {
        return switch (side) {
            case DOWN -> true;
            default -> false;
        };
    }

    @Override
    public boolean canOutputTo(Direction side) {
        return switch (side) {
            case UP -> true;
            default -> false;
        };
    }

    @Override
    public void setSuction(@Nullable Holder<IAspect> type, int strength) {}

    @Override
    public int getSuctionAmount(@Nullable Direction side) {
        if (side != Direction.DOWN || level == null || level.hasNeighborSignal(worldPosition)) {
            return 0;
        }
        return inputAspect != null ? BUSY_SUCTION : IDLE_SUCTION;
    }

    @Override
    public @Nullable Holder<IAspect> getSuctionType(@Nullable Direction side) {
        return null;
    }

    @Override
    public @Nullable Holder<IAspect> getEssentiaType(@Nullable Direction side) {
        return outputAspect != null ? Aspects.resolve(level, outputAspect) : null;
    }

    @Override
    public int getEssentiaAmount(@Nullable Direction side) {
        if (outputAspect == null) {
            return 0;
        }
        return SINGLE_POINT;
    }

    @Override
    public int takeEssentia(Holder<IAspect> type, int count, Direction side) {
        boolean matches = outputAspect != null && type.is(outputAspect);
        if (!matches || side != Direction.UP || count != SINGLE_POINT) {
            return 0;
        }
        outputAspect = null;
        setChangedAndSync();
        return SINGLE_POINT;
    }

    @Override
    public int addEssentia(Holder<IAspect> type, int count, Direction side) {
        if (inputAspect != null || count <= 0 || type.value().isPrimal()) {
            return 0;
        }
        Optional<ResourceKey<IAspect>> key = type.unwrapKey();
        if (key.isEmpty()) {
            return 0;
        }
        startProcessing(key.get());
        return SINGLE_POINT;
    }

    @Override
    public int getMinimumSuction() {
        return IDLE_MINIMUM;
    }
}
