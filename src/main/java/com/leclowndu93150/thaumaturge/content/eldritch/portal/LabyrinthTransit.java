package com.leclowndu93150.thaumaturge.content.eldritch.portal;

import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthLandmarks;
import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthPhase;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeServerConfig;
import com.leclowndu93150.thaumaturge.config.labyrinth.LabyrinthConfig;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.LabyrinthService;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.MazeRecord;
import com.leclowndu93150.thaumaturge.content.misc.TTActionBar;
import com.leclowndu93150.thaumaturge.registry.TTAttachments;
import com.leclowndu93150.thaumaturge.registry.TTTicketTypes;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;

public final class LabyrinthTransit {
    private static final long NOTICE_INTERVAL = 40;
    private static final int CREATIVE_WARMUP = 1;
    private static final String CLOSED = "gui.thaumaturge.labyrinth.closed";

    private LabyrinthTransit() {}

    public static boolean touch(ServerLevel level, ServerPlayer player, Optional<PortalLink> link) {
        TransitState state = player.getData(TTAttachments.LABYRINTH_TRANSIT);
        long now = level.getGameTime();
        if (now < state.cooldownUntil()) {
            return false;
        }
        Optional<Target> target = link.flatMap(value -> resolve(level.getServer(), value));
        if (target.isEmpty()) {
            if (state.noticeDue(now, NOTICE_INTERVAL)) {
                TTActionBar.sendPurple(player, CLOSED);
            }
            return false;
        }
        Optional<TransitState.Pending> pending = state.pending();
        if (pending.isEmpty() || !pending.get().link().equals(link.get()) || now - pending.get().started() > TTTicketTypes.TRANSIT_LIFETIME) {
            release(level.getServer(), state);
            ServerLevel destination = target.get().level();
            ChunkPos chunk = ChunkPos.containing(target.get().anchor());
            int radius = ThaumaturgeServerConfig.LABYRINTH.preloadRadiusChunks.get();
            CompletableFuture<?> loaded = destination.getChunkSource().addTicketAndLoadWithRadius(TTTicketTypes.LABYRINTH_TRANSIT.get(), chunk, radius);
            state.setPending(Optional.of(new TransitState.Pending(link.get(), destination.dimension(), chunk, radius, loaded, now)));
        }
        return true;
    }

    public static int transitionTime(ServerLevel level, ServerPlayer player) {
        Optional<TransitState.Pending> pending = player.getData(TTAttachments.LABYRINTH_TRANSIT).pending();
        if (pending.isEmpty()) {
            return Integer.MAX_VALUE;
        }
        LabyrinthConfig config = ThaumaturgeServerConfig.LABYRINTH;
        boolean ready = pending.get().loaded().isDone() || level.getGameTime() - pending.get().started() >= config.maxPreloadWaitTicks.get();
        if (!ready) {
            return Integer.MAX_VALUE;
        }
        return player.getAbilities().invulnerable ? CREATIVE_WARMUP : config.warmupTicks.get();
    }

    public static Optional<TeleportTransition> destination(ServerLevel level, ServerPlayer player) {
        Optional<TransitState.Pending> pending = player.getData(TTAttachments.LABYRINTH_TRANSIT).pending();
        if (pending.isEmpty()) {
            return Optional.empty();
        }
        PortalLink link = pending.get().link();
        Optional<Target> target = resolve(level.getServer(), link);
        if (target.isEmpty()) {
            return Optional.empty();
        }
        TeleportTransition.PostTeleportTransition post = TeleportTransition.PLAY_PORTAL_SOUND.then(LabyrinthTransit::finish);
        if (link.kind() == PortalLink.Kind.TO_LABYRINTH && target.get().maze().isPresent()) {
            ArrivalPad.Arrival arrival = ArrivalPad.prepare(target.get().level(), target.get().maze().get());
            return Optional.of(new TeleportTransition(target.get().level(), arrival.pos(), Vec3.ZERO, arrival.yaw(), 0.0F, post));
        }
        return Optional.of(ReturnPointFinder.find(player, target.get().level(), target.get().anchor(), post));
    }

    public static TeleportTransition returnTransition(ServerPlayer player, Optional<MazeRecord> record, TeleportTransition.PostTeleportTransition post) {
        Target target = origin(player.level().getServer(), record);
        return ReturnPointFinder.find(player, target.level(), target.anchor(), post);
    }

    public static void abandon(ServerPlayer player) {
        release(player.level().getServer(), player.getData(TTAttachments.LABYRINTH_TRANSIT));
    }

    private static void finish(Entity entity) {
        if (entity instanceof ServerPlayer player) {
            TransitState state = player.getData(TTAttachments.LABYRINTH_TRANSIT);
            release(player.level().getServer(), state);
            state.setCooldownUntil(player.level().getGameTime() + ThaumaturgeServerConfig.LABYRINTH.cooldownTicks.get());
        }
    }

    private static void release(MinecraftServer server, TransitState state) {
        state.pending().ifPresent(pending -> {
            ServerLevel level = server.getLevel(pending.level());
            if (level != null) {
                level.getChunkSource().removeTicketWithRadius(TTTicketTypes.LABYRINTH_TRANSIT.get(), pending.chunk(), pending.radius());
            }
        });
        state.setPending(Optional.empty());
    }

    private static Optional<Target> resolve(MinecraftServer server, PortalLink link) {
        Optional<MazeRecord> record = LabyrinthService.byId(server, link.maze());
        return switch (link.kind()) {
            case TO_LABYRINTH -> record.filter(maze -> maze.state().phase() != LabyrinthPhase.RETIRED)
                    .flatMap(maze -> LabyrinthService.outer(server).flatMap(outer -> maze.plan().landmarks().point(LabyrinthLandmarks.ARRIVAL).map(anchor -> new Target(outer, anchor, record))));
            case TO_ORIGIN -> Optional.of(origin(server, record));
        };
    }

    private static Target origin(MinecraftServer server, Optional<MazeRecord> record) {
        Optional<GlobalPos> origin = record.map(maze -> maze.plan().origin());
        Optional<ServerLevel> level = origin.flatMap(pos -> Optional.ofNullable(server.getLevel(pos.dimension())));
        if (level.isPresent()) {
            return new Target(level.get(), origin.get().pos(), record);
        }
        GlobalPos spawn = server.getRespawnData().globalPos();
        ServerLevel spawnLevel = Optional.ofNullable(server.getLevel(spawn.dimension())).orElseGet(server::overworld);
        return new Target(spawnLevel, spawn.pos(), record);
    }

    private record Target(ServerLevel level, BlockPos anchor, Optional<MazeRecord> maze) {
    }
}
