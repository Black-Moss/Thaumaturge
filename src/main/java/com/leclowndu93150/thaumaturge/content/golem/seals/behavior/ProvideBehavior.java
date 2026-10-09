package com.leclowndu93150.thaumaturge.content.golem.seals.behavior;

import com.leclowndu93150.thaumaturge.api.golems.GolemHelper;
import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import com.leclowndu93150.thaumaturge.api.golems.ProvisionRequest;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealBehavior;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealSetting;
import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import com.leclowndu93150.thaumaturge.api.items.InvHelper;
import com.leclowndu93150.thaumaturge.api.items.InvHelper.InvFilter;
import com.leclowndu93150.thaumaturge.content.golem.seals.SealAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jspecify.annotations.Nullable;

public final class ProvideBehavior implements ISealBehavior {
    public static final SealSetting SINGLE_ITEM = new SealSetting("single_item", "gui.thaumaturge.seal.setting.single", false);
    public static final SealSetting LEAVE_ONE = new SealSetting("leave_one", "gui.thaumaturge.seal.setting.leave", false);

    private static final int STAGGER = 88;
    private static final int CLEANUP_PERIOD = 100;
    private static final int SERVE_PERIOD = 20;
    private static final double SERVE_RANGE_SQR = 4096.0;
    private static final int SEAL_COLLECT_LIFE = 10;
    private static final int OPEN_COLLECT_LIFE = 31000;
    private static final int DELIVERY_LIFE = 31000;
    private static final byte OPEN_COLLECT_PRIORITY = 5;
    private static final int PROVIDER_XP = 1;
    private static final int LEAVE_ONE_MINIMUM = 2;
    private static final int SINGLE_AMOUNT = 1;
    private static final double CELL_CENTER = 0.5;
    private static final double DROP_SPEED = 0.3;

    private final SealClock clock = new SealClock(STAGGER);

    public static boolean supplies(ISealEntity seal, ItemStack stack) {
        return seal.filter().map(filter -> InvHelper.matchesFilters(filter.stacks(), filter.isBlacklist(), stack, ItemMatchSettings.of(seal))).orElse(true);
    }

    @Override
    public void tick(ServerLevel level, ISealEntity seal) {
        int step = clock.advance();
        if (step % CLEANUP_PERIOD == 0) {
            GolemHelper.getProvisionRequests(level).removeIf(request -> isStale(level, request));
        }
        if (step % SERVE_PERIOD == 0) {
            serve(level, seal);
        }
    }

    @Override
    public boolean canPerform(ISealEntity seal, IGolemAPI golem, Task task) {
        ProvisionRequest request = task.linkedProvision();
        if (request == null || !withinHome(golem, destinationOf(request))) {
            return false;
        }
        ItemStack wanted = request.getStack();
        boolean carrying = golem.hands().holds(wanted);
        if (Leg.of(task) == Leg.COLLECT) {
            return !carrying && SealAccess.allows(request.getSeal(), golem) && golem.hands().room(wanted) > 0;
        }
        return carrying;
    }

    @Override
    public boolean completeTask(ServerLevel level, ISealEntity seal, IGolemAPI golem, Task task) {
        ProvisionRequest request = task.linkedProvision();
        if (request != null) {
            switch (Leg.of(task)) {
                case COLLECT -> collect(level, seal, golem, task, request);
                case DELIVER_TO_ENTITY, DELIVER_TO_BLOCK -> deliver(level, golem, request);
            }
        }
        task.end();
        return true;
    }

    @Override
    public void onTaskSuspended(ServerLevel level, ISealEntity seal, Task task) {
        ProvisionRequest request = task.linkedProvision();
        if (request != null && request.getLinkedTask() == task) {
            request.unlink();
        }
    }

    private static boolean withinHome(IGolemAPI golem, @Nullable BlockPos destination) {
        return destination != null && golem.asEntity() instanceof Mob mob && mob.isWithinHome(destination);
    }

    private void serve(ServerLevel level, ISealEntity seal) {
        BlockPos pos = seal.pos().pos();
        ResourceHandler<ItemResource> container = InvHelper.getItemHandlerAt(level, pos, seal.pos().face());
        if (container == null) {
            return;
        }
        int required = seal.setting(LEAVE_ONE) ? LEAVE_ONE_MINIMUM : SINGLE_AMOUNT;
        for (ProvisionRequest request : GolemHelper.getProvisionRequests(level)) {
            if (isServable(seal, container, request, required)) {
                postCollect(level, seal, request);
                return;
            }
        }
    }

    private boolean isServable(ISealEntity seal, ResourceHandler<ItemResource> container, ProvisionRequest request, int required) {
        BlockPos destination = destinationOf(request);
        return !request.isSpent() && request.getLinkedTask() == null && destination != null && destination.distSqr(seal.pos().pos()) < SERVE_RANGE_SQR && supplies(seal, request.getStack())
                && InvHelper.countTotalItemsIn(container, request.getStack(), InvFilter.STRICT) >= required;
    }

    private void postCollect(ServerLevel level, ISealEntity seal, ProvisionRequest request) {
        ISealEntity requester = request.getSeal();
        byte priority = OPEN_COLLECT_PRIORITY;
        int life = OPEN_COLLECT_LIFE;
        if (requester != null) {
            priority = requester.priority();
            life = SEAL_COLLECT_LIFE;
        }
        Task pickup = Task.atBlock(seal.pos(), seal.pos().pos());
        pickup.setPriority(priority);
        pickup.setLife(life);
        pickup.setData(Leg.COLLECT.ordinal());
        GolemHelper.addGolemTask(level, pickup);
        request.link(pickup);
    }

    private void collect(ServerLevel level, ISealEntity seal, IGolemAPI golem, Task task, ProvisionRequest request) {
        BlockPos pos = seal.pos().pos();
        Direction face = seal.pos().face();
        ResourceHandler<ItemResource> container = InvHelper.getItemHandlerAt(level, pos, face);
        if (container == null) {
            request.unlink();
            return;
        }
        ItemStack wanted = request.getStack();
        int amount = seal.setting(SINGLE_ITEM) ? SINGLE_AMOUNT : wanted.getCount();
        if (seal.setting(LEAVE_ONE)) {
            amount = Math.min(amount, InvHelper.countTotalItemsIn(container, wanted, InvFilter.STRICT) - 1);
        }
        amount = Math.min(amount, golem.hands().room(wanted));
        ItemStack removed = amount > 0 ? InvHelper.removeStackFrom(container, wanted.copyWithCount(amount), InvFilter.STRICT, false) : ItemStack.EMPTY;
        if (removed.isEmpty()) {
            request.unlink();
            return;
        }
        ItemStack refused = InvHelper.insertStack(container, golem.hands().hold(removed), false);
        drop(level, pos, face, refused);
        HandlingSound.play(golem, HandlingSound.HIGH);
        golem.addRankXp(PROVIDER_XP);
        golem.swingArm();
        if (request.getSeal() == null) {
            postDelivery(level, seal, task, request);
        }
    }

    private void postDelivery(ServerLevel level, ISealEntity seal, Task collectTask, ProvisionRequest request) {
        Entity recipient = request.getEntity();
        BlockPos block = request.getPos();
        Task delivery;
        Leg leg;
        if (recipient != null) {
            delivery = Task.onEntity(seal.pos(), recipient);
            leg = Leg.DELIVER_TO_ENTITY;
        } else if (block != null) {
            delivery = Task.atBlock(seal.pos(), block);
            leg = Leg.DELIVER_TO_BLOCK;
        } else {
            return;
        }
        delivery.setData(leg.ordinal());
        delivery.setPriority(collectTask.priority());
        delivery.setLife(DELIVERY_LIFE);
        GolemHelper.addGolemTask(level, delivery);
        request.link(delivery);
    }

    private void deliver(ServerLevel level, IGolemAPI golem, ProvisionRequest request) {
        ItemStack wanted = request.getStack();
        ItemStack released = golem.hands().release(wanted.copy());
        int shortBy = wanted.getCount() - released.getCount();
        if (shortBy > 0) {
            requestMissing(level, request, wanted.copyWithCount(shortBy));
        }
        Entity recipient = request.getEntity();
        if (recipient != null) {
            InvHelper.dropItemAtEntity(level, released, recipient);
        } else if (request.getPos() != null && request.getSide() != null) {
            insertOrDrop(level, golem, request.getPos(), request.getSide(), released);
        }
        HandlingSound.play(golem, HandlingSound.LOW);
        golem.swingArm();
        request.markSpent();
    }

    private void requestMissing(ServerLevel level, ProvisionRequest request, ItemStack missing) {
        GolemHelper.getProvisionRequests(level).remove(request);
        Entity recipient = request.getEntity();
        if (recipient != null) {
            GolemHelper.requestProvisioning(level, recipient, missing);
            return;
        }
        if (request.getPos() != null && request.getSide() != null) {
            GolemHelper.requestProvisioning(level, request.getPos(), request.getSide(), missing);
        }
    }

    private void insertOrDrop(ServerLevel level, IGolemAPI golem, BlockPos block, Direction side, ItemStack released) {
        ResourceHandler<ItemResource> container = InvHelper.getItemHandlerAt(level, block, side);
        if (container == null) {
            drop(level, block, side, released);
            return;
        }
        ItemStack leftover = InvHelper.insertStack(container, released, false);
        drop(level, block, side, leftover.isEmpty() ? leftover : golem.hands().hold(leftover));
    }

    private static boolean isStale(Level level, ProvisionRequest request) {
        if (request.isSpent() || level.getGameTime() > request.expiresAt()) {
            return true;
        }
        Task linked = request.getLinkedTask();
        if (linked != null && (linked.isEnded() || linked.isCompleted())) {
            return true;
        }
        ISealEntity requester = request.getSeal();
        return requester != null && GolemHelper.getSealEntity(level, requester.pos()) == null;
    }

    private static @Nullable BlockPos destinationOf(ProvisionRequest request) {
        Entity recipient = request.getEntity();
        ISealEntity requester = request.getSeal();
        if (requester == null) {
            return recipient != null ? recipient.blockPosition() : request.getPos();
        }
        return requester.pos().pos();
    }

    private static void drop(Level level, BlockPos pos, Direction face, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        BlockPos cell = level.getBlockState(pos).isCollisionShapeFullBlock(level, pos) ? pos.relative(face) : pos;
        Vec3 push = Vec3.atLowerCornerOf(face.getUnitVec3i()).scale(DROP_SPEED);
        ItemEntity dropped = new ItemEntity(level, cell.getX() + CELL_CENTER, cell.getY() + CELL_CENTER, cell.getZ() + CELL_CENTER, stack);
        dropped.setDeltaMovement(push);
        level.addFreshEntity(dropped);
    }

    private enum Leg {
        COLLECT, DELIVER_TO_ENTITY, DELIVER_TO_BLOCK;

        private static final Leg[] ALL = values();

        static Leg of(Task task) {
            return ALL[Math.floorMod(task.data(), ALL.length)];
        }
    }
}
