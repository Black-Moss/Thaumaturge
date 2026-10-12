package com.leclowndu93150.thaumaturge.content.aura;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aura.BiomeAuraModifier;
import com.leclowndu93150.thaumaturge.registry.TTDataMaps;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;

@EventBusSubscriber(modid = TTIds.MODID)
public final class AuraGenHandler {
    private static final float DEFAULT_MODIFIER = 0.5F;
    private static final int MAX_BASE = 500;
    private static final float BASE_SPREAD = 0.1F;
    private static final int PROBE_HEIGHT = 50;
    private static final int[][] NEIGHBOURHOOD = {{0, 0}, {0, 1}, {-1, 0}, {0, -1}, {1, 0}};

    private AuraGenHandler() {}

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel level) {
            AuraManager.onChunkLoaded(level, event.getChunk().getPos());
        }
    }

    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level) {
            AuraManager.onChunkUnloaded(level, event.getChunk().getPos());
        }
    }

    static void initializeIfNeeded(ServerLevel level, LevelChunk chunk, AuraData data) {
        if (data.isInitialized()) {
            return;
        }
        ChunkPos pos = chunk.getPos();
        boolean pristine = data.getVis() == 0.0F && data.getFlux() == 0.0F;
        short base = rollBase(level, pos);
        data.setBase(base);
        data.setChunkPos(pos);
        if (pristine) {
            data.setVis(base);
        }
        chunk.markUnsaved();
    }

    private static short rollBase(ServerLevel level, ChunkPos pos) {
        float average = neighbourhoodModifier(level, pos);
        RandomSource seeded = new LegacyRandomSource(level.getSeed() ^ ChunkPos.pack(pos.x(), pos.z()));
        float noise = (float) (1.0 + seeded.nextGaussian() * BASE_SPREAD);
        short scaled = (short) (average * MAX_BASE * noise);
        return (short) Mth.clamp(scaled, 0, MAX_BASE);
    }

    private static float neighbourhoodModifier(ServerLevel level, ChunkPos pos) {
        float sum = 0.0F;
        for (int[] step : NEIGHBOURHOOD) {
            ChunkPos sample = new ChunkPos(pos.x() + step[0], pos.z() + step[1]);
            sum += modifierAtCentre(level, sample);
        }
        return sum / NEIGHBOURHOOD.length;
    }

    private static float modifierAtCentre(ServerLevel level, ChunkPos pos) {
        BiomeAuraModifier modifier = level.getBiome(new BlockPos(pos.getMiddleBlockX(), PROBE_HEIGHT, pos.getMiddleBlockZ())).getData(TTDataMaps.BIOME_AURA_MODIFIER);
        return modifier == null ? DEFAULT_MODIFIER : modifier.value();
    }
}
