package com.leclowndu93150.thaumaturge.content.golem.seals.behavior;

import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import com.leclowndu93150.thaumaturge.content.equipment.EnchantMining;
import com.leclowndu93150.thaumaturge.server.TTFakePlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.FakePlayer;

public final class FellBehavior extends CellWorkBehavior {
    private static final int STAGGER = 33;
    private static final int FELL_XP = 1;

    public FellBehavior() {
        super(STAGGER);
    }

    @Override
    protected boolean isWorkable(Level level, ISealEntity seal, BlockPos pos) {
        return level.getBlockState(pos).is(BlockTags.LOGS);
    }

    @Override
    public boolean completeTask(ServerLevel level, ISealEntity seal, IGolemAPI golem, Task task) {
        boolean continuing = stillMine(level, seal, task) && fell(level, golem, task);
        if (continuing) {
            return false;
        }
        release(task);
        task.end();
        return true;
    }

    private boolean fell(ServerLevel level, IGolemAPI golem, Task task) {
        BlockPos origin = task.pos();
        FakePlayer player = TTFakePlayer.GOLEM.at(level, golem.asEntity());
        golem.swingArm();
        boolean broke = EnchantMining.breakFurthest(level, origin, level.getBlockState(origin), player);
        if (broke) {
            keepAlive(task);
            golem.addRankXp(FELL_XP);
        }
        return broke;
    }
}
