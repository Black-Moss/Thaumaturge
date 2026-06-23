package com.leclowndu93150.thaumcraft.content.particle;

import com.leclowndu93150.thaumcraft.registry.TCParticles;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record BoreSparkleData(double tx, double ty, double tz, float r, float g, float b) implements ParticleOptions {
    public static final MapCodec<BoreSparkleData> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Codec.DOUBLE.fieldOf("tx").forGetter(BoreSparkleData::tx),
            Codec.DOUBLE.fieldOf("ty").forGetter(BoreSparkleData::ty),
            Codec.DOUBLE.fieldOf("tz").forGetter(BoreSparkleData::tz),
            Codec.FLOAT.fieldOf("r").forGetter(BoreSparkleData::r),
            Codec.FLOAT.fieldOf("g").forGetter(BoreSparkleData::g),
            Codec.FLOAT.fieldOf("b").forGetter(BoreSparkleData::b)
    ).apply(inst, BoreSparkleData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, BoreSparkleData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, BoreSparkleData::tx,
            ByteBufCodecs.DOUBLE, BoreSparkleData::ty,
            ByteBufCodecs.DOUBLE, BoreSparkleData::tz,
            ByteBufCodecs.FLOAT, BoreSparkleData::r,
            ByteBufCodecs.FLOAT, BoreSparkleData::g,
            ByteBufCodecs.FLOAT, BoreSparkleData::b,
            BoreSparkleData::new);

    @Override
    public ParticleType<?> getType() {
        return TCParticles.BORE_SPARKLE.get();
    }
}
