package com.leclowndu93150.thaumaturge.content.golem.seals.behavior;

import com.leclowndu93150.thaumaturge.api.golems.GolemHelper;
import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealBehavior;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealFilter;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealSetting;
import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import com.leclowndu93150.thaumaturge.api.items.InvHelper;
import com.leclowndu93150.thaumaturge.api.items.InvHelper.InvFilter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jspecify.annotations.Nullable;

public final class EmptyBehavior implements ISealBehavior {
    public static final SealSetting CYCLE = new SealSetting("cycle_whitelist", "gui.thaumaturge.seal.setting.cycle", false);
    public static final SealSetting LEAVE_ONE = new SealSetting("leave_one", "gui.thaumaturge.seal.setting.leave", false);

    private static final int STAGGER = 30;
    private static final int SCAN_PERIOD = 20;
    private static final int CLEANUP_PERIOD = 100;
    private static final int TASK_LIFE = 5;
    private static final int MINIMUM_REMAINING = 1;

    private final SealClock clock = new SealClock(STAGGER);
    private final TaskLedger<ItemStack> wanted = new TaskLedger<>();
    private int turn;

    @Override
    public void tick(ServerLevel level, ISealEntity seal) {
        boolean cleanupDue = clock.at(CLEANUP_PERIOD);
        boolean scanDue = clock.at(SCAN_PERIOD);
        clock.advance();
        if (cleanupDue) {
            wanted.dropFinished(level);
        }
        if (scanDue) {
            requestNext(level, seal);
        }
    }

    private void requestNext(ServerLevel level, ISealEntity seal) {
        ResourceHandler<ItemResource> source = containerOf(level, seal);
        if (source == null) {
            return;
        }
        ItemStack target = firstWanted(seal, source);
        if (target.isEmpty()) {
            return;
        }
        post(level, seal, target);
    }

    private ItemStack firstWanted(ISealEntity seal, ResourceHandler<ItemResource> source) {
        ISealFilter filter = ItemMatchSettings.filterOf(seal);
        List<ItemStack> pool = candidates(seal, filter);
        return InvHelper.findFirstMatchFromFilter(pool, filter.isBlacklist(), source, ItemMatchSettings.of(seal), seal.setting(LEAVE_ONE));
    }

    private void post(ServerLevel level, ISealEntity seal, ItemStack target) {
        Task errand = newErrand(seal);
        GolemHelper.addGolemTask(level, errand);
        wanted.record(errand, target);
    }

    private static Task newErrand(ISealEntity seal) {
        Task errand = Task.atBlock(seal.pos(), seal.pos().pos());
        configure(errand, seal.priority());
        return errand;
    }

    private static void configure(Task errand, byte priority) {
        errand.setPriority(priority);
        errand.setLife(TASK_LIFE);
    }

    @Override
    public void onTaskSuspended(ServerLevel level, ISealEntity seal, Task task) {
        wanted.forget(task);
    }

    @Override
    public boolean canPerform(ISealEntity seal, IGolemAPI golem, Task task) {
        return Optional.ofNullable(wanted.get(task)).filter(EmptyBehavior::isUsable).map(stack -> golem.hands().canTake(stack, true)).orElse(false);
    }

    @Override
    public boolean completeTask(ServerLevel level, ISealEntity seal, IGolemAPI golem, Task task) {
        turn++;
        ItemStack stack = wanted.get(task);
        wanted.forget(task);
        if (isUsable(stack)) {
            fetch(level, seal, golem, stack);
        }
        return true;
    }

    private static boolean isUsable(@Nullable ItemStack stack) {
        if (stack == null) {
            return false;
        }
        return !stack.isEmpty();
    }

    private void fetch(ServerLevel level, ISealEntity seal, IGolemAPI golem, ItemStack stack) {
        ResourceHandler<ItemResource> source = containerOf(level, seal);
        if (source == null) {
            return;
        }
        ItemStack removed = withdraw(seal, golem, source, stack);
        if (removed != null) {
            hand(level, seal, golem, removed);
        }
        HandlingSound.play(golem, HandlingSound.HIGH);
        golem.swingArm();
    }

    private static void hand(ServerLevel level, ISealEntity seal, IGolemAPI golem, ItemStack removed) {
        ItemStack overflow = golem.hands().hold(removed);
        if (overflow.isEmpty()) {
            return;
        }
        InvHelper.ejectStackAt(level, seal.pos().pos(), seal.pos().face(), overflow);
    }

    private static @Nullable ItemStack withdraw(ISealEntity seal, IGolemAPI golem, ResourceHandler<ItemResource> source, ItemStack stack) {
        InvFilter match = ItemMatchSettings.of(seal);
        int capacity = Math.min(golem.hands().room(stack), stack.getCount());
        int amount = seal.setting(LEAVE_ONE) ? Math.min(capacity, InvHelper.countTotalItemsIn(source, stack, match) - MINIMUM_REMAINING) : capacity;
        if (amount <= 0) {
            return null;
        }
        return InvHelper.removeStackFrom(source, stack.copyWithCount(amount), match, false);
    }

    private List<ItemStack> candidates(ISealEntity seal, ISealFilter filter) {
        List<ItemStack> all = filter.stacks();
        boolean cycling = seal.setting(CYCLE) && !filter.isBlacklist();
        if (!cycling) {
            return all;
        }
        int live = 0;
        for (ItemStack ghost : all) {
            live += ghost.isEmpty() ? 0 : 1;
        }
        if (live == 0) {
            return all;
        }
        int skip = Math.floorMod(turn, live);
        for (ItemStack ghost : all) {
            if (ghost.isEmpty()) {
                continue;
            }
            if (skip-- == 0) {
                return Collections.singletonList(ghost);
            }
        }
        return all;
    }

    private static @Nullable ResourceHandler<ItemResource> containerOf(ServerLevel level, ISealEntity seal) {
        return InvHelper.getItemHandlerAt(level, seal.pos().pos(), seal.pos().face());
    }
}
