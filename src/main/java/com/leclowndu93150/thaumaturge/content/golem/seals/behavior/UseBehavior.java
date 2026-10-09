package com.leclowndu93150.thaumaturge.content.golem.seals.behavior;

import com.leclowndu93150.thaumaturge.api.golems.GolemHelper;
import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealBehavior;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealFilter;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealSetting;
import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import com.leclowndu93150.thaumaturge.content.golem.GolemInteractionHelper;
import com.leclowndu93150.thaumaturge.content.golem.tasks.TaskBoard;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public final class UseBehavior implements ISealBehavior {
    public static final SealSetting LEFT_CLICK = new SealSetting("left_click", "gui.thaumaturge.seal.setting.left", false);
    public static final SealSetting INTO_AIR = new SealSetting("into_air", "gui.thaumaturge.seal.setting.empty", false);
    public static final SealSetting BARE_HANDED = new SealSetting("bare_handed", "gui.thaumaturge.seal.setting.emptyhand", false);
    public static final SealSetting SNEAKING = new SealSetting("sneaking", "gui.thaumaturge.seal.setting.sneak", false);
    public static final SealSetting REQUEST_ITEMS = new SealSetting("request_items", "gui.thaumaturge.seal.setting.provision_whitelist", false);

    private static final int STAGGER = 49;
    private static final int SCAN_PERIOD = 5;
    private static final int TOOL_SLOT = 0;

    private final SealClock clock = new SealClock(STAGGER);
    private @Nullable Task pending;

    @Override
    public void tick(ServerLevel level, ISealEntity seal) {
        if (clock.advance() % SCAN_PERIOD != 0) {
            return;
        }
        if (pending != null && TaskBoard.of(level).isLive(pending.id())) {
            return;
        }
        if (isReady(level, seal)) {
            Task task = Task.atBlock(seal.pos(), seal.pos().pos());
            task.setPriority(seal.priority());
            GolemHelper.addGolemTask(level, task);
            pending = task;
        }
    }

    @Override
    public boolean completeTask(ServerLevel level, ISealEntity seal, IGolemAPI golem, Task task) {
        boolean bare = seal.setting(BARE_HANDED);
        ItemStack tool = selectTool(seal, golem);
        if (isReady(level, seal) && (!tool.isEmpty() || bare)) {
            ItemStack released = tool.isEmpty() ? ItemStack.EMPTY : golem.hands().release(tool.copy());
            ItemStack held = bare ? ItemStack.EMPTY : released;
            GolemInteractionHelper.golemClick(level, golem, seal.pos().pos(), seal.pos().face(), held, seal.setting(LEFT_CLICK), seal.setting(SNEAKING));
        }
        task.end();
        return true;
    }

    @Override
    public boolean canPerform(ISealEntity seal, IGolemAPI golem, Task task) {
        if (seal.setting(BARE_HANDED)) {
            return true;
        }
        ISealFilter filter = ItemMatchSettings.filterOf(seal);
        ItemStack required = filter.stack(TOOL_SLOT);
        if (required.isEmpty()) {
            return false;
        }
        for (ItemStack carried : golem.hands().contents()) {
            if (!carried.isEmpty() && ItemMatchSettings.accepts(seal, carried)) {
                return true;
            }
        }
        if (seal.setting(REQUEST_ITEMS) && !filter.isBlacklist()) {
            GolemHelper.requestProvisioning(golem.level(), seal, required);
        }
        return false;
    }

    private static boolean isReady(Level level, ISealEntity seal) {
        return level.getBlockState(seal.pos().pos()).isAir() == seal.setting(INTO_AIR);
    }

    private static ItemStack selectTool(ISealEntity seal, IGolemAPI golem) {
        boolean unrestricted = ItemMatchSettings.filterOf(seal).stack(TOOL_SLOT).isEmpty();
        for (ItemStack carried : golem.hands().contents()) {
            if (!carried.isEmpty() && (unrestricted || ItemMatchSettings.accepts(seal, carried))) {
                return carried;
            }
        }
        return ItemStack.EMPTY;
    }
}
