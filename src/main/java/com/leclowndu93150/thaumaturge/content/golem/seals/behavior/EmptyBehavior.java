package com.leclowndu93150.thaumaturge.content.golem.seals.behavior;

import com.leclowndu93150.thaumaturge.api.golems.GolemDispatch;
import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealBehavior;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealFilter;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealPos;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealSetting;
import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import com.leclowndu93150.thaumaturge.api.items.InvHelper;
import com.leclowndu93150.thaumaturge.api.items.ItemMatch;
import com.leclowndu93150.thaumaturge.api.items.StackFilter;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public final class EmptyBehavior implements ISealBehavior {
    public static final SealSetting CYCLE = new SealSetting("pcycle", "gui.thaumaturge.seal.setting.cycle", false);
    public static final SealSetting LEAVE_ONE = new SealSetting("pleave", "gui.thaumaturge.seal.setting.leave", false);

    private static final int STAGGER = 43;
    private static final int SCAN_PERIOD = 20;
    private static final int PURGE_PERIOD = 100;
    private static final int TASK_LIFE = 5;

    private final SealClock clock = new SealClock(STAGGER);
    private final TaskLedger<ItemStack> wanted = new TaskLedger<>();
    private int cycleCursor;

    @Override
    public void tick(ServerLevel level, ISealEntity seal) {
        int now = clock.advance();
        if (now % PURGE_PERIOD == 0) {
            wanted.dropFinished(level);
        }
        if (now % SCAN_PERIOD != 0) {
            return;
        }
        SealPos at = seal.pos();
        ResourceHandler<ItemResource> container = InvHelper.handlerAt(level, at.pos(), at.face());
        if (container == null) {
            return;
        }
        ISealFilter filter = ItemMatchSettings.filterOf(seal);
        ItemStack found = StackFilter.of(scanGhosts(seal, filter), filter.isBlacklist(), ItemMatchSettings.of(seal)).firstAccepted(container, seal.setting(LEAVE_ONE));
        if (found.isEmpty()) {
            return;
        }
        Task task = Task.atBlock(at, at.pos());
        task.setPriority(seal.priority());
        task.setLife(TASK_LIFE);
        GolemDispatch.postTask(level, task);
        wanted.record(task, found.copy());
    }

    private List<ItemStack> scanGhosts(ISealEntity seal, ISealFilter filter) {
        if (!seal.setting(CYCLE) || filter.isBlacklist()) {
            return filter.stacks();
        }
        List<ItemStack> filled = filter.stacks().stream().filter(ghost -> !ghost.isEmpty()).toList();
        if (filled.isEmpty()) {
            return filled;
        }
        return List.of(filled.get(Math.floorMod(cycleCursor, filled.size())));
    }

    @Override
    public boolean canPerform(ISealEntity seal, IGolemAPI golem, Task task) {
        ItemStack remembered = wanted.get(task);
        return remembered != null && golem.hands().room(remembered) > 0;
    }

    @Override
    public boolean completeTask(ServerLevel level, ISealEntity seal, IGolemAPI golem, Task task) {
        ItemStack remembered = wanted.get(task);
        if (remembered != null) {
            takeOut(level, seal, golem, remembered);
        }
        wanted.forget(task);
        cycleCursor++;
        task.end();
        return true;
    }

    @Override
    public void onTaskSuspended(ServerLevel level, ISealEntity seal, Task task) {
        wanted.forget(task);
    }

    private static void takeOut(ServerLevel level, ISealEntity seal, IGolemAPI golem, ItemStack remembered) {
        SealPos at = seal.pos();
        ResourceHandler<ItemResource> container = InvHelper.handlerAt(level, at.pos(), at.face());
        if (container == null) {
            return;
        }
        ItemMatch match = ItemMatchSettings.of(seal);
        int amount = Math.min(remembered.getCount(), golem.hands().room(remembered));
        if (seal.setting(LEAVE_ONE)) {
            amount = Math.min(amount, InvHelper.count(container, remembered, match) - 1);
        }
        if (amount <= 0) {
            return;
        }
        List<ItemStack> taken = extract(container, remembered, match, amount);
        if (taken.isEmpty()) {
            return;
        }
        for (ItemStack stack : taken) {
            ItemStack refused = golem.hands().hold(stack);
            if (!refused.isEmpty()) {
                dropRemainder(level, at, InvHelper.insertStack(container, refused, false));
            }
        }
        HandlingSound.play(golem, HandlingSound.HIGH);
        golem.animateSwing();
    }

    private static void dropRemainder(ServerLevel level, SealPos at, ItemStack remainder) {
        if (remainder.isEmpty()) {
            return;
        }
        BlockPos containerCell = at.pos();
        boolean solid = level.getBlockState(containerCell).isCollisionShapeFullBlock(level, containerCell);
        BlockPos cell = solid ? containerCell.relative(at.face()) : containerCell;
        LooseItems.spawn(level, Vec3.atBottomCenterOf(cell), remainder, LooseItems.pushToward(at.face().getOpposite()));
    }

    private static List<ItemStack> extract(ResourceHandler<ItemResource> container, ItemStack remembered, ItemMatch match, int amount) {
        List<ItemStack> taken = new ArrayList<>();
        int left = amount;
        try (Transaction transaction = Transaction.openRoot()) {
            for (int slot = 0; slot < container.size() && left > 0; slot++) {
                ItemResource resource = container.getResource(slot);
                if (resource.isEmpty() || !match.test(remembered, resource.toStack())) {
                    continue;
                }
                int pulled = container.extract(slot, resource, left, transaction);
                left -= pulled;
                for (int rest = pulled; rest > 0; rest -= resource.getMaxStackSize()) {
                    taken.add(resource.toStack(Math.min(rest, resource.getMaxStackSize())));
                }
            }
            transaction.commit();
        }
        return taken;
    }
}
