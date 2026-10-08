package com.leclowndu93150.thaumaturge.content.aura;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.leclowndu93150.thaumaturge.gametest.base.TTTestRegistrar;
import com.leclowndu93150.thaumaturge.registry.TTAttachments;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.event.level.ChunkEvent;

public final class AuraRetrogenTests {
    private static final BlockPos SPOT = new BlockPos(2, 1, 3);

    private AuraRetrogenTests() {}

    public static void register(TTTestRegistrar r) {
        r.add("aura/retrogen_loaded_old_chunk", 40, helper -> {
            LevelChunk chunk = chunk(helper);
            AuraData original = chunk.getData(TTAttachments.AURA.get());
            AuraData missing = new AuraData();
            chunk.setData(TTAttachments.AURA.get(), missing);
            AuraGenHandler.onChunkLoad(new ChunkEvent.Load(chunk, false));
            helper.assertTrue(!missing.isInitialized(), "Chunk loading must defer biome queries until the aura tick");
            helper.runAfterDelay(21, () -> {
                try {
                    helper.assertTrue(missing.isInitialized() && missing.getBase() > 0, "The aura tick must initialize an old loaded chunk");
                    helper.assertTrue(missing.getVis() > 0, "An empty old chunk must gain usable vis");
                    helper.assertTrue(missing.getChunkPos().equals(chunk.getPos()), "Repaired aura must retain the chunk position");
                    helper.succeed();
                } finally {
                    chunk.setData(TTAttachments.AURA.get(), original);
                }
            });
        });

        r.add("aura/retrogen_matches_new_chunk", 20, helper -> {
            LevelChunk chunk = chunk(helper);
            AuraData fresh = new AuraData();
            AuraData legacy = decode("{}");
            helper.assertTrue(!legacy.isInitialized(), "An old empty attachment must need initialization");
            AuraGenHandler.initializeIfNeeded(helper.getLevel(), chunk, fresh);
            AuraGenHandler.initializeIfNeeded(helper.getLevel(), chunk, legacy);
            helper.assertTrue(legacy.isInitialized() && legacy.getBase() > 0, "An empty legacy attachment must initialize");
            helper.assertTrue(legacy.getBase() == fresh.getBase() && legacy.getVis() == fresh.getVis(), "Old and new chunks must use the same deterministic aura generation");
            helper.assertTrue(legacy.getVis() == legacy.getBase() && legacy.getFlux() == 0, "Empty chunks must begin with their normal vis capacity and no flux");
            helper.succeed();
        });

        r.add("aura/retrogen_preserves_partial_aura", 20, helper -> {
            LevelChunk chunk = chunk(helper);
            for (AuraData partial : new AuraData[]{new AuraData((short) 0, 42, 13), new AuraData((short) 0, 0, 15), new AuraData((short) 0, 900, 0)}) {
                float vis = partial.getVis();
                float flux = partial.getFlux();
                AuraGenHandler.initializeIfNeeded(helper.getLevel(), chunk, partial);
                helper.assertTrue(partial.isInitialized() && partial.getBase() > 0, "A partial attachment must acquire a base aura");
                helper.assertTrue(partial.getVis() == vis && partial.getFlux() == flux, "Repair must preserve existing vis and flux exactly");
            }
            helper.succeed();
        });

        r.add("aura/retrogen_preserves_legacy_aura", 20, helper -> {
            AuraData legacy = decode("{\"base\":200,\"vis\":0,\"flux\":25}");
            helper.assertTrue(legacy.isInitialized(), "A legacy nonzero base must count as initialized without the new marker");
            AuraGenHandler.initializeIfNeeded(helper.getLevel(), chunk(helper), legacy);
            helper.assertTrue(legacy.getBase() == 200 && legacy.getVis() == 0 && legacy.getFlux() == 25, "Existing depleted aura must not be reset or refilled");
            helper.succeed();
        });

        r.add("aura/retrogen_zero_base_survives_save", 20, helper -> {
            AuraData barren = new AuraData();
            barren.setBase((short) 0);
            AuraData restored = roundTrip(barren);
            helper.assertTrue(restored.isInitialized(), "A generated zero base must retain its initialization marker when saved");
            AuraGenHandler.initializeIfNeeded(helper.getLevel(), chunk(helper), restored);
            helper.assertTrue(restored.getBase() == 0 && restored.getVis() == 0 && restored.getFlux() == 0, "A deliberately barren chunk must not regenerate on reload");
            helper.succeed();
        });

        r.add("aura/retrogen_does_not_refill_after_reload", 20, helper -> {
            LevelChunk chunk = chunk(helper);
            AuraData repaired = new AuraData();
            AuraGenHandler.initializeIfNeeded(helper.getLevel(), chunk, repaired);
            short base = repaired.getBase();
            repaired.setVis(0);
            repaired.setFlux(35);
            AuraData restored = roundTrip(repaired);
            AuraGenHandler.initializeIfNeeded(helper.getLevel(), chunk, restored);
            helper.assertTrue(restored.isInitialized() && restored.getBase() == base, "Repaired aura must stay initialized across saving");
            helper.assertTrue(restored.getVis() == 0 && restored.getFlux() == 35, "Reloading repaired aura must not refill vis or erase pollution");
            helper.succeed();
        });
    }

    private static LevelChunk chunk(GameTestHelper helper) {
        return helper.getLevel().getChunkAt(helper.absolutePos(SPOT));
    }

    private static AuraData decode(String json) {
        return AuraData.CODEC.codec().parse(JsonOps.INSTANCE, JsonParser.parseString(json)).getOrThrow();
    }

    private static AuraData roundTrip(AuraData data) {
        JsonElement encoded = AuraData.CODEC.codec().encodeStart(JsonOps.INSTANCE, data).getOrThrow();
        return AuraData.CODEC.codec().parse(JsonOps.INSTANCE, encoded).getOrThrow();
    }
}
