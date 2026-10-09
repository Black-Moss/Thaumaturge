package com.leclowndu93150.thaumaturge.content.eldritch.block;

import com.leclowndu93150.thaumaturge.api.warp.WarpHelper;
import com.leclowndu93150.thaumaturge.api.warp.WarpType;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeServerConfig;
import com.leclowndu93150.thaumaturge.content.effect.Effects;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public final class BlockEntityEldritchTrap extends BlockEntity {
    private static final int INITIAL_TIMER = 20;
    private static final int INTERVAL_BASE = 10;
    private static final int INTERVAL_SPREAD = 25;
    private static final double TRIGGER_RANGE = 3.0;
    private static final int MAX_WARP = 2;

    private int checkTimer = INITIAL_TIMER;

    public BlockEntityEldritchTrap(BlockPos pos, BlockState state) {
        super(TTBlockEntities.ELDRITCH_TRAP.get(), pos, state);
    }

    public void serverTick(Level level, BlockPos pos) {
        if (checkTimer-- > 0) {
            return;
        }
        RandomSource random = level.getRandom();
        checkTimer = INTERVAL_BASE + random.nextInt(INTERVAL_SPREAD);
        if (level instanceof ServerLevel serverLevel) {
            probe(serverLevel, Vec3.atCenterOf(pos), random);
        }
    }

    private static void probe(ServerLevel level, Vec3 center, RandomSource random) {
        Player victim = level.getNearestPlayer(center.x, center.y, center.z, TRIGGER_RANGE, EntitySelector.NO_SPECTATORS);
        if (victim == null) {
            return;
        }
        float damage = ThaumaturgeServerConfig.LABYRINTH.trapDamage.get().floatValue();
        victim.hurtServer(level, level.damageSources().magic(), damage);
        if (victim instanceof ServerPlayer player && random.nextBoolean()) {
            WarpHelper.addWarp(player, random.nextInt(MAX_WARP) + 1, WarpType.TEMPORARY);
        }
        Effects.boltStrike(level, center).to(victim.getEyePosition()).send();
    }
}
