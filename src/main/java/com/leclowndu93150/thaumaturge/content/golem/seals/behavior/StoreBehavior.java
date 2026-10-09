package com.leclowndu93150.thaumaturge.content.golem.seals.behavior;

import com.leclowndu93150.thaumaturge.api.golems.GolemHelper;
import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealBehavior;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealFilter;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealSetting;
import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import com.leclowndu93150.thaumaturge.api.items.InvHelper;
import com.leclowndu93150.thaumaturge.api.items.InvHelper.FilterMatch;
import com.leclowndu93150.thaumaturge.api.items.InvHelper.InvFilter;
import com.leclowndu93150.thaumaturge.content.golem.tasks.TaskBoard;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jspecify.annotations.Nullable;

public final class StoreBehavior implements ISealBehavior {
    public static final SealSetting ONLY_EXISTING = new SealSetting("only_existing", "gui.thaumaturge.seal.setting.exist", false);

    private static final int STAGGER = 50;
    private static final int SCAN_PERIOD = 20;
    private static final double LOOSE_ITEM_RANGE = 1.5;
    private static final double CELL_CENTER = 0.5;
    private static final double TOSS_HORIZONTAL_SCALE = 0.2;
    private static final double TOSS_VERTICAL_SCALE = 0.5;
    private static final int PUT_DOWN_XP = 1;

    private final SealClock clock = new SealClock(STAGGER);
    private @Nullable Task pending;

    @Override
    public void tick(ServerLevel level, ISealEntity seal) {
        if (clock.advance() % SCAN_PERIOD == 0) {
            replenish(level, seal, null);
        }
    }

    @Override
    public void onTaskStarted(ServerLevel level, ISealEntity seal, IGolemAPI golem, Task task) {
        if (!seal.isStoppedByRedstone(level)) {
            replenish(level, seal, task);
        }
    }

    @Override
    public boolean completeTask(ServerLevel level, ISealEntity seal, IGolemAPI golem, Task task) {
        FilterMatch match = carried(seal, golem);
        ItemStack stack = match.stack();
        if (stack.isEmpty()) {
            return true;
        }
        BlockPos pos = seal.pos().pos();
        Direction face = seal.pos().face();
        InvFilter compare = ItemMatchSettings.of(seal);
        ResourceHandler<ItemResource> container = InvHelper.getItemHandlerAt(level, pos, face);
        int limit = limitOf(seal, match);
        int present = container != null ? InvHelper.countTotalItemsIn(container, stack, compare) : InvHelper.countStackInWorld(level, pos, stack, LOOSE_ITEM_RANGE, compare);
        int amount = limit > 0 ? Math.max(0, limit - present) : stack.getCount();
        if (amount > 0) {
            ItemStack released = golem.hands().release(stack.copyWithCount(amount));
            if (!released.isEmpty()) {
                putDown(level, golem, pos, face, container, released);
                HandlingSound.play(golem, HandlingSound.LOW);
                golem.addRankXp(PUT_DOWN_XP);
                golem.swingArm();
            }
        }
        return true;
    }

    @Override
    public boolean canPerform(ISealEntity seal, IGolemAPI golem, Task task) {
        FilterMatch match = carried(seal, golem);
        ItemStack stack = match.stack();
        if (stack.isEmpty()) {
            return false;
        }
        Level level = golem.level();
        BlockPos pos = seal.pos().pos();
        Direction face = seal.pos().face();
        int limit = limitOf(seal, match);
        InvFilter compare = ItemMatchSettings.of(seal);
        ResourceHandler<ItemResource> container = InvHelper.getItemHandlerAt(level, pos, face);
        if (container == null) {
            return limit <= 0 || InvHelper.countStackInWorld(level, pos, stack, LOOSE_ITEM_RANGE, compare) < limit;
        }
        if (!InvHelper.hasRoomForSome(level, pos, face, stack)) {
            return false;
        }
        int present = InvHelper.countTotalItemsIn(container, stack, compare);
        if (seal.setting(ONLY_EXISTING) && present <= 0) {
            return false;
        }
        return limit <= 0 || present < limit;
    }

    private void replenish(ServerLevel level, ISealEntity seal, @Nullable Task claimed) {
        if (pending != null && !pending.equals(claimed) && isOpen(level, pending)) {
            return;
        }
        Task task = Task.atBlock(seal.pos(), seal.pos().pos());
        task.setPriority(seal.priority());
        GolemHelper.addGolemTask(level, task);
        pending = task;
    }

    private static boolean isOpen(Level level, Task task) {
        return TaskBoard.of(level).isLive(task.id()) && !task.isClaimed() && !task.isEnded() && !task.isCompleted();
    }

    private static FilterMatch carried(ISealEntity seal, IGolemAPI golem) {
        ISealFilter filter = ItemMatchSettings.filterOf(seal);
        return InvHelper.findFirstMatchFromFilterWithSize(filter.stacks(), filter.limits(), filter.isBlacklist(), golem.hands().contents(), ItemMatchSettings.of(seal));
    }

    private static int limitOf(ISealEntity seal, FilterMatch match) {
        return ItemMatchSettings.filterOf(seal).usesLimits() ? match.sizeLimit() : 0;
    }

    private static void putDown(ServerLevel level, IGolemAPI golem, BlockPos pos, Direction face, @Nullable ResourceHandler<ItemResource> container, ItemStack released) {
        if (container == null) {
            BlockPos spot = pos.relative(face);
            ItemEntity item = new ItemEntity(level, spot.getX() + CELL_CENTER, spot.getY() + CELL_CENTER, spot.getZ() + CELL_CENTER, released);
            Vec3 motion = item.getDeltaMovement();
            item.setDeltaMovement(motion.x * TOSS_HORIZONTAL_SCALE, motion.y * TOSS_VERTICAL_SCALE, motion.z * TOSS_HORIZONTAL_SCALE);
            level.addFreshEntity(item);
            return;
        }
        ItemStack refused = InvHelper.insertStack(container, released, false);
        if (!refused.isEmpty()) {
            ItemStack overflow = golem.hands().hold(refused);
            InvHelper.dropItemAtEntity(level, overflow, golem.asEntity());
        }
    }
}
