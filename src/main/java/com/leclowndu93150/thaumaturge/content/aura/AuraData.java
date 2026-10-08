package com.leclowndu93150.thaumaturge.content.aura;

import com.leclowndu93150.thaumaturge.api.aura.IAuraChunk;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public final class AuraData implements IAuraChunk {
    public static final MapCodec<AuraData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(Codec.SHORT.optionalFieldOf("base", (short) 0).forGetter(AuraData::getBase), Codec.FLOAT.optionalFieldOf("vis", 0.0F).forGetter(AuraData::getVis),
                    Codec.FLOAT.optionalFieldOf("flux", 0.0F).forGetter(AuraData::getFlux), Codec.BOOL.optionalFieldOf("initialized", false).forGetter(AuraData::isInitialized))
            .apply(instance, AuraData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, AuraData> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.SHORT, AuraData::getBase, ByteBufCodecs.FLOAT, AuraData::getVis,
            ByteBufCodecs.FLOAT, AuraData::getFlux, ByteBufCodecs.BOOL, AuraData::isInitialized, AuraData::new);

    private short base;
    private float vis;
    private float flux;
    private boolean initialized;
    private ChunkPos pos = new ChunkPos(0, 0);
    private final VisJournal visJournal = new VisJournal();

    public AuraData() {}

    public AuraData(short base, float vis, float flux) {
        this(base, vis, flux, false);
    }

    private AuraData(short base, float vis, float flux, boolean initialized) {
        this.base = base;
        this.vis = clamp(vis);
        this.flux = clamp(flux);
        // Older saves have no marker, but a nonzero base already proves initialization.
        this.initialized = initialized || base != 0;
    }

    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public short getBase() {
        return this.base;
    }

    public void setBase(short value) {
        this.base = value;
        this.initialized = true;
    }

    @Override
    public float getVis() {
        return this.vis;
    }

    public void setVis(float value) {
        this.vis = clamp(value);
    }

    public void setVis(float value, TransactionContext transaction) {
        visJournal.updateSnapshots(transaction);
        setVis(value);
    }

    @Override
    public float getFlux() {
        return this.flux;
    }

    public void setFlux(float value) {
        this.flux = clamp(value);
    }

    @Override
    public ChunkPos getChunkPos() {
        return this.pos;
    }

    public void setChunkPos(ChunkPos value) {
        this.pos = value;
    }

    private static float clamp(float value) {
        return Math.min(32766.0F, Math.max(0.0F, value));
    }

    private final class VisJournal extends SnapshotJournal<Float> {
        @Override
        protected Float createSnapshot() {
            return vis;
        }

        @Override
        protected void revertToSnapshot(Float snapshot) {
            vis = snapshot;
        }
    }
}
