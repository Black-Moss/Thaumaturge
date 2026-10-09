package com.leclowndu93150.thaumaturge.content.device.mirror;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.IAspectSource;
import com.leclowndu93150.thaumaturge.content.infusion.EssentiaSources;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class BlockEntityMirrorEssentia extends BlockEntityMirrorBase implements IAspectSource {
    private static final int INSTABILITY_THRESHOLD = 64;
    private static final int TARGET_RANGE = 8;
    private static final int MAX_TRANSFER = 1;
    private static final int NOTHING_STORED = 0;
    private static final boolean BLOCKED = false;

    private @Nullable EssentiaSources targetSources;
    private @Nullable BlockPos targetSourcesCenter;

    public BlockEntityMirrorEssentia(BlockPos pos, BlockState state) {
        super(TTBlockEntities.MIRROR_ESSENTIA.get(), pos, state);
    }

    @Override
    protected int instabilityThreshold() {
        return INSTABILITY_THRESHOLD;
    }

    @Override
    protected boolean isSameKind(BlockEntity target) {
        return target instanceof BlockEntityMirrorEssentia;
    }

    @Override
    public AspectList getAspects() {
        return AspectList.EMPTY;
    }

    @Override
    public void setAspects(AspectList aspects) {}

    public void serverTick(Level level, BlockPos pos) {
        tickLink();
    }

    private @Nullable EssentiaSources sourcesAtTarget() {
        if (link == null) {
            return null;
        }
        BlockPos center = link.pos();
        if (targetSources != null && center.equals(targetSourcesCenter)) {
            return targetSources;
        }
        ServerLevel destination = targetLevel();
        if (destination == null) {
            return null;
        }
        BlockState mirrorState = destination.getBlockState(center);
        if (!mirrorState.hasProperty(BlockMirror.FACING)) {
            return null;
        }
        targetSources = buildSources(center, mirrorState.getValue(BlockMirror.FACING));
        targetSourcesCenter = center;
        return targetSources;
    }

    private static EssentiaSources buildSources(BlockPos center, Direction facing) {
        return new EssentiaSources(center, TARGET_RANGE).facing(facing).ignoring(BlockEntityMirrorEssentia.class::isInstance).drainEffectTarget(Vec3.atCenterOf(center));
    }

    private @Nullable Route openRoute(int amount) {
        if (amount > MAX_TRANSFER || !verifyPairing()) {
            return null;
        }
        ServerLevel destination = targetLevel();
        EssentiaSources sources = sourcesAtTarget();
        return destination == null || sources == null ? null : new Route(destination, sources);
    }

    @Override
    public int fill(Holder<IAspect> aspect, int amount) {
        Route route = openRoute(amount);
        if (route == null || !route.insert(aspect)) {
            return amount;
        }
        pileOn(amount);
        return 0;
    }

    @Override
    public boolean drain(Holder<IAspect> aspect, int amount) {
        Route route = openRoute(amount);
        if (route == null || !route.extract(aspect)) {
            return false;
        }
        pileOn(amount);
        return true;
    }

    @Override
    public int amountOf(Holder<IAspect> aspect) {
        return NOTHING_STORED;
    }

    @Override
    public boolean accepts(Holder<IAspect> aspect) {
        return pairingIntact();
    }

    @Override
    public boolean isBlocked() {
        return BLOCKED;
    }

    @Override
    public boolean holds(Holder<IAspect> aspect, int amount) {
        return amountOf(aspect) >= amount && amount > 0;
    }

    private record Route(ServerLevel destination, EssentiaSources sources) {
        boolean insert(Holder<IAspect> aspect) {
            return sources.insert(destination, aspect, 0);
        }

        boolean extract(Holder<IAspect> aspect) {
            return sources.drain(destination, aspect, 0);
        }
    }
}
