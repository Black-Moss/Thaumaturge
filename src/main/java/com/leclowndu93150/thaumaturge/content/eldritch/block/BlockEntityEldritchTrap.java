package com.leclowndu93150.thaumaturge.content.eldritch.block;

import com.leclowndu93150.thaumaturge.api.warp.WarpHelper;
import com.leclowndu93150.thaumaturge.api.warp.WarpType;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeServerConfig;
import com.leclowndu93150.thaumaturge.network.effect.ClientboundStreamEffectPayload;
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
import net.neoforged.neoforge.network.PacketDistributor;

public final class BlockEntityEldritchTrap extends BlockEntity {
    private static final int ARMING_DELAY = 20;
    private static final int MIN_REARM_DELAY = 10;
    private static final int EXTRA_REARM_DELAY = 26;
    private static final double REACH = 3.0;
    private static final float WARP_CHANCE = 0.5F;
    private static final int MIN_WARP = 1;
    private static final int EXTRA_WARP = 2;
    private static final double BOLT_VISIBILITY = 32.0;
    private static final int BOLT_COLOR = 0xD8B8FF;
    private static final float BOLT_WIDTH = 1.0F;
    private static final int NO_SOURCE_ENTITY = -1;

    private int countdown = ARMING_DELAY;

    public BlockEntityEldritchTrap(BlockPos pos, BlockState state) {
        super(TTBlockEntities.ELDRITCH_TRAP.get(), pos, state);
    }

    public void serverTick(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel server) || --countdown > 0) {
            return;
        }
        RandomSource random = server.getRandom();
        countdown = MIN_REARM_DELAY + random.nextInt(EXTRA_REARM_DELAY);
        Vec3 center = Vec3.atCenterOf(pos);
        Player victim = server.getNearestPlayer(center.x, center.y, center.z, REACH, EntitySelector.NO_SPECTATORS);
        if (victim instanceof ServerPlayer player) {
            zap(server, center, player, random);
        }
    }

    private static void zap(ServerLevel level, Vec3 center, ServerPlayer player, RandomSource random) {
        float damage = ThaumaturgeServerConfig.LABYRINTH.trapDamage.get().floatValue();
        boolean hurt = player.hurtServer(level, level.damageSources().magic(), damage);
        if (hurt && random.nextFloat() < WARP_CHANCE) {
            WarpHelper.addWarp(player, MIN_WARP + random.nextInt(EXTRA_WARP), WarpType.TEMPORARY);
        }
        Vec3 eyes = player.getEyePosition();
        ClientboundStreamEffectPayload bolt = ClientboundStreamEffectPayload.bolt(center.x, center.y, center.z, eyes.x, eyes.y, eyes.z, BOLT_COLOR, BOLT_WIDTH, NO_SOURCE_ENTITY);
        PacketDistributor.sendToPlayersNear(level, null, center.x, center.y, center.z, BOLT_VISIBILITY, bolt);
    }
}
