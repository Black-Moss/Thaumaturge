package com.leclowndu93150.thaumaturge.content.golem.seals.behavior;

import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealFilter;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealSetting;
import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import com.leclowndu93150.thaumaturge.content.casters.BlockBreakerEngine;
import com.leclowndu93150.thaumaturge.server.TTFakePlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

public final class BreakBehavior extends CellWorkBehavior {
    public static final SealSetting SILK_TOUCH = new SealSetting("psilk", "gui.thaumaturge.seal.setting.silk", false);

    private static final int STAGGER = 42;
    private static final float WORK_PER_HARDNESS = 10.0F;
    private static final int CHIP_POWER = 21;
    private static final int SILK_CHIP_POWER = 7;
    private static final int CRACK_STAGES = 9;
    private static final int FINAL_CRACK_STAGE = 10;
    private static final float SOUND_VOLUME_BIAS = 0.7F;
    private static final float SOUND_VOLUME_DIVISOR = 8.0F;
    private static final float SOUND_PITCH_FACTOR = 0.5F;
    private static final int BREAK_XP = 1;
    private static final int NO_FORTUNE = 0;

    public BreakBehavior() {
        super(STAGGER);
    }

    @Override
    protected boolean isWorkable(Level level, ISealEntity seal, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir() || state.getDestroySpeed(level, pos) < 0.0F) {
            return false;
        }
        ISealFilter filter = seal.filter().orElse(null);
        return filter == null || passesFilter(filter, state);
    }

    private static boolean passesFilter(ISealFilter filter, BlockState state) {
        Item blockItem = state.getBlock().asItem();
        boolean blacklist = filter.isBlacklist();
        for (ItemStack entry : filter.stacks()) {
            if (entry.isEmpty()) {
                continue;
            }
            boolean equal = blockItem != Items.AIR && entry.is(blockItem);
            if (blacklist == equal) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void prepare(ServerLevel level, BlockPos pos, Task task) {
        task.setData((int) (level.getBlockState(pos).getDestroySpeed(level, pos) * WORK_PER_HARDNESS));
    }

    @Override
    public boolean completeTask(ServerLevel level, ISealEntity seal, IGolemAPI golem, Task task) {
        if (!stillMine(level, seal, task)) {
            task.end();
            return true;
        }
        BlockPos pos = task.pos();
        BlockState state = level.getBlockState(pos);
        int breaker = golem.asEntity().getId();
        golem.swingArm();
        boolean silk = seal.setting(SILK_TOUCH);
        int chip = silk ? SILK_CHIP_POWER : CHIP_POWER;
        if (task.data() > chip) {
            chipAway(level, pos, state, task, chip, breaker);
            return false;
        }
        level.destroyBlockProgress(breaker, pos, FINAL_CRACK_STAGE);
        BlockBreakerEngine.harvestBlock(level, TTFakePlayer.GOLEM.at(level, golem.asEntity()), pos, silk, NO_FORTUNE);
        golem.addRankXp(BREAK_XP);
        release(task);
        task.end();
        return true;
    }

    private static void chipAway(ServerLevel level, BlockPos pos, BlockState state, Task task, int chip, int breaker) {
        keepAlive(task);
        int remaining = task.data() - chip;
        task.setData(remaining);
        SoundType sound = state.getSoundType();
        level.playSound(null, pos, sound.getBreakSound(), SoundSource.BLOCKS, (sound.getVolume() + SOUND_VOLUME_BIAS) / SOUND_VOLUME_DIVISOR, sound.getPitch() * SOUND_PITCH_FACTOR);
        float total = state.getDestroySpeed(level, pos) * WORK_PER_HARDNESS;
        int stage = Mth.clamp(Mth.floor(CRACK_STAGES * (1.0F - remaining / total)), 0, CRACK_STAGES);
        level.destroyBlockProgress(breaker, pos, stage);
    }
}
