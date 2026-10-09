package com.leclowndu93150.thaumaturge.content.golem;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.golem.seals.SealEntity;
import com.leclowndu93150.thaumaturge.content.golem.seals.SealHandler;
import com.leclowndu93150.thaumaturge.content.golem.tasks.TaskBoard;
import com.leclowndu93150.thaumaturge.network.ClientboundSealPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.level.ChunkWatchEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = TTIds.MODID)
public final class GolemWorldEvents {
    private static final int TASK_CLEAR_INTERVAL_TICKS = 20;

    private GolemWorldEvents() {}

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel serverLevel && event.getChunk() instanceof LevelChunk chunk) {
            SealHandler.loadChunkSeals(serverLevel, chunk);
        }
    }

    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel serverLevel && event.getChunk() instanceof LevelChunk chunk) {
            SealHandler.unloadChunkSeals(serverLevel, chunk);
        }
    }

    @SubscribeEvent
    public static void onChunkWatch(ChunkWatchEvent.Sent event) {
        ServerPlayer viewer = event.getPlayer();
        for (SealEntity seal : SealHandler.chunkSeals(event.getChunk())) {
            PacketDistributor.sendToPlayer(viewer, ClientboundSealPayload.update(seal));
        }
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        if (isSweepTick(level.getGameTime())) {
            sweepTasks(level);
        }
        SealHandler.runTicks(level);
    }

    private static boolean isSweepTick(long gameTime) {
        return gameTime % TASK_CLEAR_INTERVAL_TICKS == 0;
    }

    private static void sweepTasks(ServerLevel level) {
        TaskBoard.of(level).sweep(level);
    }
}
