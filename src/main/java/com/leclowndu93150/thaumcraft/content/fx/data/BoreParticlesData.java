package com.leclowndu93150.thaumcraft.content.fx.data;

import com.leclowndu93150.thaumcraft.registry.TCParticles;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public record BoreParticlesData(BlockState state, int targetEntityId, double tx, double ty, double tz,
                                double sx, double sy, double sz) implements ParticleOptions {
    public static final int NO_ENTITY = -1;
    private static final Codec<BlockState> BLOCK_STATE_CODEC = Codec.withAlternative(
            BlockState.CODEC, BuiltInRegistries.BLOCK.byNameCodec(), Block::defaultBlockState);

    public static final MapCodec<BoreParticlesData> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            BLOCK_STATE_CODEC.fieldOf("block_state").forGetter(BoreParticlesData::state),
            Codec.INT.fieldOf("target_entity_id").forGetter(BoreParticlesData::targetEntityId),
            Codec.DOUBLE.fieldOf("tx").forGetter(BoreParticlesData::tx),
            Codec.DOUBLE.fieldOf("ty").forGetter(BoreParticlesData::ty),
            Codec.DOUBLE.fieldOf("tz").forGetter(BoreParticlesData::tz),
            Codec.DOUBLE.fieldOf("sx").forGetter(BoreParticlesData::sx),
            Codec.DOUBLE.fieldOf("sy").forGetter(BoreParticlesData::sy),
            Codec.DOUBLE.fieldOf("sz").forGetter(BoreParticlesData::sz)
    ).apply(inst, BoreParticlesData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, BoreParticlesData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.idMapper(Block.BLOCK_STATE_REGISTRY), BoreParticlesData::state,
            ByteBufCodecs.VAR_INT, BoreParticlesData::targetEntityId,
            ByteBufCodecs.DOUBLE, BoreParticlesData::tx,
            ByteBufCodecs.DOUBLE, BoreParticlesData::ty,
            ByteBufCodecs.DOUBLE, BoreParticlesData::tz,
            ByteBufCodecs.DOUBLE, BoreParticlesData::sx,
            ByteBufCodecs.DOUBLE, BoreParticlesData::sy,
            ByteBufCodecs.DOUBLE, BoreParticlesData::sz,
            BoreParticlesData::new);

    public BoreParticlesData(BlockState state, double tx, double ty, double tz, double sx, double sy, double sz) {
        this(state, NO_ENTITY, tx, ty, tz, sx, sy, sz);
    }

    @Override
    public ParticleType<?> getType() {
        return TCParticles.BORE_PARTICLES.get();
    }
}
