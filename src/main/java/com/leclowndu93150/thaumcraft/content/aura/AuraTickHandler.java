package com.leclowndu93150.thaumcraft.content.aura;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.registry.TCAttachments;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.MoonPhase;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber(modid = TCIds.MODID)
public final class AuraTickHandler {
    private static final int TICK_INTERVAL = 20;

    private static final float[] PHASE_TABLE = new float[]{0.25F, 0.15F, 0.1F, 0.05F, 0.0F, 0.05F, 0.1F, 0.15F};
    private static final float[] MAX_TABLE = new float[]{0.15F, 0.05F, 0.0F, -0.05F, -0.15F, -0.05F, 0.0F, 0.05F};

    private static final Map<ResourceKey<Level>, PhaseState> PHASES = new HashMap<>();
    private static long tickCounter;

    private AuraTickHandler() {}

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        tickCounter++;
        if (tickCounter % TICK_INTERVAL != 0) {
            return;
        }
        MinecraftServer server = event.getServer();
        for (ServerLevel level : server.getAllLevels()) {
            tickLevel(level);
        }
    }

    private static void tickLevel(ServerLevel level) {
        PhaseState state = PHASES.computeIfAbsent(level.dimension(), k -> new PhaseState());
        long worldTime = level.getGameTime();
        if (state.lastWorldTime == worldTime) {
            return;
        }
        state.lastWorldTime = worldTime;
        MoonPhase moonPhase = level.environmentAttributes().getValue(
                EnvironmentAttributes.MOON_PHASE,
                Vec3.ZERO
        );
        int phaseIndex = moonPhase.index();
        state.phaseVis = PHASE_TABLE[phaseIndex];
        state.phaseMax = 1.0F + MAX_TABLE[phaseIndex];
        state.phaseFlux = 0.25F - state.phaseVis;

        RandomSource rand = level.getRandom();
        Set<ChunkPos> loaded = AuraManager.loadedChunksSnapshot(level);
        for (ChunkPos pos : loaded) {
            if (!level.hasChunk(pos.x(), pos.z())) {
                continue;
            }
            LevelChunk chunk = level.getChunk(pos.x(), pos.z());
            AuraData data = chunk.getData(TCAttachments.AURA.get());
            if (data.getBase() == 0) {
                continue;
            }
            data.setChunkPos(pos);
            processAuraChunk(level, chunk, data, state, rand);
        }
    }

    private static void processAuraChunk(ServerLevel level, LevelChunk chunk, AuraData auraChunk, PhaseState state, RandomSource rand) {
        List<Integer> directions = new ArrayList<>(Arrays.asList(0, 1, 2, 3));
        Collections.shuffle(directions, new java.util.Random(rand.nextLong()));
        int x = auraChunk.getChunkPos().x();
        int z = auraChunk.getChunkPos().z();
        float base = auraChunk.getBase() * state.phaseMax;
        boolean dirty = false;
        float currentVis = auraChunk.getVis();
        float currentFlux = auraChunk.getFlux();
        AuraData neighbourVisChunk = null;
        AuraData neighbourFluxChunk = null;
        LevelChunk neighbourVisLevelChunk = null;
        LevelChunk neighbourFluxLevelChunk = null;
        float lowestVis = Float.MAX_VALUE;
        float lowestFlux = Float.MAX_VALUE;

        for (Integer a : directions) {
            Direction dir = Direction.from2DDataValue(a);
            int nx = x + dir.getStepX();
            int nz = z + dir.getStepZ();
            if (!level.hasChunk(nx, nz)) {
                continue;
            }
            LevelChunk n = level.getChunk(nx, nz);
            AuraData nd = n.getData(TCAttachments.AURA.get());
            if (nd.getBase() == 0) {
                continue;
            }
            nd.setChunkPos(new ChunkPos(nx, nz));
            if ((neighbourVisChunk == null || lowestVis > nd.getVis()) && nd.getVis() + nd.getFlux() < nd.getBase() * state.phaseMax) {
                neighbourVisChunk = nd;
                neighbourVisLevelChunk = n;
                lowestVis = nd.getVis();
            }
            if (neighbourFluxChunk == null || lowestFlux > nd.getFlux()) {
                neighbourFluxChunk = nd;
                neighbourFluxLevelChunk = n;
                lowestFlux = nd.getFlux();
            }
        }

        if (neighbourVisChunk != null && lowestVis < currentVis && lowestVis / currentVis < 0.75F) {
            float inc = Math.min(currentVis - lowestVis, 1.0F);
            currentVis -= inc;
            neighbourVisChunk.setVis(lowestVis + inc);
            dirty = true;
            neighbourVisLevelChunk.markUnsaved();
        }

        if (neighbourFluxChunk != null && currentFlux > Math.max(5.0F, auraChunk.getBase() / 10.0F) && lowestFlux < currentFlux / 1.75F) {
            float inc = Math.min(currentFlux - lowestFlux, 1.0F);
            currentFlux -= inc;
            neighbourFluxChunk.setFlux(lowestFlux + inc);
            dirty = true;
            neighbourFluxLevelChunk.markUnsaved();
        }

        if (currentVis + currentFlux < base) {
            float inc = Math.min(base - (currentVis + currentFlux), state.phaseVis);
            currentVis += inc;
            dirty = true;
        } else if (currentVis > base * 1.25F && rand.nextFloat() < 0.1F) {
            currentFlux += state.phaseFlux;
            currentVis -= state.phaseFlux;
            dirty = true;
        } else if (currentVis <= base * 0.1F && currentVis >= currentFlux && rand.nextFloat() < 0.1F) {
            currentFlux += state.phaseFlux;
            dirty = true;
        }

        if (dirty) {
            auraChunk.setVis(currentVis);
            auraChunk.setFlux(currentFlux);
            chunk.markUnsaved();
        }

        if (currentFlux > base * 0.75F && rand.nextFloat() < currentFlux / 500.0F / 10.0F) {
            AuraManager.queueRiftTrigger(level, new BlockPos(x * 16, 0, z * 16));
        }
    }

    private static final class PhaseState {
        long lastWorldTime = Long.MIN_VALUE;
        float phaseVis;
        float phaseFlux;
        float phaseMax;
    }

    @SuppressWarnings("unused")
    private static @Nullable AuraData neighbour(ServerLevel level, int x, int z) {
        if (!level.hasChunk(x, z)) {
            return null;
        }
        return level.getChunk(x, z).getData(TCAttachments.AURA.get());
    }
}
