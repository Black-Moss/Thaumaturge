package com.leclowndu93150.thaumaturge.content.essentia.reservoir;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaAccess;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaTransport;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class BlockEntityEssentiaReservoir extends BlockEntity implements IEssentiaTransport {
    public static final int CAPACITY = 256;

    private static final String ESSENTIA_KEY = "Essentia";
    private static final int SUCTION = 24;
    private static final int PULL_INTERVAL = 5;
    private static final int CREAK_BASE = 500;
    private static final float CREAK_VOLUME = 1.0F;
    private static final float CREAK_PITCH_BASE = 1.4F;
    private static final float CREAK_PITCH_SPREAD = 0.2F;
    private static final float FLUX_PER_ESSENTIA = 0.25F;
    private static final float MAX_RUPTURE_FLUX = 64.0F;
    private static final int ESSENTIA_PER_POCKET = 16;
    private static final float RUPTURE_EXPLOSION_RADIUS = 1.0F;

    private AspectList contents = AspectList.EMPTY;

    public BlockEntityEssentiaReservoir(BlockPos pos, BlockState state) {
        super(TTBlockEntities.ESSENTIA_RESERVOIR.get(), pos, state);
    }

    public AspectList contents() {
        return contents;
    }

    public int getStoredAmount() {
        return contents.totalAmount();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityEssentiaReservoir reservoir) {
        int total = reservoir.getStoredAmount();
        if (total > 0) {
            RandomSource random = level.getRandom();
            if (random.nextInt(CREAK_BASE - total) == 0) {
                level.playSound(null, pos, TTSounds.CREAK.get(), SoundSource.BLOCKS, CREAK_VOLUME, CREAK_PITCH_BASE + random.nextFloat() * CREAK_PITCH_SPREAD);
            }
        }
        if (total < CAPACITY && level.getGameTime() % PULL_INTERVAL == 0) {
            reservoir.pullFromNeighbour(level, pos, state.getValue(BlockStateProperties.FACING));
        }
    }

    private void pullFromNeighbour(Level level, BlockPos pos, Direction facing) {
        BlockPos neighbourPos = pos.relative(facing);
        if (!level.hasChunkAt(neighbourPos)) {
            return;
        }
        Direction face = facing.getOpposite();
        IEssentiaTransport source = EssentiaAccess.transport(level, neighbourPos, face);
        if (source == null || !canYield(source, face)) {
            return;
        }
        Holder<IAspect> type = source.getEssentiaType(face);
        if (type != null && source.takeEssentia(type, 1, face) == 1) {
            store(type, 1);
        }
    }

    private static boolean canYield(IEssentiaTransport source, Direction face) {
        return sourceCanOutput(source, face) && sourceHasStock(source, face) && withinSuctionWindow(source, face);
    }

    private static boolean sourceCanOutput(IEssentiaTransport source, Direction face) {
        return source.canOutputTo(face);
    }

    private static boolean sourceHasStock(IEssentiaTransport source, Direction face) {
        return source.getEssentiaAmount(face) > 0;
    }

    private static boolean withinSuctionWindow(IEssentiaTransport source, Direction face) {
        return source.getSuctionAmount(face) < SUCTION && source.getMinimumSuction() <= SUCTION;
    }

    private void store(Holder<IAspect> aspect, int amount) {
        contents = contents.add(aspect, amount);
        notifyChanged();
    }

    private void withdraw(Holder<IAspect> aspect, int amount) {
        contents = contents.remove(aspect, amount);
        notifyChanged();
    }

    private @Nullable AspectInstance leadingStock(@Nullable Direction face) {
        if (!isOutlet(face) || contents.isEmpty()) {
            return null;
        }
        return contents.entries().getFirst();
    }

    private boolean isOutlet(@Nullable Direction face) {
        return face != null && face == facing();
    }

    private void notifyChanged() {
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
            level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
        }
    }

    private Direction facing() {
        return getBlockState().getValue(BlockStateProperties.FACING);
    }

    @Override
    public void setSuction(@Nullable Holder<IAspect> aspect, int amount) {}

    @Override
    public @Nullable Holder<IAspect> getSuctionType(@Nullable Direction face) {
        return null;
    }

    @Override
    public boolean isConnectable(Direction face) {
        return face == facing();
    }

    @Override
    public boolean canInputFrom(Direction face) {
        return face == facing();
    }

    @Override
    public boolean canOutputTo(Direction face) {
        return face == facing();
    }

    @Override
    public int getMinimumSuction() {
        return SUCTION;
    }

    @Override
    public int spaceFor(Holder<IAspect> aspect, Direction face) {
        if (!canInputFrom(face)) {
            return 0;
        }
        return Math.max(0, CAPACITY - getStoredAmount());
    }

    @Override
    public int getSuctionAmount(@Nullable Direction face) {
        if (!isOutlet(face) || getStoredAmount() >= CAPACITY) {
            return 0;
        }
        return SUCTION;
    }

    @Override
    public @Nullable Holder<IAspect> getEssentiaType(@Nullable Direction face) {
        AspectInstance lead = leadingStock(face);
        return lead == null ? null : lead.aspect();
    }

    @Override
    public int getEssentiaAmount(@Nullable Direction face) {
        AspectInstance lead = leadingStock(face);
        return lead == null ? 0 : lead.amount();
    }

    @Override
    public int addEssentia(Holder<IAspect> aspect, int amount, Direction face) {
        int room = isOutlet(face) ? spaceFor(aspect, face) : 0;
        int accepted = Math.clamp(amount, 0, room);
        if (accepted > 0) {
            store(aspect, accepted);
        }
        return accepted;
    }

    @Override
    public int takeEssentia(Holder<IAspect> aspect, int amount, Direction face) {
        boolean available = isOutlet(face) && amount > 0 && contents.amountOf(aspect) >= amount;
        if (available) {
            withdraw(aspect, amount);
        }
        return available ? amount : 0;
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        int total = getStoredAmount();
        if (total > 0 && level instanceof ServerLevel server) {
            rupture(server, pos, total);
        }
    }

    private static void rupture(ServerLevel server, BlockPos pos, int total) {
        releaseFlux(server, pos, total);
        int pockets = total / ESSENTIA_PER_POCKET;
        if (pockets > 0) {
            burst(server, pos, pockets);
        }
    }

    private static void releaseFlux(ServerLevel server, BlockPos pos, int total) {
        float flux = Math.min(total * FLUX_PER_ESSENTIA, MAX_RUPTURE_FLUX);
        AuraHelper.polluteAura(server, pos, flux, true);
    }

    private static void burst(ServerLevel server, BlockPos pos, int pockets) {
        Vec3 centre = Vec3.atCenterOf(pos);
        server.explode(null, centre.x(), centre.y(), centre.z(), RUPTURE_EXPLOSION_RADIUS, Level.ExplosionInteraction.NONE);
        ReservoirPockets.scatter(server, pos, pockets + 1);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        AspectList loaded = input.read(ESSENTIA_KEY, AspectList.CODEC).orElse(AspectList.EMPTY);
        contents = loaded.totalAmount() > CAPACITY ? AspectList.EMPTY : loaded;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store(ESSENTIA_KEY, AspectList.CODEC, contents);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
