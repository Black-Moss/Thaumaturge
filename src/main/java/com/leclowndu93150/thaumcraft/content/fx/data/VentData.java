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

public record VentData(
        double vx, double vy, double vz,
        int color,
        float scale,
        boolean variant
) implements ParticleOptions {

    public static final MapCodec<VentData> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Codec.DOUBLE.fieldOf("vx").forGetter(VentData::vx),
            Codec.DOUBLE.fieldOf("vy").forGetter(VentData::vy),
            Codec.DOUBLE.fieldOf("vz").forGetter(VentData::vz),
            Codec.INT.fieldOf("color").forGetter(VentData::color),
            Codec.FLOAT.fieldOf("scale").forGetter(VentData::scale),
            Codec.BOOL.fieldOf("variant").forGetter(VentData::variant)
    ).apply(inst, VentData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, VentData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, VentData::vx,
            ByteBufCodecs.DOUBLE, VentData::vy,
            ByteBufCodecs.DOUBLE, VentData::vz,
            ByteBufCodecs.INT, VentData::color,
            ByteBufCodecs.FLOAT, VentData::scale,
            ByteBufCodecs.BOOL, VentData::variant,
            VentData::new);

    @Override
    public ParticleType<?> getType() {
        return TCParticles.VENT.get();
    }
}
