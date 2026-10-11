package com.leclowndu93150.thaumaturge.content.essentia.flow;

import com.leclowndu93150.thaumaturge.api.aspect.Aspects;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaAccess;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaPull;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaConduit;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public final class EssentiaIntake implements IEssentiaConduit {
    private final EssentiaIntakeHost host;
    private final ResourceKey<IAspect> aspect;
    private final int suction;
    private final int attemptInterval;
    private int cooldown;

    public EssentiaIntake(EssentiaIntakeHost host, ResourceKey<IAspect> aspect, int suction, int attemptInterval) {
        this.host = host;
        this.aspect = aspect;
        this.suction = suction;
        this.attemptInterval = attemptInterval;
    }

    public boolean pullOne() {
        if (--cooldown > 0) {
            return false;
        }
        cooldown = attemptInterval;
        Level level = host.getLevel();
        Holder<IAspect> wanted = Aspects.resolve(level, aspect);
        if (wanted == null) {
            return false;
        }
        Direction face = host.intakeFace();
        Direction remoteFace = face.getOpposite();
        IEssentiaConduit supplier = EssentiaAccess.conduit(level, host.getBlockPos().relative(face), remoteFace);
        if (supplier == null || !supplier.releasesOn(remoteFace) || supplier.pull(remoteFace).strength() >= strengthOn(face)) {
            return false;
        }
        return supplier.extract(wanted, 1, remoteFace, false) == 1;
    }

    @Override
    public boolean connectsOn(Direction face) {
        return face == host.intakeFace();
    }

    @Override
    public boolean receivesOn(Direction face) {
        return connectsOn(face);
    }

    @Override
    public boolean releasesOn(Direction face) {
        return false;
    }

    @Override
    public EssentiaPull pull(@Nullable Direction face) {
        int strength = strengthOn(face);
        return strength > 0 ? EssentiaPull.of(Aspects.resolve(host.getLevel(), aspect), strength) : EssentiaPull.NONE;
    }

    private int strengthOn(@Nullable Direction face) {
        return connectsOn(face) && host.wantsEssentia() ? host.intakeSuction(suction) : 0;
    }

    @Override
    public int extract(Holder<IAspect> aspect, int amount, Direction face, boolean simulate) {
        return 0;
    }

    @Override
    public int insert(Holder<IAspect> aspect, int amount, Direction face, boolean simulate) {
        return 0;
    }

    @Override
    public @Nullable Holder<IAspect> heldAspect(@Nullable Direction face) {
        return null;
    }

    @Override
    public int heldAmount(@Nullable Direction face) {
        return 0;
    }

    @Override
    public int pullThreshold() {
        return 0;
    }
}
