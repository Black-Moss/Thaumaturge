package com.leclowndu93150.thaumaturge.api.essentia;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import org.jspecify.annotations.Nullable;

@SuppressWarnings("removal")
final class ConduitAsTransport implements IEssentiaTransport {
    private final IEssentiaConduit conduit;

    private ConduitAsTransport(IEssentiaConduit conduit) {
        this.conduit = conduit;
    }

    static @Nullable IEssentiaTransport view(@Nullable IEssentiaConduit conduit) {
        if (conduit == null) {
            return null;
        }
        if (conduit instanceof IEssentiaTransport transport) {
            return transport;
        }
        return new ConduitAsTransport(conduit);
    }

    @Override
    public int addEssentia(Holder<IAspect> type, int count, Direction side) {
        return conduit.insert(type, count, side, false);
    }

    @Override
    public int takeEssentia(Holder<IAspect> type, int count, Direction side) {
        return conduit.extract(type, count, side, false);
    }

    @Override
    public int addEssentia(Holder<IAspect> type, int count, Direction side, boolean simulate) {
        return conduit.insert(type, count, side, simulate);
    }

    @Override
    public int takeEssentia(Holder<IAspect> type, int count, Direction side, boolean simulate) {
        return conduit.extract(type, count, side, simulate);
    }

    @Override
    public @Nullable Holder<IAspect> getEssentiaType(@Nullable Direction side) {
        return conduit.heldAspect(side);
    }

    @Override
    public int getEssentiaAmount(@Nullable Direction side) {
        return conduit.heldAmount(side);
    }

    @Override
    public void setSuction(@Nullable Holder<IAspect> type, int count) {}

    @Override
    public @Nullable Holder<IAspect> getSuctionType(@Nullable Direction side) {
        return conduit.pull(side).aspect();
    }

    @Override
    public int getSuctionAmount(@Nullable Direction side) {
        return conduit.pull(side).strength();
    }

    @Override
    public int getMinimumSuction() {
        return conduit.pullThreshold();
    }

    @Override
    public boolean canOutputTo(Direction side) {
        return conduit.releasesOn(side);
    }

    @Override
    public boolean canInputFrom(Direction side) {
        return conduit.receivesOn(side);
    }

    @Override
    public boolean isConnectable(Direction side) {
        return conduit.connectsOn(side);
    }

    @Override
    public int spaceFor(Holder<IAspect> type, Direction side) {
        return conduit.roomFor(type, side);
    }

    @Override
    public int insert(Holder<IAspect> aspect, int amount, Direction side, boolean simulate) {
        return conduit.insert(aspect, amount, side, simulate);
    }

    @Override
    public int extract(Holder<IAspect> aspect, int amount, Direction side, boolean simulate) {
        return conduit.extract(aspect, amount, side, simulate);
    }

    @Override
    public @Nullable Holder<IAspect> heldAspect(@Nullable Direction side) {
        return conduit.heldAspect(side);
    }

    @Override
    public int heldAmount(@Nullable Direction side) {
        return conduit.heldAmount(side);
    }

    @Override
    public EssentiaPull pull(@Nullable Direction side) {
        return conduit.pull(side);
    }

    @Override
    public int pullThreshold() {
        return conduit.pullThreshold();
    }

    @Override
    public boolean releasesOn(Direction side) {
        return conduit.releasesOn(side);
    }

    @Override
    public boolean receivesOn(Direction side) {
        return conduit.receivesOn(side);
    }

    @Override
    public boolean connectsOn(Direction side) {
        return conduit.connectsOn(side);
    }

    @Override
    public int roomFor(Holder<IAspect> aspect, Direction side) {
        return conduit.roomFor(aspect, side);
    }
}
