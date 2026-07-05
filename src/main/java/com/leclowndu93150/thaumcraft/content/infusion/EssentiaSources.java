package com.leclowndu93150.thaumcraft.content.infusion;

import com.leclowndu93150.thaumcraft.api.aspect.IAspect;
import com.leclowndu93150.thaumcraft.api.aspect.IAspectSource;
import com.leclowndu93150.thaumcraft.content.fx.TCParticleDispatch;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class EssentiaSources {
    private static final int RANGE = 12;
    private static final int RETRY_DELAY_TICKS = 200;

    private final BlockPos center;
    private @Nullable List<BlockPos> sources;
    private long retryAt;

    public EssentiaSources(BlockPos center) {
        this.center = center.immutable();
    }

    public void invalidate() {
        this.sources = null;
        this.retryAt = 0L;
    }

    public boolean drain(ServerLevel level, Holder<IAspect> aspect, int fxExtendTicks) {
        if (level.getGameTime() < retryAt) {
            return false;
        }
        if (sources == null) {
            sources = scan(level);
        }
        for (BlockPos sourcePos : sources) {
            BlockEntity be = level.getBlockEntity(sourcePos);
            if (be instanceof IAspectSource source
                    && !source.isBlocked()
                    && source.takeFromContainer(aspect, 1)) {
                be.setChanged();
                TCParticleDispatch.spawnEssentiaStream(level,
                        Vec3.atCenterOf(sourcePos),
                        Vec3.atCenterOf(center),
                        aspect.value().color(),
                        0,
                        level.getRandom().nextInt(8),
                        0.15F,
                        fxExtendTicks,
                        0.0);
                return true;
            }
        }
        sources = null;
        retryAt = level.getGameTime() + RETRY_DELAY_TICKS;
        return false;
    }

    private List<BlockPos> scan(ServerLevel level) {
        List<BlockPos> found = new ArrayList<>();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int y = -RANGE; y <= RANGE; y++) {
            for (int x = -RANGE; x <= RANGE; x++) {
                for (int z = -RANGE; z <= RANGE; z++) {
                    cursor.set(center.getX() + x, center.getY() + y, center.getZ() + z);
                    if (!level.isLoaded(cursor)) {
                        continue;
                    }
                    if (level.getBlockEntity(cursor) instanceof IAspectSource) {
                        found.add(cursor.immutable());
                    }
                }
            }
        }
        found.sort((a, b) -> Double.compare(a.distSqr(center), b.distSqr(center)));
        return found;
    }
}
