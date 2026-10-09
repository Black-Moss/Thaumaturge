package com.leclowndu93150.thaumaturge.content.golem.seals.behavior;

import com.leclowndu93150.thaumaturge.api.golems.GolemHelper;
import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealBehavior;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealFilter;
import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import com.leclowndu93150.thaumaturge.api.items.InvHelper;
import com.leclowndu93150.thaumaturge.api.items.InvHelper.InvFilter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

public final class StockBehavior implements ISealBehavior {
    private static final int STAGGER = 50;
    private static final int SCAN_PERIOD = 20;

    private final SealClock clock = new SealClock(STAGGER);

    @Override
    public boolean canPerform(ISealEntity owner, IGolemAPI worker, Task job) {
        return false;
    }

    @Override
    public boolean completeTask(ServerLevel world, ISealEntity owner, IGolemAPI worker, Task job) {
        return true;
    }

    @Override
    public void tick(ServerLevel level, ISealEntity seal) {
        if (clock.advance() % SCAN_PERIOD != 0) {
            return;
        }
        BlockPos container = seal.pos().pos();
        Direction side = seal.pos().face();
        ResourceHandler<ItemResource> stored = InvHelper.getItemHandlerAt(level, container, side);
        if (stored != null) {
            ISealFilter filter = ItemMatchSettings.filterOf(seal);
            InvFilter match = ItemMatchSettings.of(seal);
            for (int slot = 0; slot < filter.spec().slots(); slot++) {
                topUp(level, container, side, stored, match, filter, slot);
            }
        }
    }

    private static void topUp(ServerLevel level, BlockPos container, Direction side, ResourceHandler<ItemResource> stored, InvFilter match, ISealFilter filter, int slot) {
        ItemStack ghost = filter.stack(slot);
        if (ghost.isEmpty()) {
            return;
        }
        int missing = Math.min(filter.limit(slot) - InvHelper.countTotalItemsIn(stored, ghost, match), ghost.getMaxStackSize());
        if (missing <= 0) {
            return;
        }
        ItemStack request = InvHelper.hasRoomFor(level, container, side, ghost.copyWithCount(missing));
        if (!request.isEmpty()) {
            GolemHelper.requestProvisioning(level, container, side, request);
        }
    }
}
