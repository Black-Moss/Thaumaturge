package com.leclowndu93150.thaumcraft.content.fx.data;

import com.leclowndu93150.thaumcraft.registry.TCParticles;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record BlockRunesData(
        float r, float g, float b,
        int duration,
        float gravity,
        boolean variant
) implements ParticleOptions {

    public static final MapCodec<BlockRunesData> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Codec.FLOAT.fieldOf("r").forGetter(BlockRunesData::r),
            Codec.FLOAT.fieldOf("g").forGetter(BlockRunesData::g),
            Codec.FLOAT.fieldOf("b").forGetter(BlockRunesData::b),
            Codec.INT.fieldOf("duration").forGetter(BlockRunesData::duration),
            Codec.FLOAT.fieldOf("gravity").forGetter(BlockRunesData::gravity),
            Codec.BOOL.fieldOf("variant").forGetter(BlockRunesData::variant)
    ).apply(inst, BlockRunesData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, BlockRunesData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, BlockRunesData::r,
            ByteBufCodecs.FLOAT, BlockRunesData::g,
            ByteBufCodecs.FLOAT, BlockRunesData::b,
            ByteBufCodecs.VAR_INT, BlockRunesData::duration,
            ByteBufCodecs.FLOAT, BlockRunesData::gravity,
            ByteBufCodecs.BOOL, BlockRunesData::variant,
            BlockRunesData::new);

    @Override
    public ParticleType<?> getType() {
        return TCParticles.BLOCK_RUNES.get();
    }
}
