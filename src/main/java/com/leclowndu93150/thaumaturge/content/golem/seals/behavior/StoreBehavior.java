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
import com.leclowndu93150.thaumaturge.api.items.InvHelper.FilterMatch;
import com.leclowndu93150.thaumaturge.api.items.ItemMatch;
import com.leclowndu93150.thaumaturge.api.items.StackFilter;
import com.leclowndu93150.thaumaturge.content.golem.tasks.TaskBoard;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jspecify.annotations.Nullable;

public final class StoreBehavior implements ISealBehavior {
    public static final SealSetting ONLY_EXISTING = new SealSetting("pexist", "gui.thaumaturge.seal.setting.exist", false);

    private static final int STAGGER = 47;
    private static final int CHECK_PERIOD = 20;
    private static final double LOOSE_RANGE = 1.5;
    private static final double TOSS_DRAW = 0.1;
    private static final double TOSS_HORIZONTAL_FACTOR = 0.2;
    private static final double TOSS_BASE_Y = 0.2;
    private static final double TOSS_VERTICAL_FACTOR = 0.5;
    private static final int STORE_XP = 1;

    private final SealClock clock = new SealClock(STAGGER);
    private @Nullable Task standing;

    @Override
    public void tick(ServerLevel level, ISealEntity seal) {
        if (clock.advance() % CHECK_PERIOD == 0 && needsFreshTask(level)) {
            post(level, seal);
        }
    }

    private boolean needsFreshTask(Level level) {
        Task current = standing;
        return current == null || current.isClaimed() || current.isEnded() || current.isCompleted() || !TaskBoard.of(level).isLive(current.id());
    }

    private void post(ServerLevel level, ISealEntity seal) {
        Task task = Task.atBlock(seal.pos(), seal.pos().pos());
        task.setPriority(seal.priority());
        GolemDispatch.postTask(level, task);
        standing = task;
    }

    @Override
    public void onTaskStarted(ServerLevel level, ISealEntity seal, IGolemAPI golem, Task task) {
        if (!seal.isStoppedByRedstone(level)) {
            post(level, seal);
        }
    }

    @Override
    public boolean canPerform(ISealEntity seal, IGolemAPI golem, Task task) {
        Load load = loadOf(seal, golem);
        if (load == null) {
            return false;
        }
        Level level = golem.level();
        SealPos at = seal.pos();
        ResourceHandler<ItemResource> container = InvHelper.handlerAt(level, at.pos(), at.face());
        if (container == null) {
            return load.limit() <= 0 || load.present(level, seal, null) < load.limit();
        }
        if (!InvHelper.acceptsAny(level, at.pos(), at.face(), load.stack().copyWithCount(1))) {
            return false;
        }
        int present = load.present(level, seal, container);
        if (seal.setting(ONLY_EXISTING) && present <= 0) {
            return false;
        }
        return load.limit() <= 0 || present < load.limit();
    }

    @Override
    public boolean completeTask(ServerLevel level, ISealEntity seal, IGolemAPI golem, Task task) {
        Load load = loadOf(seal, golem);
        if (load != null) {
            unload(level, seal, golem, load);
        }
        task.end();
        return true;
    }

    private static void unload(ServerLevel level, ISealEntity seal, IGolemAPI golem, Load load) {
        SealPos at = seal.pos();
        ResourceHandler<ItemResource> container = InvHelper.handlerAt(level, at.pos(), at.face());
        int amount = load.limit() > 0 ? load.limit() - load.present(level, seal, container) : load.stack().getCount();
        if (amount <= 0) {
            return;
        }
        ItemStack given = golem.hands().release(load.stack().copyWithCount(amount));
        if (given.isEmpty()) {
            return;
        }
        int placed = container == null ? drop(level, seal, given) : insert(level, golem, container, given);
        if (placed > 0) {
            HandlingSound.play(golem, HandlingSound.LOW);
            golem.grantRankExperience(STORE_XP);
            golem.animateSwing();
        }
    }

    private static int insert(ServerLevel level, IGolemAPI golem, ResourceHandler<ItemResource> container, ItemStack given) {
        int offered = given.getCount();
        ItemStack refused = InvHelper.insertStack(container, given, false);
        if (!refused.isEmpty()) {
            LooseItems.giveOrDrop(level, golem, refused);
        }
        return offered - refused.getCount();
    }

    private static int drop(ServerLevel level, ISealEntity seal, ItemStack given) {
        RandomSource random = level.getRandom();
        Vec3 toss = new Vec3(tossDraw(random) * TOSS_HORIZONTAL_FACTOR, TOSS_BASE_Y * TOSS_VERTICAL_FACTOR, tossDraw(random) * TOSS_HORIZONTAL_FACTOR);
        LooseItems.spawn(level, LooseItems.inFrontOf(seal.pos().pos(), seal.pos().face()), given, toss);
        return given.getCount();
    }

    private static double tossDraw(RandomSource random) {
        return random.nextDouble() * 2 * TOSS_DRAW - TOSS_DRAW;
    }

    private static @Nullable Load loadOf(ISealEntity seal, IGolemAPI golem) {
        ISealFilter filter = ItemMatchSettings.filterOf(seal);
        FilterMatch match = new StackFilter(filter.stacks(), filter.limits(), filter.isBlacklist(), ItemMatchSettings.of(seal)).firstAcceptedWithLimit(golem.hands().contents());
        if (match.stack().isEmpty()) {
            return null;
        }
        return new Load(match.stack(), filter.usesLimits() ? match.sizeLimit() : 0);
    }

    private record Load(ItemStack stack, int limit) {
        int present(Level level, ISealEntity seal, @Nullable ResourceHandler<ItemResource> container) {
            ItemMatch match = ItemMatchSettings.of(seal);
            if (container != null) {
                return InvHelper.count(container, stack, match);
            }
            return InvHelper.countDropped(level, seal.pos().pos(), stack, LOOSE_RANGE, match);
        }
    }
}
