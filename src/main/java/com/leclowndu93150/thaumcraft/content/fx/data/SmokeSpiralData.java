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

public record SmokeSpiralData(
        float radius,
        int start,
        int minY,
        float r, float g, float b
) implements ParticleOptions {

    public static final MapCodec<SmokeSpiralData> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Codec.FLOAT.fieldOf("radius").forGetter(SmokeSpiralData::radius),
            Codec.INT.fieldOf("start").forGetter(SmokeSpiralData::start),
            Codec.INT.fieldOf("min_y").forGetter(SmokeSpiralData::minY),
            Codec.FLOAT.fieldOf("r").forGetter(SmokeSpiralData::r),
            Codec.FLOAT.fieldOf("g").forGetter(SmokeSpiralData::g),
            Codec.FLOAT.fieldOf("b").forGetter(SmokeSpiralData::b)
    ).apply(inst, SmokeSpiralData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SmokeSpiralData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, SmokeSpiralData::radius,
            ByteBufCodecs.VAR_INT, SmokeSpiralData::start,
            ByteBufCodecs.VAR_INT, SmokeSpiralData::minY,
            ByteBufCodecs.FLOAT, SmokeSpiralData::r,
            ByteBufCodecs.FLOAT, SmokeSpiralData::g,
            ByteBufCodecs.FLOAT, SmokeSpiralData::b,
            SmokeSpiralData::new);

    @Override
    public ParticleType<?> getType() {
        return TCParticles.SMOKE_SPIRAL.get();
    }
}
